package no.ntnu.resturant_manager.dto;

public record CreateTaskRequest(
	String title,
	String description,
	Integer orderIndex,
	Boolean requiredTask,
	Boolean active,
	Long checklistId
) {
}
