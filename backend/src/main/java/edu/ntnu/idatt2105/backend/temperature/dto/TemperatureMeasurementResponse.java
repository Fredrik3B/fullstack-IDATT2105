package edu.ntnu.idatt2105.backend.temperature.dto;

import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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

