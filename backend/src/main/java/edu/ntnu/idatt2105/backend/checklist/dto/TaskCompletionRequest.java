package edu.ntnu.idatt2105.backend.checklist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Request DTO for setting a task's completion state ({@code "completed"} or {@code "todo"}).
 */
public record TaskCompletionRequest(
	@NotBlank
	String state,

	@NotBlank
	String periodKey,

	LocalDateTime completedAt
) {
}

