package edu.ntnu.idatt2105.backend.temperature.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Compact response DTO used in inspection report sections to summarise a temperature reading.
 *
 * <p>Omits module, checklist, and task identifiers; retains only the measurement value,
 * timestamp, period key, and deviation flag.
 */
public record TemperatureMeasurementSummaryResponse(
	Long id,
	BigDecimal valueC,
	LocalDateTime measuredAt,
	String periodKey,
	Boolean deviation
) {
}
