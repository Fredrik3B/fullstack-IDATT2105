package edu.ntnu.idatt2105.backend.security;

import edu.ntnu.idatt2105.backend.user.model.UserModel;
import edu.ntnu.idatt2105.backend.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@AllArgsConstructor
public class UserPrincipalService implements UserDetailsService {
  private final UserRepository userRepository;


  @Override
  public UserPrincipal loadUserByUsername(String username) throws UsernameNotFoundException {
    UserModel user = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    return new UserPrincipal(user);
  }

}
