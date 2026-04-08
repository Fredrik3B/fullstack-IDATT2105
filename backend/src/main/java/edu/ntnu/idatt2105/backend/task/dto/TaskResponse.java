package edu.ntnu.idatt2105.backend.task.dto;

import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.checklist.model.enums.SectionTypes;
import edu.ntnu.idatt2105.backend.temperature.model.enums.TemperatureZone;
import java.math.BigDecimal;

public record TaskResponse(
	Long id,
	IcModule module,
	String title,
	String meta,
	SectionTypes sectionType,
	Long temperatureZoneId,
	String temperatureZoneName,
	TemperatureZone temperatureZoneType,
	String unit,
	BigDecimal targetMin,
	BigDecimal targetMax
) {
}
