package edu.ntnu.idatt2105.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user registration.
 */
@Schema(description = "Request to register a new user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

  @NotBlank
  @Email
  @Schema(description = "User's email address", example = "john.doe@example.com")
  private String email;

  @NotBlank
  @Size(min = 8, max = 100)
  @Schema(description = "User's password, minimum 8 characters")
  private String password;

  @NotBlank
  @Size(max = 50)
  @Schema(description = "User's first name", example = "John")
  private String firstName;

  @NotBlank
  @Size(max = 50)
  @Schema(description = "User's last name", example = "Doe")
  private String lastName;
}
