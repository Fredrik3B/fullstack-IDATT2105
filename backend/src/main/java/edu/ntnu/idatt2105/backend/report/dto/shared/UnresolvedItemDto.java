package edu.ntnu.idatt2105.backend.report.dto.shared;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO representing a task that ended without being completed (a deviation).
 */
@Data
@AllArgsConstructor
public class UnresolvedItemDto {
  private String name;
  private LocalDateTime notDoneBy;
}
