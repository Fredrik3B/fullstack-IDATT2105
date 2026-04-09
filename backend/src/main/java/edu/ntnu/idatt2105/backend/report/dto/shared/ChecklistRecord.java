package edu.ntnu.idatt2105.backend.report.dto.shared;

import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import lombok.Builder;
import lombok.Data;

/**
 * Per-checklist statistics included in a {@link edu.ntnu.idatt2105.backend.report.dto.shared.ChecklistSection}.
 *
 * <p>{@code averageCompletionRate} is the mean completion rate across all period runs
 * within the report window, while {@code completionRate} is the simple overall rate
 * across all tasks in the period.
 */
@Data
@Builder
public class ChecklistRecord {
  private String name;
  private String description;
  private String frequency;
  private ComplianceArea complianceArea;
  private int completionsInPeriod;
  private int expectedRuns;
  private int totalTasks;
  private int completedTasks;
  private int deviatedTasks;
  private double completionRate;
  private double averageCompletionRate;
}
