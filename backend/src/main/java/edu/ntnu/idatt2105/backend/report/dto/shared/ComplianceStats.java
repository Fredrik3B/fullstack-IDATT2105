package edu.ntnu.idatt2105.backend.report.dto.shared;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * Aggregated compliance statistics for a single compliance area within the report period.
 *
 * <p>Rates ({@code completionRate}, {@code outOfRangeRate}) are expressed as percentages (0–100).
 */
@Builder
@Schema(description = "Compliance statistics for a module")
public record ComplianceStats(

    @Schema(description = "Total number of tasks in the period")
    int totalTasks,

    @Schema(description = "Number of completed tasks")
    int completedTasks,

    @Schema(description = "Completion rate as a percentage", example = "92.5")
    double completionRate,

    @Schema(description = "Number of tasks with deviations")
    int deviatedTasks,

    @Schema(description = "Total temperature readings taken")
    int temperatureReadings,

    @Schema(description = "Number of readings outside allowed range")
    int outOfRangeReadings,

    @Schema(description = "Out of range rate as a percentage", example = "4.2")
    double outOfRangeRate
) {

}
