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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final Logger logger = LoggerFactory.getLogger(JwtService.class);

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration-ms}")
  private long expiration;

  private Claims getClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public String generateToken(UserPrincipal principal) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", principal.getUser().getId().toString());
    claims.put("organizationId", principal.getUser().getOrganizationId().toString());
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

  public String generateRefreshToken(UserPrincipal user) {
    return Jwts.builder()
        .subject(user.getUsername())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + 604800000))
        .signWith(getSigningKey())
        .compact();
  }

  public String extractUsername(String token) {
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
    return UUID.fromString(getClaims(token).get("organizationId", String.class));
  }

  public boolean tokenExpired(String token) {
    return getClaims(token).getExpiration().before(new Date());
  }

  public boolean validateToken(String token, String username) {
    return extractUsername(token).equals(username) && !tokenExpired(token);
  }

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
  }
}
