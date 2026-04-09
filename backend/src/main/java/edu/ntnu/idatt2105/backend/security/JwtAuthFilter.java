package edu.ntnu.idatt2105.backend.security;

import java.io.IOException;
import java.util.UUID;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import edu.ntnu.idatt2105.backend.user.model.UserModel;
import edu.ntnu.idatt2105.backend.user.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * Extracts and validates the JWT from every request Authorization header.
 *
 * <p>Stateless authentication based on JWT verification. User authorities
 * and organization are loaded from the database to reflect role and membership
 * changes immediately without requiring a re-login. The resulting
 * {@link JwtAuthenticatedPrincipal} is placed in the SecurityContext
 * for the duration of the request period only.
 *
 * <p>If the token is missing, invalid, or expired, the filter skips authentication and lets
 * Spring Security's authorization rules handle it via {@link JwtAuthenticationEntryPoint}.
 *
 * @see JwtService
 * @see JwtAuthenticatedPrincipal
 * @author Fredrik Borbe
 * @version 0.1
 */
@Component
@AllArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {


  private final JwtService jwtService;
  private final UserRepository userRepository;

  /**
   * Finds the Authorization header field and extracts the jwt string from here.
   *
   * @param request the incoming HTTP request
   * @param response the response object
   * @param chain the cain of filers for the request
   * @throws ServletException an error occurs during the processing of the request
  * @throws IOException an I/O error occurs during the processing of the request
   */
  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain chain
  ) throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      authenticateUser(authHeader.substring(7));
    }
    chain.doFilter(request, response);
  }

  /**
   * Extracts the JWT, validates it, and sets the SecurityContext.
   *
   * <p>Builds a {@link JwtAuthenticatedPrincipal} from token claims
   * and wraps it in a custom {@link JwtAuthenticationToken}, since we don't need the credentials
   * fields (it is stateless).
   *
   * @param jwt the token string without the "Bearer " prefix
   */
  private void authenticateUser(String jwt) {
    try {
      String email = jwtService.extractEmail(jwt);
      if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        if (jwtService.validateToken(jwt, email)) {
          UserModel user = resolveCurrentUser(jwt, email);
          if (user == null) {
            return;
          }

          UUID organizationId = user.getOrganization() != null
              ? user.getOrganization().getId()
              : null;

          var authorities = user.getRoles().stream()
              .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
              .toList();

          JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
              user.getId(), organizationId, email, authorities
          );

          JwtAuthenticationToken authToken = new JwtAuthenticationToken(principal, authorities);

          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }
    } catch (JwtException ignored) {
      // Expired or malformed token — skip authentication and let Spring Security
      // handle access control. permitAll endpoints will still be reachable.
    }
  }

  private UserModel resolveCurrentUser(String jwt, String email) {
    try {
      UUID tokenUserId = jwtService.extractUserId(jwt);
      if (tokenUserId != null) {
        UserModel user = userRepository.findById(tokenUserId).orElse(null);
        if (user != null && email.equals(user.getEmail())) {
          return user;
        }
      }
    } catch (RuntimeException ignored) {
      // Fall through to email lookup so older or partially-migrated tokens can still authenticate.
    }

    return userRepository.findByEmail(email).orElse(null);
  }
}
