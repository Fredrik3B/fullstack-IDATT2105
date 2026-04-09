package edu.ntnu.idatt2105.backend.user.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

/**
 * Response DTO representing an organisation summary.
 */
@Data
@Builder
public class OrganizationResponse {
  private final UUID id;
  private final String name;
  private final String joinCode;
}
