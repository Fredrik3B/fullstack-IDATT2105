package edu.ntnu.idatt2105.backend.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

public final class AuthenticationUtils {

  private AuthenticationUtils() {
  }

  public static JwtAuthenticatedPrincipal requirePrincipal(Authentication authentication) {
    if (authentication == null || authentication.getPrincipal() == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
    }
    if (!(authentication.getPrincipal() instanceof JwtAuthenticatedPrincipal principal)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication principal");
    }
    return principal;
  }
}
