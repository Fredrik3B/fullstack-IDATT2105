package edu.ntnu.idatt2105.backend.common.dto.icchecklist;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

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
