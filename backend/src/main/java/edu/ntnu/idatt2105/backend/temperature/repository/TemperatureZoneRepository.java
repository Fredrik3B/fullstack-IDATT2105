package edu.ntnu.idatt2105.backend.temperature.repository;

import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.temperature.model.TemperatureZoneModel;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository for {@link TemperatureZoneModel}.
 *
 * <p>Provides organisation-scoped lookup methods used by
 * {@link edu.ntnu.idatt2105.backend.temperature.service.TemperatureZoneService}.
 */
public interface TemperatureZoneRepository extends JpaRepository<TemperatureZoneModel, Long> {

  List<TemperatureZoneModel> findAllByOrganizationIdAndComplianceAreaOrderByZoneTypeAscNameAsc(
      UUID organizationId,
      ComplianceArea complianceArea
  );

  Optional<TemperatureZoneModel> findByIdAndOrganizationIdAndComplianceArea(
      Long id,
      UUID organizationId,
      ComplianceArea complianceArea
  );

  Optional<TemperatureZoneModel> findByIdAndOrganizationId(Long id, UUID organizationId);
}
