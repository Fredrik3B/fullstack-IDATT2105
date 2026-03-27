package edu.ntnu.idatt2105.backend.common.repository;

import edu.ntnu.idatt2105.backend.common.model.DeviationModel;
import org.springframework.data.jpa.repository.JpaRepository;


public interface DeviationRepository extends JpaRepository<DeviationModel, Long> {
}
