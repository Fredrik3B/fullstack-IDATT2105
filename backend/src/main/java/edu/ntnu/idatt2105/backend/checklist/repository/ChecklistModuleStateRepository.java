package edu.ntnu.idatt2105.backend.checklist.repository;

import edu.ntnu.idatt2105.backend.checklist.model.ChecklistModuleState;
import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository for {@link ChecklistModuleState}.
 *
 * <p>Looked up by the unique {@code (organization_id, compliance_area)} pair to retrieve
 * or update the last-modified timestamp for HTTP conditional requests.
 */
public interface ChecklistModuleStateRepository extends JpaRepository<ChecklistModuleState, Long> {

	Optional<ChecklistModuleState> findByOrganizationIdAndComplianceArea(UUID organizationId, ComplianceArea complianceArea);
}
