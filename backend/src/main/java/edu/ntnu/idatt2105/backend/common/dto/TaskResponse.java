package edu.ntnu.idatt2105.backend.common.dto;

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
