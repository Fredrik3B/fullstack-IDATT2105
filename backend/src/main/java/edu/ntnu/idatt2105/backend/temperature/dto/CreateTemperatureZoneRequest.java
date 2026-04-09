package edu.ntnu.idatt2105.backend.temperature.dto;

import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import edu.ntnu.idatt2105.backend.temperature.model.enums.TemperatureZone;
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
public record CreateTemperatureZoneRequest(
	@NotNull
	IcModule module,

	@NotBlank
	@Size(max = 120)
	String name,

	@NotNull
	TemperatureZone zoneType,

	@NotNull
	@DecimalMin("-999.99")
	@DecimalMax("999.99")
	BigDecimal targetMin,

	@NotNull
	@DecimalMin("-999.99")
	@DecimalMax("999.99")
	BigDecimal targetMax
) {
}
