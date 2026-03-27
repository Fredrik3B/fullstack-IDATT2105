package no.ntnu.resturant_manager.dto;

import no.ntnu.resturant_manager.model.enums.ChecklistFrequency;
import no.ntnu.resturant_manager.model.enums.ComplianceArea;

public record CreateChecklistRequest(
	String name,
	String description,
	ChecklistFrequency frequency,
	ComplianceArea complianceArea,
	Boolean active,
	Long organizationId
) {
}
