package edu.ntnu.idatt2105.backend.checklist.dto;

import jakarta.validation.constraints.NotNull;

public record ChecklistWorkbenchStateRequest(
	@NotNull
	Boolean displayedOnWorkbench
) {
}
