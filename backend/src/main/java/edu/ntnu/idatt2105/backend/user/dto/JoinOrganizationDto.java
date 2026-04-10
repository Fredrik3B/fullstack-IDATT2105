package edu.ntnu.idatt2105.backend.user.dto;

import edu.ntnu.idatt2105.backend.user.model.enums.JoinOrgStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

/**
 * DTO representing a join request entry shown to admins/managers in the pending-requests list.
 */
@Data
@Builder
@Schema(description = "A join request entry shown to admins and managers")
public class JoinOrganizationDto {

  @Schema(description = "ID of the join request")
  UUID requestId;

  @Schema(description = "Email of the requesting user", example = "john.doe@example.com")
  String email;

  @Schema(description = "First name of the requesting user", example = "John")
  String firstName;

  @Schema(description = "Last name of the requesting user", example = "Doe")
  String lastName;

  @Schema(description = "Current status of the request", example = "PENDING")
  JoinOrgStatus status;

  @Schema(description = "When the request was created")
  LocalDateTime createdAt;
}