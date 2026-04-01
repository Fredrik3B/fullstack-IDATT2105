package edu.ntnu.idatt2105.backend.user.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import edu.ntnu.idatt2105.backend.exception.UserAlreadyExistsException;
import edu.ntnu.idatt2105.backend.security.JwtService;
import edu.ntnu.idatt2105.backend.security.UserPrincipal;
import edu.ntnu.idatt2105.backend.user.dto.AuthDto;
import edu.ntnu.idatt2105.backend.user.dto.CreateUserRequest;
import edu.ntnu.idatt2105.backend.user.dto.LoginRequest;
import edu.ntnu.idatt2105.backend.user.dto.MeResponse;
import edu.ntnu.idatt2105.backend.user.mapper.UserMapper;
import edu.ntnu.idatt2105.backend.user.model.RoleModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import edu.ntnu.idatt2105.backend.user.model.enums.JoinOrgStatus;
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
  private final OrganizationRepository organizationRepository;
  private final JoinRequestRepository joinRequestRepository;


  public AuthDto register(CreateUserRequest request) {
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
    UserPrincipal principal = new UserPrincipal(user);

    return new AuthDto(
        jwtService.generateToken(principal),
        jwtService.generateRefreshToken(principal),
        savedUser.getEmail()
    );

  }

  public AuthDto login(LoginRequest request) {
    String email = request.getEmail();
    String password = request.getPassword();

    UserModel user = userRepository.findByEmail(email)
        .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new BadCredentialsException("Invalid credentials");
    }

    UserPrincipal principal = new UserPrincipal(user);

    return new AuthDto(
        jwtService.generateToken(principal),
        jwtService.generateRefreshToken(principal),
        user.getEmail()
    );
  }

  public AuthDto refreshToken(String refreshToken) {
    if (jwtService.tokenExpired(refreshToken)) {
      throw new BadCredentialsException("Refresh token expired");
    }
    String email = jwtService.extractEmail(refreshToken);
    UserModel user = userRepository.findByEmail(email)
        .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

    UserPrincipal principal = new UserPrincipal(user);

    return new AuthDto(
        jwtService.generateToken(principal),
        jwtService.generateRefreshToken(principal),
        user.getEmail()
    );
  }

  public MeResponse getMe(UUID userId) {
    UserModel user = userRepository.findById(userId)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    String name = (user.getFirstName() + " " + user.getLastName()).trim();
    UUID orgId = user.getOrganizationId();

    if (orgId != null) {
      String orgName = organizationRepository.findById(orgId)
          .map(org -> org.getName())
          .orElse(null);
      return new MeResponse(new MeResponse.UserInfo(user.getEmail(), name),
          "active", orgId, orgName);
    }

    return joinRequestRepository.findFirstByUserIdAndStatus(userId, JoinOrgStatus.PENDING)
        .map(request -> {
          String orgName = organizationRepository.findById(request.getOrganizationId())
              .map(org -> org.getName())
              .orElse(null);
          return new MeResponse(new MeResponse.UserInfo(user.getEmail(), name),
              "pending", request.getOrganizationId(), orgName);
        })
        .orElse(new MeResponse(new MeResponse.UserInfo(user.getEmail(), name),
            null, null, null));
  }
}
