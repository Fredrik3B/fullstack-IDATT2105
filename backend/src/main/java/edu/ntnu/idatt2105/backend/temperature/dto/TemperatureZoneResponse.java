package edu.ntnu.idatt2105.backend.temperature.dto;

import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.temperature.model.enums.TemperatureZone;
import java.math.BigDecimal;

public record TemperatureZoneResponse(
	Long id,
	IcModule module,
	String name,
	TemperatureZone zoneType,
	BigDecimal targetMin,
	BigDecimal targetMax
) {
}
