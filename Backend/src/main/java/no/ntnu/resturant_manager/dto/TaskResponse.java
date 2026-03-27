package no.ntnu.resturant_manager.dto;

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
