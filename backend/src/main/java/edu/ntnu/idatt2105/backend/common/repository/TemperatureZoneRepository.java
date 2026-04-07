package edu.ntnu.idatt2105.backend.common.repository;

import edu.ntnu.idatt2105.backend.common.model.TemperatureZoneModel;
import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

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
