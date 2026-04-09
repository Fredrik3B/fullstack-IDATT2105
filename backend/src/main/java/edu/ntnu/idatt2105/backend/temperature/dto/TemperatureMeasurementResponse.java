package edu.ntnu.idatt2105.backend.temperature.dto;

import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for a single temperature measurement.
 *
 * <p>{@code deviation} is {@code true} when the recorded {@code valueC} falls outside
 * the task template's {@code targetMin} / {@code targetMax} range.
 */
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

