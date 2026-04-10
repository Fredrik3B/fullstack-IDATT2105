package edu.ntnu.idatt2105.backend.security;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import edu.ntnu.idatt2105.backend.user.model.RoleModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import edu.ntnu.idatt2105.backend.user.model.enums.RoleEnum;
import io.jsonwebtoken.ExpiredJwtException;

class JwtServiceTest {
  UserPrincipal principal;
  JwtService jwtService;

  @BeforeEach
  public void setUp() {
    jwtService = new JwtService();
    ReflectionTestUtils.setField(jwtService, "secret",
        "dGhpcyBpcyBhIHZlcnkgc2VjcmV0IGtleSBmb3IgdGVzdGluZyBwdXJwb3Nlcw==");
    ReflectionTestUtils.setField(jwtService, "expiration", 100000000L);
    ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604800000L);

    UserModel user = new UserModel();
    user.setId(UUID.randomUUID());
    user.setEmail("test@example.com");
    user.setPassword("hashedPass");
    user.setFirstName("Test");
    user.setLastName("User");

    RoleModel role = new RoleModel();
    role.setName(RoleEnum.STAFF);
    user.setRoles(Set.of(role));

    principal = new UserPrincipal(user);
  }

  @Test
  void generatedTokenContainsCorrectSubject() {
    String token = jwtService.generateToken(principal);
    String email = jwtService.extractEmail(token);
    assertThat(email).isEqualTo("test@example.com");
  }

  @Test
  void generatedTokenContainsUserId() {
    String token = jwtService.generateToken(principal);
    UUID userId = jwtService.extractUserId(token);
    assertThat(userId).isEqualTo(principal.getUserId());
  }

  @Test
  void generatedTokenContainsRoles() {
    String token = jwtService.generateToken(principal);
    List<String> roles = jwtService.extractRoles(token);
    assertThat(roles).containsExactly("ROLE_STAFF");
  }

  @Test
  void generateTokenNullOrgId() {
    String token = jwtService.generateToken(principal);
    UUID orgId = jwtService.extractOrganizationId(token);
    assertThat(orgId).isNull();
  }

  @Test
  void validateValidToken() {
    String token = jwtService.generateToken(principal);
    assertThat(jwtService.validateToken(token, "test@example.com")).isTrue();
  }

  @Test
  void validateInvalidToken() {
    String token = jwtService.generateToken(principal);
    assertThat(jwtService.validateToken(token, "wrong@example.com")).isFalse();
  }

  @Test
  void validateExpiredToken() {
    ReflectionTestUtils.setField(jwtService, "expiration", 0);
    String token = jwtService.generateToken(principal);
    assertThatThrownBy(() -> jwtService.extractEmail(token))
        .isInstanceOf(ExpiredJwtException.class);
  }

  @Test
  void generateRefreshToken() {
    String token = jwtService.generateRefreshToken(principal);
    String email = jwtService.extractEmail(token);
    assertThat(email).isEqualTo("test@example.com");
  }


}