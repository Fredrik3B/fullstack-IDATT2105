package edu.ntnu.idatt2105.backend.common.dto.task;

public record TaskResponse(
	Long id,
	String title,
	String description,
	int orderIndex,
	boolean requiredTask,
	boolean active,
	Long checklistId
) {
}
