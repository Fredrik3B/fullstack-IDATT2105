package edu.ntnu.idatt2105.backend.user.service;

import static org.junit.jupiter.api.Assertions.*;

import edu.ntnu.idatt2105.backend.security.JwtService;
import edu.ntnu.idatt2105.backend.user.dto.AuthDto;
import edu.ntnu.idatt2105.backend.user.dto.CreateUserRequest;
import edu.ntnu.idatt2105.backend.user.mapper.UserMapper;
import edu.ntnu.idatt2105.backend.user.model.RoleModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import edu.ntnu.idatt2105.backend.user.model.enums.RoleEnum;
import edu.ntnu.idatt2105.backend.user.repository.JoinRequestRepository;
import edu.ntnu.idatt2105.backend.user.repository.OrganizationRepository;
import edu.ntnu.idatt2105.backend.user.repository.RoleRepository;
import edu.ntnu.idatt2105.backend.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  private UserService userService;
  @Mock private UserRepository userRepository;
  @Mock private RoleRepository roleRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private UserMapper userMapper;
  @Mock private JwtService jwtService;
  @Mock private OrganizationRepository organizationRepository;
  @Mock private JoinRequestRepository joinRequestRepository;

  @BeforeEach
  void setUp() {
  }

  @Test
  void registerSuccess() {

      CreateUserRequest request = new CreateUserRequest("mail@test.com", "pass", "Ole", "Hansen");
      when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
      when(passwordEncoder.encode("pass")).thenReturn("encoded");
      when(jwtService.generateToken(any())).thenReturn("token");
      when(jwtService.generateRefreshToken(any())).thenReturn("refresh");

      UserModel saved = new UserModel();
      saved.setEmail(request.getEmail());

      when(userRepository.save(any())).thenReturn(saved);

      AuthDto result = userService.register(request);

      assertThat(result)
          .isNotNull()
          .extracting(AuthDto::getEmail, AuthDto::getAccessToken, AuthDto::getRefreshToken)
          .containsExactly("mail@test.com", "token", "refresh");

  }

  @Test
  void login() {
  }

  @Test
  void refreshToken() {
  }

  @Test
  void getMe() {
  }
}