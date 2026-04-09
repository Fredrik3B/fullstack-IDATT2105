package edu.ntnu.idatt2105.backend.security;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

  @Mock
  private JwtService jwtService;

  private JwtAuthFilter jwtAuthFilter;

  @BeforeEach
  void setUp() {
    jwtAuthFilter = new JwtAuthFilter(jwtService);
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void doFilterInternal_usesTokenClaimsForAuthoritiesAndOrganization() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID organizationId = UUID.randomUUID();
    String email = "jwt.user@test.com";
    List<String> roles = List.of("ROLE_ADMIN");

    when(jwtService.extractEmail("token")).thenReturn(email);
    when(jwtService.validateToken("token", email)).thenReturn(true);
    when(jwtService.extractUserId("token")).thenReturn(userId);
    when(jwtService.extractOrganizationId("token")).thenReturn(organizationId);
    when(jwtService.extractRoles("token")).thenReturn(roles);

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

    verify(jwtService).extractOrganizationId("token");
    verify(jwtService).extractRoles("token");
  }

  @Test
  void doFilterInternal_doesNothingWhenTokenValidationFails() throws Exception {
    String email = "jwt.user@test.com";

    when(jwtService.extractEmail("token")).thenReturn(email);
    when(jwtService.validateToken("token", email)).thenReturn(false);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer token");
    MockHttpServletResponse response = new MockHttpServletResponse();

    jwtAuthFilter.doFilterInternal(request, response, new MockFilterChain());

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isNull();
  }

  @Test
  void jwtAuthFilter_hasNoDatabaseRepositoryFieldDependency() {
    assertThat(JwtAuthFilter.class.getDeclaredFields())
        .extracting(field -> field.getType().getSimpleName())
        .doesNotContain("UserRepository");
  }
}
