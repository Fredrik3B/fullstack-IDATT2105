package edu.ntnu.idatt2105.backend.task.service;

import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import edu.ntnu.idatt2105.backend.checklist.model.enums.SectionTypes;
import edu.ntnu.idatt2105.backend.checklist.service.ChecklistCacheStateService;
import edu.ntnu.idatt2105.backend.task.dto.CreateTaskRequest;
import edu.ntnu.idatt2105.backend.task.dto.TaskResponse;
import edu.ntnu.idatt2105.backend.checklist.model.ChecklistModel;
import edu.ntnu.idatt2105.backend.task.mapper.TaskMapper;
import edu.ntnu.idatt2105.backend.task.model.TaskTemplate;
import edu.ntnu.idatt2105.backend.temperature.model.TemperatureZoneModel;
import edu.ntnu.idatt2105.backend.task.model.TasksModel;
import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.checklist.repository.ChecklistRepository;
import edu.ntnu.idatt2105.backend.task.repository.TaskTemplateRepository;
import edu.ntnu.idatt2105.backend.temperature.repository.TemperatureMeasurementRepository;
import edu.ntnu.idatt2105.backend.temperature.repository.TemperatureZoneRepository;
import edu.ntnu.idatt2105.backend.task.repository.TasksRepository;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TaskService {

	private final TaskTemplateRepository taskTemplateRepository;
	private final ChecklistRepository checklistRepository;
	private final TasksRepository tasksRepository;
	private final TemperatureMeasurementRepository temperatureMeasurementRepository;
	private final TemperatureZoneRepository temperatureZoneRepository;
	private final ChecklistCacheStateService checklistCacheStateService;
	private final TaskMapper taskMapper;


	public TaskResponse createTask(CreateTaskRequest request, JwtAuthenticatedPrincipal principal) {
		UUID orgId = principal.requireOrganizationId();
		ComplianceArea area = request.module().toComplianceArea();
		boolean isTemperatureControl = request.sectionType() == SectionTypes.TEMPERATURE_CONTROL;

		validateTemperatureZoneRule(request);
		TemperatureZoneModel zone = isTemperatureControl
				? requireTemperatureZone(request.temperatureZoneId(), orgId, area)
				: null;

		TaskTemplate template = TaskTemplate.builder()
				.title(request.title().trim())
				.meta(trimToNull(request.meta()))
				.sectionType(request.sectionType())
				.complianceArea(area)
				.temperatureZone(zone)
				.unit(isTemperatureControl ? "C" : null)
				.targetMin(isTemperatureControl ? zone.getTargetMin() : null)
				.targetMax(isTemperatureControl ? zone.getTargetMax() : null)
				.organisationId(orgId)
				.build();

		TaskTemplate saved = taskTemplateRepository.save(template);
		checklistCacheStateService.touch(orgId, area);
		return taskMapper.toResponse(saved);
	}

	@Transactional
	public TaskResponse updateTask(Long taskId, CreateTaskRequest request, JwtAuthenticatedPrincipal principal) {
		UUID orgId = principal.requireOrganizationId();
		ComplianceArea newArea = request.module().toComplianceArea();
		boolean isTempControl = request.sectionType() == SectionTypes.TEMPERATURE_CONTROL;

		validateTemperatureZoneRule(request);
		TaskTemplate template = requireOwnTemplate(taskId, orgId);
		ComplianceArea previousArea = template.getComplianceArea();

		TemperatureZoneModel zone = isTempControl
				? requireTemperatureZone(request.temperatureZoneId(), orgId, newArea)
				: null;

		template.setTitle(request.title().trim());
		template.setMeta(trimToNull(request.meta()));
		template.setSectionType(request.sectionType());
		template.setComplianceArea(newArea);
		template.setTemperatureZone(zone);
		template.setUnit(isTempControl ? "C" : null);
		template.setTargetMin(isTempControl ? zone.getTargetMin() : null);
		template.setTargetMax(isTempControl ? zone.getTargetMax() : null);

		List<TasksModel> activatedTasks = tasksRepository.findAllByTaskTemplate_Id(taskId);
		activatedTasks.forEach(task -> task.setMeta(template.getMeta()));
		if (!activatedTasks.isEmpty()) {
			tasksRepository.saveAll(activatedTasks);
		}

		TaskTemplate saved = taskTemplateRepository.save(template);
		checklistCacheStateService.touch(orgId, previousArea);
		if (newArea != previousArea) {
			checklistCacheStateService.touch(orgId, newArea);
		}
		return taskMapper.toResponse(saved);
	}

	public List<TaskResponse> getAllTasks(IcModule module, JwtAuthenticatedPrincipal principal) {
		UUID orgId = principal.requireOrganizationId();
		return taskTemplateRepository
				.findAllByOrganisationIdAndComplianceAreaOrderBySectionTypeAscTitleAsc(orgId, module.toComplianceArea())
				.stream()
				.map(taskMapper::toResponse)
				.toList();
	}

	public TaskResponse getTaskById(Long taskId, JwtAuthenticatedPrincipal principal) {
		return taskMapper.toResponse(requireOwnTemplate(taskId, principal.requireOrganizationId()));
	}

	@Transactional
	public void deleteTask(Long taskId, JwtAuthenticatedPrincipal principal) {
		UUID orgId = principal.requireOrganizationId();
		TaskTemplate template = requireOwnTemplate(taskId, orgId);

		List<ChecklistModel> checklists = checklistRepository.findAllByOrganization_IdOrderByIdAsc(orgId);
		for (ChecklistModel checklist : checklists) {
			if (checklist.getTaskTemplates().removeIf(t -> template.getId().equals(t.getId()))) {
				checklistRepository.save(checklist);
			}
		}

		List<TasksModel> activatedTasks = tasksRepository.findAllByTaskTemplate_Id(taskId);
		if (!activatedTasks.isEmpty()) {
			temperatureMeasurementRepository.deleteAllByTaskIn(activatedTasks);
			tasksRepository.deleteAll(activatedTasks);
		}

		taskTemplateRepository.delete(template);
		checklistCacheStateService.touch(orgId, template.getComplianceArea());
	}


	private TaskTemplate requireOwnTemplate(Long taskId, UUID orgId) {
		TaskTemplate template = taskTemplateRepository.findById(taskId)
				.orElseThrow(() -> new IllegalArgumentException("Task not found."));
		if (!template.getOrganisationId().equals(orgId)) {
			throw new IllegalArgumentException("Task not found.");
		}
		return template;
	}

	private TemperatureZoneModel requireTemperatureZone(Long zoneId, UUID orgId, ComplianceArea area) {
		return temperatureZoneRepository.findByIdAndOrganizationIdAndComplianceArea(zoneId, orgId, area)
				.orElseThrow(() -> new IllegalArgumentException("Temperature zone not found."));
	}

	/**
	 * Validates that temperatureZoneId is present for TEMPERATURE_CONTROL
	 * and absent for all other section types.
	 */
	private void validateTemperatureZoneRule(CreateTaskRequest request) {
		boolean isTempControl = request.sectionType() == SectionTypes.TEMPERATURE_CONTROL;
		if (isTempControl && request.temperatureZoneId() == null) {
			throw new IllegalArgumentException("temperatureZoneId is required for TEMPERATURE_CONTROL.");
		}
		if (!isTempControl && request.temperatureZoneId() != null) {
			throw new IllegalArgumentException("temperatureZoneId is only allowed for TEMPERATURE_CONTROL.");
		}
	}

	private String trimToNull(String value) {
		if (value == null) return null;
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
