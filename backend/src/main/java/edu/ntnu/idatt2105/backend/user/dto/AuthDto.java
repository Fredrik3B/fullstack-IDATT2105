package edu.ntnu.idatt2105.backend.user.dto;

import edu.ntnu.idatt2105.backend.user.model.UserModel;
import lombok.Data;

/**
 * Internal DTO, do not use in API, refresh token must be in a HTTPOnly Cookie.
 */
@Data
public class AuthDto {
  private final String accessToken;
  private final String refreshToken;
  private final UserModel user;}
