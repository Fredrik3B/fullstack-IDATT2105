package no.ntnu.resturant_manager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.ntnu.resturant_manager.model.enums.ChecklistFrequency;
import no.ntnu.resturant_manager.model.enums.ComplianceArea;

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
