package edu.ntnu.idatt2105.backend.document.service;

import edu.ntnu.idatt2105.backend.document.dto.DocumentDto;
import edu.ntnu.idatt2105.backend.document.mapper.DocumentMapper;
import edu.ntnu.idatt2105.backend.document.model.DocumentModel;
import edu.ntnu.idatt2105.backend.document.model.enums.DocumentCategory;
import edu.ntnu.idatt2105.backend.document.model.enums.DocumentModule;
import edu.ntnu.idatt2105.backend.document.repository.DocumentRepository;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import edu.ntnu.idatt2105.backend.user.repository.OrganizationRepository;
import edu.ntnu.idatt2105.backend.user.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final DocumentMapper documentMapper;

    @Value("${app.document-storage-path:./uploads/documents/}")
    private String storagePath;

    public DocumentDto uploadDocument(
            MultipartFile file,
            String externalUrl,
            String name,
            String description,
            DocumentCategory category,
            DocumentModule module,
            LocalDate expiryDate,
            JwtAuthenticatedPrincipal principal
    ) {
        if (file == null && (externalUrl == null || externalUrl.isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Either a file or an external URL must be provided");
        }

        UUID orgId = principal.getOrganizationId();

        DocumentModel.DocumentModelBuilder builder = DocumentModel.builder()
            .name(name)
            .description(description)
            .category(category)
            .module(module)
            .expiryDate(expiryDate)
            .uploadedBy(userRepository.getReferenceById(principal.getUserId()))
            .organization(organizationRepository.getReferenceById(orgId));

        if (externalUrl != null && !externalUrl.isBlank()) {
            builder.externalUrl(externalUrl);
        } else {
            builder
                .storagePath(saveFileToDisk(file, orgId))
                .originalFileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize());
        }

        DocumentModel saved = documentRepository.save(builder.build());
        LOGGER.info("Document saved: documentId={} orgId={} name='{}'", saved.getId(), orgId, name);
        return documentMapper.toDto(saved);
    }

    public List<DocumentDto> getDocuments(
            DocumentCategory category,
            DocumentModule module,
            JwtAuthenticatedPrincipal principal
    ) {
        UUID orgId = principal.getOrganizationId();
        List<DocumentModel> docs;

        if (category != null && module != null) {
            docs = documentRepository.findAllByOrganizationIdAndCategoryAndModule(orgId, category, module);
        } else if (category != null) {
            docs = documentRepository.findAllByOrganizationIdAndCategory(orgId, category);
        } else if (module != null) {
            docs = documentRepository.findAllByOrganizationIdAndModule(orgId, module);
        } else {
            docs = documentRepository.findAllByOrganizationId(orgId);
        }

        return docs.stream().map(documentMapper::toDto).toList();
    }

    public Resource downloadDocument(Long documentId, JwtAuthenticatedPrincipal principal) {
        DocumentModel doc = requireOwnDocument(documentId, principal.getOrganizationId());
        Path filePath = Paths.get(doc.getStoragePath());
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found on disk");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not resolve file path");
        }
    }

    public String getDocumentContentType(Long documentId, JwtAuthenticatedPrincipal principal) {
        return requireOwnDocument(documentId, principal.getOrganizationId()).getFileType();
    }

    public String getDocumentFileName(Long documentId, JwtAuthenticatedPrincipal principal) {
        return requireOwnDocument(documentId, principal.getOrganizationId()).getOriginalFileName();
    }

    public void deleteDocument(Long documentId, JwtAuthenticatedPrincipal principal) {
        DocumentModel doc = requireOwnDocument(documentId, principal.getOrganizationId());
        deleteFileFromDisk(doc.getStoragePath());
        documentRepository.delete(doc);
        LOGGER.info("Document delete: documentId={} orgId={}", documentId, principal.getOrganizationId());
    }


    private String saveFileToDisk(MultipartFile file, UUID orgId) {
        try {
            Path orgDir = Paths.get(storagePath).toAbsolutePath().normalize().resolve(orgId.toString());
            Files.createDirectories(orgDir);
            String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path destination = orgDir.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), destination);
            return destination.toAbsolutePath().toString();
        } catch (IOException e) {
            LOGGER.error("Failed to save file to disk: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file");
        }
    }

    private void deleteFileFromDisk(String path) {
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            LOGGER.warn("Could not delete file from disk at path '{}': {}", path, e.getMessage());
        }
    }

    private DocumentModel requireOwnDocument(Long documentId, UUID orgId) {
        DocumentModel doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
        if (!doc.getOrganization().getId().equals(orgId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return doc;
    }

}
