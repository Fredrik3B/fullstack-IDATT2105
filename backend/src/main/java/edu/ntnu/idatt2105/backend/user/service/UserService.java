package edu.ntnu.idatt2105.backend.user.service;

import edu.ntnu.idatt2105.backend.user.dto.LoginResponse;
import java.util.Set;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import edu.ntnu.idatt2105.backend.exception.UserAlreadyExistsException;
import edu.ntnu.idatt2105.backend.security.JwtService;
import edu.ntnu.idatt2105.backend.security.UserPrincipal;
import edu.ntnu.idatt2105.backend.user.dto.CreateUserRequest;
import edu.ntnu.idatt2105.backend.user.dto.LoginRequest;
import edu.ntnu.idatt2105.backend.user.mapper.UserMapper;
import edu.ntnu.idatt2105.backend.user.model.RoleModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import edu.ntnu.idatt2105.backend.user.model.enums.RoleEnum;
import edu.ntnu.idatt2105.backend.user.repository.JoinRequestRepository;
import edu.ntnu.idatt2105.backend.user.repository.OrganizationRepository;
import edu.ntnu.idatt2105.backend.user.repository.RoleRepository;
import edu.ntnu.idatt2105.backend.user.repository.UserRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;
  private final JwtService jwtService;

  public LoginResponse register(CreateUserRequest request) {
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new UserAlreadyExistsException(request.getEmail());
    }

    RoleModel staffRole = roleRepository.findByName(RoleEnum.STAFF)
        .orElseThrow(() -> new RuntimeException("STAFF role not found in database"));

    // Refactor
    UserModel user = new UserModel();
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setRoles(Set.of(staffRole));


    UserModel savedUser = userRepository.save(user);
    return buildLoginResponse(savedUser);

  }

  public LoginResponse login(LoginRequest request) {
    UserModel user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new BadCredentialsException("Invalid credentials");
    }

    return buildLoginResponse(user);
  }

  public LoginResponse refreshToken(String refreshToken) {
    String email = jwtService.extractEmail(refreshToken);
    UserModel user = userRepository.findByEmail(email)
        .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

    return buildLoginResponse(user);
  }

  /**
   * Builds a LoginResponse with fresh tokens for the given user.
   * The refresh token is included here but the controller moves it to an HttpOnly cookie.
   */
  private LoginResponse buildLoginResponse(UserModel user) {
    UserPrincipal principal = new UserPrincipal(user);
    String accessToken = jwtService.generateToken(principal);
    String refreshToken = jwtService.generateRefreshToken(principal);
    return userMapper.toLoginResponse(accessToken, refreshToken, user);
  }
}
