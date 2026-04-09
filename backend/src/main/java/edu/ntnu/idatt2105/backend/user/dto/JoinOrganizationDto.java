package edu.ntnu.idatt2105.backend.user.dto;

import edu.ntnu.idatt2105.backend.user.model.enums.JoinOrgStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

/**
 * DTO representing a join request entry shown to admins/managers in the pending-requests list.
 */
@Data
@Builder
public class JoinOrganizationDto {
  private UUID requestId;
  private String email;
  private String firstName;
  private String lastName;
  private JoinOrgStatus status;
  private LocalDateTime createdAt;
}