package edu.ntnu.idatt2105.backend.checklist.service;

import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.ChecklistCardResponse;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.ChecklistSectionResponse;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.ChecklistTaskItemResponse;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.ChecklistWorkbenchStateRequest;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.CreateChecklistCardRequest;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.TemperatureMeasurementSummaryResponse;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.TaskCompletionRequest;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.TaskFlagRequest;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.UpdateChecklistCardRequest;
import edu.ntnu.idatt2105.backend.checklist.model.ChecklistModel;
import edu.ntnu.idatt2105.backend.task.model.TaskTemplate;
import edu.ntnu.idatt2105.backend.temperature.model.TemperatureMeasurementModel;
import edu.ntnu.idatt2105.backend.task.model.TasksModel;
import edu.ntnu.idatt2105.backend.common.model.enums.ChecklistFrequency;
import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.common.model.enums.SectionTypes;
import edu.ntnu.idatt2105.backend.checklist.repository.ChecklistRepository;
import edu.ntnu.idatt2105.backend.task.repository.TaskTemplateRepository;
import edu.ntnu.idatt2105.backend.temperature.repository.TemperatureMeasurementRepository;
import edu.ntnu.idatt2105.backend.task.repository.TasksRepository;
import edu.ntnu.idatt2105.backend.common.service.ChecklistService;
import edu.ntnu.idatt2105.backend.checklist.service.icchecklist.PeriodKeyUtil;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.repository.OrganizationRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChecklistServiceImpl implements ChecklistService {

	private final ChecklistRepository checklistRepository;
	private final TaskTemplateRepository taskTemplateRepository;
	private final TasksRepository tasksRepository;
	private final TemperatureMeasurementRepository temperatureMeasurementRepository;
	private final OrganizationRepository organizationRepository;
	private final ChecklistCacheStateService checklistCacheStateService;

	public ChecklistServiceImpl(
		ChecklistRepository checklistRepository,
		TaskTemplateRepository taskTemplateRepository,
		TasksRepository tasksRepository,
		TemperatureMeasurementRepository temperatureMeasurementRepository,
		OrganizationRepository organizationRepository,
		ChecklistCacheStateService checklistCacheStateService
	) {
		this.checklistRepository = checklistRepository;
		this.taskTemplateRepository = taskTemplateRepository;
		this.tasksRepository = tasksRepository;
		this.temperatureMeasurementRepository = temperatureMeasurementRepository;
		this.organizationRepository = organizationRepository;
		this.checklistCacheStateService = checklistCacheStateService;
	}

	@Override
	public List<ChecklistCardResponse> fetchChecklists(IcModule module, JwtAuthenticatedPrincipal principal) {
		JwtAuthenticatedPrincipal safePrincipal = requirePrincipal(principal);
		ComplianceArea area = requireModule(module).toComplianceArea();

		List<ChecklistModel> checklists = checklistRepository
			.findAllByOrganization_IdAndComplianceAreaAndActiveTrueOrderByIdAsc(safePrincipal.getOrganizationId(), area);

		return checklists.stream()
			.map(this::sortTemplates)
			.map(this::toCardResponse)
			.toList();
	}

	@Override
	public Instant fetchChecklistsLastModified(IcModule module, JwtAuthenticatedPrincipal principal) {
		JwtAuthenticatedPrincipal safePrincipal = requirePrincipal(principal);
		ComplianceArea area = requireModule(module).toComplianceArea();
		return checklistCacheStateService.getLastModified(safePrincipal.getOrganizationId(), area);
	}

	@Override
	@Transactional
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
		checklist.setActivePeriodKey(PeriodKeyUtil.currentPeriodKey(checklist.getFrequency(), ZoneId.systemDefault()));
		checklist.setRecurring(Boolean.TRUE.equals(request.recurring()));
		checklist.setDisplayedOnWorkbench(Boolean.TRUE.equals(request.displayedOnWorkbench()));
		checklist.setActive(true);
		checklist.setOrganization(organization);
		checklist.setTaskTemplates(resolveSelectedTemplates(request.taskTemplateIds(), safePrincipal.getOrganizationId(), checklist.getComplianceArea()));

		ChecklistModel savedChecklist = checklistRepository.save(checklist);
		touchChecklistCache(savedChecklist.getOrganization().getId(), savedChecklist.getComplianceArea());
		sortTemplates(savedChecklist);
		return toCardResponse(savedChecklist);
	}

	@Override
	@Transactional
	public ChecklistCardResponse updateChecklist(
		Long checklistId,
		UpdateChecklistCardRequest request,
		JwtAuthenticatedPrincipal principal
	) {
		JwtAuthenticatedPrincipal safePrincipal = requirePrincipal(principal);
		Objects.requireNonNull(request, "Checklist request cannot be null.");

		ChecklistModel checklist = getChecklist(checklistId, safePrincipal.getOrganizationId());
		ChecklistFrequency previousFrequency = checklist.getFrequency();
		checklist.setName(requireText(request.title(), "title"));
		checklist.setDescription(trimToNull(request.subtitle()));
		checklist.setFrequency(PeriodKeyUtil.periodToFrequency(request.period()));
		checklist.setRecurring(request.recurring() == null || Boolean.TRUE.equals(request.recurring()));
		checklist.setDisplayedOnWorkbench(request.displayedOnWorkbench() == null || Boolean.TRUE.equals(request.displayedOnWorkbench()));
		if (!Objects.equals(previousFrequency, checklist.getFrequency())) {
			checklist.setActivePeriodKey(PeriodKeyUtil.currentPeriodKey(checklist.getFrequency(), ZoneId.systemDefault()));
			deactivateActiveTasks(checklist.getId());
		}
		checklist.setTaskTemplates(resolveSelectedTemplates(request.taskTemplateIds(), safePrincipal.getOrganizationId(), checklist.getComplianceArea()));
		if (!checklist.isDisplayedOnWorkbench()) {
			deleteAllActivatedTasks(checklist.getId());
		}

		ChecklistModel savedChecklist = checklistRepository.save(checklist);
		touchChecklistCache(savedChecklist.getOrganization().getId(), savedChecklist.getComplianceArea());
		sortTemplates(savedChecklist);
		return toCardResponse(savedChecklist);
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
		if (!task.isActive()) {
			throw new IllegalArgumentException("Task is not active for the current checklist period.");
		}
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
			task.setEndedAt(request.completedAt());
		} else {
			task.setCompleted(false);
			task.setEndedAt(null);
		}

		TasksModel savedTask = tasksRepository.save(task);
		touchChecklistCache(checklist.getOrganization().getId(), checklist.getComplianceArea());
		return toTaskItemResponse(savedTask, latestMeasurementByTaskId(savedTask.getId()));
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
		if (!task.isActive()) {
			throw new IllegalArgumentException("Task is not active for the current checklist period.");
		}
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
			task.setEndedAt(null);
		} else {
			task.setFlagged(false);
		}

		TasksModel savedTask = tasksRepository.save(task);
		touchChecklistCache(checklist.getOrganization().getId(), checklist.getComplianceArea());
		return toTaskItemResponse(savedTask, latestMeasurementByTaskId(savedTask.getId()));
	}

	@Override
	@Transactional
	public ChecklistCardResponse submitChecklist(Long checklistId, JwtAuthenticatedPrincipal principal) {
		ChecklistModel checklist = synchronizeChecklistPeriodState(
			getChecklist(checklistId, requirePrincipal(principal).getOrganizationId())
		);
		sortTemplates(checklist);
		if (checklist.getTaskTemplates() == null || checklist.getTaskTemplates().isEmpty()) {
			throw new IllegalArgumentException("Checklist must contain at least one task before it can be submitted.");
		}

		String activePeriodKey = resolveActivePeriodKey(checklist);
		String currentPeriodKey = PeriodKeyUtil.currentPeriodKey(checklist.getFrequency(), ZoneId.systemDefault());
		if (!Objects.equals(activePeriodKey, currentPeriodKey)) {
			throw new IllegalStateException("This checklist has already been submitted for the current period.");
		}
		List<TasksModel> activeTasks = ensureTasksForActivePeriod(checklist);
		if (activeTasks.isEmpty()) {
			throw new IllegalArgumentException("Checklist has no active tasks to submit.");
		}
		for (TasksModel task : activeTasks) {
			if (task.getEndedAt() == null) {
				task.setEndedAt(periodKeyToDateTime(checklist.getFrequency(), task.getPeriodKey()));
			}
			task.setActive(false);
		}
		tasksRepository.saveAll(activeTasks);

		checklist.setActivePeriodKey(PeriodKeyUtil.nextPeriodKey(checklist.getFrequency(), activePeriodKey));
		ChecklistModel savedChecklist = checklistRepository.save(checklist);
		touchChecklistCache(savedChecklist.getOrganization().getId(), savedChecklist.getComplianceArea());
		sortTemplates(savedChecklist);
		return toCardResponse(savedChecklist);
	}

	@Override
	@Transactional
	public ChecklistCardResponse setChecklistWorkbenchState(
		Long checklistId,
		ChecklistWorkbenchStateRequest request,
		JwtAuthenticatedPrincipal principal
	) {
		Objects.requireNonNull(request, "Workbench state request cannot be null.");
		ChecklistModel checklist = getChecklist(checklistId, requirePrincipal(principal).getOrganizationId());
		boolean displayedOnWorkbench = Boolean.TRUE.equals(request.displayedOnWorkbench());
		checklist.setDisplayedOnWorkbench(displayedOnWorkbench);
		if (!displayedOnWorkbench) {
			deleteAllActivatedTasks(checklist.getId());
		}
		ChecklistModel savedChecklist = checklistRepository.save(checklist);
		touchChecklistCache(savedChecklist.getOrganization().getId(), savedChecklist.getComplianceArea());
		sortTemplates(savedChecklist);
		return toCardResponse(savedChecklist);
	}

	@Override
	@Transactional
	public void deleteChecklist(Long checklistId, JwtAuthenticatedPrincipal principal) {
		ChecklistModel checklist = getChecklist(checklistId, requirePrincipal(principal).getOrganizationId());
		ComplianceArea complianceArea = checklist.getComplianceArea();
		UUID organizationId = checklist.getOrganization().getId();

		List<TasksModel> tasks = tasksRepository.findAllByChecklist_Id(checklistId);
		if (!tasks.isEmpty()) {
			temperatureMeasurementRepository.deleteAllByTaskIn(tasks);
			tasksRepository.deleteAll(tasks);
		}

		checklistRepository.delete(checklist);
		touchChecklistCache(organizationId, complianceArea);
	}

	private void touchChecklistCache(UUID organizationId, ComplianceArea complianceArea) {
		checklistCacheStateService.touch(organizationId, complianceArea);
	}

	private ChecklistModel sortTemplates(ChecklistModel checklist) {
		List<TaskTemplate> sorted = new ArrayList<>(checklist.getTaskTemplates());
		sorted.sort(taskTemplateComparator());
		checklist.setTaskTemplates(new LinkedHashSet<>(sorted));
		return checklist;
	}

	private Set<TaskTemplate> resolveSelectedTemplates(
		List<Long> templateIds,
		UUID organizationId,
		ComplianceArea complianceArea
	) {
		if (templateIds == null || templateIds.isEmpty()) {
			throw new IllegalArgumentException("taskTemplateIds must contain at least one task.");
		}

		List<TaskTemplate> selected = taskTemplateRepository.findAllByIdInAndOrganisationIdAndComplianceAreaOrdered(
			templateIds,
			organizationId,
			complianceArea
		);
		if (selected.size() != templateIds.stream().filter(Objects::nonNull).distinct().count()) {
			throw new IllegalArgumentException("One or more selected tasks were not found.");
		}
		return new LinkedHashSet<>(selected);
	}

	private List<TasksModel> ensureTasksForActivePeriod(ChecklistModel checklist) {
		String activePeriodKey = resolveActivePeriodKey(checklist);
		List<TasksModel> existing = tasksRepository.findAllByChecklist_IdAndPeriodKeyAndActiveTrue(checklist.getId(), activePeriodKey);
		Map<Long, TasksModel> existingByTemplateId = new LinkedHashMap<>();
		for (TasksModel task : existing) {
			if (task.getTaskTemplate() != null && task.getTaskTemplate().getId() != null) {
				existingByTemplateId.put(task.getTaskTemplate().getId(), task);
			}
		}

		List<TasksModel> toSave = new ArrayList<>();
		Set<Long> selectedTemplateIds = new LinkedHashSet<>();
		for (TaskTemplate template : checklist.getTaskTemplates()) {
			Long templateId = template.getId();
			if (templateId == null) continue;
			selectedTemplateIds.add(templateId);
			if (!existingByTemplateId.containsKey(templateId)) {
				TasksModel task = new TasksModel();
				task.setChecklist(checklist);
				task.setTaskTemplate(template);
				task.setMeta(template.getMeta());
				task.setPeriodKey(activePeriodKey);
				task.setActive(true);
				task.setCompleted(false);
				task.setFlagged(false);
				toSave.add(task);
			}
		}

		for (TasksModel task : existing) {
			Long templateId = task.getTaskTemplate() != null ? task.getTaskTemplate().getId() : null;
			if (templateId == null || !selectedTemplateIds.contains(templateId)) {
				task.setActive(false);
				toSave.add(task);
			}
		}

		if (!toSave.isEmpty()) {
			tasksRepository.saveAll(toSave);
		}

		List<TasksModel> activeTasks = tasksRepository.findAllByChecklist_IdAndPeriodKeyAndActiveTrue(checklist.getId(), activePeriodKey);
		activeTasks.sort(Comparator.comparing((TasksModel task) -> sectionLabel(task.getTaskTemplate().getSectionType()))
			.thenComparing(task -> task.getTaskTemplate().getTitle(), String.CASE_INSENSITIVE_ORDER));
		return activeTasks;
	}

	private ChecklistCardResponse toCardResponse(ChecklistModel checklist) {
		ChecklistModel currentChecklist = synchronizeChecklistPeriodState(checklist);
		String activePeriodKey = resolveActivePeriodKey(currentChecklist);
		String currentPeriodKey = PeriodKeyUtil.currentPeriodKey(currentChecklist.getFrequency(), ZoneId.systemDefault());
		boolean shouldLoadActiveTasks = currentChecklist.isDisplayedOnWorkbench()
			&& (currentChecklist.isRecurring() || Objects.equals(activePeriodKey, currentPeriodKey));
		List<TasksModel> tasks = shouldLoadActiveTasks
			? ensureTasksForActivePeriod(currentChecklist)
			: List.of();
		Map<Long, TemperatureMeasurementModel> latestMeasurementsByTaskId = latestMeasurementsByTaskId(tasks);
		int completedCount = (int) tasks.stream().filter(TasksModel::isCompleted).count();
		int total = tasks.size();
		Integer progress = total == 0 ? 0 : Math.toIntExact(Math.round((completedCount * 100.0) / total));
		boolean completed = total > 0 && completedCount == total;
		List<ChecklistSectionResponse> sections = shouldLoadActiveTasks
			? toSectionResponses(tasks, latestMeasurementsByTaskId)
			: toTemplateSectionResponses(currentChecklist);
		String statusLabel = currentChecklist.isDisplayedOnWorkbench()
			? (shouldLoadActiveTasks ? (completed ? "Ready to submit" : "In progress") : "Waiting for next period")
			: "In library";
		String statusTone = currentChecklist.isDisplayedOnWorkbench()
			? (shouldLoadActiveTasks ? (completed ? "success" : "muted") : "muted")
			: "muted";

		return new ChecklistCardResponse(
			currentChecklist.getId(),
			PeriodKeyUtil.frequencyToPeriod(currentChecklist.getFrequency()),
			activePeriodKey,
			currentChecklist.isRecurring(),
			currentChecklist.isDisplayedOnWorkbench(),
			currentChecklist.getName(),
			currentChecklist.getDescription() != null ? currentChecklist.getDescription() : "",
			null,
			Boolean.FALSE,
			statusLabel,
			statusTone,
			progress,
			sections
		);
	}

	private String resolveActivePeriodKey(ChecklistModel checklist) {
		String key = checklist.getActivePeriodKey();
		if (key == null || key.isBlank()) {
			key = PeriodKeyUtil.currentPeriodKey(checklist.getFrequency(), ZoneId.systemDefault());
			checklist.setActivePeriodKey(key);
			checklistRepository.save(checklist);
		}
		return key;
	}

	private LocalDateTime periodKeyToDateTime(ChecklistFrequency frequency, String periodKey) {
		String nextPeriodKey = PeriodKeyUtil.nextPeriodKey(frequency, periodKey);
		return PeriodKeyUtil.periodStartDate(frequency, nextPeriodKey)
			.atStartOfDay()
			.minusSeconds(1);
	}

	private ChecklistModel synchronizeChecklistPeriodState(ChecklistModel checklist) {
		String activeKey = resolveActivePeriodKey(checklist);
		String currentKey = PeriodKeyUtil.currentPeriodKey(checklist.getFrequency(), ZoneId.systemDefault());
		LocalDate activeStart = PeriodKeyUtil.periodStartDate(checklist.getFrequency(), activeKey);
		LocalDate currentStart = PeriodKeyUtil.periodStartDate(checklist.getFrequency(), currentKey);

		if (activeStart.isBefore(currentStart)) {
			deactivateActiveTasks(checklist.getId());
			checklist.setActivePeriodKey(currentKey);
			ChecklistModel savedChecklist = checklistRepository.save(checklist);
			sortTemplates(savedChecklist);
			return savedChecklist;
		}

		return checklist;
	}

	private void deactivateActiveTasks(Long checklistId) {
		List<TasksModel> activeTasks = tasksRepository.findAllByChecklist_IdAndActiveTrue(checklistId);
		if (activeTasks.isEmpty()) {
			return;
		}
		for (TasksModel task : activeTasks) {
			task.setActive(false);
		}
		tasksRepository.saveAll(activeTasks);
	}

	private void deleteAllActivatedTasks(Long checklistId) {
		List<TasksModel> tasks = tasksRepository.findAllByChecklist_Id(checklistId);
		if (tasks.isEmpty()) {
			return;
		}
		temperatureMeasurementRepository.deleteAllByTaskIn(tasks);
		tasksRepository.deleteAll(tasks);
	}

	private Map<Long, TemperatureMeasurementModel> latestMeasurementsByTaskId(List<TasksModel> tasks) {
		List<Long> taskIds = tasks.stream()
			.map(TasksModel::getId)
			.filter(Objects::nonNull)
			.toList();
		if (taskIds.isEmpty()) {
			return Map.of();
		}

		Map<Long, TemperatureMeasurementModel> latestByTaskId = new LinkedHashMap<>();
		for (TemperatureMeasurementModel measurement : temperatureMeasurementRepository.findAllByTask_IdInOrderByMeasuredAtDesc(taskIds)) {
			Long taskId = measurement.getTask() != null ? measurement.getTask().getId() : null;
			if (taskId != null) {
				latestByTaskId.putIfAbsent(taskId, measurement);
			}
		}
		return latestByTaskId;
	}

	private TemperatureMeasurementModel latestMeasurementByTaskId(Long taskId) {
		if (taskId == null) {
			return null;
		}
		return temperatureMeasurementRepository.findAllByTask_IdInOrderByMeasuredAtDesc(List.of(taskId)).stream()
			.findFirst()
			.orElse(null);
	}

	private List<ChecklistSectionResponse> toSectionResponses(
		List<TasksModel> tasks,
		Map<Long, TemperatureMeasurementModel> latestMeasurementsByTaskId
	) {
		Map<String, List<TasksModel>> grouped = new LinkedHashMap<>();
		for (TasksModel task : tasks) {
			String key = sectionLabel(task.getTaskTemplate().getSectionType());
			grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(task);
		}

		List<ChecklistSectionResponse> sections = new ArrayList<>();
		for (Map.Entry<String, List<TasksModel>> entry : grouped.entrySet()) {
			entry.getValue().sort(Comparator.comparing(task -> task.getTaskTemplate().getTitle(), String.CASE_INSENSITIVE_ORDER));
			sections.add(new ChecklistSectionResponse(
				entry.getKey(),
				entry.getValue().stream()
					.map(task -> toTaskItemResponse(task, latestMeasurementsByTaskId.get(task.getId())))
					.toList()
			));
		}
		sections.sort(Comparator.comparing(ChecklistSectionResponse::title, String.CASE_INSENSITIVE_ORDER));
		return sections;
	}

	private List<ChecklistSectionResponse> toTemplateSectionResponses(ChecklistModel checklist) {
		Map<String, List<TaskTemplate>> grouped = new LinkedHashMap<>();
		for (TaskTemplate template : checklist.getTaskTemplates()) {
			String key = sectionLabel(template.getSectionType());
			grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(template);
		}

		List<ChecklistSectionResponse> sections = new ArrayList<>();
		for (Map.Entry<String, List<TaskTemplate>> entry : grouped.entrySet()) {
			entry.getValue().sort(Comparator.comparing(TaskTemplate::getTitle, String.CASE_INSENSITIVE_ORDER));
			sections.add(new ChecklistSectionResponse(
				entry.getKey(),
				entry.getValue().stream().map(this::toTemplateTaskItemResponse).toList()
			));
		}
		sections.sort(Comparator.comparing(ChecklistSectionResponse::title, String.CASE_INSENSITIVE_ORDER));
		return sections;
	}

	private ChecklistTaskItemResponse toTaskItemResponse(TasksModel task, TemperatureMeasurementModel latestMeasurement) {
		TaskTemplate template = task.getTaskTemplate();
		String type = isTemperatureTemplate(template) ? "temperature" : null;
		String state = "todo";
		String completedForPeriodKey = null;
		LocalDateTime completedAt = null;
		String pendingForPeriodKey = null;
		Boolean highlighted = null;

		if (task.isCompleted()) {
			state = "completed";
			completedForPeriodKey = task.getPeriodKey();
			completedAt = task.getEndedAt();
		} else if (task.isFlagged()) {
			state = "pending";
			pendingForPeriodKey = task.getPeriodKey();
			highlighted = Boolean.TRUE;
		}

		return new ChecklistTaskItemResponse(
			task.getId(),
			template.getId(),
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
			pendingForPeriodKey,
			toMeasurementSummary(latestMeasurement)
		);
	}

	private ChecklistTaskItemResponse toTemplateTaskItemResponse(TaskTemplate template) {
		return new ChecklistTaskItemResponse(
			null,
			template.getId(),
			template.getTitle(),
			template.getMeta(),
			isTemperatureTemplate(template) ? "temperature" : null,
			template.getUnit(),
			template.getTargetMin(),
			template.getTargetMax(),
			"todo",
			Boolean.FALSE,
			null,
			null,
			null,
			null
		);
	}

	private TemperatureMeasurementSummaryResponse toMeasurementSummary(TemperatureMeasurementModel measurement) {
		if (measurement == null) {
			return null;
		}
		return new TemperatureMeasurementSummaryResponse(
			measurement.getId(),
			measurement.getValueC(),
			measurement.getMeasuredAt(),
			measurement.getPeriodKey(),
			isTemperatureDeviation(measurement)
		);
	}

	private boolean isTemperatureDeviation(TemperatureMeasurementModel measurement) {
		if (measurement == null || measurement.getTask() == null || measurement.getTask().getTaskTemplate() == null) {
			return false;
		}

		TaskTemplate template = measurement.getTask().getTaskTemplate();
		if (template.getTargetMin() != null && measurement.getValueC().compareTo(template.getTargetMin()) < 0) {
			return true;
		}
		if (template.getTargetMax() != null && measurement.getValueC().compareTo(template.getTargetMax()) > 0) {
			return true;
		}
		return false;
	}

	private boolean isTemperatureTemplate(TaskTemplate template) {
		return template.getUnit() != null || template.getTargetMin() != null || template.getTargetMax() != null;
	}

	private Comparator<TaskTemplate> taskTemplateComparator() {
		return Comparator.comparing((TaskTemplate template) -> sectionLabel(template.getSectionType()), String.CASE_INSENSITIVE_ORDER)
			.thenComparing(TaskTemplate::getTitle, String.CASE_INSENSITIVE_ORDER);
	}

	private String sectionLabel(SectionTypes sectionType) {
		if (sectionType == null) {
			return "Tasks";
		}
		String[] parts = sectionType.name().split("_");
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			if (i > 0) builder.append(' ');
			String part = parts[i].toLowerCase();
			builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
		}
		return builder.toString();
	}

	private ChecklistModel getChecklist(Long checklistId, UUID organizationId) {
		return checklistRepository.findByIdAndOrganization_Id(checklistId, organizationId)
			.orElseThrow(() -> new IllegalArgumentException("Checklist not found."));
	}

	private String normalizedState(String state) {
		return String.valueOf(state == null ? "" : state).trim().toLowerCase();
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
		if (trimmed.isEmpty()) throw new IllegalArgumentException(fieldName + " is required.");
		return trimmed;
	}

	private String trimToNull(String value) {
		if (value == null) return null;
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
