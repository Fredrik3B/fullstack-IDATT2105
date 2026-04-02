package edu.ntnu.idatt2105.backend.common.dto.icchecklist;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateChecklistCardRequest(
	@NotNull
	IcModule module,

	@NotBlank
	String period,

	@NotBlank
	String title,

	String subtitle,

	@NotNull
	Boolean recurring,

	@NotEmpty
	List<Long> taskTemplateIds
) {
}
