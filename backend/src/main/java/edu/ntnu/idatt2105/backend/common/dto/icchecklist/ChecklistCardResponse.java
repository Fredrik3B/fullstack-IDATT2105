package edu.ntnu.idatt2105.backend.common.dto.icchecklist;

import java.util.List;

public record ChecklistCardResponse(
	Long id,
	String period,
	String title,
	String subtitle,
	String moduleChip,
	Boolean featured,
	String statusLabel,
	String statusTone,
	Integer progress,
	List<ChecklistSectionResponse> sections
) {
}

