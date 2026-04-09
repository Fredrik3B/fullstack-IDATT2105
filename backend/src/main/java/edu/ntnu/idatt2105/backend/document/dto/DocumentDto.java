package edu.ntnu.idatt2105.backend.document.dto;

import edu.ntnu.idatt2105.backend.document.model.enums.DocumentCategory;
import edu.ntnu.idatt2105.backend.document.model.enums.DocumentModule;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO representing a compliance document.
 *
 * <p>Either {@code externalUrl} or {@code originalFileName} / {@code fileType} / {@code fileSize}
 * will be populated depending on whether the document is a link or a stored file.
 */
public record DocumentDto(
        Long id,
        String name,
        String description,
        DocumentCategory category,
        DocumentModule module,
        String externalUrl,
        String originalFileName,
        String fileType,
        Long fileSize,
        LocalDate expiryDate,
        LocalDateTime uploadedAt,
        String uploadedByName
) {}
