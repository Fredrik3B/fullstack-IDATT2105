package edu.ntnu.idatt2105.backend.common.dto.task;

import edu.ntnu.idatt2105.backend.common.model.enums.SectionTypes;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateTaskRequest(
	@NotBlank
	@Size(max = 120)
	String title,

	@Size(max = 120)
	String sectionTitle,

	SectionTypes sectionType,

	@Size(max = 10)
	String unit,

	@DecimalMin("-999.99")
	@DecimalMax("999.99")
	BigDecimal targetMin,

	@DecimalMin("-999.99")
	@DecimalMax("999.99")
	BigDecimal targetMax,

	@NotNull
	Long checklistId
) {
}
