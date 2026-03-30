package edu.ntnu.idatt2105.backend.user.service;

import edu.ntnu.idatt2105.backend.exception.ResourceNotFoundException;
import edu.ntnu.idatt2105.backend.user.dto.CreateOrganizationRequest;
import edu.ntnu.idatt2105.backend.user.dto.OrganizationResponse;
import edu.ntnu.idatt2105.backend.user.dto.JoinOrganizationRequest;
import edu.ntnu.idatt2105.backend.user.mapper.OrganizationMapper;
import edu.ntnu.idatt2105.backend.user.model.JoinRequestModel;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.RoleModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import edu.ntnu.idatt2105.backend.user.model.enums.JoinOrgStatus;
import edu.ntnu.idatt2105.backend.user.model.enums.RoleEnum;
import edu.ntnu.idatt2105.backend.user.repository.JoinRequestRepository;
import edu.ntnu.idatt2105.backend.user.repository.OrganizationRepository;
import edu.ntnu.idatt2105.backend.user.repository.RoleRepository;
import edu.ntnu.idatt2105.backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

//TODO: request new access token
@Service
@AllArgsConstructor
public class OrganizationService {
  private final OrganizationRepository organizationRepository;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final OrganizationMapper organizationMapper;
  private final JoinRequestRepository joinRequestRepository;

  public OrganizationResponse create(CreateOrganizationRequest request, UUID userId) {
    OrganizationModel org = new OrganizationModel();
    org.setName(request.getName());
    OrganizationModel saved = organizationRepository.save(org);

    UserModel user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    user.setOrganizationId(saved.getId());

    RoleModel adminRole = roleRepository.findByName(RoleEnum.ADMIN)
        .orElseThrow(() ->
            new ResourceNotFoundException("ADMIN role not found, setup of tables must be wrong"));
    user.getRoles().add(adminRole);
    userRepository.save(user);

    return organizationMapper.toResponse(org);
  }

  // TODO: just cast to user instead of lookup?
  public OrganizationResponse requestToJoin(JoinOrganizationRequest request, UUID userId) {
    OrganizationModel org = organizationRepository.findByJoinCode(request.getJoinCode())
        .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

    UserModel user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (user.getOrganizationId() != null) {
      throw new RuntimeException("User already belongs to an organization");
    }

    if (joinRequestRepository.existsRequestWithPendingStatus(
        userId, org.getId(), JoinOrgStatus.PENDING)) {
      throw new RuntimeException("You already have a pending request for this organization");
    }

    JoinRequestModel joinRequest = new JoinRequestModel();
    joinRequest.setUserId(userId);
    joinRequest.setOrganizationId(org.getId());
    joinRequest.setStatus(JoinOrgStatus.PENDING);

    joinRequest.setCreatedAt(LocalDateTime.now());

    joinRequestRepository.save(joinRequest);

    return organizationMapper.toResponse(org);
  }

  public void acceptRequest(UUID id, UUID userId) {
  }
}
