package no.ntnu.resturant_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import no.ntnu.resturant_manager.model.AlcoholComplianceModel;

public interface AlcoholComplianceRepository extends JpaRepository<AlcoholComplianceModel, Long> {
}
