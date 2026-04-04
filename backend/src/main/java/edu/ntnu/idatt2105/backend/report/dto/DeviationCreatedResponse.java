package edu.ntnu.idatt2105.backend.report.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class DeviationCreatedResponse {
  private UUID id;
  private LocalDateTime createdAt;
}
