package edu.ntnu.idatt2105.backend.security;

import java.util.Collection;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

/**
 * Lightweight principal constructed from JWT claims.
 *
 * <p>Unlike {@link UserPrincipal}, which wraps a full User entity loaded from the database, this
 * class only carry the data embedded in the JWT. It is created by {@link JwtAuthFilter} on
 * every request and placed in the SecurityContext.</p>
 *
 * <p>Access in controllers:</p>
 * <pre>
 * JwtAuthenticatedPrincipal principal = (JwtAuthenticatedPrincipal) auth.getPrincipal();
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
}
