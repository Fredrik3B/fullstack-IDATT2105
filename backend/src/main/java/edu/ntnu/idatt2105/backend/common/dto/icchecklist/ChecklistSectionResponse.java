package edu.ntnu.idatt2105.backend.common.dto.icchecklist;

import java.util.List;

public record ChecklistSectionResponse(
	String title,
	List<ChecklistTaskItemResponse> items
) {
}

