package edu.ntnu.idatt2105.backend.security;

import java.util.Collection;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;


@Getter
@Setter
@AllArgsConstructor
public class JwtAuthenticatedPrincipal {
  private final UUID userId;
  private final UUID organizationId;
  private final String username;
  private final Collection<? extends GrantedAuthority> authorities;
}
