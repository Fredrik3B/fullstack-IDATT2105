package edu.ntnu.idatt2105.backend.report.dto;

import edu.ntnu.idatt2105.backend.report.dto.shared.ChecklistSection;
import edu.ntnu.idatt2105.backend.report.dto.shared.ComplianceStats;
import edu.ntnu.idatt2105.backend.report.dto.shared.DeviationDayPoint;
import edu.ntnu.idatt2105.backend.report.dto.shared.MissedTaskRecord;
import edu.ntnu.idatt2105.backend.report.dto.shared.OrgSection;
import edu.ntnu.idatt2105.backend.report.dto.shared.ReportPeriod;
import edu.ntnu.idatt2105.backend.report.dto.shared.TemperaturePoint;
import edu.ntnu.idatt2105.backend.report.dto.shared.UnresolvedItemDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Full inspection report DTO aggregating compliance statistics, checklist activity,
 * temperature readings, deviations, and organisation metadata for a given time period.
 *
 * <p>Generated on demand by {@link edu.ntnu.idatt2105.backend.report.service.ReportService#generateInspection}
 * and intended for export to PDF or display in the management dashboard.
 */
@Data
@Builder
public class InspectionReport {
  private ReportPeriod period;
  private LocalDateTime generatedAt;
  private OrgSection organization;
  private ChecklistSection checklists;
  private List<TemperaturePoint> temperatureLog;
  private List<DeviationDayPoint> deviationsByDay;
  private List<MissedTaskRecord> missedTasks;
  private List<UnresolvedItemDto> deviations;
  private ComplianceStats foodStats;
  private ComplianceStats alcoholStats;
}
