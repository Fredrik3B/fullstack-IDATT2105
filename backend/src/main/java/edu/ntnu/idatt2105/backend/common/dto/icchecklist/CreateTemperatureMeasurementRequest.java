package edu.ntnu.idatt2105.backend.common.dto.icchecklist;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public record CreateTemperatureMeasurementRequest(
	@NotNull
	IcModule module,

	@NotNull
	Long checklistId,

	@NotNull
	Long taskId,

	@NotNull
	BigDecimal valueC,

	Instant measuredAt,

	String periodKey
) {
}

