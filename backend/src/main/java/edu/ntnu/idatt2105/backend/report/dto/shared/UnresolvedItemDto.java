package edu.ntnu.idatt2105.backend.report.dto.shared;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class UnresolvedItemDto {
  private String name;
  private LocalDateTime notDoneBy;
}
