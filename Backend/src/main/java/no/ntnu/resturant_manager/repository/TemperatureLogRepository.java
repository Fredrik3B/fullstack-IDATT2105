package no.ntnu.resturant_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import no.ntnu.resturant_manager.model.TemperatureLogModel;

public interface TemperatureLogRepository extends JpaRepository<TemperatureLogModel, Long> {
}
