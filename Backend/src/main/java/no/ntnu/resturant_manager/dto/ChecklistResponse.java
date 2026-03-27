package no.ntnu.resturant_manager.dto;

import no.ntnu.resturant_manager.model.enums.ChecklistFrequency;
import no.ntnu.resturant_manager.model.enums.ComplianceArea;

public record ChecklistResponse(
	Long id,
	String name,
	String description,
	ChecklistFrequency frequency,
	ComplianceArea complianceArea,
	boolean active,
	Long organizationId
) {
}
