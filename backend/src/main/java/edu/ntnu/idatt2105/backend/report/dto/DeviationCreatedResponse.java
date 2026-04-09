package edu.ntnu.idatt2105.backend.report.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response DTO returned after successfully creating a deviation report.
 *
 * <p>Carries the new report's UUID and the server-assigned creation timestamp.
 */
@Data
@AllArgsConstructor
public class DeviationCreatedResponse {
  private UUID id;
  private LocalDateTime createdAt;
}
