package edu.ntnu.idatt2105.backend.user.repository;

import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<OrganizationModel, UUID> {

  Optional<OrganizationModel> findByJoinCode(String joinCode);
}
