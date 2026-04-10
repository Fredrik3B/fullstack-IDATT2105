package edu.ntnu.idatt2105.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

/**
 * Request DTO for replacing a member's roles with a new set of role names.
 */
@Data
@Schema(description = "Request to replace a member's roles with a new set")
public class UpdateMemberRolesRequest {

  @NotEmpty
  @Schema(description = "List of role names to assign to the member", example = "[\"ROLE_MANAGER\"]")
  private List<String> roles;
}