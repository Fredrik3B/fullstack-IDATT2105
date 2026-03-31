package edu.ntnu.idatt2105.backend.user.dto;

import lombok.Data;

@Data
public class JoinOrganizationRequest {
  private String email;
  private String firstName;
  private String lastName;
  private String joinCode;
}
