package no.ntnu.resturant_manager.dto;

import java.time.LocalDateTime;

public record TaskResponse(
	Long id,
	String title,
	String description,
	int orderIndex,
	boolean requiredTask,
	boolean active,
	Long checklistId,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
}
