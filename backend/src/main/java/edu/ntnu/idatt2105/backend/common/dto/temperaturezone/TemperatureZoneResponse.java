package edu.ntnu.idatt2105.backend.common.dto.temperaturezone;

import edu.ntnu.idatt2105.backend.common.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.common.model.enums.TemperatureZone;
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
