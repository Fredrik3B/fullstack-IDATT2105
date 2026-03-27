package edu.ntnu.idatt2105.backend.security;

import java.util.Collection;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * Authentication token created from validated JWT claims.
 *
 * <p>{@code setAuthenticated(true)} is called in the constructor because
 * this token is only instantiated by {@link JwtAuthFilter} after the
 * JWT signature and expiration have been verified by {@link JwtService}.</p>
 *
 * @see JwtAuthFilter
 * @see JwtAuthenticatedPrincipal
 * @author Fredrik Borbe
 * @version 0.1
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {
  private final JwtAuthenticatedPrincipal principal;

  public JwtAuthenticationToken(JwtAuthenticatedPrincipal principal,
      Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.principal = principal;
    setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  @Override
  public JwtAuthenticatedPrincipal getPrincipal() {
    return principal;
  }
}
