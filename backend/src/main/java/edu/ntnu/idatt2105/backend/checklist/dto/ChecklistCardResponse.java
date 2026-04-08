package edu.ntnu.idatt2105.backend.checklist.dto;

import java.util.List;

public record ChecklistCardResponse(
	Long id,
	String period,
	String activePeriodKey,
	Boolean recurring,
	Boolean displayedOnWorkbench,
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
