package no.ntnu.resturant_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import no.ntnu.resturant_manager.model.DeviationModel;

public interface DeviationRepository extends JpaRepository<DeviationModel, Long> {
}
