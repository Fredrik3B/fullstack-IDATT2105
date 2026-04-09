package edu.ntnu.idatt2105.backend.task.dto;

import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import edu.ntnu.idatt2105.backend.checklist.model.enums.SectionTypes;
import edu.ntnu.idatt2105.backend.temperature.model.enums.TemperatureZone;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record TaskResponse(
		@NotNull Long id,
		@NotBlank IcModule module,
		@NotBlank @Size(max = 255) String title,
		@Size(max = 255) String meta,
		@NotNull SectionTypes sectionType,
		Long temperatureZoneId,
		String temperatureZoneName,
		TemperatureZone temperatureZoneType,

		String unit,
		@DecimalMin("-200.00") @DecimalMax("999.99") BigDecimal targetMin,
		@DecimalMin("-200.00") @DecimalMax("999.99") BigDecimal targetMax
) {
}
