package edu.ntnu.idatt2105.backend.report.dto.shared;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
public class ReportPeriod {
  private LocalDateTime from;
  private LocalDateTime to;
}
