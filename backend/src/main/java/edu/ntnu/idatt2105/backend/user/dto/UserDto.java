package edu.ntnu.idatt2105.backend.user.dto;

import edu.ntnu.idatt2105.backend.user.model.RoleModel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;


@Schema(description = "Response object for a user")
@Data
@Builder
public class UserDto {
  private UUID userId;
  private UUID organizationId;
  private String firstName;
  private String lastName;
  private String email;
  private Set<RoleModel> role;

}
