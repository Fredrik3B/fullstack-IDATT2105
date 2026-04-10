package edu.ntnu.idatt2105.backend.checklist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * Request DTO for setting a task's completion state ({@code "completed"} or {@code "todo"}).
 */
@Schema(description = "Request to set a task's completion state")
public record TaskCompletionRequest(
    @NotBlank
    @Schema(description = "Task state to set", example = "completed")
    String state,

    @NotBlank
    @Schema(description = "Period key this completion applies to", example = "2026-W15")
    String periodKey,

    @Schema(description = "When the task was completed, defaults to now if omitted")
    LocalDateTime completedAt
) {

}

