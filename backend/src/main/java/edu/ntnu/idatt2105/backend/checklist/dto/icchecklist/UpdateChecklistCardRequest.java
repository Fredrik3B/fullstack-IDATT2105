package edu.ntnu.idatt2105.backend.checklist.dto.icchecklist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

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
