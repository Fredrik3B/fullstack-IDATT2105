package edu.ntnu.idatt2105.backend.common.dto.icchecklist;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UpdateChecklistCardRequest(
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

