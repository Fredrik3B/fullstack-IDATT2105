package edu.ntnu.idatt2105.backend.report.dto.shared;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

/**
 * A single data point in the deviations-per-day time series used to render trend charts.
 */
@Data
@Builder
public class DeviationDayPoint {
  private LocalDate date;
  private int count;
}
