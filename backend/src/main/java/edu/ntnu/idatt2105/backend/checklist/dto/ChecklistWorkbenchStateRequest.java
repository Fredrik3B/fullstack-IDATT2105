package edu.ntnu.idatt2105.backend.checklist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for toggling a checklist's visibility on the workbench.
 */
@Schema(description = "Request to toggle a checklist's visibility on the workbench")
public record ChecklistWorkbenchStateRequest(
    @NotNull
    @Schema(description = "Whether the checklist should be displayed on the workbench", example = "true")
    Boolean displayedOnWorkbench
) {

}