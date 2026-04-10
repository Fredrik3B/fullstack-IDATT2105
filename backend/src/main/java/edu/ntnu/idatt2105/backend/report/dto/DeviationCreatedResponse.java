package edu.ntnu.idatt2105.backend.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO returned after successfully creating a deviation report.
 *
 * <p>Carries the new report's UUID and the server-assigned creation timestamp.
 */
@Schema(description = "Response after a deviation report is successfully created")
public record DeviationCreatedResponse(

    @Schema(description = "ID of the created deviation")
    UUID id,

    @Schema(description = "When the deviation was created")
    LocalDateTime createdAt
) {

}
