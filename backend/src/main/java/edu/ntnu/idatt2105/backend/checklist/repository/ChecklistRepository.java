package edu.ntnu.idatt2105.backend.checklist.repository;

import edu.ntnu.idatt2105.backend.checklist.model.ChecklistModel;
import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * JPA repository for {@link ChecklistModel}.
 *
 * <p>All list queries are ordered by ID ascending so that the order seen in the UI
 * is deterministic and matches creation order.
 */
public interface ChecklistRepository extends JpaRepository<ChecklistModel, Long> {
	List<ChecklistModel> findAllByOrganization_IdOrderByIdAsc(UUID organizationId);

	List<ChecklistModel> findAllByOrganization_IdAndComplianceAreaAndActiveTrueOrderByIdAsc(
		UUID organizationId,
		ComplianceArea complianceArea
	);

	Optional<ChecklistModel> findByIdAndOrganization_Id(Long checklistId, UUID organizationId);

  List<ChecklistModel> findAllByOrganizationId(UUID orgId);
}
