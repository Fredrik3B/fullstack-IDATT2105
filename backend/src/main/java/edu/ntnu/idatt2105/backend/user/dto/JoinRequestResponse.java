package edu.ntnu.idatt2105.backend.user.dto;

import edu.ntnu.idatt2105.backend.user.model.enums.JoinOrgStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JoinRequestResponse {
  private UUID requestId;
  private JoinOrgStatus status;
  private LocalDateTime createdAt;
}
