package edu.ntnu.idatt2105.backend.user.repository;

import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * JPA repository for {@link UserModel}.
 *
 * <p>Spring Data generates implementations for all derived query methods at startup.
 */
public interface UserRepository extends JpaRepository<UserModel, UUID> {

  Optional<UserModel> findByEmail(String email);

  List<UserModel> findAllByOrganization(OrganizationModel org);

  List<UserModel> findAllByOrganizationId(UUID organizationId);

  @Query("SELECT COUNT(u) FROM UserModel u JOIN u.roles r WHERE u.organization.id = :orgId AND r.name = 'ADMIN'")
  long countAdminsByOrganizationId(@Param("orgId") UUID orgId);
}
