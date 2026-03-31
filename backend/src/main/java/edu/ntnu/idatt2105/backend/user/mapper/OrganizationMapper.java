package edu.ntnu.idatt2105.backend.user.mapper;

import edu.ntnu.idatt2105.backend.user.dto.CreateOrganizationRequest;
import edu.ntnu.idatt2105.backend.user.dto.JoinOrganizationDto;
import edu.ntnu.idatt2105.backend.user.dto.JoinOrganizationRequest;
import edu.ntnu.idatt2105.backend.user.dto.OrganizationDto;
import edu.ntnu.idatt2105.backend.user.dto.OrganizationResponse;
import edu.ntnu.idatt2105.backend.user.dto.UserDto;
import edu.ntnu.idatt2105.backend.user.model.JoinRequestModel;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMapper {

  public OrganizationResponse toResponse(OrganizationModel organization) {
    return OrganizationResponse.builder()
        .id(organization.getId())
        .name(organization.getName())
        .joinCode(organization.getJoinCode())
        .build();
  }

  public JoinOrganizationDto toJoinRequestDto(JoinRequestModel request,  UserModel user) {
    return JoinOrganizationDto.builder()
        .requestId(request.getId())
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .status(request.getStatus())
        .createdAt(request.getCreatedAt())
        .build();
  }
}
