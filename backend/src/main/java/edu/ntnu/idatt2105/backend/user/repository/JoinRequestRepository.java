package edu.ntnu.idatt2105.backend.user.repository;

import edu.ntnu.idatt2105.backend.user.model.JoinRequestModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import edu.ntnu.idatt2105.backend.user.model.enums.JoinOrgStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * JPA repository for {@link JoinRequestModel}.
 *
 * <p>Custom JPQL queries use {@code JOIN FETCH} on the user association to
 * avoid N+1 queries when listing pending requests for an organisation.
 */
public interface JoinRequestRepository extends JpaRepository<JoinRequestModel, UUID> {

  boolean existsByUserAndStatus(UserModel user, JoinOrgStatus status);

  List<JoinRequestModel> findAllByUserAndStatus(UserModel user, JoinOrgStatus status);

  @Query("SELECT j FROM JoinRequestModel j JOIN FETCH j.user WHERE j.organization.id = :orgId AND j.status = :status")
  List<JoinRequestModel> findAllByOrganizationIdAndStatusWithUser(@Param("orgId") UUID orgId,
      @Param("status") JoinOrgStatus status);

  @Query("SELECT j FROM JoinRequestModel j JOIN FETCH j.user WHERE j.organization.id = :orgId")
  List<JoinRequestModel> findAllByOrganizationIdWithUser(@Param("orgId") UUID orgId);

  Optional<JoinRequestModel> findFirstByUser_IdAndStatus(UUID userId, JoinOrgStatus status);
}
