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

/**
 * Maps organisation-related entities to their response and DTO representations.
 */
@Component
public class OrganizationMapper {

  /**
   * Maps an {@link OrganizationModel} to an {@link OrganizationResponse}.
   *
   * @param organization the organisation entity
   * @return the response DTO
   */
  public OrganizationResponse toResponse(OrganizationModel organization) {
    return OrganizationResponse.builder()
        .id(organization.getId())
        .name(organization.getName())
        .joinCode(organization.getJoinCode())
        .build();
  }

  /**
   * Maps a {@link JoinRequestModel} to a {@link JoinOrganizationDto} for admin review lists.
   *
   * @param request the join request entity (user must be loaded)
   * @return the DTO containing applicant details and request status
   */
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

  /**
   * Maps a {@link JoinRequestModel} to a {@link JoinRequestResponse} for the requesting user.
   *
   * @param request the join request entity
   * @return the response DTO showing status and timestamps
   */
  public JoinRequestResponse toJoinRequestResponse(JoinRequestModel request) {
    return JoinRequestResponse.builder()
        .requestId(request.getId())
        .status(request.getStatus())
        .createdAt(request.getCreatedAt())
        .build();
  }

  /**
   * Maps a {@link UserModel} to a {@link MemberDto} for the member list endpoint.
   *
   * @param user the user entity (roles must be loaded)
   * @return the member DTO with profile and role information
   */
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
