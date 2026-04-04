package edu.ntnu.idatt2105.backend.user.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MeResponse {

  private UserInfo user;
  private String restaurantStatus;
  private UUID restaurantId;
  private String restaurantName;
  private String restaurantJoinCode;

  @Data
  @AllArgsConstructor
  public static class UserInfo {
    private String email;
    private String name;
  }
}
