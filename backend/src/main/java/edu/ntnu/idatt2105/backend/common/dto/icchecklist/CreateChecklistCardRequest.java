 package edu.ntnu.idatt2105.backend.common.dto.icchecklist;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
	@Valid
	List<ChecklistSectionUpsertRequest> sections
) {
}

