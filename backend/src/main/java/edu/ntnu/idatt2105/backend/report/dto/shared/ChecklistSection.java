package edu.ntnu.idatt2105.backend.report.dto.shared;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

/**
 * Report section summarising checklist activity within the report period.
 */
@Builder
@Schema(description = "Checklist completion summary for the report period")
public record ChecklistSection(

    @Schema(description = "Total number of checklists")
    int totalChecklists,

    @Schema(description = "Number of currently active checklists")
    int activeChecklists,

    @Schema(description = "Individual checklist records")
    List<ChecklistRecord> checklists
) {

}
