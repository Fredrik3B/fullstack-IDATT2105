package edu.ntnu.idatt2105.backend.common.dto.document;

import edu.ntnu.idatt2105.backend.common.model.enums.DocumentCategory;
import edu.ntnu.idatt2105.backend.common.model.enums.DocumentModule;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DocumentDTO(
        Long id,
        String name,
        String description,
        DocumentCategory category,
        DocumentModule module,
        String originalFileName,
        String fileType,
        Long fileSize,
        LocalDate expiryDate,
        LocalDateTime uploadedAt,
        String uploadedByName
) {}
