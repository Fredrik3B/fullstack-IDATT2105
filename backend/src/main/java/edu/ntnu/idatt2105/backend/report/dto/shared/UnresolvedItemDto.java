package edu.ntnu.idatt2105.backend.report.dto.shared;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(description = "An unresolved task or deviation")
public record UnresolvedItemDto(

    @Schema(description = "Name or title of the item")
    String name,

    @Schema(description = "Deadline that was missed")
    LocalDateTime notDoneBy
) {}