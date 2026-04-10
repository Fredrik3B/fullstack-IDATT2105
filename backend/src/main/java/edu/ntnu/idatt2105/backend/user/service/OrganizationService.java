package edu.ntnu.idatt2105.backend.user.service;

import edu.ntnu.idatt2105.backend.document.model.DocumentModel;
import edu.ntnu.idatt2105.backend.document.model.enums.DocumentCategory;
import edu.ntnu.idatt2105.backend.document.model.enums.DocumentModule;
import edu.ntnu.idatt2105.backend.document.repository.DocumentRepository;
import edu.ntnu.idatt2105.backend.exception.ResourceNotFoundException;
import edu.ntnu.idatt2105.backend.user.dto.CreateOrganizationRequest;
import edu.ntnu.idatt2105.backend.user.dto.JoinOrganizationDto;
import edu.ntnu.idatt2105.backend.user.dto.JoinRequestResponse;
import edu.ntnu.idatt2105.backend.user.dto.MemberDto;
import edu.ntnu.idatt2105.backend.user.dto.OrganizationResponse;
import edu.ntnu.idatt2105.backend.user.dto.JoinOrganizationRequest;
import edu.ntnu.idatt2105.backend.user.mapper.OrganizationMapper;
import edu.ntnu.idatt2105.backend.user.model.JoinRequestModel;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.RoleModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import edu.ntnu.idatt2105.backend.user.model.enums.JoinOrgStatus;
import edu.ntnu.idatt2105.backend.user.model.enums.RoleEnum;
import edu.ntnu.idatt2105.backend.user.repository.JoinRequestRepository;
import edu.ntnu.idatt2105.backend.user.repository.OrganizationRepository;
import edu.ntnu.idatt2105.backend.user.repository.RoleRepository;
import edu.ntnu.idatt2105.backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Business logic for creating and managing organisations and their membership.
 *
 * <p>When a new organisation is created the founding user is promoted to ADMIN
 * and a set of default regulatory documents is seeded. Membership follows a
 * request/accept flow: users submit a join-code request which an ADMIN or MANAGER
 * must resolve before the user appears as a member.
 */
@Service
@AllArgsConstructor
public class OrganizationService {
  private final OrganizationRepository organizationRepository;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final OrganizationMapper organizationMapper;
  private final JoinRequestRepository joinRequestRepository;
  private final DocumentRepository documentRepository;

  /**
   * Creates a new organisation, promotes the creator to ADMIN, and seeds default documents.
   *
   * @param request the organisation details
   * @param userId  the ID of the user creating the organisation
   * @return the newly created organisation
   */
  public OrganizationResponse create(CreateOrganizationRequest request, UUID userId) {
    UserModel user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    OrganizationModel org = new OrganizationModel();
    org.setName(request.getName());
    org.setJoinCode(generateJoinCode(request.getName()));
    OrganizationModel saved = organizationRepository.save(org);

    user.setOrganization(saved);

    RoleModel adminRole = roleRepository.findByName(RoleEnum.ADMIN)
        .orElseThrow(() ->
            new ResourceNotFoundException("ADMIN role not found, setup of tables must be wrong"));
    user.getRoles().add(adminRole);

    seedDefaultDocuments(saved, user);
    userRepository.save(user);

    return organizationMapper.toResponse(saved);
  }

  /**
   * Deletes any pending join requests for the given user.
   *
   * @param userId the user withdrawing their request
   * @throws edu.ntnu.idatt2105.backend.exception.ResourceNotFoundException if no pending request exists
   */
  public void withdrawJoinRequest(UUID userId) {
    UserModel user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    List<JoinRequestModel> pending = joinRequestRepository.findAllByUserAndStatus(user, JoinOrgStatus.PENDING);
    if (pending.isEmpty()) {
      throw new ResourceNotFoundException("No pending join request found");
    }
    joinRequestRepository.deleteAll(pending);
  }

  /**
   * Looks up an organisation by its join code so the user can preview it before requesting.
   *
   * @param code the join code entered by the user
   * @return the matching organisation
   * @throws edu.ntnu.idatt2105.backend.exception.ResourceNotFoundException if no organisation has that code
   */
  public OrganizationResponse lookupByCode(String code) {
    OrganizationModel org = organizationRepository.findByJoinCode(code)
        .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
    return organizationMapper.toResponse(org);
  }

  /**
   * Creates a pending join request for the given user and organisation.
   *
   * @param request contains the join code identifying the target organisation
   * @param userId  the user submitting the request
   * @return the organisation the user is requesting to join
   * @throws IllegalStateException if the user already belongs to an organisation or has a pending request
   */
  public OrganizationResponse requestToJoin(JoinOrganizationRequest request, UUID userId) {
    OrganizationModel org = organizationRepository.findByJoinCode(request.getJoinCode())
        .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

    UserModel user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (user.getOrganization() != null) {
      throw new IllegalStateException("User already belongs to an organization");
    }

    if (joinRequestRepository.existsByUserAndStatus(user, JoinOrgStatus.PENDING)) {
      throw new IllegalStateException("You already have a pending join request");
    }

    JoinRequestModel joinRequest = new JoinRequestModel();
    joinRequest.setUser(user);
    joinRequest.setOrganization(org);
    joinRequest.setStatus(JoinOrgStatus.PENDING);
    joinRequest.setCreatedAt(LocalDateTime.now());
    joinRequestRepository.save(joinRequest);

    return organizationMapper.toResponse(org);
  }

  /**
   * Seeds a set of default regulatory reference documents for a newly created organisation.
   *
   * <p>Includes links to the Norwegian Alcohol Act, Food Safety regulations, and other
   * relevant authorities so that managers have a starting document library.
   *
   * @param org     the organisation to seed documents for
   * @param creator the user who created the organisation (used as uploader)
   */
  private void seedDefaultDocuments(OrganizationModel org, UserModel creator) {
    record Seed(String name, String description, DocumentCategory category, DocumentModule module, String url) {}
    List<Seed> defaults = List.of(
        new Seed("Alkoholloven", "Lov om omsetning av alkoholholdig drikk (alkoholloven)", DocumentCategory.GUIDELINES, DocumentModule.IC_ALCOHOL, "https://lovdata.no/dokument/NL/lov/1989-06-02-27"),
        new Seed("Serveringsloven", "Lov om serveringsvirksomhet – krav til serveringsbevilling og styrer", DocumentCategory.GUIDELINES, DocumentModule.SHARED, "https://lovdata.no/dokument/NL/lov/1997-06-13-55"),
        new Seed("Internkontrollforskriften", "Forskrift om systematisk HMS-arbeid i virksomheter", DocumentCategory.GUIDELINES, DocumentModule.SHARED, "https://lovdata.no/dokument/SF/forskrift/1996-12-06-1127"),
        new Seed("Internkontroll og HACCP – Mattilsynet", "Veiledning om internkontroll og HACCP-basert styringssystem for næringsmiddelvirksomheter", DocumentCategory.HACCP, DocumentModule.IC_FOOD, "https://mattilsynet.no/mat-og-drikke/startpakke-for-nye-matbedrifter#kap_6_internkontroll"),
        new Seed("Krav til allergeninformasjon – Mattilsynet", "Regler for informasjon om allergener på meny og til gjester", DocumentCategory.GUIDELINES, DocumentModule.IC_FOOD, "https://www.mattilsynet.no/mat-og-drikke/merking-av-mat/slik-skal-allergenene-merkes"),
        new Seed("Ansvarlig vertskap – Helsedirektoratet", "Kompetansekrav og kursinfo for ansvarlig alkoholservering", DocumentCategory.TRAINING, DocumentModule.IC_ALCOHOL, "https://www.helsedirektoratet.no/forebygging-diagnose-og-behandling/forebygging-og-levevaner/alkohol/ansvarlig-alkoholhandtering"),
        new Seed("Bransjeveiviser – Arbeidstilsynet", "HMS-veiledning for serveringsbransjen: arbeidstid, ergonomi, kjemikalier", DocumentCategory.GUIDELINES, DocumentModule.SHARED, "https://arbeidsmiljohjelpen.arbeidstilsynet.no/bransje/servering/#:~:text=Arbeidstid%20*%20Skift.%20*%20Uforutsigbarhet.%20*%20Arbeidsplan.")
    );
    List<DocumentModel> docs = defaults.stream().map(s -> {
      DocumentModel doc = new DocumentModel();
      doc.setName(s.name());
      doc.setDescription(s.description());
      doc.setCategory(s.category());
      doc.setModule(s.module());
      doc.setExternalUrl(s.url());
      doc.setOrganization(org);
      doc.setUploadedBy(creator);
      return doc;
    }).toList();

    documentRepository.saveAll(docs);
  }

  /**
   * Generates a unique join code derived from the organisation name.
   *
   * <p>The code takes the form {@code ABC-1234} where the letters are the
   * initials of the first three words (or the first three characters if the
   * name has fewer than three words) and the digits are a random 4-digit number.
   *
   * @param name the organisation name
   * @return the generated join code
   */
  private String generateJoinCode(String name) {
    String[] words = name.trim().split("\\s+");
    StringBuilder prefix = new StringBuilder();
    for (String word : words) {
      if (!word.isEmpty()) prefix.append(Character.toUpperCase(word.charAt(0)));
      if (prefix.length() >= 3) break;
    }
    if (prefix.length() < 3) {
      String letters = name.replaceAll("\\s+", "").toUpperCase();
      prefix = new StringBuilder(letters.substring(0, Math.min(3, letters.length())));
    }
    int number = 1000 + new Random().nextInt(9000);
    return prefix + "-" + number;
  }

  /**
   * Accepts or declines a pending join request.
   *
   * <p>If accepted, the requesting user is added to the organisation. The request
   * is marked with the resolver's ID and timestamp regardless of the outcome.
   *
   * @param requestId      the ID of the join request to resolve
   * @param userId         the ID of the admin/manager resolving the request
   * @param organizationId the organisation the resolver belongs to (ownership check)
   * @param action         {@link JoinOrgStatus#ACCEPTED} or {@link JoinOrgStatus#DECLINED}
   */
  public void resolveRequest(UUID requestId, UUID userId, UUID organizationId, JoinOrgStatus action) {
    JoinRequestModel request = joinRequestRepository.findById(requestId)
        .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

    if (!request.getOrganization().getId().equals(organizationId)) {
      throw new IllegalStateException("Request does not belong to your organization");
    }
    if (request.getStatus() != JoinOrgStatus.PENDING) {
      throw new IllegalStateException("Request already resolved");
    }

    UserModel resolver = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (action == JoinOrgStatus.ACCEPTED) {
      UserModel requestUser = request.getUser();
      requestUser.setOrganization(request.getOrganization());
      userRepository.save(requestUser);
    }

    request.setStatus(action == JoinOrgStatus.ACCEPTED ? JoinOrgStatus.ACCEPTED : JoinOrgStatus.DECLINED);
    request.setResolvedAt(LocalDateTime.now());
    request.setResolvedBy(resolver);
    joinRequestRepository.save(request);
  }

  /**
   * Returns join requests for an organisation, optionally filtered by status.
   *
   * @param organizationId the organisation whose requests to list
   * @param status         optional status filter; if {@code null}, all requests are returned
   * @return list of join request DTOs
   */
  public List<JoinOrganizationDto> getRequests(UUID organizationId, JoinOrgStatus status) {
    List<JoinRequestModel> requests = status != null
        ? joinRequestRepository.findAllByOrganizationIdAndStatusWithUser(organizationId, status)
        : joinRequestRepository.findAllByOrganizationIdWithUser(organizationId);

    return requests.stream()
        .map(organizationMapper::toJoinRequestDto)
        .toList();
  }

  /**
   * Returns all members of an organisation.
   *
   * @param organizationId the target organisation
   * @return list of member DTOs
   */
  public List<MemberDto> getMembers(UUID organizationId) {
    return userRepository.findAllByOrganizationId(organizationId).stream()
        .map(organizationMapper::toMemberDto)
        .toList();
  }

  /**
   * Removes a member from an organisation and resets their role to STAFF.
   *
   * @param organizationId   the organisation from which to remove the member
   * @param targetUserId     the user to remove
   * @param requestingUserId the admin performing the action (cannot remove themselves)
   * @throws IllegalStateException if the requesting user tries to remove themselves
   */
  public void removeMember(UUID organizationId, UUID targetUserId, UUID requestingUserId) {
    if (targetUserId.equals(requestingUserId)) {
      throw new IllegalStateException("Cannot remove yourself from the organization");
    }

    UserModel user = requireOrgMember(targetUserId, organizationId);

    RoleModel staffRole = roleRepository.findByName(RoleEnum.STAFF)
        .orElseThrow(() -> new ResourceNotFoundException("STAFF role not found"));
    user.getRoles().clear();
    user.getRoles().add(staffRole);
    user.setOrganization(null);
    userRepository.save(user);
  }

  /**
   * Replaces the roles of an organisation member.
   *
   * <p>Guards against removing the last admin from an organisation to prevent lockout.
   *
   * @param organizationId   the organisation in which to update roles
   * @param targetUserId     the user whose roles to update
   * @param requestingUserId the admin performing the action (cannot update their own roles)
   * @param roleNames        the new role names to assign (must be valid {@link RoleEnum} names)
   * @throws IllegalStateException if the change would leave the organisation without an admin
   */
  public void updateMemberRoles(UUID organizationId, UUID targetUserId, UUID requestingUserId, List<String> roleNames) {
    if (targetUserId.equals(requestingUserId)) {
      throw new IllegalStateException("Cannot change your own roles");
    }

    UserModel user = requireOrgMember(targetUserId, organizationId);

    List<RoleModel> newRoles = roleNames.stream()
        .map(name -> roleRepository.findByName(RoleEnum.valueOf(name))
            .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + name)))
        .toList();

    boolean wouldRemoveLastAdmin = user.getRoles().stream()
        .anyMatch(r -> r.getName() == RoleEnum.ADMIN)
        && newRoles.stream().noneMatch(r -> r.getName() == RoleEnum.ADMIN);

    if (wouldRemoveLastAdmin && userRepository.countAdminsByOrganizationId(organizationId) <= 1) {
      throw new IllegalStateException("Cannot remove the last admin from the organization");
    }

    user.getRoles().clear();
    user.getRoles().addAll(newRoles);
    userRepository.save(user);
  }

  /**
   * Returns the user's current pending join request, or {@code null} if none exists.
   *
   * @param userId the user to check
   * @return the pending request response, or {@code null}
   */
  public JoinRequestResponse seeJoinRequest(UUID userId) {
    return joinRequestRepository.findFirstByUser_IdAndStatus(userId, JoinOrgStatus.PENDING)
        .map(organizationMapper::toJoinRequestResponse)
        .orElse(null);
  }

  /**
   * Loads a user by ID and verifies they belong to the given organisation.
   *
   * @param userId         the user to load
   * @param organizationId the expected organisation
   * @return the verified user entity
   * @throws IllegalStateException if the user does not belong to the organisation
   */
  private UserModel requireOrgMember(UUID userId, UUID organizationId) {
    UserModel user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    UUID userOrgId = user.getOrganization() != null ? user.getOrganization().getId() : null;
    if (!organizationId.equals(userOrgId)) {
      throw new IllegalStateException("User does not belong to your organization");
    }
    return user;
  }
}
