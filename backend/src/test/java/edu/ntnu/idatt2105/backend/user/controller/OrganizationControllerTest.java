package edu.ntnu.idatt2105.backend.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ntnu.idatt2105.backend.exception.ResourceNotFoundException;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import edu.ntnu.idatt2105.backend.user.dto.JoinOrganizationDto;
import edu.ntnu.idatt2105.backend.user.dto.JoinRequestResponse;
import edu.ntnu.idatt2105.backend.user.dto.MemberDto;
import edu.ntnu.idatt2105.backend.user.dto.OrganizationResponse;
import edu.ntnu.idatt2105.backend.user.model.enums.JoinOrgStatus;
import edu.ntnu.idatt2105.backend.user.service.OrganizationService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@DisplayName("OrganizationController (/api/organizations)")
class OrganizationControllerTest {

  @Autowired
  private WebApplicationContext context;

  @MockitoBean
  private OrganizationService organizationService;

  private MockMvc mockMvc;

  private final UUID userId = UUID.randomUUID();
  private final UUID orgId = UUID.randomUUID();
  private final UUID memberId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .build();
  }

  private Authentication staffAuth() {
    JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
        userId, orgId, "staff@test.com",
        List.of(new SimpleGrantedAuthority("ROLE_STAFF")));
    return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
  }

  private Authentication adminAuth() {
    JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
        userId, orgId, "admin@test.com",
        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
  }

  private OrganizationResponse sampleOrg() {
    return OrganizationResponse.builder()
        .id(orgId).name("Test Restaurant").joinCode("TES-1234").build();
  }

  // ── POST /api/organizations ───────────────────────────────────────────────

  @Test
  @DisplayName("POST /api/organizations - authenticated staff creates org and returns 200")
  void createOrganization_asStaff_returns200() throws Exception {
    when(organizationService.create(any(), eq(userId))).thenReturn(sampleOrg());

    mockMvc.perform(post("/api/organizations")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"Test Restaurant"}
                """)
            .with(authentication(staffAuth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Test Restaurant"))
        .andExpect(jsonPath("$.joinCode").value("TES-1234"));
  }

  @Test
  @DisplayName("POST /api/organizations - unauthenticated returns 401")
  void createOrganization_unauthenticated_returns401() throws Exception {
    mockMvc.perform(post("/api/organizations")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"Test"}
                """))
        .andExpect(status().isUnauthorized());
  }

  // ── GET /api/organizations/lookup ─────────────────────────────────────────

  @Test
  @DisplayName("GET /api/organizations/lookup - valid code returns 200")
  void lookupOrganization_validCode_returns200() throws Exception {
    when(organizationService.lookupByCode("TES-1234")).thenReturn(sampleOrg());

    mockMvc.perform(get("/api/organizations/lookup")
            .param("code", "TES-1234")
            .with(authentication(staffAuth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Test Restaurant"));
  }

  @Test
  @DisplayName("GET /api/organizations/lookup - invalid code returns 404")
  void lookupOrganization_invalidCode_returns404() throws Exception {
    when(organizationService.lookupByCode("INVALID"))
        .thenThrow(new ResourceNotFoundException("Organization not found"));

    mockMvc.perform(get("/api/organizations/lookup")
            .param("code", "INVALID")
            .with(authentication(staffAuth())))
        .andExpect(status().isNotFound());
  }

  // ── POST /api/organizations/join ──────────────────────────────────────────

  @Test
  @DisplayName("POST /api/organizations/join - valid request returns 200")
  void joinOrganization_success_returns200() throws Exception {
    when(organizationService.requestToJoin(any(), eq(userId))).thenReturn(sampleOrg());

    mockMvc.perform(post("/api/organizations/join")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "joinCode": "TES-1234",
                  "firstName": "John",
                  "lastName": "Doe"
                }
                """)
            .with(authentication(staffAuth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Test Restaurant"));
  }

  @Test
  @DisplayName("POST /api/organizations/join - unauthenticated returns 401")
  void joinOrganization_unauthenticated_returns401() throws Exception {
    mockMvc.perform(post("/api/organizations/join")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"joinCode":"TES-1234"}
                """))
        .andExpect(status().isUnauthorized());
  }

  // ── GET /api/organizations/join-request ──────────────────────────────────

  @Test
  @DisplayName("GET /api/organizations/join-request - pending request returns 200")
  void seeJoinRequest_pending_returns200() throws Exception {
    JoinRequestResponse response = JoinRequestResponse.builder()
        .requestId(UUID.randomUUID()).status(JoinOrgStatus.PENDING).build();
    when(organizationService.seeJoinRequest(userId)).thenReturn(response);

    mockMvc.perform(get("/api/organizations/join-request")
            .with(authentication(staffAuth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  @Test
  @DisplayName("GET /api/organizations/join-request - no pending request returns 204")
  void seeJoinRequest_noPending_returns204() throws Exception {
    when(organizationService.seeJoinRequest(userId)).thenReturn(null);

    mockMvc.perform(get("/api/organizations/join-request")
            .with(authentication(staffAuth())))
        .andExpect(status().isNoContent());
  }

  // ── DELETE /api/organizations/join-request ────────────────────────────────

  @Test
  @DisplayName("DELETE /api/organizations/join-request - success returns 200")
  void withdrawJoinRequest_success_returns200() throws Exception {
    mockMvc.perform(delete("/api/organizations/join-request")
            .with(authentication(staffAuth())))
        .andExpect(status().isOk());

    verify(organizationService).withdrawJoinRequest(userId);
  }

  // ── GET /api/organizations/requests ──────────────────────────────────────

  @Test
  @DisplayName("GET /api/organizations/requests - admin returns 200 with list")
  void getRequests_asAdmin_returns200() throws Exception {
    JoinOrganizationDto dto = JoinOrganizationDto.builder()
        .email("applicant@test.com").firstName("A").lastName("B")
        .status(JoinOrgStatus.PENDING).build();
    when(organizationService.getRequests(eq(orgId), any())).thenReturn(List.of(dto));

    mockMvc.perform(get("/api/organizations/requests")
            .with(authentication(adminAuth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].email").value("applicant@test.com"));
  }

  @Test
  @DisplayName("GET /api/organizations/requests - staff returns 403")
  void getRequests_asStaff_returns403() throws Exception {
    mockMvc.perform(get("/api/organizations/requests")
            .with(authentication(staffAuth())))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("GET /api/organizations/requests - unauthenticated returns 401")
  void getRequests_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/organizations/requests"))
        .andExpect(status().isUnauthorized());
  }

  // ── GET /api/organizations/members ───────────────────────────────────────

  @Test
  @DisplayName("GET /api/organizations/members - admin returns 200 with list")
  void getMembers_asAdmin_returns200() throws Exception {
    MemberDto member = MemberDto.builder()
        .userId(memberId).email("member@test.com")
        .firstName("John").lastName("Doe").roles(Set.of("STAFF")).build();
    when(organizationService.getMembers(orgId)).thenReturn(List.of(member));

    mockMvc.perform(get("/api/organizations/members")
            .with(authentication(adminAuth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].email").value("member@test.com"));
  }

  @Test
  @DisplayName("GET /api/organizations/members - staff returns 403")
  void getMembers_asStaff_returns403() throws Exception {
    mockMvc.perform(get("/api/organizations/members")
            .with(authentication(staffAuth())))
        .andExpect(status().isForbidden());
  }

  // ── DELETE /api/organizations/members/{userId} ────────────────────────────

  @Test
  @DisplayName("DELETE /api/organizations/members/{userId} - admin returns 200")
  void removeMember_asAdmin_returns200() throws Exception {
    mockMvc.perform(delete("/api/organizations/members/{userId}", memberId)
            .with(authentication(adminAuth())))
        .andExpect(status().isOk());

    verify(organizationService).removeMember(orgId, memberId, userId);
  }

  @Test
  @DisplayName("DELETE /api/organizations/members/{userId} - staff returns 403")
  void removeMember_asStaff_returns403() throws Exception {
    mockMvc.perform(delete("/api/organizations/members/{userId}", memberId)
            .with(authentication(staffAuth())))
        .andExpect(status().isForbidden());
  }

  // ── PUT /api/organizations/members/{userId}/roles ─────────────────────────

  @Test
  @DisplayName("PUT /api/organizations/members/{userId}/roles - admin returns 200")
  void updateMemberRoles_asAdmin_returns200() throws Exception {
    mockMvc.perform(put("/api/organizations/members/{userId}/roles", memberId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"roles":["STAFF"]}
                """)
            .with(authentication(adminAuth())))
        .andExpect(status().isOk());

    verify(organizationService).updateMemberRoles(eq(orgId), eq(memberId), eq(userId), any());
  }

  @Test
  @DisplayName("PUT /api/organizations/members/{userId}/roles - staff returns 403")
  void updateMemberRoles_asStaff_returns403() throws Exception {
    mockMvc.perform(put("/api/organizations/members/{userId}/roles", memberId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"roles":["ADMIN"]}
                """)
            .with(authentication(staffAuth())))
        .andExpect(status().isForbidden());
  }
}
