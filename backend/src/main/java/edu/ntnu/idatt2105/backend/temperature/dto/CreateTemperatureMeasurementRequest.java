package edu.ntnu.idatt2105.backend.temperature.dto;

import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for logging a temperature measurement against an active checklist task.
 *
 * <p>{@code measuredAt} defaults to now if omitted. If {@code periodKey} is supplied it
 * must match the activated task's period key; otherwise the task's period key is used.
 */
@Schema(description = "Request to log a temperature measurement against an active checklist task")
public record CreateTemperatureMeasurementRequest(
		@NotNull
		@Schema(description = "Module this measurement belongs to", example = "IC_FOOD")
		IcModule module,

		@NotNull
		@Schema(description = "ID of the checklist this task belongs to")
		Long checklistId,

		@NotNull
		@Schema(description = "ID of the activated task to log against")
		Long taskId,

		@NotNull
		@Schema(description = "Measured temperature in Celsius", example = "3.5")
		BigDecimal valueC,

		@Schema(description = "When the measurement was taken, defaults to now if omitted")
		LocalDateTime measuredAt,

		@Schema(description = "Period key to log against, defaults to the task's period key if omitted", example = "2026-W15")
		String periodKey
) {}
