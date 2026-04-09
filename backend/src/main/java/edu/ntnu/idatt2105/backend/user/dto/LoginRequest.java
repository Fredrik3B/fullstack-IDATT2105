package edu.ntnu.idatt2105.backend.user.dto;

import lombok.Data;

/**
 * Request DTO for email/password login.
 */
@Data
public class LoginRequest {
  public String email;
  public String password;
}
