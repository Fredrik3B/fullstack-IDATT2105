package edu.ntnu.idatt2105.backend.common.dto.icchecklist;

import java.math.BigDecimal;
import java.time.Instant;

public record TemperatureMeasurementSummaryResponse(
	Long id,
	BigDecimal valueC,
	Instant measuredAt,
	String periodKey,
	Boolean deviation
) {
}
