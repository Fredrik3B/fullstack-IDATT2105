package edu.ntnu.idatt2105.backend.user.repository;

import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserModel, UUID> {
  Optional<UserModel> findByEmail(String email);

  List<UserModel> findAllByOrganization(OrganizationModel org);

  List<UserModel> findAllByOrganizationId(UUID organizationId);
}
