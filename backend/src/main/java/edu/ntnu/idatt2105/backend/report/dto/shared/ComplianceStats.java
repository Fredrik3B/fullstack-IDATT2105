package edu.ntnu.idatt2105.backend.report.dto.shared;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComplianceStats {
  private int totalTasks;
  private int completedTasks;
  private double completionRate;
  private int deviatedTasks;
  private int temperatureReadings;
  private int outOfRangeReadings;
  private double outOfRangeRate;
}
