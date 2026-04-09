package edu.ntnu.idatt2105.backend.report.dto.shared;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import edu.ntnu.idatt2105.backend.temperature.model.enums.TemperatureZone;
import lombok.Builder;
import lombok.Data;

@Builder
@Schema(description = "A single temperature log entry")
public record TemperaturePoint(

    @Schema(description = "When the temperature was measured")
    LocalDateTime measuredAt,

    @Schema(description = "Zone ID")
    Long zoneId,

    @Schema(description = "Zone display name")
    String zoneName,

    @Schema(description = "Type of temperature zone", example = "FREEZER")
    TemperatureZone zoneType,

    @Schema(description = "Associated task name") String taskName,

    @Schema(description = "Measured temperature in Celsius", example = "3.5")
    BigDecimal valueC,

    @Schema(description = "Minimum allowed temperature", example = "2.0")
    BigDecimal targetMin,

    @Schema(description = "Maximum allowed temperature", example = "8.0")
    BigDecimal targetMax,

    @Schema(description = "Whether the reading was within the allowed range")
    boolean withinRange,

    @Schema(description = "Name of the employee who recorded this")
    String recordedBy
) {}
