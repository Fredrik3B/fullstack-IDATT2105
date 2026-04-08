package edu.ntnu.idatt2105.backend.document.dto;

import edu.ntnu.idatt2105.backend.document.model.enums.DocumentCategory;
import edu.ntnu.idatt2105.backend.document.model.enums.DocumentModule;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
