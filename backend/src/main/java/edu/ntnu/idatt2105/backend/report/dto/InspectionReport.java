package edu.ntnu.idatt2105.backend.report.dto;

import edu.ntnu.idatt2105.backend.report.dto.shared.ChecklistSection;
import edu.ntnu.idatt2105.backend.report.dto.shared.ComplianceStats;
import edu.ntnu.idatt2105.backend.report.dto.shared.DeviationDayPoint;
import edu.ntnu.idatt2105.backend.report.dto.shared.MissedTaskRecord;
import edu.ntnu.idatt2105.backend.report.dto.shared.OrgSection;
import edu.ntnu.idatt2105.backend.report.dto.shared.ReportPeriod;
import edu.ntnu.idatt2105.backend.report.dto.shared.TemperaturePoint;
import edu.ntnu.idatt2105.backend.report.dto.shared.UnresolvedItemDto;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Builder
@Schema(description = "Full inspection report for regulatory authorities")
public record InspectionReport (

    @Schema(description = "Report time period")
    ReportPeriod period,

    @Schema(description = "When the report was generated")
    LocalDateTime generatedAt,

    @Schema(description = "Organization details")
    OrgSection organization,

    @Schema(description = "Checklist information")
    ChecklistSection checklists,

    @Schema(description = "Temperature log entries")
    List<TemperaturePoint> temperatureLog,

    @Schema(description = "Deviations grouped by day")
    List<DeviationDayPoint> deviationsByDay,

    @Schema(description = "Tasks that were not completed")
    List<MissedTaskRecord> missedTasks,

    @Schema(description = "Task deviations")
    List<UnresolvedItemDto> deviations,

    @Schema(description = "Food compliance statistics")
    ComplianceStats foodStats,

    @Schema(description = "Alcohol compliance statistics")
    ComplianceStats alcoholStats
) {}
