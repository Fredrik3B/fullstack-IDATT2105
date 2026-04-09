package edu.ntnu.idatt2105.backend.security;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.RoleModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import edu.ntnu.idatt2105.backend.user.model.enums.RoleEnum;
import edu.ntnu.idatt2105.backend.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

  @Mock
  private JwtService jwtService;

  @Mock
  private UserRepository userRepository;

  private JwtAuthFilter jwtAuthFilter;

  @BeforeEach
  void setUp() {
    jwtAuthFilter = new JwtAuthFilter(jwtService, userRepository);
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void doFilterInternal_usesCurrentDatabaseRolesAfterPromotion() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID organizationId = UUID.randomUUID();
    String email = "promoted.user@test.com";

    UserModel user = new UserModel();
    user.setId(userId);
    user.setEmail(email);

    OrganizationModel organization = new OrganizationModel();
    organization.setId(organizationId);
    user.setOrganization(organization);

    RoleModel adminRole = new RoleModel();
    adminRole.setName(RoleEnum.ADMIN);
    user.getRoles().add(adminRole);

    when(jwtService.extractEmail("token")).thenReturn(email);
    when(jwtService.validateToken("token", email)).thenReturn(true);
    when(jwtService.extractUserId("token")).thenReturn(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer token");
    MockHttpServletResponse response = new MockHttpServletResponse();

    jwtAuthFilter.doFilterInternal(request, response, new MockFilterChain());

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isNotNull();
    assertThat(authentication.getPrincipal()).isInstanceOf(JwtAuthenticatedPrincipal.class);

    JwtAuthenticatedPrincipal principal = (JwtAuthenticatedPrincipal) authentication.getPrincipal();
    assertThat(principal.getUserId()).isEqualTo(userId);
    assertThat(principal.getOrganizationId()).isEqualTo(organizationId);
    assertThat(principal.getAuthorities())
        .extracting(grantedAuthority -> grantedAuthority.getAuthority())
        .containsExactly("ROLE_ADMIN");

    // Regression guard: stale token role claims should not drive authorization after role updates.
    verify(jwtService, never()).extractRoles("token");
    verify(jwtService, never()).extractOrganizationId("token");
  }
}
