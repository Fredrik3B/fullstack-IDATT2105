package edu.ntnu.idatt2105.backend.report.dto.shared;

import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import lombok.Builder;
import lombok.Data;

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
