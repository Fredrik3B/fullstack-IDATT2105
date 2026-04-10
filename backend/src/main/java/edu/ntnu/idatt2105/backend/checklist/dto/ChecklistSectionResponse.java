package edu.ntnu.idatt2105.backend.checklist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Response DTO representing a named section within a checklist card, grouping related tasks.
 */
@Schema(description = "A named section within a checklist, grouping related tasks")
public record ChecklistSectionResponse(

		@Schema(description = "Section title", example = "Temperature Checks")
		String title,

		@Schema(description = "List of tasks in this section")
		List<ChecklistTaskItemResponse> items
) {}
