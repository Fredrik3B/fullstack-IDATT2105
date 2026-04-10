package edu.ntnu.idatt2105.backend.checklist.dto;

import edu.ntnu.idatt2105.backend.temperature.dto.TemperatureMeasurementSummaryResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for a single task item within a checklist section.
 *
 * <p>The {@code state} field is one of {@code "todo"}, {@code "completed"}, or {@code "pending"}.
 * Temperature-type tasks additionally carry {@code unit}, {@code targetMin}/{@code targetMax},
 * and the {@code latestMeasurement} summary.
 */
@Schema(description = "A single task item within a checklist section")
public record ChecklistTaskItemResponse(

		@Schema(description = "Task instance ID")
		Long id,

		@Schema(description = "Template ID this task is based on")
		Long templateId,

		@Schema(description = "Task label shown in the UI")
		String label,

		@Schema(description = "Additional metadata or instructions")
		String meta,

		@Schema(description = "Task type", example = "TEMPERATURE_CONTROL")
		String type,

		@Schema(description = "Unit of measurement", example = "C")
		String unit,

		@Schema(description = "Minimum target value", example = "2.0")
		BigDecimal targetMin,

		@Schema(description = "Maximum target value", example = "8.0")
		BigDecimal targetMax,

		@Schema(description = "Task state", example = "todo")
		String state,

		@Schema(description = "Whether the task is highlighted")
		Boolean highlighted,

		@Schema(description = "Period key this task was completed for", example = "2026-W15")
		String completedForPeriodKey,

		@Schema(description = "When the task was completed")
		LocalDateTime completedAt,

		@Schema(description = "Period key this task is pending for", example = "2026-W15")
		String pendingForPeriodKey,

		@Schema(description = "Latest temperature measurement for this task")
		TemperatureMeasurementSummaryResponse latestMeasurement
) {}
