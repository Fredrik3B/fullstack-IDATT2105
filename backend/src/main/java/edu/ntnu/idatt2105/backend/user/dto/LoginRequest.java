package edu.ntnu.idatt2105.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request DTO for email/password login.
 */
@Data
@Schema(description = "Request to log in with email and password")
public class LoginRequest {

  @NotBlank
  @Email
  @Schema(description = "User's email address", example = "john.doe@example.com")
  public String email;

  @NotBlank
  @Schema(description = "User's password")
  public String password;
}