package edu.ntnu.idatt2105.backend.checklist.dto;

import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a new checklist card.
 */
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

	@NotNull
	Boolean displayedOnWorkbench,

	@NotEmpty
	List<Long> taskTemplateIds
) {
}
