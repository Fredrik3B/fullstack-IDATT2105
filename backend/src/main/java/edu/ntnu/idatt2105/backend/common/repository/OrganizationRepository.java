package edu.ntnu.idatt2105.backend.common.repository;

import edu.ntnu.idatt2105.backend.common.model.OrganizationModel;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OrganizationRepository extends JpaRepository<OrganizationModel, Long> {
}
