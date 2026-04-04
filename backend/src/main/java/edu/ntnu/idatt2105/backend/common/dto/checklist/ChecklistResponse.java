package edu.ntnu.idatt2105.backend.common.dto.checklist;


import edu.ntnu.idatt2105.backend.common.model.enums.ChecklistFrequency;
import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import java.util.UUID;

public record ChecklistResponse(
	Long id,
	String name,
	String description,
	ChecklistFrequency frequency,
	ComplianceArea complianceArea,
	boolean active,
	UUID organizationId
) {
}
