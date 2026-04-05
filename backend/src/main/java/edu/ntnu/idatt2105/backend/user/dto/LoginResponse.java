package edu.ntnu.idatt2105.backend.user.dto;

import edu.ntnu.idatt2105.backend.user.model.enums.JoinOrgStatus;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
  private String accessToken;
  private UserInfo user;
  private RestaurantInfo restaurant;

  @Data
  @AllArgsConstructor
  public static class UserInfo {
    private String email;
    private String name;
  }

  @Data
  @AllArgsConstructor
  public static class RestaurantInfo {
    private UUID id;
    private String name;
    private String joinCode;
  }
}