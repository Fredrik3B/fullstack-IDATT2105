package edu.ntnu.idatt2105.backend.user.mapper;

import edu.ntnu.idatt2105.backend.user.dto.JoinOrganizationDto;
import edu.ntnu.idatt2105.backend.user.dto.JoinRequestResponse;
import edu.ntnu.idatt2105.backend.user.dto.MemberDto;
import edu.ntnu.idatt2105.backend.user.dto.OrganizationResponse;
import edu.ntnu.idatt2105.backend.user.model.JoinRequestModel;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import java.util.stream.Collectors;
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

  public JoinOrganizationDto toJoinRequestDto(JoinRequestModel request) {
    UserModel user = request.getUser();
    return JoinOrganizationDto.builder()
        .requestId(request.getId())
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .status(request.getStatus())
        .createdAt(request.getCreatedAt())
        .build();
  }

  public JoinRequestResponse toJoinRequestResponse(JoinRequestModel request) {
    return JoinRequestResponse.builder()
        .requestId(request.getId())
        .status(request.getStatus())
        .createdAt(request.getCreatedAt())
        .build();
  }

  public MemberDto toMemberDto(UserModel user) {
    return MemberDto.builder()
        .userId(user.getId())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .roles(user.getRoles().stream()
            .map(role -> role.getName().name())
            .collect(Collectors.toSet()))
        .build();
  }
}
