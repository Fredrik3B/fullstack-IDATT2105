package edu.ntnu.idatt2105.backend.report.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UnresolvedItemDto {
  private String name;
  private LocalDate notDoneBy;
}
