package edu.ntnu.idatt2105.backend.report.dto;

import edu.ntnu.idatt2105.backend.report.dto.shared.ComplianceStats;
import edu.ntnu.idatt2105.backend.report.dto.shared.ReportPeriod;
import edu.ntnu.idatt2105.backend.report.dto.shared.UnresolvedItemDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Lightweight compliance summary DTO used for the internal dashboard widget.
 *
 * <p>Contains food and alcohol compliance statistics plus unresolved (deviated) items
 * for the requested time period.
 */
@Builder
@Schema(description = "Short summary report")
public record InternalSummary(

    @Schema(description = "Report time period")
    ReportPeriod period,

    @Schema(description = "Food compliance statistics")
    ComplianceStats foodStats,

    @Schema(description = "Alcohol compliance statistics")
    ComplianceStats alcoholStats,

    @Schema(description = "All currently unresolved tasks")
    List<UnresolvedItemDto> unresolvedItems
) {}