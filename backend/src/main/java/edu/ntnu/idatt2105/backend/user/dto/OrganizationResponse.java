package edu.ntnu.idatt2105.backend.user.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrganizationResponse {
  private final UUID id;
  private final String name;
  private final String joinCode;
}
