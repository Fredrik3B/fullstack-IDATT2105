package edu.ntnu.idatt2105.backend.checklist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Request DTO for setting a task's flag state ({@code "pending"} or {@code "todo"}).
 */
@Schema(description = "Request to set a task's flag state")
public record TaskFlagRequest(

		@NotBlank
		@Schema(description = "Flag state to set", example = "pending")
		String state,

		@NotBlank
		@Schema(description = "Period key this flag applies to", example = "2026-W15")
		String periodKey,

		@Schema(description = "When the task was flagged, defaults to now if omitted")
		LocalDateTime flaggedAt
) {}

