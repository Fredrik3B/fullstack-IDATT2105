package edu.ntnu.idatt2105.backend.user.service;

import edu.ntnu.idatt2105.backend.user.dto.CreateUserRequest;
import edu.ntnu.idatt2105.backend.user.dto.LoginRequest;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import edu.ntnu.idatt2105.backend.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserRepository register(CreateUserRequest request) {
    if (userRepository.findByUsername(request.getUsername()).isPresent()) {
      throw new UserAlreadyExistsException(request.getUsername();
    }
    UserModel user = new UserModel();
    user.setUsername(request.getUsername());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    return userMapper.toResponse(userRepository.save(user));

  }

  public UserModel login(LoginRequest request) {
    String username = request.getUsername();
    String password = request.getPassword();

    UserModel user = userRepository.findByUsername(username).orElseThrow(UsernameNotFoundException::new);

    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new BadCredentialsException("Incorrect password");
    }

  }
}
