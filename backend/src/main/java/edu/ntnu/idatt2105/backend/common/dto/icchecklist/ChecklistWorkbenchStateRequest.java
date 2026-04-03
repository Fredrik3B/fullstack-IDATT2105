package edu.ntnu.idatt2105.backend.common.dto.icchecklist;

import jakarta.validation.constraints.NotNull;

public record ChecklistWorkbenchStateRequest(
	@NotNull
	Boolean displayedOnWorkbench
) {
}
