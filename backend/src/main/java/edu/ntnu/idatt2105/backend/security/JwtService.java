package edu.ntnu.idatt2105.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

/**
 * Handles JWT creation, validation and extractions of claims.
 *
 * <p>Each token lives for 5 mins (set in env vars), and will carry the users identity,
 * organization and roles (used for permissions) stored as claims. This allows the {@link
 * JwtAuthFilter} to authenticate the user without a database call on every request</p>
 *
 * <p>Token structure:
 * <pre>
 * {
 *   "sub": "username",
 *   "userId": "uuid",
 *   "organizationId": "uuid",
 *   "roles": ["ROLE_MANAGER", "ROLE_STAFF"],
 *   "iat": 8932454400,
 *   "exp": 8932455300
 * }
 * </pre>
 *
 * @author Fredrik Borbe
 * @see JwtAuthFilter
 * @see JwtAuthenticatedPrincipal
 * @version 0.1
 */
@Service
public class JwtService {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration-ms}")
  private long expiration;

  private final int refreshExpiration = 604800000;

  private Claims getClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  /**
   * Generates an JWT containing user claims.
   *
   * <p>Roles are stored as authority strings (prefixed with "ROLE_") the user is granted,
   * so the filter can create Spring Security authorities directly from the token. If the org
   * is not set (like on signup), the field is set to null.</p>
   *
   * @param principal the authenticated user
   * @return a JWT string
   */
  public String generateToken(UserPrincipal principal) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", principal.getUser().getId().toString());
    claims.put("organizationId", principal.getOrganizationId() != null
        ? principal.getOrganizationId().toString()
        : null);
    claims.put("roles", principal.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList()));

    return Jwts.builder()
        .claims(claims)
        .subject(principal.getUsername())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSigningKey())
        .compact();
  }

  /**
   * Generates a refresh token
   *
   * <p>This token only carries the username, no other data. Is used to prove that a user has
   * been previously authenticated, and can be used for getting a new JWT when it has expired.
   * This token has 7 day expiration</p>
   *
   * @param principal the authenticated user
   * @return a token
   */
  public String generateRefreshToken(UserPrincipal principal) {
    return Jwts.builder()
        .subject(principal.getUsername())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
        .signWith(getSigningKey())
        .compact();
  }

  /**
   * Validates that the token is not expired and that the subject matches with expected username.
   *
   * @param token the JWT string
   * @param username the username subject is expected to match
   * @return true if token is valid
   */
  public boolean validateToken(String token, String username) {
    return extractEmail(token).equals(username) && !tokenExpired(token);
  }

  public String extractEmail(String token) {
    return getClaims(token).getSubject();  // reads the "sub" field
  }
  
  @SuppressWarnings("unchecked")
  public List<String> extractRoles(String token) {
    return getClaims(token).get("roles", List.class);
  }

  public UUID extractUserId(String token) {
    return UUID.fromString(getClaims(token).get("userId", String.class));
  }

  public UUID extractOrganizationId(String token) {
    String orgId = getClaims(token).get("organizationId", String.class);
    return orgId != null ? UUID.fromString(orgId) : null;
  }

  public boolean tokenExpired(String token) {
    return getClaims(token).getExpiration().before(new Date());
  }

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
  }
}
