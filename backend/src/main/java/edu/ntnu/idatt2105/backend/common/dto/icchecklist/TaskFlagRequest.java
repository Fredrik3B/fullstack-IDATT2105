package edu.ntnu.idatt2105.backend.common.dto.icchecklist;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public record TaskFlagRequest(
	@NotBlank
	String state,

	@NotBlank
	String periodKey,

	Instant flaggedAt
) {
}

