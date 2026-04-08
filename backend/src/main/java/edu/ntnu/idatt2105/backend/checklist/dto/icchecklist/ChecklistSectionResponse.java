package edu.ntnu.idatt2105.backend.checklist.dto.icchecklist;

import java.util.List;

public record ChecklistSectionResponse(
	String title,
	List<ChecklistTaskItemResponse> items
) {
}

