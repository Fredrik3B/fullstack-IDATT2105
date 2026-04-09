package edu.ntnu.idatt2105.backend.task.dto;

import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import edu.ntnu.idatt2105.backend.checklist.model.enums.SectionTypes;
import edu.ntnu.idatt2105.backend.temperature.model.enums.TemperatureZone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "Task details")
public record TaskResponse(

		@Schema(description = "Task ID")
		Long id,

		@Schema(description = "Module this task belongs to", example = "IC_FOOD")
		IcModule module,

		@Schema(description = "Task title")
		String title,

		@Schema(description = "Additional metadata or instructions")
		String meta,

		@Schema(description = "Section type", example = "TEMPERATURE_CONTROL")
		SectionTypes sectionType,

		@Schema(description = "Temperature zone ID if applicable")
		Long temperatureZoneId,

		@Schema(description = "Temperature zone display name")
		String temperatureZoneName,

		@Schema(description = "Temperature zone type")
		TemperatureZone temperatureZoneType,

		@Schema(description = "Unit of measurement")
		String unit,

		@Schema(description = "Minimum target temperature", example = "2.0")
		BigDecimal targetMin,

		@Schema(description = "Maximum target temperature", example = "8.0")
		BigDecimal targetMax
) {}