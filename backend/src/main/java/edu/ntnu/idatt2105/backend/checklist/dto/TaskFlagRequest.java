package edu.ntnu.idatt2105.backend.checklist.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Request DTO for setting a task's flag state ({@code "pending"} or {@code "todo"}).
 */
public record TaskFlagRequest(
	@NotBlank
	String state,

	@NotBlank
	String periodKey,

	LocalDateTime flaggedAt
) {
}

