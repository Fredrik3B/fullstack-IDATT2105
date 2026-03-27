package edu.ntnu.idatt2105.backend.common.repository;

import edu.ntnu.idatt2105.backend.common.model.AlcoholComplianceModel;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AlcoholComplianceRepository extends JpaRepository<AlcoholComplianceModel, Long> {
}
