package edu.ntnu.idatt2105.backend.temperature.dto;

import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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

