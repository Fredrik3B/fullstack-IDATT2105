package edu.ntnu.idatt2105.backend.common.dto.icchecklist;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public record TemperatureMeasurementResponse(
	Long id,
	IcModule module,
	Long checklistId,
	Long taskId,
	BigDecimal valueC,
	LocalDateTime measuredAt,
	String periodKey,
	Boolean deviation
) {
}

