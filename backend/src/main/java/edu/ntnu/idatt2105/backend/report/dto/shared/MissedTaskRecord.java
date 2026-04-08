package edu.ntnu.idatt2105.backend.report.dto.shared;

import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MissedTaskRecord {
  private String taskName;
  private String checklistName;
  private ComplianceArea complianceArea;
  private int missedCount;
}
