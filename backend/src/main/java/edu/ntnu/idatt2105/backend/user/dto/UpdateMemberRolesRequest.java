package edu.ntnu.idatt2105.backend.user.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

/**
 * Request DTO for replacing a member's roles with a new set of role names.
 */
@Data
public class UpdateMemberRolesRequest {
  @NotEmpty
  private List<String> roles;
}
