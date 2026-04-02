package edu.ntnu.idatt2105.backend.common.dto.icchecklist;

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

	@NotEmpty
	List<Long> taskTemplateIds
) {
}
