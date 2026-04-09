package edu.ntnu.idatt2105.backend.checklist.dto;

import edu.ntnu.idatt2105.backend.temperature.dto.TemperatureMeasurementSummaryResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for a single task item within a checklist section.
 *
 * <p>The {@code state} field is one of {@code "todo"}, {@code "completed"}, or {@code "pending"}.
 * Temperature-type tasks additionally carry {@code unit}, {@code targetMin}/{@code targetMax},
 * and the {@code latestMeasurement} summary.
 */
public record ChecklistTaskItemResponse(
	Long id,
	Long templateId,
	String label,
	String meta,
	String type,
	String unit,
	BigDecimal targetMin,
	BigDecimal targetMax,
	String state,
	Boolean highlighted,
	String completedForPeriodKey,
	LocalDateTime completedAt,
	String pendingForPeriodKey,
	TemperatureMeasurementSummaryResponse latestMeasurement
) {
}
