package edu.ntnu.idatt2105.backend.checklist.dto.checklist;


import edu.ntnu.idatt2105.backend.checklist.model.enums.ChecklistFrequency;
import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
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
