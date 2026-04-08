package edu.ntnu.idatt2105.backend.user.repository;

import edu.ntnu.idatt2105.backend.user.model.JoinRequestModel;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import edu.ntnu.idatt2105.backend.user.model.enums.JoinOrgStatus;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserModel, UUID> {
  Optional<UserModel> findByEmail(String email);

  List<UserModel> findAllByOrganization(OrganizationModel org);

  List<UserModel> findAllByOrganizationId(UUID organizationId);


  @Query("SELECT j FROM JoinRequestModel j JOIN FETCH j.user WHERE j.organization.id = :orgId AND j.status = :status")
  List<JoinRequestModel> findAllByOrganizationIdAndStatusWithUser(@Param("orgId") UUID orgId, @Param("status") JoinOrgStatus status);

  @Query("SELECT COUNT(u) FROM UserModel u JOIN u.roles r WHERE u.organization.id = :orgId AND r.name = 'ADMIN'")
  long countAdminsByOrganizationId(@Param("orgId") UUID orgId);
}
