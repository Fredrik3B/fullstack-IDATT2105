package edu.ntnu.idatt2105.backend.checklist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Request DTO for updating an existing checklist card's metadata and task selection.
 */
@Schema(description = "Request to update an existing checklist card's metadata and task selection")
public record UpdateChecklistCardRequest(

    @NotBlank
    @Schema(description = "Display period for the checklist", example = "Week 15")
    String period,

    @NotBlank
    @Schema(description = "Checklist title", example = "Daily Kitchen Hygiene")
    String title,

    @Schema(description = "Optional subtitle or description")
    String subtitle,

    @Schema(description = "Whether the checklist recurs automatically")
    Boolean recurring,

    @Schema(description = "Whether the checklist is shown on the workbench")
    Boolean displayedOnWorkbench,

    @NotEmpty
    @Schema(description = "Updated list of task template IDs to include")
    List<Long> taskTemplateIds
) {

}
