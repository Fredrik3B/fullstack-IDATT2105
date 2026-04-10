package edu.ntnu.idatt2105.backend.checklist.dto;

import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request DTO for creating a new checklist card.
 */
@Schema(description = "Request to create a new checklist card")
public record CreateChecklistCardRequest(
    @NotNull
    @Schema(description = "Module this checklist belongs to", example = "IC_FOOD")
    IcModule module,

    @NotBlank
    @Schema(description = "Display period for the checklist", example = "Week 15")
    String period,

    @NotBlank
    @Schema(description = "Checklist title", example = "Daily Kitchen Hygiene")
    String title,

    @Schema(description = "Optional subtitle or description")
    String subtitle,

    @NotNull
    @Schema(description = "Whether the checklist recurs automatically")
    Boolean recurring,

    @NotNull
    @Schema(description = "Whether the checklist is shown on the workbench")
    Boolean displayedOnWorkbench,

    @NotEmpty
    @Schema(description = "List of task template IDs to include in this checklist")
    List<Long> taskTemplateIds
) {

}