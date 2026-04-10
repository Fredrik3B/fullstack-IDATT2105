package edu.ntnu.idatt2105.backend.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.ntnu.idatt2105.backend.user.model.enums.JoinOrgStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Response DTO returned after a successful login, registration, or token refresh.
 *
 * <p>The {@code refreshToken} field is annotated with {@code @JsonIgnore} so it is
 * never serialised to the response body, the controller moves it to an HttpOnly cookie.
 */
@Data
@AllArgsConstructor
@Schema(description = "Response returned after successful login, registration, or token refresh")
public class LoginResponse {

  @Schema(description = "Short-lived JWT access token")
  private String accessToken;

  @Schema(description = "Basic user info")
  private UserInfo user;

  @Schema(description = "Organisation info, null if user has no organisation")
  private RestaurantInfo restaurant;

  @JsonIgnore
  private String refreshToken;

  @Data
  @AllArgsConstructor
  @Schema(description = "Compact user summary embedded in the login response")
  public static class UserInfo {
    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "User's full name", example = "John Doe")
    private String name;
  }

  @Data
  @AllArgsConstructor
  @Schema(description = "Organisation info embedded in the login response")
  public static class RestaurantInfo {
    @Schema(description = "Organisation ID")
    UUID id;

    @Schema(description = "Organisation name", example = "Restaurant AS")
    String name;
    @Schema(description = "Join code for inviting staff", example = "ABC-1423")
    String joinCode;
  }
}