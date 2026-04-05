package edu.ntnu.idatt2105.backend.user.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.ntnu.idatt2105.backend.common.repository.DocumentRepository;
import edu.ntnu.idatt2105.backend.exception.ResourceNotFoundException;
import edu.ntnu.idatt2105.backend.user.dto.CreateOrganizationRequest;
import edu.ntnu.idatt2105.backend.user.dto.JoinOrganizationDto;
import edu.ntnu.idatt2105.backend.user.dto.JoinOrganizationRequest;
import edu.ntnu.idatt2105.backend.user.dto.OrganizationResponse;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {
    @Mock private OrganizationRepository organizationRepository;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private OrganizationMapper organizationMapper;
    @Mock private JoinRequestRepository joinRequestRepository;
    @Mock private DocumentRepository documentRepository;

    @InjectMocks
    private OrganizationService organizationService;

    private UUID userId;
    private UUID orgId;
    private UserModel user;
    private OrganizationModel org;
    private RoleModel adminRole;

    @BeforeEach
    void setUp() {
      userId = UUID.randomUUID();
      orgId = UUID.randomUUID();

      user = new UserModel();
      user.setId(userId);
      user.setEmail("test@example.com");
      user.setRoles(new HashSet<>());

      org = new OrganizationModel();
      org.setId(orgId);
      org.setName("Test Restaurant");
      org.setJoinCode("TES-1234");

      adminRole = new RoleModel();
      adminRole.setName(RoleEnum.ADMIN);
    }

    // --- create ---

    @Test
    void create_success_assignsOrgAndAdminRole() {
      CreateOrganizationRequest request = new CreateOrganizationRequest();
      request.setName("Everest Sushi");

      when(organizationRepository.save(any(OrganizationModel.class))).thenReturn(org);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(roleRepository.findByName(RoleEnum.ADMIN)).thenReturn(Optional.of(adminRole));
      when(userRepository.save(any(UserModel.class))).thenReturn(user);
      when(organizationMapper.toResponse(any())).thenReturn(
          OrganizationResponse.builder()
              .id(orgId).name("Everest Sushi").joinCode("EVE-1234").build());

      OrganizationResponse response = organizationService.create(request, userId);

      assertThat(response.getId()).isEqualTo(orgId);
      assertThat(user.getOrganization()).isEqualTo(org);
      assertThat(user.getRoles()).contains(adminRole);

      // We save 7 docs each time a org is created, might change
      verify(documentRepository, times(7)).save(any());
    }

    @Test
    void create_userNotFound_throwsException() {
      CreateOrganizationRequest request = new CreateOrganizationRequest();
      request.setName("Test");

      when(organizationRepository.save(any())).thenReturn(org);
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> organizationService.create(request, userId))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("User not found");
    }

    @Test
    void create_adminRoleMissing_throwsException() {
      CreateOrganizationRequest request = new CreateOrganizationRequest();
      request.setName("Test");

      when(organizationRepository.save(any())).thenReturn(org);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(roleRepository.findByName(RoleEnum.ADMIN)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> organizationService.create(request, userId))
          .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_seedsDefaultDocuments() {
      CreateOrganizationRequest request = new CreateOrganizationRequest();
      request.setName("Test");

      when(organizationRepository.save(any())).thenReturn(org);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(roleRepository.findByName(RoleEnum.ADMIN)).thenReturn(Optional.of(adminRole));
      when(userRepository.save(any())).thenReturn(user);
      when(organizationMapper.toResponse(any())).thenReturn(
          OrganizationResponse.builder().id(orgId).build());

      organizationService.create(request, userId);

      verify(documentRepository, times(7)).save(any());
    }

    // --- requestToJoin ---

    @Test
    void requestToJoin_success_createsPendingRequest() {
      JoinOrganizationRequest request = new JoinOrganizationRequest();
      request.setJoinCode("TES-1234");

      when(organizationRepository.findByJoinCode("TES-1234")).thenReturn(Optional.of(org));
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(joinRequestRepository.existsByUserIdAndStatus(userId, JoinOrgStatus.PENDING)).thenReturn(false);
      when(organizationMapper.toResponse(org)).thenReturn(
          OrganizationResponse.builder().id(orgId).name("Test Restaurant").build());

      OrganizationResponse response = organizationService.requestToJoin(request, userId);

      assertThat(response.getName()).isEqualTo("Test Restaurant");
      verify(joinRequestRepository).save(any(JoinRequestModel.class));
    }

    @Test
    void requestToJoin_invalidCode_throwsException() {
      JoinOrganizationRequest request = new JoinOrganizationRequest();
      request.setJoinCode("INVALID");

      when(organizationRepository.findByJoinCode("INVALID")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> organizationService.requestToJoin(request, userId))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Organization not found");
    }

    @Test
    void requestToJoin_userAlreadyInOrg_throwsException() {
      JoinOrganizationRequest request = new JoinOrganizationRequest();
      request.setJoinCode("TES-1234");
      user.setOrganization(org);

      when(organizationRepository.findByJoinCode("TES-1234")).thenReturn(Optional.of(org));
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      assertThatThrownBy(() -> organizationService.requestToJoin(request, userId))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("User already belongs to an organization");
    }

    @Test
    void requestToJoin_alreadyPending_throwsException() {
      JoinOrganizationRequest request = new JoinOrganizationRequest();
      request.setJoinCode("TES-1234");

      when(organizationRepository.findByJoinCode("TES-1234")).thenReturn(Optional.of(org));
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(joinRequestRepository.existsByUserIdAndStatus(userId, JoinOrgStatus.PENDING)).thenReturn(true);

      assertThatThrownBy(() -> organizationService.requestToJoin(request, userId))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("You already have a pending join request");
    }

    // --- resolveRequest ---

    @Test
    void resolveRequest_accept_assignsUserToOrg() {
      UUID requestId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();

      JoinRequestModel joinRequest = new JoinRequestModel();
      joinRequest.setId(requestId);
      joinRequest.setUserId(userId);
      joinRequest.setOrganizationId(orgId);
      joinRequest.setStatus(JoinOrgStatus.PENDING);

      when(joinRequestRepository.findById(requestId)).thenReturn(Optional.of(joinRequest));
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));

      organizationService.resolveRequest(requestId, adminId, orgId, JoinOrgStatus.ACCEPTED);

      assertThat(user.getOrganization()).isEqualTo(org);
      assertThat(joinRequest.getStatus()).isEqualTo(JoinOrgStatus.ACCEPTED);
      assertThat(joinRequest.getResolvedBy()).isEqualTo(adminId);
      assertThat(joinRequest.getResolvedAt()).isNotNull();
      verify(userRepository).save(user);
    }

    @Test
    void resolveRequest_decline_doesNotAssignUser() {
      UUID requestId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();

      JoinRequestModel joinRequest = new JoinRequestModel();
      joinRequest.setId(requestId);
      joinRequest.setUserId(userId);
      joinRequest.setOrganizationId(orgId);
      joinRequest.setStatus(JoinOrgStatus.PENDING);

      when(joinRequestRepository.findById(requestId)).thenReturn(Optional.of(joinRequest));

      organizationService.resolveRequest(requestId, adminId, orgId, JoinOrgStatus.DECLINED);

      assertThat(user.getOrganization()).isNull();
      assertThat(joinRequest.getStatus()).isEqualTo(JoinOrgStatus.DECLINED);
      verify(userRepository, never()).save(any());
    }

    @Test
    void resolveRequest_wrongOrg_throwsException() {
      UUID requestId = UUID.randomUUID();
      UUID wrongOrgId = UUID.randomUUID();

      JoinRequestModel joinRequest = new JoinRequestModel();
      joinRequest.setOrganizationId(orgId);
      joinRequest.setStatus(JoinOrgStatus.PENDING);

      when(joinRequestRepository.findById(requestId)).thenReturn(Optional.of(joinRequest));

      assertThatThrownBy(() -> organizationService.resolveRequest(requestId, userId, wrongOrgId, JoinOrgStatus.ACCEPTED))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Request does not belong to your organization");
    }

    @Test
    void resolveRequest_alreadyResolved_throwsException() {
      UUID requestId = UUID.randomUUID();

      JoinRequestModel joinRequest = new JoinRequestModel();
      joinRequest.setOrganizationId(orgId);
      joinRequest.setStatus(JoinOrgStatus.ACCEPTED);

      when(joinRequestRepository.findById(requestId)).thenReturn(Optional.of(joinRequest));

      assertThatThrownBy(() -> organizationService.resolveRequest(requestId, userId, orgId, JoinOrgStatus.ACCEPTED))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Request already resolved");
    }

    @Test
    void resolveRequest_requestNotFound_throwsException() {
      UUID requestId = UUID.randomUUID();

      when(joinRequestRepository.findById(requestId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> organizationService.resolveRequest(requestId, userId, orgId, JoinOrgStatus.ACCEPTED))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Request not found");
    }

    // --- withdrawJoinRequest ---

    @Test
    void withdrawJoinRequest_success_deletesPending() {
      JoinRequestModel pending = new JoinRequestModel();
      pending.setStatus(JoinOrgStatus.PENDING);

      when(joinRequestRepository.findAllByUserIdAndStatus(userId, JoinOrgStatus.PENDING))
          .thenReturn(List.of(pending));

      organizationService.withdrawJoinRequest(userId);

      verify(joinRequestRepository).deleteAll(List.of(pending));
    }

    @Test
    void withdrawJoinRequest_noPending_throwsException() {
      when(joinRequestRepository.findAllByUserIdAndStatus(userId, JoinOrgStatus.PENDING))
          .thenReturn(List.of());

      assertThatThrownBy(() -> organizationService.withdrawJoinRequest(userId))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("No pending join request found");
    }

    // --- lookupByCode ---

    @Test
    void lookupByCode_found_returnsResponse() {
      when(organizationRepository.findByJoinCode("TES-1234")).thenReturn(Optional.of(org));
      when(organizationMapper.toResponse(org)).thenReturn(
          OrganizationResponse.builder().id(orgId).name("Test Restaurant").joinCode("TES-1234").build());

      OrganizationResponse response = organizationService.lookupByCode("TES-1234");

      assertThat(response.getJoinCode()).isEqualTo("TES-1234");
    }

    @Test
    void lookupByCode_notFound_throwsException() {
      when(organizationRepository.findByJoinCode("NOPE")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> organizationService.lookupByCode("NOPE"))
          .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- getRequests ---

    @Test
    void getRequests_withStatusFilter_filtersCorrectly() {
      JoinRequestModel request = new JoinRequestModel();
      request.setUserId(userId);
      UserModel requestUser = new UserModel();
      requestUser.setId(userId);
      requestUser.setEmail("requester@example.com");
      requestUser.setFirstName("Test");
      requestUser.setLastName("User");

      JoinOrganizationDto dto = JoinOrganizationDto.builder()
          .email("requester@example.com").build();

      when(joinRequestRepository.findAllByOrganizationIdAndStatus(orgId, JoinOrgStatus.PENDING))
          .thenReturn(List.of(request));
      when(userRepository.findById(userId)).thenReturn(Optional.of(requestUser));
      when(organizationMapper.toJoinRequestDto(request, requestUser)).thenReturn(dto);

      List<JoinOrganizationDto> results = organizationService.getRequests(orgId, JoinOrgStatus.PENDING);

      assertThat(results).hasSize(1);
      assertThat(results.get(0).getEmail()).isEqualTo("requester@example.com");
    }

    @Test
    void getRequests_noFilter_returnsAll() {
      when(joinRequestRepository.findAllByOrganizationId(orgId)).thenReturn(List.of());

      List<JoinOrganizationDto> results = organizationService.getRequests(orgId, null);

      assertThat(results).isEmpty();
      verify(joinRequestRepository).findAllByOrganizationId(orgId);
      verify(joinRequestRepository, never()).findAllByOrganizationIdAndStatus(any(), any());
    }
}