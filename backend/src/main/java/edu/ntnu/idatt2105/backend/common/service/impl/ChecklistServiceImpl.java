package edu.ntnu.idatt2105.backend.common.service.impl;

import edu.ntnu.idatt2105.backend.common.dto.icchecklist.ChecklistCardResponse;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.ChecklistSectionResponse;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.ChecklistSectionUpsertRequest;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.ChecklistTaskItemResponse;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.ChecklistTaskUpsertRequest;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.CreateChecklistCardRequest;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.TaskCompletionRequest;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.TaskFlagRequest;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.UpdateChecklistCardRequest;
import edu.ntnu.idatt2105.backend.common.model.ChecklistModel;
import edu.ntnu.idatt2105.backend.common.model.TaskTemplate;
import edu.ntnu.idatt2105.backend.common.model.TasksModel;
import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.common.model.enums.SectionTypes;
import edu.ntnu.idatt2105.backend.common.repository.ChecklistRepository;
import edu.ntnu.idatt2105.backend.common.repository.TaskTemplateRepository;
import edu.ntnu.idatt2105.backend.common.repository.TemperatureMeasurementRepository;
import edu.ntnu.idatt2105.backend.common.repository.TasksRepository;
import edu.ntnu.idatt2105.backend.common.service.ChecklistService;
import edu.ntnu.idatt2105.backend.common.service.icchecklist.PeriodKeyUtil;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.repository.OrganizationRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ChecklistServiceImpl implements ChecklistService {

	private final ChecklistRepository checklistRepository;
	private final TaskTemplateRepository taskTemplateRepository;
	private final TasksRepository tasksRepository;
	private final TemperatureMeasurementRepository temperatureMeasurementRepository;
	private final OrganizationRepository organizationRepository;

	public ChecklistServiceImpl(
		ChecklistRepository checklistRepository,
		TaskTemplateRepository taskTemplateRepository,
		TasksRepository tasksRepository,
		TemperatureMeasurementRepository temperatureMeasurementRepository,
		OrganizationRepository organizationRepository
	) {
		this.checklistRepository = checklistRepository;
		this.taskTemplateRepository = taskTemplateRepository;
		this.tasksRepository = tasksRepository;
		this.temperatureMeasurementRepository = temperatureMeasurementRepository;
		this.organizationRepository = organizationRepository;
	}

	@Override
	public List<ChecklistCardResponse> fetchChecklists(IcModule module, JwtAuthenticatedPrincipal principal) {
		JwtAuthenticatedPrincipal safePrincipal = requirePrincipal(principal);
		ComplianceArea area = requireModule(module).toComplianceArea();

		List<ChecklistModel> checklists = checklistRepository
			.findAllByOrganization_IdAndComplianceAreaAndActiveTrueOrderByIdAsc(safePrincipal.getOrganizationId(), area);
		if (checklists.isEmpty()) {
			return List.of();
		}

		List<Long> checklistIds = checklists.stream().map(ChecklistModel::getId).toList();
		List<TaskTemplate> templates = taskTemplateRepository.findActiveByChecklistIdsOrdered(checklistIds);
		Map<Long, List<TaskTemplate>> templatesByChecklist = templates.stream()
			.collect(Collectors.groupingBy(template -> template.getChecklist().getId(), LinkedHashMap::new, Collectors.toList()));

		return checklists.stream()
			.map(checklist -> {
				List<TaskTemplate> checklistTemplates = templatesByChecklist.getOrDefault(checklist.getId(), List.of());
				List<TasksModel> periodTasks = ensureTasksForCurrentPeriod(checklist, checklistTemplates);
				return toCardResponse(checklist, periodTasks);
			})
			.toList();
	}

	@Override
	public ChecklistCardResponse createChecklist(CreateChecklistCardRequest request, JwtAuthenticatedPrincipal principal) {
		JwtAuthenticatedPrincipal safePrincipal = requirePrincipal(principal);
		Objects.requireNonNull(request, "Checklist request cannot be null.");

		OrganizationModel organization = organizationRepository.findById(safePrincipal.getOrganizationId())
			.orElseThrow(() -> new IllegalArgumentException("Organization not found."));

		ChecklistModel checklist = new ChecklistModel();
		checklist.setName(requireText(request.title(), "title"));
		checklist.setDescription(trimToNull(request.subtitle()));
		checklist.setFrequency(PeriodKeyUtil.periodToFrequency(request.period()));
		checklist.setComplianceArea(requireModule(request.module()).toComplianceArea());
		checklist.setActive(true);
		checklist.setOrganization(organization);

		ChecklistModel savedChecklist = checklistRepository.save(checklist);
		List<TaskTemplate> templates = replaceTemplates(savedChecklist, request.sections(), safePrincipal.getOrganizationId());
		List<TasksModel> periodTasks = ensureTasksForCurrentPeriod(savedChecklist, templates);
		return toCardResponse(savedChecklist, periodTasks);
	}

	@Override
	public ChecklistCardResponse updateChecklist(
		Long checklistId,
		UpdateChecklistCardRequest request,
		JwtAuthenticatedPrincipal principal
	) {
		JwtAuthenticatedPrincipal safePrincipal = requirePrincipal(principal);
		Objects.requireNonNull(request, "Checklist request cannot be null.");

		ChecklistModel checklist = checklistRepository.findByIdAndOrganization_Id(checklistId, safePrincipal.getOrganizationId())
			.orElseThrow(() -> new IllegalArgumentException("Checklist not found."));

		checklist.setName(requireText(request.title(), "title"));
		checklist.setDescription(trimToNull(request.subtitle()));
		checklist.setFrequency(PeriodKeyUtil.periodToFrequency(request.period()));

		ChecklistModel savedChecklist = checklistRepository.save(checklist);
		List<TaskTemplate> templates = replaceTemplates(savedChecklist, request.sections(), safePrincipal.getOrganizationId());
		List<TasksModel> periodTasks = ensureTasksForCurrentPeriod(savedChecklist, templates);
		return toCardResponse(savedChecklist, periodTasks);
	}

	@Override
	public ChecklistTaskItemResponse setTaskCompletion(
		Long checklistId,
		Long taskId,
		TaskCompletionRequest request,
		JwtAuthenticatedPrincipal principal
	) {
		requirePrincipal(principal);
		Objects.requireNonNull(request, "Request cannot be null.");

		ChecklistModel checklist = getChecklist(checklistId, principal.getOrganizationId());
		PeriodKeyUtil.validatePeriodKey(request.periodKey(), checklist.getFrequency());
		String periodKey = request.periodKey().trim();

		TasksModel task = tasksRepository.findByIdAndChecklist_Id(taskId, checklistId)
			.orElseThrow(() -> new IllegalArgumentException("Activated task not found."));
		if (!Objects.equals(task.getPeriodKey(), periodKey)) {
			throw new IllegalArgumentException("Task does not belong to requested period.");
		}

		String state = normalizedState(request.state());
		if (!state.equals("completed") && !state.equals("todo")) {
			throw new IllegalArgumentException("Invalid state: " + request.state());
		}

		if (state.equals("completed")) {
			task.setCompleted(true);
			task.setFlagged(false);
			task.setCompletedAt(toLocalDateTime(request.completedAt()));
		} else {
			task.setCompleted(false);
			task.setCompletedAt(null);
		}

		TasksModel saved = tasksRepository.save(task);
		return toTaskItemResponse(saved);
	}

	@Override
	public ChecklistTaskItemResponse setTaskFlag(
		Long checklistId,
		Long taskId,
		TaskFlagRequest request,
		JwtAuthenticatedPrincipal principal
	) {
		requirePrincipal(principal);
		Objects.requireNonNull(request, "Request cannot be null.");

		ChecklistModel checklist = getChecklist(checklistId, principal.getOrganizationId());
		PeriodKeyUtil.validatePeriodKey(request.periodKey(), checklist.getFrequency());
		String periodKey = request.periodKey().trim();

		TasksModel task = tasksRepository.findByIdAndChecklist_Id(taskId, checklistId)
			.orElseThrow(() -> new IllegalArgumentException("Activated task not found."));
		if (!Objects.equals(task.getPeriodKey(), periodKey)) {
			throw new IllegalArgumentException("Task does not belong to requested period.");
		}

		String state = normalizedState(request.state());
		if (!state.equals("pending") && !state.equals("todo")) {
			throw new IllegalArgumentException("Invalid state: " + request.state());
		}

		if (state.equals("pending")) {
			task.setFlagged(true);
			task.setCompleted(false);
			task.setCompletedAt(null);
		} else {
			task.setFlagged(false);
		}

		TasksModel saved = tasksRepository.save(task);
		return toTaskItemResponse(saved);
	}

	@Override
	public void deleteChecklist(Long checklistId, JwtAuthenticatedPrincipal principal) {
		JwtAuthenticatedPrincipal safePrincipal = requirePrincipal(principal);
		ChecklistModel checklist = getChecklist(checklistId, safePrincipal.getOrganizationId());

		List<TasksModel> tasks = tasksRepository.findAllByChecklist_Id(checklistId);
		if (!tasks.isEmpty()) {
			temperatureMeasurementRepository.deleteAllByTaskIn(tasks);
			tasksRepository.deleteAll(tasks);
		}

		List<TaskTemplate> templates = taskTemplateRepository.findAllByChecklist_IdOrderBySectionTitleAscIdAsc(checklistId);
		if (!templates.isEmpty()) {
			taskTemplateRepository.deleteAll(templates);
		}

		checklistRepository.delete(checklist);
	}

	private List<TaskTemplate> replaceTemplates(
		ChecklistModel checklist,
		List<ChecklistSectionUpsertRequest> sections,
		UUID organizationId
	) {
		List<TaskTemplate> existing = taskTemplateRepository.findAllByChecklist_IdOrderBySectionTitleAscIdAsc(checklist.getId());
		if (!existing.isEmpty()) {
			taskTemplateRepository.deleteAll(existing);
		}

		List<ChecklistSectionUpsertRequest> safeSections = sections != null ? sections : List.of();
		List<TaskTemplate> templatesToSave = new ArrayList<>();

		for (ChecklistSectionUpsertRequest section : safeSections) {
			String sectionTitle = requireText(section.title(), "section.title");
			List<ChecklistTaskUpsertRequest> items = section.items() != null ? section.items() : List.of();

			for (ChecklistTaskUpsertRequest item : items) {
				TaskTemplate template = new TaskTemplate();
				template.setChecklist(checklist);
				template.setOrganisationId(organizationId);
				template.setTitle(requireText(item.label(), "task.label"));
				template.setSectionTitle(sectionTitle);
				template.setSectionType(resolveSectionType(sectionTitle, item.type()));
				template.setUnit(trimToNull(item.unit()));
				template.setTargetMin(item.targetMin());
				template.setTargetMax(item.targetMax());
				templatesToSave.add(template);
			}
		}

		if (templatesToSave.isEmpty()) {
			return List.of();
		}

		return taskTemplateRepository.saveAll(templatesToSave);
	}

	private List<TasksModel> ensureTasksForCurrentPeriod(ChecklistModel checklist, List<TaskTemplate> templates) {
		String currentPeriodKey = PeriodKeyUtil.currentPeriodKey(checklist.getFrequency(), ZoneId.systemDefault());
		List<TasksModel> existing = tasksRepository.findAllByChecklist_IdAndPeriodKeyAndActiveTrue(checklist.getId(), currentPeriodKey);
		Map<Long, TasksModel> existingByTemplateId = existing.stream()
			.filter(task -> task.getTaskTemplate() != null && task.getTaskTemplate().getId() != null)
			.collect(Collectors.toMap(task -> task.getTaskTemplate().getId(), task -> task, (left, right) -> left, LinkedHashMap::new));

		List<TasksModel> toSave = new ArrayList<>();
		Map<Long, TaskTemplate> templatesById = new LinkedHashMap<>();
		for (TaskTemplate template : templates) {
			if (template.getId() == null) {
				continue;
			}
			templatesById.put(template.getId(), template);
			TasksModel existingTask = existingByTemplateId.get(template.getId());
			if (existingTask == null) {
				TasksModel created = new TasksModel();
				created.setChecklist(checklist);
				created.setTaskTemplate(template);
				created.setPeriodKey(currentPeriodKey);
				created.setActive(true);
				created.setCompleted(false);
				created.setFlagged(false);
				toSave.add(created);
			}
		}

		for (TasksModel task : existing) {
			TaskTemplate template = task.getTaskTemplate();
			Long templateId = template != null ? template.getId() : null;
			if (templateId == null || !templatesById.containsKey(templateId)) {
				task.setActive(false);
				toSave.add(task);
			}
		}

		if (!toSave.isEmpty()) {
			tasksRepository.saveAll(toSave);
		}

		return tasksRepository.findAllByChecklist_IdAndPeriodKeyAndActiveTrue(checklist.getId(), currentPeriodKey);
	}

	private ChecklistCardResponse toCardResponse(ChecklistModel checklist, List<TasksModel> tasks) {
		int completedCount = (int) tasks.stream().filter(TasksModel::isCompleted).count();
		int total = tasks.size();
		Integer progress = total == 0 ? 0 : Math.toIntExact(Math.round((completedCount * 100.0) / total));
		return new ChecklistCardResponse(
			checklist.getId(),
			PeriodKeyUtil.frequencyToPeriod(checklist.getFrequency()),
			checklist.getName(),
			checklist.getDescription() != null ? checklist.getDescription() : "",
			null,
			Boolean.FALSE,
			"",
			"muted",
			progress,
			toSectionResponses(tasks)
		);
	}

	private List<ChecklistSectionResponse> toSectionResponses(List<TasksModel> tasks) {
		Map<String, List<TasksModel>> grouped = new LinkedHashMap<>();
		for (TasksModel task : tasks) {
			TaskTemplate template = task.getTaskTemplate();
			String sectionTitle = template != null ? template.getSectionTitle() : null;
			String key = sectionTitle == null || sectionTitle.isBlank() ? "Tasks" : sectionTitle.trim();
			grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(task);
		}

		return grouped.entrySet().stream()
			.map(entry -> new ChecklistSectionResponse(
				entry.getKey(),
				entry.getValue().stream().map(this::toTaskItemResponse).toList()
			))
			.toList();
	}

	private ChecklistTaskItemResponse toTaskItemResponse(TasksModel task) {
		TaskTemplate template = task.getTaskTemplate();
		if (template == null) {
			throw new IllegalArgumentException("Activated task is missing template.");
		}
		String type = isTemperatureTemplate(template) ? "temperature" : null;
		String state = "todo";
		String completedForPeriodKey = null;
		Instant completedAt = null;
		String pendingForPeriodKey = null;
		Boolean highlighted = null;

		if (task.isCompleted()) {
			state = "completed";
			completedForPeriodKey = task.getPeriodKey();
			completedAt = toInstant(task.getCompletedAt());
		} else if (task.isFlagged()) {
			state = "pending";
			pendingForPeriodKey = task.getPeriodKey();
			highlighted = Boolean.TRUE;
		}

		return new ChecklistTaskItemResponse(
			task.getId(),
			template.getTitle(),
			task.getMeta(),
			type,
			template.getUnit(),
			template.getTargetMin(),
			template.getTargetMax(),
			state,
			highlighted,
			completedForPeriodKey,
			completedAt,
			pendingForPeriodKey
		);
	}

	private boolean isTemperatureTemplate(TaskTemplate template) {
		if (template.getSectionType() == SectionTypes.TEMPERATURE_CONTROL) {
			return true;
		}
		return template.getUnit() != null || template.getTargetMin() != null || template.getTargetMax() != null;
	}

	private SectionTypes resolveSectionType(String sectionTitle, String itemType) {
		if ("temperature".equalsIgnoreCase(String.valueOf(itemType))) {
			return SectionTypes.TEMPERATURE_CONTROL;
		}

		if (sectionTitle == null || sectionTitle.isBlank()) {
			return null;
		}

		String normalized = sectionTitle.trim().toUpperCase()
			.replace('&', '_')
			.replace('-', '_')
			.replace(' ', '_');

		try {
			return SectionTypes.valueOf(normalized);
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}

	private ChecklistModel getChecklist(Long checklistId, UUID organizationId) {
		return checklistRepository.findByIdAndOrganization_Id(checklistId, organizationId)
			.orElseThrow(() -> new IllegalArgumentException("Checklist not found."));
	}

	private String normalizedState(String state) {
		return String.valueOf(state == null ? "" : state).trim().toLowerCase();
	}

	private LocalDateTime toLocalDateTime(Instant instant) {
		if (instant == null) {
			return LocalDateTime.now();
		}
		return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
	}

	private Instant toInstant(LocalDateTime localDateTime) {
		if (localDateTime == null) {
			return null;
		}
		return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
	}

	private JwtAuthenticatedPrincipal requirePrincipal(JwtAuthenticatedPrincipal principal) {
		if (principal == null) throw new IllegalArgumentException("Authentication required.");
		if (principal.getOrganizationId() == null) throw new IllegalArgumentException("Organization required.");
		if (principal.getUserId() == null) throw new IllegalArgumentException("User required.");
		return principal;
	}

	private IcModule requireModule(IcModule module) {
		if (module == null) throw new IllegalArgumentException("module is required.");
		return module;
	}

	private String requireText(String value, String fieldName) {
		String trimmed = value == null ? "" : value.trim();
		if (trimmed.isEmpty()) {
			throw new IllegalArgumentException(fieldName + " is required.");
		}
		return trimmed;
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
