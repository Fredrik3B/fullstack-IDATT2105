package edu.ntnu.idatt2105.backend.user.mapper;

import edu.ntnu.idatt2105.backend.user.dto.UserDto;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public UserDto toResponse(UserModel user) {
    return UserDto.builder()
        .userId(user.getId())
        .organizationId(user.getOrganization() != null ? user.getOrganization().getId() : null)
        .email(user.getEmail())
        .role(user.getRoles())
        .build();
  }
}
