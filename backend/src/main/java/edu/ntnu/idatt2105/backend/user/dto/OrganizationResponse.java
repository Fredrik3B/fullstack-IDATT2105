package edu.ntnu.idatt2105.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

/**
 * Response DTO representing an organisation summary.
 */
@Data
@Builder
@Schema(description = "Organisation summary")
public class OrganizationResponse {

  @Schema(description = "Organisation ID")
  private final UUID id;

  @Schema(description = "Organisation name", example = "Restaurant AS")
  private final String name;
  @Schema(description = "Join code for inviting staff", example = "ABC-3123")
  private final String joinCode;
}