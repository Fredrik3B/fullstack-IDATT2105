package edu.ntnu.idatt2105.backend.checklist.repository;

import edu.ntnu.idatt2105.backend.checklist.model.ChecklistModuleState;
import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChecklistModuleStateRepository extends JpaRepository<ChecklistModuleState, Long> {

	Optional<ChecklistModuleState> findByOrganizationIdAndComplianceArea(UUID organizationId, ComplianceArea complianceArea);
}
