package edu.ntnu.idatt2105.backend.user.service;

import edu.ntnu.idatt2105.backend.common.model.DocumentModel;
import edu.ntnu.idatt2105.backend.common.model.enums.DocumentCategory;
import edu.ntnu.idatt2105.backend.common.model.enums.DocumentModule;
import edu.ntnu.idatt2105.backend.common.repository.DocumentRepository;
import edu.ntnu.idatt2105.backend.exception.ResourceNotFoundException;
import edu.ntnu.idatt2105.backend.user.dto.CreateOrganizationRequest;
import edu.ntnu.idatt2105.backend.user.dto.JoinOrganizationDto;
import edu.ntnu.idatt2105.backend.user.dto.OrganizationResponse;
import edu.ntnu.idatt2105.backend.user.dto.JoinOrganizationRequest;
import edu.ntnu.idatt2105.backend.user.dto.ResolveJoinRequest;
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
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

//TODO: request new access token
@Service
@AllArgsConstructor
public class OrganizationService {
  private final OrganizationRepository organizationRepository;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final OrganizationMapper organizationMapper;
  private final JoinRequestRepository joinRequestRepository;
  private final DocumentRepository documentRepository;

  public OrganizationResponse create(CreateOrganizationRequest request, UUID userId) {
    OrganizationModel org = new OrganizationModel();
    org.setName(request.getName());
    org.setJoinCode(generateJoinCode(request.getName()));
    OrganizationModel saved = organizationRepository.save(org);

    UserModel user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    user.setOrganization(saved);

    seedDefaultDocuments(saved, user);

    RoleModel adminRole = roleRepository.findByName(RoleEnum.ADMIN)
        .orElseThrow(() ->
            new ResourceNotFoundException("ADMIN role not found, setup of tables must be wrong"));
    user.getRoles().add(adminRole);
    userRepository.save(user);

    return organizationMapper.toResponse(org);
  }

  public void withdrawJoinRequest(UUID userId) {
    List<JoinRequestModel> pending = joinRequestRepository.findAllByUserIdAndStatus(userId, JoinOrgStatus.PENDING);
    if (pending.isEmpty()) {
      throw new ResourceNotFoundException("No pending join request found");
    }
    joinRequestRepository.deleteAll(pending);
  }

  public OrganizationResponse lookupByCode(String code) {
    OrganizationModel org = organizationRepository.findByJoinCode(code)
        .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
    return organizationMapper.toResponse(org);
  }

  // TODO: just cast to user instead of lookup?
  public OrganizationResponse requestToJoin(JoinOrganizationRequest request, UUID userId) {
    OrganizationModel org = organizationRepository.findByJoinCode(request.getJoinCode())
        .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

    UserModel user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (user.getOrganization() != null) {
      throw new RuntimeException("User already belongs to an organization");
    }

    if (joinRequestRepository.existsByUserIdAndStatus(userId, JoinOrgStatus.PENDING)) {
      throw new RuntimeException("You already have a pending join request");
    }

    JoinRequestModel joinRequest = new JoinRequestModel();
    joinRequest.setUserId(userId);
    joinRequest.setOrganizationId(org.getId());
    joinRequest.setStatus(JoinOrgStatus.PENDING);

    joinRequest.setCreatedAt(LocalDateTime.now());

    joinRequestRepository.save(joinRequest);

    return organizationMapper.toResponse(org);
  }

  private void seedDefaultDocuments(OrganizationModel org, UserModel creator) {
    record Seed(String name, String description, DocumentCategory category, DocumentModule module, String url) {}
    List<Seed> defaults = List.of(
        new Seed("Alkoholloven", "Lov om omsetning av alkoholholdig drikk (alkoholloven)", DocumentCategory.GUIDELINES, DocumentModule.IC_ALCOHOL, "https://lovdata.no/dokument/NL/lov/1989-06-02-27"),
        new Seed("Serveringsloven", "Lov om serveringsvirksomhet – krav til serveringsbevilling og styrer", DocumentCategory.GUIDELINES, DocumentModule.SHARED, "https://lovdata.no/dokument/NL/lov/1997-06-13-55"),
        new Seed("Internkontrollforskriften", "Forskrift om systematisk HMS-arbeid i virksomheter", DocumentCategory.GUIDELINES, DocumentModule.SHARED, "https://lovdata.no/dokument/SF/forskrift/1996-12-06-1127"),
        new Seed("Internkontroll og HACCP – Mattilsynet", "Veiledning om internkontroll og HACCP-basert styringssystem for næringsmiddelvirksomheter", DocumentCategory.HACCP, DocumentModule.IC_FOOD, "https://mattilsynet.no/mat-og-drikke/startpakke-for-nye-matbedrifter#kap_6_internkontroll"),
        new Seed("Krav til allergeninformasjon – Mattilsynet", "Regler for informasjon om allergener på meny og til gjester", DocumentCategory.GUIDELINES, DocumentModule.IC_FOOD, "https://www.mattilsynet.no/mat-og-drikke/merking-av-mat/slik-skal-allergenene-merkes#kap-5-merking-av-kan-inneholde-spor-av"),
        new Seed("Ansvarlig vertskap – Helsedirektoratet", "Kompetansekrav og kursinfo for ansvarlig alkoholservering", DocumentCategory.TRAINING, DocumentModule.IC_ALCOHOL, "https://www.helsedirektoratet.no/forebygging-diagnose-og-behandling/forebygging-og-levevaner/alkohol/ansvarlig-alkoholhandtering"),
        new Seed("Bransjeveiviser – Arbeidstilsynet", "HMS-veiledning for serveringsbransjen: arbeidstid, ergonomi, kjemikalier", DocumentCategory.GUIDELINES, DocumentModule.SHARED, "https://arbeidsmiljohjelpen.arbeidstilsynet.no/bransje/servering/#:~:text=Arbeidstid%20*%20Skift.%20*%20Uforutsigbarhet.%20*%20Arbeidsplan.")
    );

    for (Seed s : defaults) {
      DocumentModel doc = new DocumentModel();
      doc.setName(s.name());
      doc.setDescription(s.description());
      doc.setCategory(s.category());
      doc.setModule(s.module());
      doc.setExternalUrl(s.url());
      doc.setOrganization(org);
      doc.setUploadedBy(creator);
      documentRepository.save(doc);
    }
  }

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

  // TODO: fix exceptions
  public void resolveRequest(UUID requestId, UUID userId, UUID organizationId, JoinOrgStatus action) {
    JoinRequestModel request = joinRequestRepository.findById(requestId)
        .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
    if (!request.getOrganizationId().equals(organizationId)) {
      throw new RuntimeException("Request does not belong to your organization");
    }
    if (request.getStatus() != JoinOrgStatus.PENDING) {
      throw new RuntimeException("Request already resolved");
    }


    if (action == JoinOrgStatus.ACCEPTED) {
      UserModel user = userRepository.findById(request.getUserId())
          .orElseThrow(() -> new ResourceNotFoundException("User not found"));
      OrganizationModel org = organizationRepository.findById(request.getOrganizationId())
          .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

      user.setOrganization(org);
      userRepository.save(user);
    }

    request.setStatus(action == JoinOrgStatus.ACCEPTED
        ? JoinOrgStatus.ACCEPTED : JoinOrgStatus.DECLINED);
    request.setResolvedAt(LocalDateTime.now());
    request.setResolvedBy(userId);
    joinRequestRepository.save(request);
  }

  public List<JoinOrganizationDto> getRequests(UUID organizationId, JoinOrgStatus status) {
    List<JoinRequestModel> requests;
    if (status != null) {
      requests = joinRequestRepository.findAllByOrganizationIdAndStatus(organizationId, status);
    } else {
      requests = joinRequestRepository.findAllByOrganizationId(organizationId);
    }
    return requests.stream()
        .map(request -> {
          UserModel user = userRepository.findById(request.getUserId())
              .orElseThrow(() -> new ResourceNotFoundException("User not found"));
          return organizationMapper.toJoinRequestDto(request, user);
        })
        .toList();
  }
}
