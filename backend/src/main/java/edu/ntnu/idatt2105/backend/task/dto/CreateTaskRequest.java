package edu.ntnu.idatt2105.backend.task.dto;

import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import edu.ntnu.idatt2105.backend.checklist.model.enums.SectionTypes;
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
public record CreateTaskRequest(
	@NotNull
	IcModule module,

	@NotBlank
	@Size(max = 120)
	String title,

	@Size(max = 255)
	String meta,

	SectionTypes sectionType,

	Long temperatureZoneId,

	@DecimalMin("-999.99")
	@DecimalMax("999.99")
	BigDecimal targetMin,

	@DecimalMin("-999.99")
	@DecimalMax("999.99")
	BigDecimal targetMax
) {
}
