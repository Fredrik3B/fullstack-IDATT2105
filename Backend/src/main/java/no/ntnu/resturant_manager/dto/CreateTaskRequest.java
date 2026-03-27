package no.ntnu.resturant_manager.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
	@NotBlank
	@Size(max = 120)
	String title,

	@Size(max = 1000)
	String description,

	@Min(0)
	Integer orderIndex,

	Boolean requiredTask,

	Boolean active,

	@NotNull
	Long checklistId
) {
}
