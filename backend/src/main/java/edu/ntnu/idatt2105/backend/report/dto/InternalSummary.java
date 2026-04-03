package edu.ntnu.idatt2105.backend.report.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InternalSummary {
  private ReportPeriod period;
  private ComplianceStats matStats;
  private ComplianceStats alkoholStats;
  private List<UnresolvedItemDto> unresolvedItems;
}
