package edu.ntnu.idatt2105.backend.common.dto.task;

import edu.ntnu.idatt2105.backend.common.model.enums.SectionTypes;
import java.math.BigDecimal;

public record TaskResponse(
	Long id,
	String title,
	String sectionTitle,
	SectionTypes sectionType,
	String unit,
	BigDecimal targetMin,
	BigDecimal targetMax,
	Long checklistId
) {
}
