package edu.ntnu.idatt2105.backend.user.repository;

import edu.ntnu.idatt2105.backend.user.model.JoinRequestModel;
import edu.ntnu.idatt2105.backend.user.model.enums.JoinOrgStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JoinRequestRepository extends JpaRepository<JoinRequestModel, UUID> {

  Optional<JoinRequestModel> findFirstByUserIdAndStatus(UUID userId, JoinOrgStatus status);

  boolean existsByUserIdAndStatus(UUID userId, JoinOrgStatus status);

  boolean existsByUserIdAndOrganizationIdAndStatus(
      UUID userId, UUID organizationId, JoinOrgStatus status);

  List<JoinRequestModel> findAllByUserIdAndStatus(UUID userId, JoinOrgStatus status);

  List<JoinRequestModel> findAllByOrganizationIdAndStatus(
      UUID organizationId, JoinOrgStatus status);

  List<JoinRequestModel> findAllByOrganizationId(UUID organizationId);
}
