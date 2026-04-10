package edu.ntnu.idatt2105.backend.temperature.dto;

import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import edu.ntnu.idatt2105.backend.temperature.model.enums.TemperatureZone;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * Response DTO representing a temperature zone.
 */
@Schema(description = "A temperature zone with its allowed range")
public record TemperatureZoneResponse(

    @Schema(description = "Zone ID")
    Long id,

    @Schema(description = "Module this zone belongs to", example = "IC_FOOD")
    IcModule module,

    @Schema(description = "Display name of the zone", example = "Fridge")
    String name,

    @Schema(description = "Type of temperature zone", example = "FRIDGE")
    TemperatureZone zoneType,

    @Schema(description = "Minimum allowed temperature in Celsius", example = "2.0")
    BigDecimal targetMin,

    @Schema(description = "Maximum allowed temperature in Celsius", example = "8.0")
    BigDecimal targetMax
) {

}
