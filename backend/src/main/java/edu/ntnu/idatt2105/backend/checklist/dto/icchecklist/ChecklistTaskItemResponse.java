package edu.ntnu.idatt2105.backend.checklist.dto.icchecklist;

import edu.ntnu.idatt2105.backend.temperature.dto.TemperatureMeasurementSummaryResponse;
import java.math.BigDecimal;
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
