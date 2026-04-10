package edu.ntnu.idatt2105.backend.report.dto.shared;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Builder;

/**
 * A single data point in the deviations-per-day time series used to render trend charts.
 */
@Builder
@Schema(description = "Number of deviations reported on a specific day")
public record DeviationDayPoint(

    @Schema(description = "The date")
    LocalDate date,

    @Schema(description = "Number of deviations that day")
    int count
) {

}
