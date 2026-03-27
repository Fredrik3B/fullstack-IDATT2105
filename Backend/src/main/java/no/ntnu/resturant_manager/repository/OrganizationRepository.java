package no.ntnu.resturant_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import no.ntnu.resturant_manager.model.OrganizationModel;

public interface OrganizationRepository extends JpaRepository<OrganizationModel, Long> {
}
