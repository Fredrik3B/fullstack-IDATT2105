package edu.ntnu.idatt2105.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

/**
 * DTO representing a member of an organisation, including their current roles.
 */
@Data
@Builder
@Schema(description = "A member of an organisation with their current roles")
public class MemberDto {

  @Schema(description = "User ID")
  UUID userId;

  @Schema(description = "First name", example = "John")
  String firstName;

  @Schema(description = "Last name", example = "Doe")
  String lastName;

  @Schema(description = "Email address", example = "john.doe@example.com")
  String email;

  @Schema(description = "Set of roles assigned to this member")
  Set<String> roles;
}
