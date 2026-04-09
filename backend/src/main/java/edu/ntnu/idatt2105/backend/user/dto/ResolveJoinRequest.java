package edu.ntnu.idatt2105.backend.user.dto;

import edu.ntnu.idatt2105.backend.user.model.enums.JoinOrgStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request DTO for accepting or declining a pending join request.
 */
@Data
public class ResolveJoinRequest {
  @NotNull
  private JoinOrgStatus action;
}