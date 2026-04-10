package edu.ntnu.idatt2105.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import edu.ntnu.idatt2105.backend.user.model.enums.JoinOrgStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

/**
 * Response DTO shown to a user who checks the status of their own pending join request.
 */
@Data
@Builder
@Schema(description = "Status of a user's own pending join request")
public class JoinRequestResponse {

  @Schema(description = "ID of the join request")
  UUID requestId;

  @Schema(description = "Current status of the request", example = "PENDING")
  JoinOrgStatus status;

  @Schema(description = "When the request was created")
  LocalDateTime createdAt;
}
