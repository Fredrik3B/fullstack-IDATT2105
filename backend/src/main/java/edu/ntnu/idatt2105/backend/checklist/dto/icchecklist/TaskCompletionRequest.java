package edu.ntnu.idatt2105.backend.checklist.dto.icchecklist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDateTime;

public record TaskCompletionRequest(
	@NotBlank
	String state,

	@NotBlank
	String periodKey,

	LocalDateTime completedAt
) {
}

