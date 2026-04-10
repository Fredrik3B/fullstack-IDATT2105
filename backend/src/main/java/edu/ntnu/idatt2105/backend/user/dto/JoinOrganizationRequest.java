package edu.ntnu.idatt2105.backend.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;


/**
 * Request DTO for submitting a join request using an organisation's join code.
 */
@Data
@Schema(description = "Request to join an organisation using a join code")
public class JoinOrganizationRequest {

  @Schema(description = "Email of the user joining", example = "john.doe@example.com")
  private String email;

  @NotBlank
  @Schema(description = "First name of the user", example = "John")
  private String firstName;

  @NotBlank
  @Schema(description = "Last name of the user", example = "Doe")
  private String lastName;

  @NotBlank
  @Schema(description = "Join code for the organisation", example = "ABC-3123")
  private String joinCode;
}
