package edu.ntnu.idatt2105.backend.common.repository;

import edu.ntnu.idatt2105.backend.common.model.TemperatureLogModel;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TemperatureLogRepository extends JpaRepository<TemperatureLogModel, Long> {
}
