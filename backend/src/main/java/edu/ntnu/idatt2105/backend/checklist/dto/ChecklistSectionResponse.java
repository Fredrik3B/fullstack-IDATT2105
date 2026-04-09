package edu.ntnu.idatt2105.backend.checklist.dto;

import java.util.List;

/**
 * Response DTO representing a named section within a checklist card, grouping related tasks.
 */
public record ChecklistSectionResponse(
	String title,
	List<ChecklistTaskItemResponse> items
) {
}

