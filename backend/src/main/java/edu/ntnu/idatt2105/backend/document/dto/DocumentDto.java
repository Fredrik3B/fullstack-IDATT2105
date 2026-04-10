package edu.ntnu.idatt2105.backend.document.dto;

import edu.ntnu.idatt2105.backend.document.model.enums.DocumentCategory;
import edu.ntnu.idatt2105.backend.document.model.enums.DocumentModule;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO representing a compliance document.
 *
 * <p>Either {@code externalUrl} or {@code originalFileName} / {@code fileType} / {@code fileSize}
 * will be populated depending on whether the document is a link or a stored file.
 */
@Schema(description = "A compliance document, either a stored file or an external link")
public record DocumentDto(

    @Schema(description = "Document ID")
    Long id,

    @Schema(description = "Display name of the document")
    String name,

    @Schema(description = "Optional description")
    String description,

    @Schema(description = "Document category", example = "DEVIATION_REPORT")
    DocumentCategory category,

    @Schema(description = "Module this document belongs to", example = "IC_FOOD")
    DocumentModule module,

    @Schema(description = "External URL if document is a link")
    String externalUrl,

    @Schema(description = "Original file name if document is an uploaded file")
    String originalFileName,

    @Schema(description = "MIME type of the uploaded file", example = "application/pdf")
    String fileType,

    @Schema(description = "File size in bytes")
    Long fileSize,

    @Schema(description = "Expiry date of the document", example = "2027-01-01")
    LocalDate expiryDate,

    @Schema(description = "When the document was uploaded")
    LocalDateTime uploadedAt,

    @Schema(description = "Full name of the user who uploaded the document")
    String uploadedByName
) {}
