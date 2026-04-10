package edu.ntnu.idatt2105.backend.temperature.dto;

import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for a single temperature measurement.
 *
 * <p>{@code deviation} is {@code true} when the recorded {@code valueC} falls outside
 * the task template's {@code targetMin} / {@code targetMax} range.
 */
@Schema(description = "A single temperature measurement")
public record TemperatureMeasurementResponse(

    @Schema(description = "Measurement ID")
    Long id,

    @Schema(description = "Module this measurement belongs to", example = "IC_FOOD")
    IcModule module,

    @Schema(description = "ID of the checklist this measurement belongs to")
    Long checklistId,

    @Schema(description = "ID of the task this measurement was logged against")
    Long taskId,

    @Schema(description = "Measured temperature", example = "3.5")
    BigDecimal valueC,

    @Schema(description = "When the measurement was taken")
    LocalDateTime measuredAt,

    @Schema(description = "Period key this measurement belongs to", example = "2026-W15")
    String periodKey,

    @Schema(description = "Whether the measurement is outside the allowed range")
    Boolean deviation
) {

}
