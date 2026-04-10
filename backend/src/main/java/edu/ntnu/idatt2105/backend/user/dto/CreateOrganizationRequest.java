package edu.ntnu.idatt2105.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for creating a new organisation.
 */
@Data
@Schema(description = "Request to create a new organisation")
public class CreateOrganizationRequest {

  @NotBlank
  @Size(max = 200)
  @Schema(description = "Name of the organisation", example = "Everest Sushi & Fusion AS")
  private String name;
}
