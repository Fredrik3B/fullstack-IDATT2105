package edu.ntnu.idatt2105.backend.report.dto;

import edu.ntnu.idatt2105.backend.report.dto.shared.ChecklistSection;
import edu.ntnu.idatt2105.backend.report.dto.shared.ComplianceStats;
import edu.ntnu.idatt2105.backend.report.dto.shared.OrgSection;
import edu.ntnu.idatt2105.backend.report.dto.shared.ReportPeriod;
import edu.ntnu.idatt2105.backend.report.dto.shared.TemperaturePoint;
import edu.ntnu.idatt2105.backend.report.dto.shared.UnresolvedItemDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InspectionReport {
  private ReportPeriod period;
  private LocalDateTime generatedAt;
  private OrgSection organization;
  private ChecklistSection checklists;
  private List<TemperaturePoint> temperatureLog;
  private List<UnresolvedItemDto> deviations;
  private ComplianceStats foodStats;
  private ComplianceStats alcoholStats;
}
