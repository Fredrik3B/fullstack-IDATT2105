package edu.ntnu.idatt2105.backend.temperature.dto;

import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for logging a temperature measurement against an active checklist task.
 *
 * <p>{@code measuredAt} defaults to now if omitted. If {@code periodKey} is supplied it
 * must match the activated task's period key; otherwise the task's period key is used.
 */
public record CreateTemperatureMeasurementRequest(
	@NotNull
  IcModule module,

	@NotNull
	Long checklistId,

	@NotNull
	Long taskId,

	@NotNull
	BigDecimal valueC,

	LocalDateTime measuredAt,

	String periodKey
) {
}

