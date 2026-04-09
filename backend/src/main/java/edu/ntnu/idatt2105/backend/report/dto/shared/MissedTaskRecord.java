package edu.ntnu.idatt2105.backend.report.dto.shared;

import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * Summarises how many times a specific task was missed (deviated) across all period runs
 * within the report window. Results are capped at the top 8 most-missed tasks.
 */
@Builder
@Schema(description = "A task that was not completed within it's set period")
public record MissedTaskRecord(

    @Schema(description = "Name of the missed task")
    String taskName,

    @Schema(description = "Checklist the task belongs to")
    String checklistName,

    @Schema(description = "Compliance area", example = "IK_MAT")
    ComplianceArea complianceArea,

    @Schema(description = "Number of times this task was missed")
    int missedCount
) {}