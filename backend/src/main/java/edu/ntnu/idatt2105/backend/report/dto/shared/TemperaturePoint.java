package edu.ntnu.idatt2105.backend.report.dto.shared;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import edu.ntnu.idatt2105.backend.temperature.model.enums.TemperatureZone;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TemperaturePoint {
  private LocalDateTime measuredAt;
  private Long zoneId;
  private String zoneName;
  private TemperatureZone zoneType;
  private String taskName;
  private BigDecimal valueC;
  private BigDecimal targetMin;
  private BigDecimal targetMax;
  private boolean withinRange;
  private String recordedBy;

}
