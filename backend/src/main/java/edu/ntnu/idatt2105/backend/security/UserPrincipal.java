package edu.ntnu.idatt2105.backend.security;

import edu.ntnu.idatt2105.backend.user.model.UserModel;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Adapts the {@link UserModel} entity to Spring Security's {@link UserDetails}.
 *
 * <p>Used only during login to verify credentials and extract roles
 * for JWT generation. Not used on subsequent requests — those use
 * {@link JwtAuthenticatedPrincipal} instead.
 *
 * <p>Translates the user's {@link edu.ntnu.idatt2105.backend.user.model.RoleModel} entities into
 * Spring Security authorities with the "ROLE_" prefix required by {@code hasRole()} checks.
 *
 * @author Fredrik Borbe
 * @version 0.1
 */
@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

  private final UserModel user;

  public UUID getUserId() {
    return user.getId();
  }

  public UUID getOrganizationId() {
    return user.getOrganizationId();
  }

  @Override
  public String getUsername() {
    return user.getUsername();
  }

  @Override
  public @Nullable String getPassword() {
    return user.getPassword();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return user.getRoles().stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
        .collect(Collectors.toSet());
  }
}
