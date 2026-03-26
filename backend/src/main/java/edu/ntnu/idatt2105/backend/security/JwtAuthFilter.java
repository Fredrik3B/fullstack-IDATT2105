package edu.ntnu.idatt2105.backend.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@AllArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      authenticateUser(authHeader.substring(7));
    }
    chain.doFilter(request, response);
  }

  private void authenticateUser(String jwt) {
    try {
      String username = jwtService.extractUsername(jwt);
      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        if (!jwtService.validateToken(jwt, username)) {
          UUID userId = jwtService.extractUserId(jwt);
          UUID organizationId = jwtService.extractOrganizationId(jwt);
          List<String> roles = jwtService.extractRoles(jwt);

          List<SimpleGrantedAuthority> authorities = roles.stream()
              .map(SimpleGrantedAuthority::new)
              .toList();

          JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
              userId, organizationId, username, authorities
          );
        }
      }
    } catch (ExpiredJwtException e) {
      logger.warn("JWT token expired: {}", e.getMessage());
    } catch (JwtException e) {
      logger.warn("Invalid JWT token: {}", e.getMessage());
    }
  }
}
