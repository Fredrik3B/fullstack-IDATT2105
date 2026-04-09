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

  /**
   * Returns the user's UUID primary key.
   *
   * @return the user ID
   */
  public UUID getUserId() {
    return user.getId();
  }

  /**
   * Returns the organization the user belongs to, or {@code null} if not yet in one.
   *
   * @return the organization UUID, or {@code null}
   */
  public UUID getOrganizationId() {
    return user.getOrganization() != null ? user.getOrganization().getId() : null;
  }

  /**
   * Returns the user's email address, used as the Spring Security username.
   *
   * @return the user's email
   */
  @Override
  public String getUsername() {
    return user.getEmail();
  }

  /**
   * Returns the bcrypt-hashed password stored for this user.
   *
   * @return the encoded password
   */
  @Override
  public @Nullable String getPassword() {
    return user.getPassword();
  }

  /**
   * Converts the user's roles to Spring Security {@link GrantedAuthority} objects.
   *
   * <p>Each role name is prefixed with {@code "ROLE_"} so that
   * {@code @PreAuthorize("hasRole('MANAGER')")} resolves correctly.
   *
   * @return the set of granted authorities derived from the user's roles
   */
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return user.getRoles().stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
        .collect(Collectors.toSet());
  }
}
