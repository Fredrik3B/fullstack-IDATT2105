package edu.ntnu.idatt2105.backend.report.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ReportPeriod {
  private LocalDate from;
  private LocalDate to;
}
