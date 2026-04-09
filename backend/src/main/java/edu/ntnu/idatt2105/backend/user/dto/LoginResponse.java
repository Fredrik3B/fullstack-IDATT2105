package edu.ntnu.idatt2105.backend.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.ntnu.idatt2105.backend.user.model.enums.JoinOrgStatus;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Response DTO returned after a successful login, registration, or token refresh.
 *
 * <p>The {@code refreshToken} field is annotated with {@code @JsonIgnore} so it is
 * never serialised to the response body — the controller moves it to an HttpOnly cookie.
 */
@Data
@AllArgsConstructor
public class LoginResponse {
  private String accessToken;
  private UserInfo user;
  private RestaurantInfo restaurant;
  @JsonIgnore
  private String refreshToken;

  /** Compact user summary embedded in the login response. */
  @Data
  @AllArgsConstructor
  public static class UserInfo {
    private String email;
    private String name;
  }

  /** Organisation info embedded in the login response. {@code null} if the user has no organisation. */
  @Data
  @AllArgsConstructor
  public static class RestaurantInfo {
    private UUID id;
    private String name;
    private String joinCode;
  }
}