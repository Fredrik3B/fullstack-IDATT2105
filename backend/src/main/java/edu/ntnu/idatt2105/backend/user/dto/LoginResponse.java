package edu.ntnu.idatt2105.backend.user.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LoginResponse {
  public final String email;
  public final String accessToken;
}
