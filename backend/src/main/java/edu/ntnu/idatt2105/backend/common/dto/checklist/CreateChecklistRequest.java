package edu.ntnu.idatt2105.backend.common.dto.checklist;

import edu.ntnu.idatt2105.backend.common.model.enums.ChecklistFrequency;
import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record CreateChecklistRequest(
	@NotBlank
	@Size(max = 120)
	String name,

	@Size(max = 1000)
	String description,

	@NotNull
	ChecklistFrequency frequency,

	@NotNull
	ComplianceArea complianceArea,

	Boolean active,

	@NotNull
	Long organizationId
) {
}
