package edu.ntnu.idatt2105.backend.report.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor

public class UnresolvedItemDto {
  private String name;
  private LocalDateTime notDoneBy;
}
