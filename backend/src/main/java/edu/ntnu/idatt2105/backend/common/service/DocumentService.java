package edu.ntnu.idatt2105.backend.common.service;

import edu.ntnu.idatt2105.backend.common.dto.document.DocumentDTO;
import edu.ntnu.idatt2105.backend.common.model.enums.DocumentCategory;
import edu.ntnu.idatt2105.backend.common.model.enums.DocumentModule;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface DocumentService {

    DocumentDTO uploadDocument(
            MultipartFile file,
            String externalUrl,
            String name,
            String description,
            DocumentCategory category,
            DocumentModule module,
            LocalDate expiryDate,
            JwtAuthenticatedPrincipal principal
    );

    List<DocumentDTO> getDocuments(
            DocumentCategory category,
            DocumentModule module,
            JwtAuthenticatedPrincipal principal
    );

    Resource downloadDocument(Long documentId, JwtAuthenticatedPrincipal principal);

    String getDocumentContentType(Long documentId, JwtAuthenticatedPrincipal principal);

    String getDocumentFileName(Long documentId, JwtAuthenticatedPrincipal principal);

    void deleteDocument(Long documentId, JwtAuthenticatedPrincipal principal);
}
