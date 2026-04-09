package edu.ntnu.idatt2105.backend.checklist.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for toggling a checklist's visibility on the workbench.
 */
public record ChecklistWorkbenchStateRequest(
	@NotNull
	Boolean displayedOnWorkbench
) {
}
