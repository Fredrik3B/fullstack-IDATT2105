package edu.ntnu.idatt2105.backend.checklist.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.time.LocalDateTime;

public record TaskFlagRequest(
	@NotBlank
	String state,

	@NotBlank
	String periodKey,

	LocalDateTime flaggedAt
) {
}

