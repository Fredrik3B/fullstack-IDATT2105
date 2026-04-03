package edu.ntnu.idatt2105.backend.user.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class UpdateMemberRolesRequest {
  @NotEmpty
  private List<String> roles;
}
