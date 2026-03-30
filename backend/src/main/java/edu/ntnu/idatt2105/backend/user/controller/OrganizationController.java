package edu.ntnu.idatt2105.backend.user.controller;

import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import edu.ntnu.idatt2105.backend.user.dto.CreateOrganizationRequest;
import edu.ntnu.idatt2105.backend.user.dto.JoinOrganizationRequest;
import edu.ntnu.idatt2105.backend.user.dto.OrganizationDto;
import edu.ntnu.idatt2105.backend.user.model.Organization;
import edu.ntnu.idatt2105.backend.user.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class OrganizationController {
  private final OrganizationService organizationService;

  @PostMapping("/organizations")
  public ResponseEntity<OrganizationDto> createOrganization(
      @RequestBody @Valid CreateOrganizationRequest request,
      Authentication auth) {
    JwtAuthenticatedPrincipal principal =
        (JwtAuthenticatedPrincipal) auth.getPrincipal();
    return ResponseEntity.ok(organizationService.create(request, principal.getUserId()));
  }
  public ResponseEntity<OrganizationDto> joinOrganization(
      @RequestBody @Valid JoinOrganizationRequest request,
      Authentication auth) {
    JwtAuthenticatedPrincipal principal =
        (JwtAuthenticatedPrincipal) auth.getPrincipal();
    return ResponseEntity.ok(organizationService.join(request, principal.getUserId()));
  }

}
