package edu.ntnu.idatt2105.backend.common.repository;

import edu.ntnu.idatt2105.backend.common.model.ChecklistModel;
import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ChecklistRepository extends JpaRepository<ChecklistModel, Long> {
	List<ChecklistModel> findAllByOrganization_IdOrderByIdAsc(UUID organizationId);

	List<ChecklistModel> findAllByOrganization_IdAndComplianceAreaAndActiveTrueOrderByIdAsc(
		UUID organizationId,
		ComplianceArea complianceArea
	);

	Optional<ChecklistModel> findByIdAndOrganization_Id(Long checklistId, UUID organizationId);
}
