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
  public UserPrincipal loadUserByUsername(String email) throws UsernameNotFoundException {
    UserModel user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    return new UserPrincipal(user);
  }

}
