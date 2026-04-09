package edu.ntnu.idatt2105.backend.user.dto;

import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

/**
 * DTO representing a member of an organisation, including their current roles.
 */
@Data
@Builder
public class MemberDto {
  private UUID userId;
  private String firstName;
  private String lastName;
  private String email;
  private Set<String> roles;
}
