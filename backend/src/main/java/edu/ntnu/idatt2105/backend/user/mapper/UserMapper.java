package edu.ntnu.idatt2105.backend.user.mapper;

import edu.ntnu.idatt2105.backend.user.dto.LoginResponse;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import org.springframework.stereotype.Component;

/**
 * Maps {@link UserModel} and token strings to authentication response DTOs.
 */
@Component
public class UserMapper {

  /**
   * Assembles a full {@link LoginResponse} from token strings and the user entity.
   *
   * @param accessToken  the newly generated JWT access token
   * @param refreshToken the newly generated refresh token (will be stripped from the response body
   *                     by the controller and moved to an HttpOnly cookie)
   * @param user         the authenticated user
   * @return the login response DTO
   */
  public LoginResponse toLoginResponse(String accessToken, String refreshToken, UserModel user) {
    return new LoginResponse(
        accessToken,
        toUserInfo(user),
        toRestaurantInfo(user.getOrganization()),
        refreshToken
    );
  }

  /**
   * Maps a {@link UserModel} to a compact {@link LoginResponse.UserInfo} summary.
   *
   * @param user the user entity
   * @return email and full name
   */
  public LoginResponse.UserInfo toUserInfo(UserModel user) {
    return new LoginResponse.UserInfo(
        user.getEmail(),
        user.getFirstName() + " " + user.getLastName()
    );
  }

  /**
   * Maps an {@link OrganizationModel} to a {@link LoginResponse.RestaurantInfo} summary, or returns
   * {@code null} if the user has not yet joined an organisation.
   *
   * @param org the organisation entity, may be {@code null}
   * @return the restaurant info DTO, or {@code null}
   */
  public LoginResponse.RestaurantInfo toRestaurantInfo(OrganizationModel org) {
    if (org == null) {
      return null;
    }
    return new LoginResponse.RestaurantInfo(org.getId(), org.getName(), org.getJoinCode());
  }
}
