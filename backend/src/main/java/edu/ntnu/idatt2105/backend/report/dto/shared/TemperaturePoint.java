package edu.ntnu.idatt2105.backend.report.dto.shared;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TemperaturePoint {
  private LocalDateTime measuredAt;
  private String taskName;
  private double valueC;
  private Double targetMin;
  private Double targetMax;
  private boolean withinRange;
  private String recordedBy;

}
