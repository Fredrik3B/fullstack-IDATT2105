package edu.ntnu.idatt2105.backend.user.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.ntnu.idatt2105.backend.document.repository.DocumentRepository;
import edu.ntnu.idatt2105.backend.exception.ResourceNotFoundException;
import edu.ntnu.idatt2105.backend.user.dto.CreateOrganizationRequest;
import edu.ntnu.idatt2105.backend.user.dto.JoinOrganizationDto;
import edu.ntnu.idatt2105.backend.user.dto.JoinOrganizationRequest;
import edu.ntnu.idatt2105.backend.user.dto.JoinRequestResponse;
import edu.ntnu.idatt2105.backend.user.dto.MemberDto;
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
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

  @Mock private OrganizationRepository organizationRepository;
  @Mock private UserRepository userRepository;
  @Mock private RoleRepository roleRepository;
  @Spy  private OrganizationMapper organizationMapper;
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
    user.setFirstName("Test");
    user.setLastName("User");
    user.setRoles(new HashSet<>());

    org = new OrganizationModel();
    org.setId(orgId);
    org.setName("Test Restaurant");
    org.setJoinCode("TES-1234");

    adminRole = new RoleModel();
    adminRole.setName(RoleEnum.ADMIN);
  }

  private JoinRequestModel buildJoinRequest(JoinOrgStatus status) {
    JoinRequestModel request = new JoinRequestModel();
    request.setId(UUID.randomUUID());
    request.setUser(user);
    request.setOrganization(org);
    request.setStatus(status);
    return request;
  }

  @Test
  void create_success_assignsOrgAndAdminRole() {
    CreateOrganizationRequest request = new CreateOrganizationRequest();
    request.setName("Everest Sushi");

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(organizationRepository.save(any(OrganizationModel.class))).thenReturn(org);
    when(roleRepository.findByName(RoleEnum.ADMIN)).thenReturn(Optional.of(adminRole));
    when(userRepository.save(any(UserModel.class))).thenReturn(user);

    OrganizationResponse response = organizationService.create(request, userId);

    assertThat(response.getId()).isEqualTo(orgId);
    assertThat(user.getOrganization()).isEqualTo(org);
    assertThat(user.getRoles()).contains(adminRole);
    verify(documentRepository).saveAll(anyList());
  }

  @Test
  void create_userNotFound_throwsException() {
    CreateOrganizationRequest request = new CreateOrganizationRequest();
    request.setName("Test");

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> organizationService.create(request, userId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("User not found");
  }

  @Test
  void create_adminRoleMissing_throwsException() {
    CreateOrganizationRequest request = new CreateOrganizationRequest();
    request.setName("Test");

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(organizationRepository.save(any())).thenReturn(org);
    when(roleRepository.findByName(RoleEnum.ADMIN)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> organizationService.create(request, userId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void create_seedsDefaultDocuments() {
    CreateOrganizationRequest request = new CreateOrganizationRequest();
    request.setName("Test");

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(organizationRepository.save(any())).thenReturn(org);
    when(roleRepository.findByName(RoleEnum.ADMIN)).thenReturn(Optional.of(adminRole));
    when(userRepository.save(any())).thenReturn(user);

    organizationService.create(request, userId);

    verify(documentRepository).saveAll(anyList());
  }


  @Test
  void requestToJoin_success_createsPendingRequest() {
    JoinOrganizationRequest request = new JoinOrganizationRequest();
    request.setJoinCode("TES-1234");

    when(organizationRepository.findByJoinCode("TES-1234")).thenReturn(Optional.of(org));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(joinRequestRepository.existsByUserAndStatus(user, JoinOrgStatus.PENDING)).thenReturn(false);

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
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("User already belongs to an organization");
  }

  @Test
  void requestToJoin_alreadyPending_throwsException() {
    JoinOrganizationRequest request = new JoinOrganizationRequest();
    request.setJoinCode("TES-1234");

    when(organizationRepository.findByJoinCode("TES-1234")).thenReturn(Optional.of(org));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(joinRequestRepository.existsByUserAndStatus(user, JoinOrgStatus.PENDING)).thenReturn(true);

    assertThatThrownBy(() -> organizationService.requestToJoin(request, userId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("You already have a pending join request");
  }

  @Test
  void resolveRequest_accept_assignsUserToOrg() {
    UUID adminId = UUID.randomUUID();
    UserModel admin = new UserModel();
    admin.setId(adminId);

    JoinRequestModel joinRequest = buildJoinRequest(JoinOrgStatus.PENDING);

    when(joinRequestRepository.findById(joinRequest.getId())).thenReturn(Optional.of(joinRequest));
    when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

    organizationService.resolveRequest(joinRequest.getId(), adminId, orgId, JoinOrgStatus.ACCEPTED);

    assertThat(user.getOrganization()).isEqualTo(org);
    assertThat(joinRequest.getStatus()).isEqualTo(JoinOrgStatus.ACCEPTED);
    assertThat(joinRequest.getResolvedBy()).isEqualTo(admin);
    assertThat(joinRequest.getResolvedAt()).isNotNull();
    verify(userRepository).save(user);
  }

  @Test
  void resolveRequest_decline_doesNotAssignUser() {
    UUID adminId = UUID.randomUUID();
    UserModel admin = new UserModel();
    admin.setId(adminId);

    JoinRequestModel joinRequest = buildJoinRequest(JoinOrgStatus.PENDING);

    when(joinRequestRepository.findById(joinRequest.getId())).thenReturn(Optional.of(joinRequest));
    when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));

    organizationService.resolveRequest(joinRequest.getId(), adminId, orgId, JoinOrgStatus.DECLINED);

    assertThat(user.getOrganization()).isNull();
    assertThat(joinRequest.getStatus()).isEqualTo(JoinOrgStatus.DECLINED);
    verify(userRepository, never()).save(any());
  }

  @Test
  void resolveRequest_wrongOrg_throwsException() {
    UUID wrongOrgId = UUID.randomUUID();
    JoinRequestModel joinRequest = buildJoinRequest(JoinOrgStatus.PENDING);

    when(joinRequestRepository.findById(joinRequest.getId())).thenReturn(Optional.of(joinRequest));

    assertThatThrownBy(() -> organizationService.resolveRequest(joinRequest.getId(), userId, wrongOrgId, JoinOrgStatus.ACCEPTED))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Request does not belong to your organization");
  }

  @Test
  void resolveRequest_alreadyResolved_throwsException() {
    JoinRequestModel joinRequest = buildJoinRequest(JoinOrgStatus.ACCEPTED);

    when(joinRequestRepository.findById(joinRequest.getId())).thenReturn(Optional.of(joinRequest));

    assertThatThrownBy(() -> organizationService.resolveRequest(joinRequest.getId(), userId, orgId, JoinOrgStatus.ACCEPTED))
        .isInstanceOf(IllegalStateException.class)
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

  @Test
  void withdrawJoinRequest_success_deletesPending() {
    JoinRequestModel pending = buildJoinRequest(JoinOrgStatus.PENDING);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(joinRequestRepository.findAllByUserAndStatus(user, JoinOrgStatus.PENDING))
        .thenReturn(List.of(pending));

    organizationService.withdrawJoinRequest(userId);

    verify(joinRequestRepository).deleteAll(List.of(pending));
  }

  @Test
  void withdrawJoinRequest_noPending_throwsException() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(joinRequestRepository.findAllByUserAndStatus(user, JoinOrgStatus.PENDING))
        .thenReturn(List.of());

    assertThatThrownBy(() -> organizationService.withdrawJoinRequest(userId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("No pending join request found");
  }


  @Test
  void lookupByCode_found_returnsResponse() {
    when(organizationRepository.findByJoinCode("TES-1234")).thenReturn(Optional.of(org));

    OrganizationResponse response = organizationService.lookupByCode("TES-1234");

    assertThat(response.getJoinCode()).isEqualTo("TES-1234");
  }

  @Test
  void lookupByCode_notFound_throwsException() {
    when(organizationRepository.findByJoinCode("NOPE")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> organizationService.lookupByCode("NOPE"))
        .isInstanceOf(ResourceNotFoundException.class);
  }


  @Test
  void getRequests_withStatusFilter_filtersCorrectly() {
    JoinRequestModel request = buildJoinRequest(JoinOrgStatus.PENDING);

    JoinOrganizationDto dto = JoinOrganizationDto.builder()
        .email("test@example.com").firstName("Test").lastName("User").build();

    when(joinRequestRepository.findAllByOrganizationIdAndStatusWithUser(orgId, JoinOrgStatus.PENDING))
        .thenReturn(List.of(request));
    when(organizationMapper.toJoinRequestDto(request)).thenReturn(dto);

    List<JoinOrganizationDto> results = organizationService.getRequests(orgId, JoinOrgStatus.PENDING);

    assertThat(results).hasSize(1);
    assertThat(results.get(0).getEmail()).isEqualTo("test@example.com");
  }

  @Test
  void getRequests_noFilter_returnsAll() {
    when(joinRequestRepository.findAllByOrganizationIdWithUser(orgId)).thenReturn(List.of());

    List<JoinOrganizationDto> results = organizationService.getRequests(orgId, null);

    assertThat(results).isEmpty();
    verify(joinRequestRepository).findAllByOrganizationIdWithUser(orgId);
    verify(joinRequestRepository, never()).findAllByOrganizationIdAndStatusWithUser(any(), any());
  }

  @Test
  void getMembers_returnsAllOrgMembers() {
    when(userRepository.findAllByOrganizationId(orgId)).thenReturn(List.of(user));
    when(organizationMapper.toMemberDto(user)).thenReturn(
        MemberDto.builder().userId(userId).email("test@example.com")
            .firstName("Test").lastName("User").roles(Set.of("STAFF")).build());

    List<MemberDto> members = organizationService.getMembers(orgId);

    assertThat(members).hasSize(1);
    assertThat(members.get(0).getEmail()).isEqualTo("test@example.com");
  }

  @Test
  void removeMember_success_clearsOrgAndReassignsStaffRole() {
    UUID adminId = UUID.randomUUID();
    user.setOrganization(org);
    RoleModel staffRole = new RoleModel();
    staffRole.setName(RoleEnum.STAFF);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(roleRepository.findByName(RoleEnum.STAFF)).thenReturn(Optional.of(staffRole));

    organizationService.removeMember(orgId, userId, adminId);

    assertThat(user.getOrganization()).isNull();
    assertThat(user.getRoles()).containsOnly(staffRole);
    verify(userRepository).save(user);
  }

  @Test
  void removeMember_selfRemoval_throwsException() {
    assertThatThrownBy(() -> organizationService.removeMember(orgId, userId, userId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Cannot remove yourself from the organization");
  }

  @Test
  void removeMember_userNotInOrg_throwsException() {
    UUID adminId = UUID.randomUUID();

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> organizationService.removeMember(orgId, userId, adminId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("User does not belong to your organization");
  }

  @Test
  void updateMemberRoles_selfUpdate_throwsException() {
    assertThatThrownBy(() -> organizationService.updateMemberRoles(orgId, userId, userId, List.of("STAFF")))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Cannot change your own roles");
  }

  @Test
  void updateMemberRoles_success_updatesRoles() {
    UUID adminId = UUID.randomUUID();
    RoleModel managerRole = new RoleModel();
    managerRole.setName(RoleEnum.MANAGER);
    user.setOrganization(org);
    user.getRoles().add(managerRole);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(roleRepository.findByName(RoleEnum.STAFF)).thenReturn(Optional.of(new RoleModel()));

    organizationService.updateMemberRoles(orgId, userId, adminId, List.of("STAFF"));

    verify(userRepository).save(user);
  }

  @Test
  void updateMemberRoles_removeLastAdmin_throwsException() {
    UUID adminId = UUID.randomUUID();
    RoleModel existingAdminRole = new RoleModel();
    existingAdminRole.setName(RoleEnum.ADMIN);
    user.setOrganization(org);
    user.getRoles().add(existingAdminRole);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(roleRepository.findByName(RoleEnum.STAFF)).thenReturn(Optional.of(new RoleModel()));
    when(userRepository.countAdminsByOrganizationId(orgId)).thenReturn(1L);

    assertThatThrownBy(() -> organizationService.updateMemberRoles(orgId, userId, adminId, List.of("STAFF")))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Cannot remove the last admin from the organization");
  }

  @Test
  void seeJoinRequest_pendingExists_returnsResponse() {
    JoinRequestModel pending = buildJoinRequest(JoinOrgStatus.PENDING);
    JoinRequestResponse response = JoinRequestResponse.builder()
        .requestId(pending.getId()).status(JoinOrgStatus.PENDING).build();

    when(joinRequestRepository.findFirstByUser_IdAndStatus(userId, JoinOrgStatus.PENDING))
        .thenReturn(Optional.of(pending));
    when(organizationMapper.toJoinRequestResponse(pending)).thenReturn(response);

    JoinRequestResponse result = organizationService.seeJoinRequest(userId);

    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(JoinOrgStatus.PENDING);
  }

  @Test
  void seeJoinRequest_noPending_returnsNull() {
    when(joinRequestRepository.findFirstByUser_IdAndStatus(userId, JoinOrgStatus.PENDING))
        .thenReturn(Optional.empty());

    JoinRequestResponse result = organizationService.seeJoinRequest(userId);

    assertThat(result).isNull();
  }
}