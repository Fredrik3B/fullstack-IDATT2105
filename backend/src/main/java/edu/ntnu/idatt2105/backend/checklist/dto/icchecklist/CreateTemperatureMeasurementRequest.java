package edu.ntnu.idatt2105.backend.checklist.dto.icchecklist;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateTemperatureMeasurementRequest(
	@NotNull
	IcModule module,

	@NotNull
	Long checklistId,

	@NotNull
	Long taskId,

	@NotNull
	BigDecimal valueC,

	LocalDateTime measuredAt,

	String periodKey
) {
}

