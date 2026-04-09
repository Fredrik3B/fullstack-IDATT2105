package edu.ntnu.idatt2105.backend.user.dto;

import lombok.Data;

/**
 * Request DTO for creating a new organisation.
 */
@Data
public class CreateOrganizationRequest {
  private String name;
}
