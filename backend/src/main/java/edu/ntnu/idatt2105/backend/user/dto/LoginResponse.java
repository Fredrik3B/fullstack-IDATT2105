package edu.ntnu.idatt2105.backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
public class LoginResponse {
  public final String email;
  public final String jwt;
}
