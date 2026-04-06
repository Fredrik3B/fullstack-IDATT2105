package edu.ntnu.idatt2105.backend.common.service.impl;

import edu.ntnu.idatt2105.backend.common.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.common.dto.task.CreateTaskRequest;
import edu.ntnu.idatt2105.backend.common.dto.task.TaskResponse;
import edu.ntnu.idatt2105.backend.common.model.ChecklistModel;
import edu.ntnu.idatt2105.backend.common.model.TaskTemplate;
import edu.ntnu.idatt2105.backend.common.model.TemperatureZoneModel;
import edu.ntnu.idatt2105.backend.common.model.TasksModel;
import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.common.repository.ChecklistRepository;
import edu.ntnu.idatt2105.backend.common.repository.TaskTemplateRepository;
import edu.ntnu.idatt2105.backend.common.repository.TemperatureMeasurementRepository;
import edu.ntnu.idatt2105.backend.common.repository.TemperatureZoneRepository;
import edu.ntnu.idatt2105.backend.common.repository.TasksRepository;
import edu.ntnu.idatt2105.backend.common.service.TaskService;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskServiceImpl implements TaskService {

	private final TaskTemplateRepository taskTemplateRepository;
	private final ChecklistRepository checklistRepository;
	private final TasksRepository tasksRepository;
	private final TemperatureMeasurementRepository temperatureMeasurementRepository;
	private final TemperatureZoneRepository temperatureZoneRepository;

	public TaskServiceImpl(
		TaskTemplateRepository taskTemplateRepository,
		ChecklistRepository checklistRepository,
		TasksRepository tasksRepository,
		TemperatureMeasurementRepository temperatureMeasurementRepository,
		TemperatureZoneRepository temperatureZoneRepository
	) {
		this.taskTemplateRepository = taskTemplateRepository;
		this.checklistRepository = checklistRepository;
		this.tasksRepository = tasksRepository;
		this.temperatureMeasurementRepository = temperatureMeasurementRepository;
		this.temperatureZoneRepository = temperatureZoneRepository;
	}

	@Override
	public TaskResponse createTask(CreateTaskRequest request, JwtAuthenticatedPrincipal principal) {
		JwtAuthenticatedPrincipal safePrincipal = requirePrincipal(principal);
		validateRequest(request);
		boolean isTemperatureControl = request.sectionType() == edu.ntnu.idatt2105.backend.common.model.enums.SectionTypes.TEMPERATURE_CONTROL;
		TemperatureZoneModel temperatureZone = isTemperatureControl
			? getTemperatureZone(request.temperatureZoneId(), safePrincipal.getOrganizationId(), requireModule(request.module()).toComplianceArea())
			: null;

		TaskTemplate template = new TaskTemplate();
		template.setTitle(request.title().trim());
		template.setMeta(trimToNull(request.meta()));
		template.setSectionType(request.sectionType());
		template.setComplianceArea(requireModule(request.module()).toComplianceArea());
		template.setTemperatureZone(temperatureZone);
		template.setUnit(isTemperatureControl ? "C" : null);
		template.setTargetMin(isTemperatureControl ? temperatureZone.getTargetMin() : null);
		template.setTargetMax(isTemperatureControl ? temperatureZone.getTargetMax() : null);
		template.setOrganisationId(safePrincipal.getOrganizationId());

		return toResponse(taskTemplateRepository.save(template));
	}

	@Override
	@Transactional
	public TaskResponse updateTask(Long taskId, CreateTaskRequest request, JwtAuthenticatedPrincipal principal) {
		JwtAuthenticatedPrincipal safePrincipal = requirePrincipal(principal);
		validateRequest(request);
		TaskTemplate template = getTemplate(taskId, safePrincipal);
		boolean isTemperatureControl = request.sectionType() == edu.ntnu.idatt2105.backend.common.model.enums.SectionTypes.TEMPERATURE_CONTROL;
		TemperatureZoneModel temperatureZone = isTemperatureControl
			? getTemperatureZone(request.temperatureZoneId(), safePrincipal.getOrganizationId(), requireModule(request.module()).toComplianceArea())
			: null;

		template.setTitle(request.title().trim());
		template.setMeta(trimToNull(request.meta()));
		template.setSectionType(request.sectionType());
		template.setComplianceArea(requireModule(request.module()).toComplianceArea());
		template.setTemperatureZone(temperatureZone);
		template.setUnit(isTemperatureControl ? "C" : null);
		template.setTargetMin(isTemperatureControl ? temperatureZone.getTargetMin() : null);
		template.setTargetMax(isTemperatureControl ? temperatureZone.getTargetMax() : null);

		List<TasksModel> activatedTasks = tasksRepository.findAllByTaskTemplate_Id(taskId);
		for (TasksModel task : activatedTasks) {
			task.setMeta(template.getMeta());
		}
		if (!activatedTasks.isEmpty()) {
			tasksRepository.saveAll(activatedTasks);
		}

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
	@Transactional
	public void deleteTask(Long taskId, JwtAuthenticatedPrincipal principal) {
		JwtAuthenticatedPrincipal safePrincipal = requirePrincipal(principal);
		TaskTemplate template = getTemplate(taskId, safePrincipal);

		List<ChecklistModel> checklists = checklistRepository.findAllByOrganization_IdOrderByIdAsc(safePrincipal.getOrganizationId());
		for (ChecklistModel checklist : checklists) {
			if (checklist.getTaskTemplates().removeIf(task -> template.getId().equals(task.getId()))) {
				checklistRepository.save(checklist);
			}
		}

		List<TasksModel> activatedTasks = tasksRepository.findAllByTaskTemplate_Id(taskId);
		if (!activatedTasks.isEmpty()) {
			temperatureMeasurementRepository.deleteAllByTaskIn(activatedTasks);
			tasksRepository.deleteAll(activatedTasks);
		}

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
			template.getMeta(),
			template.getSectionType(),
			template.getTemperatureZone() != null ? template.getTemperatureZone().getId() : null,
			template.getTemperatureZone() != null ? template.getTemperatureZone().getName() : null,
			template.getTemperatureZone() != null ? template.getTemperatureZone().getZoneType() : null,
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
		if (!isTemperatureControl && request.temperatureZoneId() != null) {
			throw new IllegalArgumentException("temperatureZoneId is only allowed for TEMPERATURE_CONTROL.");
		}
		if (isTemperatureControl && request.temperatureZoneId() == null) {
			throw new IllegalArgumentException("temperatureZoneId is required for TEMPERATURE_CONTROL.");
		}
	}

	private TemperatureZoneModel getTemperatureZone(Long zoneId, UUID organizationId, ComplianceArea complianceArea) {
		return temperatureZoneRepository.findByIdAndOrganizationIdAndComplianceArea(zoneId, organizationId, complianceArea)
			.orElseThrow(() -> new IllegalArgumentException("Temperature zone not found."));
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private IcModule toModule(ComplianceArea complianceArea) {
		if (complianceArea == ComplianceArea.IK_ALKOHOL) {
			return IcModule.IC_ALCOHOL;
		}
		return IcModule.IC_FOOD;
	}
}
