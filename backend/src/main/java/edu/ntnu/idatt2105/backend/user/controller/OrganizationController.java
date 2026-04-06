package edu.ntnu.idatt2105.backend.user.controller;

import edu.ntnu.idatt2105.backend.security.AuthenticationUtils;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import edu.ntnu.idatt2105.backend.user.dto.CreateOrganizationRequest;
import edu.ntnu.idatt2105.backend.user.dto.JoinOrganizationDto;
import edu.ntnu.idatt2105.backend.user.dto.JoinOrganizationRequest;
import edu.ntnu.idatt2105.backend.user.dto.JoinRequestResponse;
import edu.ntnu.idatt2105.backend.user.dto.MemberDto;
import edu.ntnu.idatt2105.backend.user.dto.OrganizationResponse;
import edu.ntnu.idatt2105.backend.user.dto.ResolveJoinRequest;
import edu.ntnu.idatt2105.backend.user.dto.UpdateMemberRolesRequest;
import edu.ntnu.idatt2105.backend.user.model.enums.JoinOrgStatus;
import edu.ntnu.idatt2105.backend.user.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Organizations", description = "Create, join, and manage organizations")
@RestController
@AllArgsConstructor
public class OrganizationController {
  private final OrganizationService organizationService;

  @Operation(summary = "Create a new organization",
      description = "Creator becomes ADMIN of the organization. Generates a join code.")
  @PostMapping("/organizations")
  public ResponseEntity<OrganizationResponse> createOrganization(
      @RequestBody @Valid CreateOrganizationRequest request, Authentication auth
  ) {
    JwtAuthenticatedPrincipal principal = AuthenticationUtils.requirePrincipal(auth);

    OrganizationResponse resp =  organizationService.create(request, principal.getUserId());
    return ResponseEntity.ok(resp);
  }

  @Operation(summary = "Withdraw a pending join request")
  @DeleteMapping("/organizations/join-request")
  public ResponseEntity<Void> withdrawJoinRequest(Authentication auth) {
    JwtAuthenticatedPrincipal principal = AuthenticationUtils.requirePrincipal(auth);
    organizationService.withdrawJoinRequest(principal.getUserId());
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "See current join request")
  @GetMapping("/organizations/join-request")
  public ResponseEntity<JoinRequestResponse> seeJoinRequest(Authentication auth) {
    JwtAuthenticatedPrincipal principal = (JwtAuthenticatedPrincipal) auth.getPrincipal();
    return ResponseEntity.ok(organizationService.seeJoinRequest(principal.getUserId()));
  }

  @Operation(summary = "Look up an organization by join code",
      description = "Returns the organization name for a given join code. Used to preview before joining.")
  @GetMapping("/organizations/lookup")
  public ResponseEntity<OrganizationResponse> lookupOrganization(@RequestParam String code) {
    return ResponseEntity.ok(organizationService.lookupByCode(code));
  }

  @Operation(summary = "Request to join an organization",
      description = "Creates a pending request. An admin must accept before the user is added.")
  @PostMapping("/organizations/join")
  public ResponseEntity<OrganizationResponse> joinOrganization(
      @RequestBody @Valid JoinOrganizationRequest request,
      Authentication auth) {
    JwtAuthenticatedPrincipal principal = AuthenticationUtils.requirePrincipal(auth);
    return ResponseEntity.ok(organizationService.requestToJoin(request, principal.getUserId()));
  }

  // TODO: Dobbelsjekk sikkerhet her
  @Operation(summary = "Accept or decline a join request")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @PostMapping("/organizations/requests/{id}")
  public ResponseEntity<Void> acceptRequest(
      @PathVariable UUID id, @RequestBody @Valid ResolveJoinRequest request, Authentication auth
  ) {
    JwtAuthenticatedPrincipal principal = (JwtAuthenticatedPrincipal) auth.getPrincipal();
    organizationService.resolveRequest(
        id, principal.getUserId(), principal.getOrganizationId(), request.getAction()
    );

    return ResponseEntity.ok().build();
  }

  @Operation(summary = "List join requests for your organization")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @GetMapping("/organizations/requests")
  public ResponseEntity<List<JoinOrganizationDto>> getOrganizationRequests(
      @RequestParam(required = false) JoinOrgStatus status, Authentication auth
  ) {
    JwtAuthenticatedPrincipal principal = (JwtAuthenticatedPrincipal) auth.getPrincipal();
    List<JoinOrganizationDto> requests = organizationService.getRequests(
        principal.getOrganizationId(), status);
    return ResponseEntity.ok(requests);
  }

  @Operation(summary = "List all members of your organization")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  @GetMapping("/organizations/members")
  public ResponseEntity<List<MemberDto>> getMembers(Authentication auth) {
    JwtAuthenticatedPrincipal principal = (JwtAuthenticatedPrincipal) auth.getPrincipal();
    return ResponseEntity.ok(organizationService.getMembers(principal.getOrganizationId()));
  }

  @Operation(summary = "Remove a member from your organization")
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/organizations/members/{userId}")
  public ResponseEntity<Void> removeMember(@PathVariable UUID userId, Authentication auth) {
    JwtAuthenticatedPrincipal principal = (JwtAuthenticatedPrincipal) auth.getPrincipal();
    organizationService.removeMember(principal.getOrganizationId(), userId, principal.getUserId());
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Update roles of a member in your organization")
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/organizations/members/{userId}/roles")
  public ResponseEntity<Void> updateMemberRoles(
      @PathVariable UUID userId, @RequestBody @Valid UpdateMemberRolesRequest request, Authentication auth
  ) {
    JwtAuthenticatedPrincipal principal = (JwtAuthenticatedPrincipal) auth.getPrincipal();
    organizationService.updateMemberRoles(
        principal.getOrganizationId(), userId, principal.getUserId(), request.getRoles());
    return ResponseEntity.ok().build();
  }

}
