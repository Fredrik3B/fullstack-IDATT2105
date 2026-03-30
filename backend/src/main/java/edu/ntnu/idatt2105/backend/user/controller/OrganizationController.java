package edu.ntnu.idatt2105.backend.user.controller;

import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import edu.ntnu.idatt2105.backend.user.dto.CreateOrganizationRequest;
import edu.ntnu.idatt2105.backend.user.dto.JoinOrganizationRequest;
import edu.ntnu.idatt2105.backend.user.dto.OrganizationResponse;
import edu.ntnu.idatt2105.backend.user.service.OrganizationService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class OrganizationController {
  private final OrganizationService organizationService;

  @PostMapping("/organizations")
  public ResponseEntity<OrganizationResponse> createOrganization(
      @RequestBody @Valid CreateOrganizationRequest request, Authentication auth
  ) {
    JwtAuthenticatedPrincipal principal = (JwtAuthenticatedPrincipal) auth.getPrincipal();

    OrganizationResponse resp =  organizationService.create(request, principal.getUserId());
    return ResponseEntity.ok(resp);
  }

  @PostMapping("/organizations/join")
  public ResponseEntity<OrganizationResponse> joinOrganization(
      @RequestBody @Valid JoinOrganizationRequest request,
      Authentication auth) {
    JwtAuthenticatedPrincipal principal = (JwtAuthenticatedPrincipal) auth.getPrincipal();
    return ResponseEntity.ok(organizationService.requestToJoin(request, principal.getUserId()));
  }

  // TODO: Dobbelsjekk sikkerhet her
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @PostMapping("/organizations/requests/{id}/accept")
  public ResponseEntity<Void> acceptRequest(
      @PathVariable UUID id, Authentication auth
  ) {
    JwtAuthenticatedPrincipal principal = (JwtAuthenticatedPrincipal) auth.getPrincipal();
    organizationService.acceptRequest(id, principal.getUserId());

    return ResponseEntity.ok().build();
  }
}
