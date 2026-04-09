package edu.ntnu.idatt2105.backend.user.dto;

import lombok.Data;

/**
 * Request DTO for submitting a join request using an organisation's join code.
 */
@Data
public class JoinOrganizationRequest {
  private String email;
  private String firstName;
  private String lastName;
  private String joinCode;
}
