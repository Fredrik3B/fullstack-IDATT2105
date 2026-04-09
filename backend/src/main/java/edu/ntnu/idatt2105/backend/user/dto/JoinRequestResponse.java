package edu.ntnu.idatt2105.backend.user.dto;

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
public class JoinRequestResponse {
  private UUID requestId;
  private JoinOrgStatus status;
  private LocalDateTime createdAt;
}
