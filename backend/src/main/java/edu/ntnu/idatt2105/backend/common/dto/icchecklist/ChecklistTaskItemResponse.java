package edu.ntnu.idatt2105.backend.common.dto.icchecklist;

import java.math.BigDecimal;
import java.time.Instant;

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
	Instant completedAt,
	String pendingForPeriodKey,
	TemperatureMeasurementSummaryResponse latestMeasurement
) {
}
