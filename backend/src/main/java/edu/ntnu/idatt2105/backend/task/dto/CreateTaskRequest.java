package edu.ntnu.idatt2105.backend.task.dto;

import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import edu.ntnu.idatt2105.backend.checklist.model.enums.SectionTypes;
import edu.ntnu.idatt2105.backend.task.validation.TemperatureZoneRequiredIfTemperature;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Request DTO for creating or updating a task template.
 *
 * <p>Temperature-control tasks ({@code sectionType = TEMPERATURE_CONTROL}) must supply
 * a {@code temperatureZoneId}; all other section types must leave it {@code null}.
 */
@TemperatureZoneRequiredIfTemperature
@Schema(description = "Request to create a new task")
public record CreateTaskRequest(
		@NotNull
		@Schema(description = "Module this task belongs to", example = "IC_FOOD")
		IcModule module,

		@NotBlank
		@Size(max = 120)
		@Schema(description = "Task title", example = "Check fridge temperature")
		String title,

		@Size(max = 255)
		@Schema(description = "Additional metadata or instructions")
		String meta,

		@NotNull
		@Schema(description = "Section type for the task", example = "CLEANING_SANITATION")
		SectionTypes sectionType,

		@Schema(description = "ID of the temperature zone, required if sectionType is TEMPERATURE_CONTROL")
		Long temperatureZoneId,

		@DecimalMin("-999.99")
		@DecimalMax("999.99")
		@Schema(description = "Minimum target temperature", example = "2.0")
		BigDecimal targetMin,

		@DecimalMin("-999.99")
		@DecimalMax("999.99")
		@Schema(description = "Maximum target temperature", example = "8.0")
		BigDecimal targetMax
) {}
