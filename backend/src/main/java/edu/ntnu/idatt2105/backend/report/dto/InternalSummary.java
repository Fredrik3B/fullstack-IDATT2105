package edu.ntnu.idatt2105.backend.report.dto;

import edu.ntnu.idatt2105.backend.report.dto.shared.ComplianceStats;
import edu.ntnu.idatt2105.backend.report.dto.shared.ReportPeriod;
import edu.ntnu.idatt2105.backend.report.dto.shared.UnresolvedItemDto;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Lightweight compliance summary DTO used for the internal dashboard widget.
 *
 * <p>Contains food and alcohol compliance statistics plus unresolved (deviated) items
 * for the requested time period.
 */
@Data
@Builder
public class InternalSummary {
  private ReportPeriod period;
  private ComplianceStats foodStats;
  private ComplianceStats alcoholStats;
  private List<UnresolvedItemDto> unresolvedItems;
}
