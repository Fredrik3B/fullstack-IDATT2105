package edu.ntnu.idatt2105.backend.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.ntnu.idatt2105.backend.exception.UserAlreadyExistsException;
import edu.ntnu.idatt2105.backend.security.JwtService;
import edu.ntnu.idatt2105.backend.user.dto.CreateUserRequest;
import edu.ntnu.idatt2105.backend.user.dto.LoginRequest;
import edu.ntnu.idatt2105.backend.user.dto.LoginResponse;
import edu.ntnu.idatt2105.backend.user.mapper.UserMapper;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.RoleModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import edu.ntnu.idatt2105.backend.user.model.enums.RoleEnum;
import edu.ntnu.idatt2105.backend.user.repository.RoleRepository;
import edu.ntnu.idatt2105.backend.user.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @InjectMocks
  private UserService userService;
  @Mock
  private UserRepository userRepository;
  @Mock
  private RoleRepository roleRepository;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Spy
  private UserMapper userMapper;
  @Mock
  private JwtService jwtService;

  private UUID userId;
  private RoleModel staffRole;
  private UserModel user;
  private OrganizationModel org;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();

    staffRole = new RoleModel();
    staffRole.setName(RoleEnum.STAFF);

    user = new UserModel();
    user.setId(userId);
    user.setEmail("test@example.com");
    user.setPassword("hashed123");
    user.setFirstName("Test");
    user.setLastName("User");
    user.setRoles(new HashSet<>());

    org = new OrganizationModel();
    org.setId(UUID.randomUUID());
    org.setName("Restaurant");
    org.setJoinCode("RES-1234");
  }

  @Test
  void loginSuccess() {
    LoginRequest request = new LoginRequest();
    request.setEmail("test@example.com");
    request.setPassword("pass");

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("pass", user.getPassword())).thenReturn(true);
    when(jwtService.generateToken(any())).thenReturn("token");
    when(jwtService.generateRefreshToken(any())).thenReturn("refresh");

    LoginResponse result = userService.login(request);

    assertThat(result).isNotNull();
    assertThat(result.getAccessToken()).isEqualTo("token");
    assertThat(result.getRefreshToken()).isEqualTo("refresh");
    assertThat(result.getUser().getEmail()).isEqualTo("test@example.com");
    assertThat(result.getUser().getName()).isEqualTo("Test User");
    assertThat(result.getRestaurant()).isNull();
  }

  @Test
  void loginSuccess_withOrganization() {
    user.setOrganization(org);

    LoginRequest request = new LoginRequest();
    request.setEmail("test@example.com");
    request.setPassword("pass");

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("pass", user.getPassword())).thenReturn(true);
    when(jwtService.generateToken(any())).thenReturn("token");
    when(jwtService.generateRefreshToken(any())).thenReturn("refresh");

    LoginResponse result = userService.login(request);

    assertThat(result.getRestaurant()).isNotNull();
    assertThat(result.getRestaurant().getName()).isEqualTo("Restaurant");
    assertThat(result.getRestaurant().getJoinCode()).isEqualTo("RES-1234");
  }

  @Test
  void registerSuccess() {
    CreateUserRequest request = new CreateUserRequest("mail@test.com", "pass", "Ole", "Hansen");

    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
    when(passwordEncoder.encode("pass")).thenReturn("encoded");
    when(jwtService.generateToken(any())).thenReturn("token");
    when(jwtService.generateRefreshToken(any())).thenReturn("refresh");
    when(roleRepository.findByName(RoleEnum.STAFF)).thenReturn(Optional.of(staffRole));
    when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    LoginResponse result = userService.register(request);

    assertThat(result).isNotNull();
    assertThat(result.getAccessToken()).isEqualTo("token");
    assertThat(result.getRefreshToken()).isEqualTo("refresh");
    assertThat(result.getUser().getEmail()).isEqualTo("mail@test.com");
    assertThat(result.getUser().getName()).isEqualTo("Ole Hansen");
    assertThat(result.getRestaurant()).isNull();
  }

  @Test
  void register_assignsStaffRole() {
    CreateUserRequest request = CreateUserRequest.builder()
        .email("new@example.com").password("pass").firstName("A").lastName("B").build();

    when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
    when(roleRepository.findByName(RoleEnum.STAFF)).thenReturn(Optional.of(staffRole));
    when(passwordEncoder.encode(any())).thenReturn("hashed");
    when(userRepository.save(any(UserModel.class))).thenAnswer(inv -> inv.getArgument(0));
    when(jwtService.generateToken(any())).thenReturn("t");
    when(jwtService.generateRefreshToken(any())).thenReturn("r");

    userService.register(request);

    verify(userRepository).save(argThat(u -> u.getRoles().contains(staffRole)));
  }

  @Test
  void register_duplicateEmail_throwsException() {
    CreateUserRequest request = CreateUserRequest.builder()
        .email("exists@example.com").password("pass")
        .firstName("A").lastName("B").build();

    when(userRepository.findByEmail("exists@example.com"))
        .thenReturn(Optional.of(new UserModel()));

    assertThatThrownBy(() -> userService.register(request))
        .isInstanceOf(UserAlreadyExistsException.class);
  }

  @Test
  void register_staffRoleMissing_throwsException() {
    CreateUserRequest request = CreateUserRequest.builder()
        .email("new@example.com").password("pass")
        .firstName("A").lastName("B").build();

    when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
    when(roleRepository.findByName(RoleEnum.STAFF)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.register(request))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("STAFF role not found in database");
  }

  @Test
  void login_wrongPassword_throwsBadCredentials() {
    LoginRequest request = new LoginRequest();
    request.setEmail("test@example.com");
    request.setPassword("wrong");

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong", "hashed123")).thenReturn(false);

    assertThatThrownBy(() -> userService.login(request))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Invalid credentials");
  }

  @Test
  void login_unknownEmail_throwsBadCredentials() {
    LoginRequest request = new LoginRequest();
    request.setEmail("unknown@example.com");
    request.setPassword("pass");

    when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.login(request))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Invalid credentials");
  }

  @Test
  void login_sameErrorForWrongEmailAndWrongPassword() {
    LoginRequest wrongEmail = new LoginRequest();
    wrongEmail.setEmail("no@example.com");
    wrongEmail.setPassword("pass");

    LoginRequest wrongPass = new LoginRequest();
    wrongPass.setEmail("test@example.com");
    wrongPass.setPassword("wrong");

    when(userRepository.findByEmail("no@example.com")).thenReturn(Optional.empty());
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong", "hashed123")).thenReturn(false);

    assertThatThrownBy(() -> userService.login(wrongEmail))
        .hasMessage("Invalid credentials");
    assertThatThrownBy(() -> userService.login(wrongPass))
        .hasMessage("Invalid credentials");
  }

  @Test
  void refreshToken_success_returnsNewTokens() {
    when(jwtService.extractEmail("old-refresh")).thenReturn("test@example.com");
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(jwtService.generateToken(any())).thenReturn("new-access");
    when(jwtService.generateRefreshToken(any())).thenReturn("new-refresh");

    LoginResponse result = userService.refreshToken("old-refresh");

    assertThat(result.getAccessToken()).isEqualTo("new-access");
    assertThat(result.getRefreshToken()).isEqualTo("new-refresh");
  }

  @Test
  void refreshToken_expired_throwsExpiredJwt() {
    when(jwtService.extractEmail("expired"))
        .thenThrow(new ExpiredJwtException(null, null, "expired"));

    assertThatThrownBy(() -> userService.refreshToken("expired"))
        .isInstanceOf(ExpiredJwtException.class)
        .hasMessage("expired");
  }

  @Test
  void refreshToken_userNotFound_throwsBadCredentials() {
    when(jwtService.extractEmail("valid")).thenReturn("gone@example.com");
    when(userRepository.findByEmail("gone@example.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.refreshToken("valid"))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Invalid refresh token");
  }
}