package edu.ntnu.idatt2105.backend.report.dto.shared;

import lombok.Builder;
import lombok.Data;

/**
 * Aggregated compliance statistics for a single compliance area within the report period.
 *
 * <p>Rates ({@code completionRate}, {@code outOfRangeRate}) are expressed as percentages (0–100).
 */
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
