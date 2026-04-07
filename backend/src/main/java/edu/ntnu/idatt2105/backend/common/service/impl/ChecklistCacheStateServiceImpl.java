package edu.ntnu.idatt2105.backend.common.service.impl;

import edu.ntnu.idatt2105.backend.common.model.ChecklistModuleState;
import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.common.repository.ChecklistModuleStateRepository;
import edu.ntnu.idatt2105.backend.common.service.ChecklistCacheStateService;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChecklistCacheStateServiceImpl implements ChecklistCacheStateService {

	private final ChecklistModuleStateRepository checklistModuleStateRepository;

	public ChecklistCacheStateServiceImpl(ChecklistModuleStateRepository checklistModuleStateRepository) {
		this.checklistModuleStateRepository = checklistModuleStateRepository;
	}

	@Override
	@Transactional
	public Instant getLastModified(UUID organizationId, ComplianceArea complianceArea) {
		UUID safeOrganizationId = Objects.requireNonNull(organizationId, "organizationId is required.");
		ComplianceArea safeComplianceArea = Objects.requireNonNull(complianceArea, "complianceArea is required.");

		ChecklistModuleState state = checklistModuleStateRepository
			.findByOrganizationIdAndComplianceArea(safeOrganizationId, safeComplianceArea)
			.orElseGet(() -> createState(safeOrganizationId, safeComplianceArea, currentHttpSecond()));

		if (state.getModifiedAt() == null) {
			state.setModifiedAt(currentHttpSecond());
			state = checklistModuleStateRepository.save(state);
		}

		return state.getModifiedAt().atZone(ZoneId.systemDefault()).toInstant();
	}

	@Override
	@Transactional
	public void touch(UUID organizationId, ComplianceArea complianceArea) {
		UUID safeOrganizationId = Objects.requireNonNull(organizationId, "organizationId is required.");
		ComplianceArea safeComplianceArea = Objects.requireNonNull(complianceArea, "complianceArea is required.");

		ChecklistModuleState state = checklistModuleStateRepository
			.findByOrganizationIdAndComplianceArea(safeOrganizationId, safeComplianceArea)
			.orElseGet(() -> createState(safeOrganizationId, safeComplianceArea, currentHttpSecond()));

		state.setModifiedAt(nextModifiedAt(state.getModifiedAt()));
		checklistModuleStateRepository.save(state);
	}

	private ChecklistModuleState createState(UUID organizationId, ComplianceArea complianceArea, LocalDateTime modifiedAt) {
		ChecklistModuleState created = new ChecklistModuleState();
		created.setOrganizationId(organizationId);
		created.setComplianceArea(complianceArea);
		created.setModifiedAt(modifiedAt);
		return checklistModuleStateRepository.save(created);
	}

	private LocalDateTime nextModifiedAt(LocalDateTime previousModifiedAt) {
		LocalDateTime currentSecond = currentHttpSecond();
		if (previousModifiedAt == null) {
			return currentSecond;
		}

		LocalDateTime previousSecond = previousModifiedAt.truncatedTo(ChronoUnit.SECONDS);
		if (currentSecond.isAfter(previousSecond)) {
			return currentSecond;
		}
		return previousSecond.plusSeconds(1);
	}

	private LocalDateTime currentHttpSecond() {
		return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
	}
}
