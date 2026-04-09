package edu.ntnu.idatt2105.backend.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(description = "Response after a deviation report is successfully created")
public record DeviationCreatedResponse(

    @Schema(description = "ID of the created deviation")
    UUID id,

    @Schema(description = "When the deviation was created")
    LocalDateTime createdAt
) {}
