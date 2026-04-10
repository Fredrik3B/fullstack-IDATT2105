package edu.ntnu.idatt2105.backend.checklist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Response DTO representing a checklist card shown in the workbench or library view.
 *
 * <p>Contains metadata, progress summary, and the full list of task sections.
 * The {@code statusLabel} and {@code statusTone} drive the UI chip colour.
 */
@Schema(description = "Checklist card shown in the workbench or library view")
public record ChecklistCardResponse(

		@Schema(description = "Checklist ID")
		Long id,

		@Schema(description = "Display period string", example = "Week 15")
		String period,

		@Schema(description = "Active period key", example = "2026-W15")
		String activePeriodKey,

		@Schema(description = "Whether the checklist recurs automatically")
		Boolean recurring,

		@Schema(description = "Whether the checklist is shown on the workbench")
		Boolean displayedOnWorkbench,

		@Schema(description = "Checklist title")
		String title,

		@Schema(description = "Subtitle or description")
		String subtitle,

		@Schema(description = "Module chip label")
		String moduleChip,

		@Schema(description = "Whether this checklist is featured")
		Boolean featured,

		@Schema(description = "Status label shown")
		String statusLabel,

		@Schema(description = "Tone/color of the status chip")
		String statusTone,

		@Schema(description = "Completion progress percentage")
		Integer progress,

		@Schema(description = "List of task sections in this checklist")
		List<ChecklistSectionResponse> sections
) {}
