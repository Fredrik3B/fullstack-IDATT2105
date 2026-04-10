package edu.ntnu.idatt2105.backend.temperature.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Compact response DTO used in inspection report sections to summarise a temperature reading.
 *
 * <p>Omits module, checklist, and task identifiers; retains only the measurement value,
 * timestamp, period key, and deviation flag.
 */
@Schema(description = "Compact temperature measurement summary used in reports and task cards")
public record TemperatureMeasurementSummaryResponse(

		@Schema(description = "Measurement ID")
		Long id,

		@Schema(description = "Measured temperature in Celsius", example = "3.5")
		BigDecimal valueC,

		@Schema(description = "When the measurement was taken")
		LocalDateTime measuredAt,

		@Schema(description = "Period key this measurement belongs to", example = "2026-W15")
		String periodKey,

		@Schema(description = "Whether the measurement is outside the allowed range")
		Boolean deviation
) {}
