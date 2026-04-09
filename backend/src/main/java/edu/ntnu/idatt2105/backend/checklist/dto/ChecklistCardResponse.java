package edu.ntnu.idatt2105.backend.checklist.dto;

import java.util.List;

/**
 * Response DTO representing a checklist card shown in the workbench or library view.
 *
 * <p>Contains metadata, progress summary, and the full list of task sections.
 * The {@code statusLabel} and {@code statusTone} drive the UI chip colour.
 */
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
