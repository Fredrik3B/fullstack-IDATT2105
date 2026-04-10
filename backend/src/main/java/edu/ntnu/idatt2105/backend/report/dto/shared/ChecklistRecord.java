package edu.ntnu.idatt2105.backend.report.dto.shared;

import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * Per-checklist statistics included in a
 * {@link edu.ntnu.idatt2105.backend.report.dto.shared.ChecklistSection}.
 *
 * <p>{@code averageCompletionRate} is the mean completion rate across all period runs
 * within the report window, while {@code completionRate} is the simple overall rate across all
 * tasks in the period.
 */

@Builder
@Schema(description = "Completion details for a single checklist")
public record ChecklistRecord(

    @Schema(description = "Checklist name")
    String name,

    @Schema(description = "Checklist description")
    String description,

    @Schema(description = "How often this checklist runs", example = "DAILY")
    String frequency,

    @Schema(description = "Compliance area", example = "IK_MAT")
    ComplianceArea complianceArea,

    @Schema(description = "Number of times completed in the period")
    int completionsInPeriod,

    @Schema(description = "Expected number of runs in the period")
    int expectedRuns,

    @Schema(description = "Total tasks across all runs")
    int totalTasks,

    @Schema(description = "Number of completed tasks")
    int completedTasks,

    @Schema(description = "Number of tasks with deviations")
    int deviatedTasks,

    @Schema(description = "Completion rate as a percentage", example = "88.0")
    double completionRate,

    @Schema(description = "Average completion rate across all runs", example = "85.5")
    double averageCompletionRate
) {

}
