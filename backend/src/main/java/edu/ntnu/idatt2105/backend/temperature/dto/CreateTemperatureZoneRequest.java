package edu.ntnu.idatt2105.backend.temperature.dto;

import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import edu.ntnu.idatt2105.backend.temperature.model.enums.TemperatureZone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Request DTO for creating or updating a temperature zone.
 *
 * <p>Both {@code targetMin} and {@code targetMax} are required and must satisfy
 * {@code targetMin <= targetMax} (validated by the service layer).
 */
@Schema(description = "Request to create or update a temperature zone")
public record CreateTemperatureZoneRequest(
    @NotNull
    @Schema(description = "Module this zone belongs to", example = "IC_FOOD")
    IcModule module,

    @NotBlank
    @Size(max = 120)
    @Schema(description = "Display name of the zone")
    String name,

    @NotNull
    @Schema(description = "Type of temperature zone", example = "FRIDGE")
    TemperatureZone zoneType,

    @NotNull
    @DecimalMin("-999.99")
    @DecimalMax("999.99")
    @Schema(description = "Minimum allowed temperature in Celsius", example = "2.0")
    BigDecimal targetMin,

    @NotNull
    @DecimalMin("-999.99")
    @DecimalMax("999.99")
    @Schema(description = "Maximum allowed temperature in Celsius", example = "8.0")
    BigDecimal targetMax
) {

}
