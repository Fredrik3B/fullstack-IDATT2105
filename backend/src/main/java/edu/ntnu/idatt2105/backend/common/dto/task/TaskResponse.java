package edu.ntnu.idatt2105.backend.common.dto.task;

import edu.ntnu.idatt2105.backend.common.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.common.model.enums.SectionTypes;
import java.math.BigDecimal;

public record TaskResponse(
	Long id,
	IcModule module,
	String title,
	String meta,
	SectionTypes sectionType,
	String unit,
	BigDecimal targetMin,
	BigDecimal targetMax
) {
}
