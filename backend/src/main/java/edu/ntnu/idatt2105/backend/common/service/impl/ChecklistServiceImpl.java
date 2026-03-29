package edu.ntnu.idatt2105.backend.common.service.impl;

import edu.ntnu.idatt2105.backend.common.dto.checklist.ChecklistResponse;
import edu.ntnu.idatt2105.backend.common.dto.checklist.CreateChecklistRequest;
import edu.ntnu.idatt2105.backend.common.model.ChecklistModel;
import edu.ntnu.idatt2105.backend.common.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.common.repository.ChecklistRepository;
import edu.ntnu.idatt2105.backend.common.repository.OrganizationRepository;
import edu.ntnu.idatt2105.backend.common.service.ChecklistService;
import java.util.List;

import org.springframework.stereotype.Service;



@Service
public class ChecklistServiceImpl implements ChecklistService {

	private final ChecklistRepository checklistRepository;
	private final OrganizationRepository organizationRepository;

	public ChecklistServiceImpl(
		ChecklistRepository checklistRepository,
		OrganizationRepository organizationRepository
	) {
		this.checklistRepository = checklistRepository;
		this.organizationRepository = organizationRepository;
	}

	@Override
	public ChecklistResponse createChecklist(CreateChecklistRequest request) {
		validateRequest(request);

		OrganizationModel organization = organizationRepository.findById(request.organizationId())
			.orElseThrow(() -> new IllegalArgumentException("Organization not found with id: " + request.organizationId()));

		ChecklistModel checklist = new ChecklistModel();
		checklist.setName(request.name().trim());
		checklist.setDescription(trimToNull(request.description()));
		checklist.setFrequency(request.frequency());
		checklist.setComplianceArea(request.complianceArea());
		checklist.setActive(request.active() == null || request.active());
		checklist.setOrganization(organization);

		return toResponse(checklistRepository.save(checklist));
	}

	@Override
	public List<ChecklistResponse> getAllChecklists() {
		return checklistRepository.findAll().stream()
			.map(this::toResponse)
			.toList();
	}

	@Override
	public ChecklistResponse getChecklistById(Long checklistId) {
		ChecklistModel checklist = checklistRepository.findById(checklistId)
			.orElseThrow(() -> new IllegalArgumentException("Checklist not found with id: " + checklistId));

		return toResponse(checklist);
	}

	@Override
	public void deleteChecklist(Long checklistId) {
		if (!checklistRepository.existsById(checklistId)) {
			throw new IllegalArgumentException("Checklist not found with id: " + checklistId);
		}
		checklistRepository.deleteById(checklistId);
	}

	private ChecklistResponse toResponse(ChecklistModel checklist) {
		return new ChecklistResponse(
			checklist.getId(),
			checklist.getName(),
			checklist.getDescription(),
			checklist.getFrequency(),
			checklist.getComplianceArea(),
			checklist.isActive(),
			checklist.getOrganization() != null ? checklist.getOrganization().getId() : null
		);
	}

	private void validateRequest(CreateChecklistRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Checklist request cannot be null.");
		}
		if (!hasText(request.name())) {
			throw new IllegalArgumentException("Checklist name is required.");
		}
		if (request.frequency() == null) {
			throw new IllegalArgumentException("Checklist frequency is required.");
		}
		if (request.complianceArea() == null) {
			throw new IllegalArgumentException("Checklist compliance area is required.");
		}
		if (request.organizationId() == null) {
			throw new IllegalArgumentException("Organization id is required.");
		}
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private String trimToNull(String value) {
		return hasText(value) ? value.trim() : null;
	}
}
