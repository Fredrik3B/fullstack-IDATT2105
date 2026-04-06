package edu.ntnu.idatt2105.backend.report.dto.shared;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeviationDayPoint {
  private LocalDate date;
  private int count;
}
