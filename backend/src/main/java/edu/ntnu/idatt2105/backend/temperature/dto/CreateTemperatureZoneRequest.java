package edu.ntnu.idatt2105.backend.temperature.dto;

import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.temperature.model.enums.TemperatureZone;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateTemperatureZoneRequest(
	@NotNull
	IcModule module,

	@NotBlank
	@Size(max = 120)
	String name,

	@NotNull
	TemperatureZone zoneType,

	@NotNull
	@DecimalMin("-999.99")
	@DecimalMax("999.99")
	BigDecimal targetMin,

	@NotNull
	@DecimalMin("-999.99")
	@DecimalMax("999.99")
	BigDecimal targetMax
) {
}
