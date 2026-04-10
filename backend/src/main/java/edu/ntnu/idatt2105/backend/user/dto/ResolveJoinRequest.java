package edu.ntnu.idatt2105.backend.user.dto;

import edu.ntnu.idatt2105.backend.user.model.enums.JoinOrgStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request DTO for accepting or declining a pending join request.
 */
@Data
@Schema(description = "Request to accept or decline a pending join request")
public class ResolveJoinRequest {

  @NotNull
  @Schema(description = "Action to take on the join request", example = "ACCEPTED")
  private JoinOrgStatus action;
}