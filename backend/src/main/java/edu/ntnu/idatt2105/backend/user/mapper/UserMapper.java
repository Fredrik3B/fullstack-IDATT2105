package edu.ntnu.idatt2105.backend.user.mapper;

import edu.ntnu.idatt2105.backend.user.dto.LoginResponse;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public LoginResponse toLoginResponse(String accessToken, String refreshToken, UserModel user) {
    return new LoginResponse(
        accessToken,
        toUserInfo(user),
        toRestaurantInfo(user.getOrganization()),
        refreshToken
    );
  }

  public LoginResponse.UserInfo toUserInfo(UserModel user) {
    return new LoginResponse.UserInfo(
        user.getEmail(),
        user.getFirstName() + " " + user.getLastName()
    );
  }

  public LoginResponse.RestaurantInfo toRestaurantInfo(OrganizationModel org) {
    if (org == null) return null;
    return new LoginResponse.RestaurantInfo(org.getId(), org.getName(), org.getJoinCode());
  }
}
