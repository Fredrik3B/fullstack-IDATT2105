package edu.ntnu.idatt2105.backend.temperature.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TemperatureMeasurementSummaryResponse(
	Long id,
	BigDecimal valueC,
	LocalDateTime measuredAt,
	String periodKey,
	Boolean deviation
) {
}
