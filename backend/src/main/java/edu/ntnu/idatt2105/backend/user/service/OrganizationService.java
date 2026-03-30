package edu.ntnu.idatt2105.backend.user.service;

import edu.ntnu.idatt2105.backend.user.dto.CreateOrganizationRequest;
import edu.ntnu.idatt2105.backend.user.dto.JoinOrganizationRequest;
import edu.ntnu.idatt2105.backend.user.dto.OrganizationDto;
import jakarta.validation.Valid;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public class OrganizationService {

  public OrganizationDto create(CreateOrganizationRequest request, UUID userId) {
  }

  public OrganizationDto join(JoinOrganizationRequest request, UUID userId) {
  }
}
