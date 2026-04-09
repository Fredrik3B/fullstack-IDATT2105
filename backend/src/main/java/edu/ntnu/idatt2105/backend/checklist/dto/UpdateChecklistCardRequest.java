package edu.ntnu.idatt2105.backend.checklist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Request DTO for updating an existing checklist card's metadata and task selection.
 */
public record UpdateChecklistCardRequest(
	@NotBlank
	String period,

	@NotBlank
	String title,

	String subtitle,

	Boolean recurring,

	Boolean displayedOnWorkbench,

	@NotEmpty
	List<Long> taskTemplateIds
) {
}
