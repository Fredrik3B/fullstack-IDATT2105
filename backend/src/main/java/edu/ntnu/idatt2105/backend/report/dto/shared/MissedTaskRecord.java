package edu.ntnu.idatt2105.backend.report.dto.shared;

import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import lombok.Builder;
import lombok.Data;

/**
 * Summarises how many times a specific task was missed (deviated) across all period runs
 * within the report window. Results are capped at the top 8 most-missed tasks.
 */
@Data
@Builder
public class MissedTaskRecord {
  private String taskName;
  private String checklistName;
  private ComplianceArea complianceArea;
  private int missedCount;
}
