package edu.ntnu.idatt2105.backend.user.repository;

import edu.ntnu.idatt2105.backend.user.model.JoinRequestModel;
import edu.ntnu.idatt2105.backend.user.model.enums.JoinOrgStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JoinRequestRepository extends JpaRepository<JoinRequestModel, UUID> {

  boolean existsByUserIdAndOrganizationIdAndStatus(
      UUID userId, UUID organizationId, JoinOrgStatus status);

  List<JoinRequestModel> findAllByOrganizationIdAndStatus(
      UUID organizationId, JoinOrgStatus status);

  List<JoinRequestModel> findAllByOrganizationId(void attr0);
}
