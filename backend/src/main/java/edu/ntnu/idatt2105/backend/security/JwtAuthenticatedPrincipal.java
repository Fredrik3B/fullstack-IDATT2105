package edu.ntnu.idatt2105.backend.security;

import edu.ntnu.idatt2105.backend.exception.OrganizationRequiredException;
import java.util.Collection;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

/**
 * Lightweight principal constructed from JWT claims.
 *
 * <p>Unlike {@link UserPrincipal}, which wraps a full User entity loaded from the database, this
 * class only carry the data embedded in the JWT. It is created by {@link JwtAuthFilter} on
 * every request and placed in the SecurityContext.</p>
 *
 * <p>Access in controllers:</p>
 * <pre>
 * JwtAuthenticatedPrincipal principal = JwtAuthenticatedPrincipal.from(auth);
 * UUID orgId = principal.getOrganizationId();
 * </pre>
 *
 * @see JwtAuthFilter
 * @author Fredrik Borbe
 * @version 0.1
 */
@Getter
@Setter
@AllArgsConstructor
public class JwtAuthenticatedPrincipal {
  private final UUID userId;
  private final UUID organizationId;
  private final String username;
  private final Collection<? extends GrantedAuthority> authorities;

  /**
   * Extracts the principal from a Spring Security {@link Authentication} object.
   *
   * @param auth the current authentication, may be {@code null}
   * @return the authenticated principal
   * @throws org.springframework.web.server.ResponseStatusException 401 if auth is missing or not JWT-based
   */
  public static JwtAuthenticatedPrincipal from(Authentication auth) {
    if (auth == null || !(auth.getPrincipal() instanceof JwtAuthenticatedPrincipal principal)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
    }
    return principal;
  }

  /**
   * Returns the organization ID, throwing if the user has not yet joined an organization.
   *
   * @return the non-null organization UUID
   * @throws edu.ntnu.idatt2105.backend.exception.OrganizationRequiredException if organizationId is null
   */
  public UUID requireOrganizationId() {
    if (organizationId == null) {
      throw new OrganizationRequiredException();
    }
    return organizationId;
  }
}
