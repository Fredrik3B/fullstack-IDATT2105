package edu.ntnu.idatt2105.backend.document.controller;

import edu.ntnu.idatt2105.backend.document.dto.DocumentDTO;
import edu.ntnu.idatt2105.backend.document.model.enums.DocumentCategory;
import edu.ntnu.idatt2105.backend.document.model.enums.DocumentModule;
import edu.ntnu.idatt2105.backend.document.service.DocumentService;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Documents", description = "Upload, list, download, and delete compliance documents")
@RestController
@AllArgsConstructor
@RequestMapping("/documents")
public class DocumentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentService documentService;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Upload a document or add a link to an external document")
    @ApiResponse(responseCode = "201", description = "Document created")
    public DocumentDTO uploadDocument(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "externalUrl", required = false) String externalUrl,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("category") DocumentCategory category,
            @RequestParam("module") DocumentModule module,
            @RequestParam(value = "expiryDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate,
            Authentication auth
    ) {
        JwtAuthenticatedPrincipal principal = JwtAuthenticatedPrincipal.from(auth);
        LOGGER.info("Document create request: orgId={} userId={} name='{}' category={} module={} isLink={}",
                principal.getOrganizationId(), principal.getUserId(), name, category, module, externalUrl != null);
        return documentService.uploadDocument(file, externalUrl, name, description, category, module, expiryDate, principal);
    }

    @GetMapping
    @Operation(summary = "List documents for the current organization")
    @ApiResponse(responseCode = "200", description = "Documents returned")
    public List<DocumentDTO> getDocuments(
            @RequestParam(value = "category", required = false) DocumentCategory category,
            @RequestParam(value = "module", required = false) DocumentModule module,
            Authentication auth
    ) {
        JwtAuthenticatedPrincipal principal = JwtAuthenticatedPrincipal.from(auth);
        LOGGER.info("Document list: orgId={} category={} module={}",
                principal.getOrganizationId(), category, module);
        return documentService.getDocuments(category, module, principal);
    }

    @GetMapping("/{documentId}/download")
    @Operation(summary = "Download a document file")
    @ApiResponse(responseCode = "200", description = "File returned")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long documentId,
            Authentication auth
    ) {
        JwtAuthenticatedPrincipal principal = JwtAuthenticatedPrincipal.from(auth);
        LOGGER.info("Document download: orgId={} documentId={}", principal.getOrganizationId(), documentId);

        Resource resource = documentService.downloadDocument(documentId, principal);
        String contentType = documentService.getDocumentContentType(documentId, principal);
        String fileName = documentService.getDocumentFileName(documentId, principal);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @DeleteMapping("/{documentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete a document")
    @ApiResponse(responseCode = "204", description = "Document deleted")
    public void deleteDocument(@PathVariable Long documentId, Authentication auth) {
        JwtAuthenticatedPrincipal principal = JwtAuthenticatedPrincipal.from(auth);
        LOGGER.info("Document delete: orgId={} documentId={}", principal.getOrganizationId(), documentId);
        documentService.deleteDocument(documentId, principal);
    }
}
