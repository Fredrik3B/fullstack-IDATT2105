package edu.ntnu.idatt2105.backend.task.service;

import edu.ntnu.idatt2105.backend.checklist.model.ChecklistModel;
import edu.ntnu.idatt2105.backend.checklist.model.enums.SectionTypes;
import edu.ntnu.idatt2105.backend.checklist.repository.ChecklistRepository;
import edu.ntnu.idatt2105.backend.checklist.service.ChecklistCacheStateService;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import edu.ntnu.idatt2105.backend.task.dto.CreateTaskRequest;
import edu.ntnu.idatt2105.backend.task.dto.TaskResponse;
import edu.ntnu.idatt2105.backend.task.mapper.TaskMapper;
import edu.ntnu.idatt2105.backend.task.model.TaskTemplate;
import edu.ntnu.idatt2105.backend.task.model.TasksModel;
import edu.ntnu.idatt2105.backend.task.repository.TaskTemplateRepository;
import edu.ntnu.idatt2105.backend.task.repository.TasksRepository;
import edu.ntnu.idatt2105.backend.temperature.model.TemperatureZoneModel;
import edu.ntnu.idatt2105.backend.temperature.repository.TemperatureMeasurementRepository;
import edu.ntnu.idatt2105.backend.temperature.repository.TemperatureZoneRepository;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for managing task templates.
 *
 * <p>Tasks are organisation-scoped reusable definitions that managers add to checklists.
 * Deleting a template cascades to all active task instances and removes it from every checklist
 * that referenced it.
 */
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


  /**
   * Creates a new task template for the caller's organisation.
   *
   * <p>Temperature-control tasks must supply a {@code temperatureZoneId}; the zone's
   * target range is copied onto the template so it is available even if the zone is later changed.
   *
   * @param request   the task details
   * @param principal the authenticated user
   * @return the created task response
   */
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

  /**
   * Updates an existing task template and propagates the new {@code meta} to all active instances.
   *
   * <p>If the compliance area changes, the cache timestamp is touched for both the old and new
   * area.
   *
   * @param taskId    the ID of the template to update
   * @param request   the updated task details
   * @param principal the authenticated user
   * @return the updated task response
   */
  @Transactional
  public TaskResponse updateTask(Long taskId, CreateTaskRequest request,
      JwtAuthenticatedPrincipal principal) {
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

  /**
   * Returns all task templates for the caller's organisation filtered by module.
   *
   * @param module    the compliance module to filter by
   * @param principal the authenticated user
   * @return ordered list of task responses (by section type then title)
   */
  public List<TaskResponse> getAllTasks(IcModule module, JwtAuthenticatedPrincipal principal) {
    UUID orgId = principal.requireOrganizationId();
    return taskTemplateRepository
        .findAllByOrganisationIdAndComplianceAreaOrderBySectionTypeAscTitleAsc(orgId,
            module.toComplianceArea())
        .stream()
        .map(taskMapper::toResponse)
        .toList();
  }

  /**
   * Returns a single task template by ID, scoped to the caller's organisation.
   *
   * @param taskId    the task template ID
   * @param principal the authenticated user
   * @return the task response
   * @throws IllegalArgumentException if not found or not owned by the caller's organisation
   */
  public TaskResponse getTaskById(Long taskId, JwtAuthenticatedPrincipal principal) {
    return taskMapper.toResponse(requireOwnTemplate(taskId, principal.requireOrganizationId()));
  }

  /**
   * Deletes a task template, removing it from all checklists and deleting all activated instances.
   *
   * @param taskId    the task template ID to delete
   * @param principal the authenticated user
   */
  @Transactional
  public void deleteTask(Long taskId, JwtAuthenticatedPrincipal principal) {
    UUID orgId = principal.requireOrganizationId();
    TaskTemplate template = requireOwnTemplate(taskId, orgId);

    List<ChecklistModel> checklists = checklistRepository.findAllByOrganization_IdOrderByIdAsc(
        orgId);
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


  /**
   * Loads a task template and verifies it belongs to the given organisation.
   *
   * @param taskId the template ID
   * @param orgId  the expected owner organisation
   * @return the verified template
   * @throws IllegalArgumentException if not found or not owned by the organisation
   */
  private TaskTemplate requireOwnTemplate(Long taskId, UUID orgId) {
    TaskTemplate template = taskTemplateRepository.findById(taskId)
        .orElseThrow(() -> new IllegalArgumentException("Task not found."));
    if (!template.getOrganisationId().equals(orgId)) {
      throw new IllegalArgumentException("Task not found.");
    }
    return template;
  }

  /**
   * Loads a temperature zone, verifying it belongs to the given organisation and compliance area.
   *
   * @param zoneId the zone ID
   * @param orgId  the expected owner organisation
   * @param area   the expected compliance area
   * @return the verified zone
   * @throws IllegalArgumentException if not found or ownership doesn't match
   */
  private TemperatureZoneModel requireTemperatureZone(Long zoneId, UUID orgId,
      ComplianceArea area) {
    return temperatureZoneRepository.findByIdAndOrganizationIdAndComplianceArea(zoneId, orgId, area)
        .orElseThrow(() -> new IllegalArgumentException("Temperature zone not found."));
  }

  /**
   * Validates that temperatureZoneId is present for TEMPERATURE_CONTROL and absent for all other
   * section types.
   */
  private void validateTemperatureZoneRule(CreateTaskRequest request) {
    boolean isTempControl = request.sectionType() == SectionTypes.TEMPERATURE_CONTROL;
    if (isTempControl && request.temperatureZoneId() == null) {
      throw new IllegalArgumentException("temperatureZoneId is required for TEMPERATURE_CONTROL.");
    }
    if (!isTempControl && request.temperatureZoneId() != null) {
      throw new IllegalArgumentException(
          "temperatureZoneId is only allowed for TEMPERATURE_CONTROL.");
    }
  }

  private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
