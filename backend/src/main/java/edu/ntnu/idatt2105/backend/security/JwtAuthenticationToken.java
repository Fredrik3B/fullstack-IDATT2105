package edu.ntnu.idatt2105.backend.security;

import java.util.Collection;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

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
