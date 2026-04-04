package edu.ntnu.idatt2105.backend.report.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ReportPeriod {
  private LocalDateTime from;
  private LocalDateTime to;
}
