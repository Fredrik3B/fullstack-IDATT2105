package edu.ntnu.idatt2105.backend.document.service;

import edu.ntnu.idatt2105.backend.document.dto.DocumentDto;
import edu.ntnu.idatt2105.backend.document.mapper.DocumentMapper;
import edu.ntnu.idatt2105.backend.document.model.DocumentModel;
import edu.ntnu.idatt2105.backend.document.model.enums.DocumentCategory;
import edu.ntnu.idatt2105.backend.document.model.enums.DocumentModule;
import edu.ntnu.idatt2105.backend.document.repository.DocumentRepository;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import edu.ntnu.idatt2105.backend.user.repository.OrganizationRepository;
import edu.ntnu.idatt2105.backend.user.repository.UserRepository;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
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

/**
 * Service for uploading, retrieving, downloading, and deleting compliance documents.
 *
 * <p>Uploaded files are stored under {@code app.document-storage-path} (default
 * {@code ./uploads/documents/}) in a per-organisation subdirectory identified by the organisation
 * UUID. External-URL documents skip file storage entirely.
 */
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

  /**
   * Saves a new document record for the caller's organisation.
   *
   * <p>Exactly one of {@code file} or {@code externalUrl} must be non-blank.
   * When a file is provided it is written to the organisation's storage directory.
   *
   * @param file        the uploaded file (nullable if {@code externalUrl} is provided)
   * @param externalUrl link to an external document (nullable if {@code file} is provided)
   * @param name        display name for the document
   * @param description optional description
   * @param category    document category
   * @param module      compliance module the document belongs to
   * @param expiryDate  optional expiry date
   * @param principal   the authenticated principal used to resolve organisation and uploader
   * @return the persisted document as a DTO
   * @throws org.springframework.web.server.ResponseStatusException (400) if neither file nor URL is
   *                                                                provided
   */
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
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Either a file or an external URL must be provided");
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

  /**
   * Returns documents for the caller's organisation, optionally filtered by category and/or
   * module.
   *
   * @param category  optional category filter
   * @param module    optional module filter
   * @param principal the authenticated principal used to resolve the organisation
   * @return list of matching document DTOs
   */
  public List<DocumentDto> getDocuments(
      DocumentCategory category,
      DocumentModule module,
      JwtAuthenticatedPrincipal principal
  ) {
    UUID orgId = principal.getOrganizationId();
    List<DocumentModel> docs;

    if (category != null && module != null) {
      docs = documentRepository.findAllByOrganizationIdAndCategoryAndModule(orgId, category,
          module);
    } else if (category != null) {
      docs = documentRepository.findAllByOrganizationIdAndCategory(orgId, category);
    } else if (module != null) {
      docs = documentRepository.findAllByOrganizationIdAndModule(orgId, module);
    } else {
      docs = documentRepository.findAllByOrganizationId(orgId);
    }

    return docs.stream().map(documentMapper::toDto).toList();
  }

  /**
   * Returns a {@link Resource} for the file associated with a stored document.
   *
   * @param documentId the ID of the document to download
   * @param principal  the authenticated principal; must own the document's organisation
   * @return a readable {@link Resource} pointing to the file on disk
   * @throws org.springframework.web.server.ResponseStatusException (404) if the file does not exist
   *                                                                on disk
   * @throws org.springframework.web.server.ResponseStatusException (403) if the document belongs to
   *                                                                another organisation
   */
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
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Could not resolve file path");
    }
  }

  /**
   * Returns the MIME type of a stored document file.
   *
   * @param documentId the document ID
   * @param principal  the authenticated principal; must own the document's organisation
   * @return the stored content type string (e.g. {@code "application/pdf"})
   */
  public String getDocumentContentType(Long documentId, JwtAuthenticatedPrincipal principal) {
    return requireOwnDocument(documentId, principal.getOrganizationId()).getFileType();
  }

  /**
   * Returns the original file name of a stored document.
   *
   * @param documentId the document ID
   * @param principal  the authenticated principal; must own the document's organisation
   * @return the original file name as uploaded
   */
  public String getDocumentFileName(Long documentId, JwtAuthenticatedPrincipal principal) {
    return requireOwnDocument(documentId, principal.getOrganizationId()).getOriginalFileName();
  }

  /**
   * Deletes a document record and its associated file from disk (if any).
   *
   * <p>If the file cannot be removed from disk, a warning is logged but the database
   * record is still deleted.
   *
   * @param documentId the ID of the document to delete
   * @param principal  the authenticated principal; must own the document's organisation
   * @throws org.springframework.web.server.ResponseStatusException (403) if the document belongs to
   *                                                                another organisation
   */
  public void deleteDocument(Long documentId, JwtAuthenticatedPrincipal principal) {
    DocumentModel doc = requireOwnDocument(documentId, principal.getOrganizationId());
    if (doc.getStoragePath() != null && !doc.getStoragePath().isBlank()) {
      deleteFileFromDisk(doc.getStoragePath());
    }
    documentRepository.delete(doc);
    LOGGER.info("Document delete: documentId={} orgId={}", documentId,
        principal.getOrganizationId());
  }


  /**
   * Saves an uploaded file to the organisation's storage directory and returns the absolute path.
   *
   * @param file  the multipart file to persist
   * @param orgId the organisation ID used as subdirectory name
   * @return absolute path to the saved file
   * @throws org.springframework.web.server.ResponseStatusException (500) if the file cannot be
   *                                                                written
   */
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

  /**
   * Attempts to delete a file from disk. Failures are logged as warnings and swallowed.
   *
   * @param path absolute path of the file to delete
   */
  private void deleteFileFromDisk(String path) {
    try {
      Files.deleteIfExists(Paths.get(path));
    } catch (IOException | RuntimeException e) {
      LOGGER.warn("Could not delete file from disk at path '{}': {}", path, e.getMessage());
    }
  }

  /**
   * Loads a document and asserts it belongs to the given organisation.
   *
   * @param documentId the document ID to look up
   * @param orgId      the organisation that must own the document
   * @return the document entity
   * @throws org.springframework.web.server.ResponseStatusException (404) if not found, (403) if
   *                                                                ownership check fails
   */
  private DocumentModel requireOwnDocument(Long documentId, UUID orgId) {
    DocumentModel doc = documentRepository.findById(documentId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
    if (!doc.getOrganization().getId().equals(orgId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }
    return doc;
  }

  public String storeFile(byte[] bytes, UUID orgId, String fileName) {
    try {
      Path orgDir = Paths.get(storagePath).toAbsolutePath().normalize().resolve(orgId.toString());
      Files.createDirectories(orgDir);
      String uniqueFileName = UUID.randomUUID() + "_" + fileName;
      Path destination = orgDir.resolve(uniqueFileName);
      Files.write(destination, bytes);
      return destination.toAbsolutePath().toString();
    } catch (IOException e) {
      LOGGER.error("Failed to store file: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file");
    }
  }

  public void saveDocumentModel(DocumentModel document) {
    documentRepository.save(document);
    LOGGER.info("Document saved: orgId={} name='{}'", document.getOrganization().getId(),
        document.getName());
  }
}
