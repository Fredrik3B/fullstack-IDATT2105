package edu.ntnu.idatt2105.backend.report.dto.shared;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Time period covered by the report")
public record ReportPeriod(

    @Schema(description = "Start of the period")
    LocalDateTime from,

    @Schema(description = "End of the period")
    LocalDateTime to
) {}
