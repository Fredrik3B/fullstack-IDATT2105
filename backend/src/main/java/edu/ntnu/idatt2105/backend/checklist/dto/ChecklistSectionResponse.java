package edu.ntnu.idatt2105.backend.checklist.dto;

import java.util.List;

public record ChecklistSectionResponse(
	String title,
	List<ChecklistTaskItemResponse> items
) {
}

