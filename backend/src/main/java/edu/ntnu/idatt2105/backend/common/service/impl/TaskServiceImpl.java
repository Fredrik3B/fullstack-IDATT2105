package edu.ntnu.idatt2105.backend.common.service.impl;

import edu.ntnu.idatt2105.backend.common.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.common.dto.task.CreateTaskRequest;
import edu.ntnu.idatt2105.backend.common.dto.task.TaskResponse;
import edu.ntnu.idatt2105.backend.common.model.TaskTemplate;
import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.common.repository.TaskTemplateRepository;
import edu.ntnu.idatt2105.backend.common.service.TaskService;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TaskServiceImpl implements TaskService {

	private final TaskTemplateRepository taskTemplateRepository;

	public TaskServiceImpl(TaskTemplateRepository taskTemplateRepository) {
		this.taskTemplateRepository = taskTemplateRepository;
	}

	@Override
	public TaskResponse createTask(CreateTaskRequest request, JwtAuthenticatedPrincipal principal) {
		JwtAuthenticatedPrincipal safePrincipal = requirePrincipal(principal);
		validateRequest(request);
		boolean isTemperatureControl = request.sectionType() == edu.ntnu.idatt2105.backend.common.model.enums.SectionTypes.TEMPERATURE_CONTROL;

		TaskTemplate template = new TaskTemplate();
		template.setTitle(request.title().trim());
		template.setSectionType(request.sectionType());
		template.setComplianceArea(requireModule(request.module()).toComplianceArea());
		template.setUnit(isTemperatureControl ? "C" : null);
		template.setTargetMin(isTemperatureControl ? request.targetMin() : null);
		template.setTargetMax(isTemperatureControl ? request.targetMax() : null);
		template.setOrganisationId(safePrincipal.getOrganizationId());

		return toResponse(taskTemplateRepository.save(template));
	}

	@Override
	public List<TaskResponse> getAllTasks(IcModule module, JwtAuthenticatedPrincipal principal) {
		JwtAuthenticatedPrincipal safePrincipal = requirePrincipal(principal);
		ComplianceArea complianceArea = requireModule(module).toComplianceArea();

		return taskTemplateRepository
			.findAllByOrganisationIdAndComplianceAreaOrderBySectionTypeAscTitleAsc(safePrincipal.getOrganizationId(), complianceArea)
			.stream()
			.map(this::toResponse)
			.toList();
	}

	@Override
	public TaskResponse getTaskById(Long taskId, JwtAuthenticatedPrincipal principal) {
		TaskTemplate template = getTemplate(taskId, requirePrincipal(principal));
		return toResponse(template);
	}

	@Override
	public void deleteTask(Long taskId, JwtAuthenticatedPrincipal principal) {
		TaskTemplate template = getTemplate(taskId, requirePrincipal(principal));
		taskTemplateRepository.delete(template);
	}

	private TaskTemplate getTemplate(Long taskId, JwtAuthenticatedPrincipal principal) {
		TaskTemplate template = taskTemplateRepository.findById(taskId)
			.orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));
		if (!template.getOrganisationId().equals(principal.getOrganizationId())) {
			throw new IllegalArgumentException("Task not found with id: " + taskId);
		}
		return template;
	}

	private TaskResponse toResponse(TaskTemplate template) {
		return new TaskResponse(
			template.getId(),
			toModule(template.getComplianceArea()),
			template.getTitle(),
			template.getSectionType(),
			template.getUnit(),
			template.getTargetMin(),
			template.getTargetMax()
		);
	}

	private JwtAuthenticatedPrincipal requirePrincipal(JwtAuthenticatedPrincipal principal) {
		if (principal == null) throw new IllegalArgumentException("Authentication required.");
		if (principal.getOrganizationId() == null) throw new IllegalArgumentException("Organization required.");
		return principal;
	}

	private IcModule requireModule(IcModule module) {
		if (module == null) throw new IllegalArgumentException("module is required.");
		return module;
	}

	private void validateRequest(CreateTaskRequest request) {
		if (request == null) throw new IllegalArgumentException("Task request cannot be null.");
		if (!hasText(request.title())) throw new IllegalArgumentException("Task title is required.");
		if (request.module() == null) throw new IllegalArgumentException("module is required.");
		if (request.sectionType() == null) throw new IllegalArgumentException("sectionType is required.");
		boolean isTemperatureControl = request.sectionType() == edu.ntnu.idatt2105.backend.common.model.enums.SectionTypes.TEMPERATURE_CONTROL;
		if (!isTemperatureControl && (request.targetMin() != null || request.targetMax() != null)) {
			throw new IllegalArgumentException("targetMin and targetMax are only allowed for TEMPERATURE_CONTROL.");
		}
		if (request.targetMin() != null && request.targetMax() != null
			&& request.targetMin().compareTo(request.targetMax()) > 0) {
			throw new IllegalArgumentException("targetMin cannot be greater than targetMax.");
		}
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private IcModule toModule(ComplianceArea complianceArea) {
		if (complianceArea == ComplianceArea.IK_ALKOHOL) {
			return IcModule.IC_ALCOHOL;
		}
		return IcModule.IC_FOOD;
	}
}
