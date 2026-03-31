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
import edu.ntnu.idatt2105.backend.common.model.TaskModel;
import edu.ntnu.idatt2105.backend.common.model.TaskPeriodStateModel;
import edu.ntnu.idatt2105.backend.common.repository.ChecklistRepository;
import edu.ntnu.idatt2105.backend.common.repository.TaskPeriodStateRepository;
import edu.ntnu.idatt2105.backend.common.repository.TaskRepository;
import edu.ntnu.idatt2105.backend.common.repository.TemperatureMeasurementRepository;
import edu.ntnu.idatt2105.backend.common.model.enums.ChecklistTaskType;
import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.common.service.ChecklistService;
import edu.ntnu.idatt2105.backend.common.service.icchecklist.PeriodKeyUtil;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import edu.ntnu.idatt2105.backend.user.repository.OrganizationRepository;
import edu.ntnu.idatt2105.backend.user.repository.UserRepository;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;



@Service
public class ChecklistServiceImpl implements ChecklistService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChecklistServiceImpl.class);

	private final ChecklistRepository checklistRepository;
	private final OrganizationRepository organizationRepository;
	private final UserRepository userRepository;
	private final TaskRepository taskRepository;
	private final TaskPeriodStateRepository taskPeriodStateRepository;
	private final TemperatureMeasurementRepository temperatureMeasurementRepository;

	public ChecklistServiceImpl(
		ChecklistRepository checklistRepository,
		OrganizationRepository organizationRepository,
		UserRepository userRepository,
		TaskRepository taskRepository,
		TaskPeriodStateRepository taskPeriodStateRepository,
		TemperatureMeasurementRepository temperatureMeasurementRepository
	) {
		this.checklistRepository = checklistRepository;
		this.organizationRepository = organizationRepository;
		this.userRepository = userRepository;
		this.taskRepository = taskRepository;
		this.taskPeriodStateRepository = taskPeriodStateRepository;
		this.temperatureMeasurementRepository = temperatureMeasurementRepository;
	}

	@Override
	public List<ChecklistCardResponse> fetchChecklists(IcModule module, JwtAuthenticatedPrincipal principal) {
		UUID orgId = requirePrincipal(principal).getOrganizationId();
		UUID userId = principal.getUserId();
		ComplianceArea area = requireModule(module).toComplianceArea();

		LOGGER.debug("fetchChecklists(orgId={}, userId={}, module={}, complianceArea={})", orgId, userId, module, area);

		List<ChecklistModel> checklists = checklistRepository
			.findAllByOrganization_IdAndComplianceAreaAndActiveTrueOrderByIdAsc(orgId, area);

		if (checklists.isEmpty()) return List.of();

		List<Long> checklistIds = checklists.stream().map(ChecklistModel::getId).toList();
		List<TaskModel> tasks = taskRepository.findActiveByChecklistIdsOrdered(checklistIds);
		LOGGER.debug("fetchChecklists: found {} checklists and {} tasks", checklists.size(), tasks.size());

		Set<Long> taskIds = tasks.stream().map(TaskModel::getId).filter(Objects::nonNull).collect(Collectors.toSet());
		Set<String> periodKeys = checklists.stream()
			.map(c -> PeriodKeyUtil.currentPeriodKey(c.getFrequency(), ZoneId.systemDefault()))
			.collect(Collectors.toSet());

		Map<String, TaskPeriodStateModel> stateByTaskAndPeriod = new HashMap<>();
		if (!taskIds.isEmpty() && !periodKeys.isEmpty()) {
			List<TaskPeriodStateModel> states = taskPeriodStateRepository
				.findAllByOrganization_IdAndUser_IdAndTask_IdInAndPeriodKeyIn(orgId, userId, taskIds, periodKeys);
			for (TaskPeriodStateModel state : states) {
				stateByTaskAndPeriod.put(state.getTask().getId() + "|" + state.getPeriodKey(), state);
			}
		}

		Map<Long, List<TaskModel>> tasksByChecklistId = tasks.stream()
			.collect(Collectors.groupingBy(t -> t.getChecklist().getId(), LinkedHashMap::new, Collectors.toList()));

		List<ChecklistCardResponse> cards = new ArrayList<>(checklists.size());
		for (ChecklistModel checklist : checklists) {
			List<TaskModel> checklistTasks = tasksByChecklistId.getOrDefault(checklist.getId(), List.of());
			String currentKey = PeriodKeyUtil.currentPeriodKey(checklist.getFrequency(), ZoneId.systemDefault());
			cards.add(toCardResponse(checklist, checklistTasks, currentKey, stateByTaskAndPeriod));
		}
		return cards;
	}

	@Override
	public ChecklistCardResponse createChecklist(CreateChecklistCardRequest request, JwtAuthenticatedPrincipal principal) {
		requirePrincipal(principal);
		Objects.requireNonNull(request, "Checklist request cannot be null.");

		UUID orgId = principal.getOrganizationId();
		LOGGER.debug("createChecklist(orgId={}, userId={}, module={}, period={})", orgId, principal.getUserId(), request.module(), request.period());
		OrganizationModel organization = organizationRepository.findById(orgId)
			.orElseThrow(() -> new IllegalArgumentException("Organization not found."));

		ChecklistModel checklist = new ChecklistModel();
		checklist.setName(requireText(request.title(), "title"));
		checklist.setDescription(trimToNull(request.subtitle()));
		checklist.setFrequency(PeriodKeyUtil.periodToFrequency(request.period()));
		checklist.setComplianceArea(requireModule(request.module()).toComplianceArea());
		checklist.setActive(true);
		checklist.setOrganization(organization);

		ChecklistModel saved = checklistRepository.save(checklist);
		List<TaskModel> savedTasks = upsertTasks(saved, request.sections(), List.of());
		LOGGER.debug("createChecklist: saved checklistId={} tasks={}", saved.getId(), savedTasks.size());

		String currentKey = PeriodKeyUtil.currentPeriodKey(saved.getFrequency(), ZoneId.systemDefault());
		return toCardResponse(saved, savedTasks, currentKey, Map.of());
	}

	@Override
	public ChecklistCardResponse updateChecklist(
		Long checklistId,
		UpdateChecklistCardRequest request,
		JwtAuthenticatedPrincipal principal
	) {
		requirePrincipal(principal);
		Objects.requireNonNull(request, "Checklist request cannot be null.");

		UUID orgId = principal.getOrganizationId();
		LOGGER.debug("updateChecklist(orgId={}, userId={}, checklistId={}, period={})", orgId, principal.getUserId(), checklistId, request.period());
		ChecklistModel checklist = checklistRepository.findByIdAndOrganization_Id(checklistId, orgId)
			.orElseThrow(() -> new IllegalArgumentException("Checklist not found."));

		checklist.setName(requireText(request.title(), "title"));
		checklist.setDescription(trimToNull(request.subtitle()));
		checklist.setFrequency(PeriodKeyUtil.periodToFrequency(request.period()));

		ChecklistModel savedChecklist = checklistRepository.save(checklist);

		List<TaskModel> existingTasks = taskRepository.findAllByChecklist_IdAndActiveTrue(savedChecklist.getId());
		List<TaskModel> savedTasks = upsertTasks(savedChecklist, request.sections(), existingTasks);
		LOGGER.debug("updateChecklist: saved checklistId={} tasks={}", savedChecklist.getId(), savedTasks.size());

		String currentKey = PeriodKeyUtil.currentPeriodKey(savedChecklist.getFrequency(), ZoneId.systemDefault());
		Map<String, TaskPeriodStateModel> stateMap = loadStatesForTasksAndPeriod(
			orgId,
			principal.getUserId(),
			savedTasks,
			Set.of(currentKey)
		);

		return toCardResponse(savedChecklist, savedTasks, currentKey, stateMap);
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

		LOGGER.debug(
			"setTaskCompletion(orgId={}, userId={}, checklistId={}, taskId={}, state={}, periodKey={})",
			principal.getOrganizationId(),
			principal.getUserId(),
			checklistId,
			taskId,
			request.state(),
			request.periodKey()
		);
		ChecklistModel checklist = checklistRepository
			.findByIdAndOrganization_Id(checklistId, principal.getOrganizationId())
			.orElseThrow(() -> new IllegalArgumentException("Checklist not found."));

		TaskModel task = taskRepository.findById(taskId)
			.orElseThrow(() -> new IllegalArgumentException("Task not found."));
		if (!Objects.equals(task.getChecklist().getId(), checklist.getId())) {
			throw new IllegalArgumentException("Task does not belong to checklist.");
		}

		String state = String.valueOf(request.state()).trim().toLowerCase();
		if (!state.equals("completed") && !state.equals("todo")) {
			throw new IllegalArgumentException("Invalid state: " + request.state());
		}

		PeriodKeyUtil.validatePeriodKey(request.periodKey(), checklist.getFrequency());
		String periodKey = request.periodKey().trim();

		TaskPeriodStateModel row = taskPeriodStateRepository
			.findByOrganization_IdAndUser_IdAndTask_IdAndPeriodKey(
				principal.getOrganizationId(),
				principal.getUserId(),
				task.getId(),
				periodKey
			)
			.orElse(null);

		if (state.equals("completed")) {
			if (row == null) row = new TaskPeriodStateModel();
			row.setOrganization(getOrganization(principal.getOrganizationId()));
			row.setUser(getUser(principal.getUserId()));
			row.setTask(task);
			row.setPeriodKey(periodKey);
			row.setCompletedAt(request.completedAt() != null ? request.completedAt() : Instant.now());
			row.setFlaggedAt(null);
			row = taskPeriodStateRepository.save(row);
		} else {
			if (row != null) {
				row.setCompletedAt(null);
				if (row.getFlaggedAt() == null) {
					taskPeriodStateRepository.delete(row);
					row = null;
				} else {
					row = taskPeriodStateRepository.save(row);
				}
			}
		}

		return toTaskItemResponse(task, periodKey, row);
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

		LOGGER.debug(
			"setTaskFlag(orgId={}, userId={}, checklistId={}, taskId={}, state={}, periodKey={})",
			principal.getOrganizationId(),
			principal.getUserId(),
			checklistId,
			taskId,
			request.state(),
			request.periodKey()
		);
		ChecklistModel checklist = checklistRepository
			.findByIdAndOrganization_Id(checklistId, principal.getOrganizationId())
			.orElseThrow(() -> new IllegalArgumentException("Checklist not found."));

		TaskModel task = taskRepository.findById(taskId)
			.orElseThrow(() -> new IllegalArgumentException("Task not found."));
		if (!Objects.equals(task.getChecklist().getId(), checklist.getId())) {
			throw new IllegalArgumentException("Task does not belong to checklist.");
		}

		String state = String.valueOf(request.state()).trim().toLowerCase();
		if (!state.equals("pending") && !state.equals("todo")) {
			throw new IllegalArgumentException("Invalid state: " + request.state());
		}

		PeriodKeyUtil.validatePeriodKey(request.periodKey(), checklist.getFrequency());
		String periodKey = request.periodKey().trim();

		TaskPeriodStateModel row = taskPeriodStateRepository
			.findByOrganization_IdAndUser_IdAndTask_IdAndPeriodKey(
				principal.getOrganizationId(),
				principal.getUserId(),
				task.getId(),
				periodKey
			)
			.orElse(null);

		if (state.equals("pending")) {
			if (row == null) row = new TaskPeriodStateModel();
			row.setOrganization(getOrganization(principal.getOrganizationId()));
			row.setUser(getUser(principal.getUserId()));
			row.setTask(task);
			row.setPeriodKey(periodKey);
			row.setFlaggedAt(request.flaggedAt() != null ? request.flaggedAt() : Instant.now());
			row.setCompletedAt(null);
			row = taskPeriodStateRepository.save(row);
		} else {
			if (row != null) {
				row.setFlaggedAt(null);
				if (row.getCompletedAt() == null) {
					taskPeriodStateRepository.delete(row);
					row = null;
				} else {
					row = taskPeriodStateRepository.save(row);
				}
			}
		}

		return toTaskItemResponse(task, periodKey, row);
	}

	@Override
	public void deleteChecklist(Long checklistId, JwtAuthenticatedPrincipal principal) {
		requirePrincipal(principal);
		UUID orgId = principal.getOrganizationId();
		LOGGER.debug("deleteChecklist(orgId={}, userId={}, checklistId={})", orgId, principal.getUserId(), checklistId);

		ChecklistModel checklist = checklistRepository.findByIdAndOrganization_Id(checklistId, orgId)
			.orElseThrow(() -> new IllegalArgumentException("Checklist not found."));

		List<TaskModel> tasks = taskRepository.findAllByChecklist_IdAndActiveTrue(checklist.getId());
		if (!tasks.isEmpty()) {
			taskPeriodStateRepository.deleteAllByTaskIn(tasks);
			temperatureMeasurementRepository.deleteAllByTaskIn(tasks);
			taskRepository.deleteAll(tasks);
		}
		checklistRepository.delete(checklist);
	}

	private ChecklistCardResponse toCardResponse(
		ChecklistModel checklist,
		List<TaskModel> tasks,
		String currentPeriodKey,
		Map<String, TaskPeriodStateModel> stateByTaskAndPeriod
	) {
		List<ChecklistSectionResponse> sections = toSectionResponses(tasks, currentPeriodKey, stateByTaskAndPeriod);
		return new ChecklistCardResponse(
			checklist.getId(),
			PeriodKeyUtil.frequencyToPeriod(checklist.getFrequency()),
			checklist.getName(),
			checklist.getDescription() != null ? checklist.getDescription() : "",
			null,
			false,
			"",
			"muted",
			null,
			sections
		);
	}

	private List<ChecklistSectionResponse> toSectionResponses(
		List<TaskModel> tasks,
		String currentPeriodKey,
		Map<String, TaskPeriodStateModel> stateByTaskAndPeriod
	) {
		Map<Integer, Map<String, List<TaskModel>>> grouped = new LinkedHashMap<>();
		for (TaskModel task : tasks) {
			Integer sectionIndex = task.getSectionOrderIndex();
			int index = sectionIndex != null ? sectionIndex : 0;
			String title = (task.getSectionTitle() == null || task.getSectionTitle().isBlank())
				? "Tasks"
				: task.getSectionTitle().trim();

			grouped.computeIfAbsent(index, ignored -> new LinkedHashMap<>())
				.computeIfAbsent(title, ignored -> new ArrayList<>())
				.add(task);
		}

		List<Integer> sectionOrder = new ArrayList<>(grouped.keySet());
		sectionOrder.sort(Comparator.naturalOrder());

		List<ChecklistSectionResponse> sections = new ArrayList<>();
		for (Integer sectionIndex : sectionOrder) {
			Map<String, List<TaskModel>> byTitle = grouped.getOrDefault(sectionIndex, Map.of());
			for (Map.Entry<String, List<TaskModel>> entry : byTitle.entrySet()) {
				List<ChecklistTaskItemResponse> items = entry.getValue().stream()
					.sorted(Comparator.comparingInt(TaskModel::getOrderIndex))
					.map(task -> {
						String lookupKey = task.getId() + "|" + currentPeriodKey;
						TaskPeriodStateModel state = stateByTaskAndPeriod.get(lookupKey);
						return toTaskItemResponse(task, currentPeriodKey, state);
					})
					.toList();
				sections.add(new ChecklistSectionResponse(entry.getKey(), items));
			}
		}
		return sections;
	}

	private ChecklistTaskItemResponse toTaskItemResponse(TaskModel task, String periodKey, TaskPeriodStateModel state) {
		String resolvedState = "todo";
		String completedFor = null;
		Instant completedAt = null;
		String pendingFor = null;
		boolean highlighted = false;

		if (state != null && state.getCompletedAt() != null) {
			resolvedState = "completed";
			completedFor = periodKey;
			completedAt = state.getCompletedAt();
		} else if (state != null && state.getFlaggedAt() != null) {
			resolvedState = "pending";
			pendingFor = periodKey;
			highlighted = true;
		}

		String type = task.getTaskType() == ChecklistTaskType.TEMPERATURE ? "temperature" : null;

		return new ChecklistTaskItemResponse(
			task.getId(),
			task.getTitle(),
			task.getDescription(),
			type,
			task.getUnit(),
			task.getTargetMin(),
			task.getTargetMax(),
			resolvedState,
			highlighted ? Boolean.TRUE : null,
			completedFor,
			completedAt,
			pendingFor
		);
	}

	private List<TaskModel> upsertTasks(
		ChecklistModel checklist,
		List<ChecklistSectionUpsertRequest> sections,
		List<TaskModel> existingTasks
	) {
		List<ChecklistSectionUpsertRequest> safeSections = sections != null ? sections : List.of();
		Map<Long, TaskModel> existingById = existingTasks.stream()
			.filter(t -> t.getId() != null)
			.collect(Collectors.toMap(TaskModel::getId, t -> t));

		List<TaskModel> toSave = new ArrayList<>();
		Set<Long> keepIds = new java.util.HashSet<>();

		for (int sectionIndex = 0; sectionIndex < safeSections.size(); sectionIndex++) {
			ChecklistSectionUpsertRequest section = safeSections.get(sectionIndex);
			String sectionTitle = requireText(section.title(), "section.title");
			List<ChecklistTaskUpsertRequest> items = section.items() != null ? section.items() : List.of();

			for (int taskIndex = 0; taskIndex < items.size(); taskIndex++) {
				ChecklistTaskUpsertRequest item = items.get(taskIndex);
				String label = requireText(item.label(), "task.label");

				Long id = parseLongOrNull(item.id());
				TaskModel task = (id != null) ? existingById.get(id) : null;
				if (task == null) task = new TaskModel();

				task.setChecklist(checklist);
				task.setTitle(label);
				task.setDescription(trimToNull(item.meta()));
				task.setSectionTitle(sectionTitle);
				task.setSectionOrderIndex(sectionIndex);
				task.setOrderIndex(taskIndex);
				task.setActive(true);
				task.setRequiredTask(true);
				task.setOrganisationId(1);

				boolean isTemperature = "temperature".equalsIgnoreCase(String.valueOf(item.type()));
				task.setTaskType(isTemperature ? ChecklistTaskType.TEMPERATURE : ChecklistTaskType.STANDARD);
				task.setUnit(isTemperature ? (item.unit() != null ? item.unit() : "C") : null);
				task.setTargetMin(isTemperature ? item.targetMin() : null);
				task.setTargetMax(isTemperature ? item.targetMax() : null);

				toSave.add(task);
				if (task.getId() != null) keepIds.add(task.getId());
			}
		}

		List<TaskModel> saved = taskRepository.saveAll(toSave);

		List<TaskModel> toDelete = existingTasks.stream()
			.filter(t -> t.getId() != null && !keepIds.contains(t.getId()))
			.toList();
		if (!toDelete.isEmpty()) {
			taskPeriodStateRepository.deleteAllByTaskIn(toDelete);
			temperatureMeasurementRepository.deleteAllByTaskIn(toDelete);
			taskRepository.deleteAll(toDelete);
		}

		return saved;
	}

	private Map<String, TaskPeriodStateModel> loadStatesForTasksAndPeriod(
		UUID organizationId,
		UUID userId,
		Collection<TaskModel> tasks,
		Collection<String> periodKeys
	) {
		Set<Long> taskIds = tasks.stream().map(TaskModel::getId).filter(Objects::nonNull).collect(Collectors.toSet());
		if (taskIds.isEmpty() || periodKeys == null || periodKeys.isEmpty()) return Map.of();

		List<TaskPeriodStateModel> states = taskPeriodStateRepository
			.findAllByOrganization_IdAndUser_IdAndTask_IdInAndPeriodKeyIn(organizationId, userId, taskIds, periodKeys);

		Map<String, TaskPeriodStateModel> map = new HashMap<>();
		for (TaskPeriodStateModel state : states) {
			map.put(state.getTask().getId() + "|" + state.getPeriodKey(), state);
		}
		return map;
	}

	private OrganizationModel getOrganization(UUID orgId) {
		return organizationRepository.findById(orgId)
			.orElseThrow(() -> new IllegalArgumentException("Organization not found."));
	}

	private UserModel getUser(UUID userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("User not found."));
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

	private Long parseLongOrNull(String raw) {
		if (raw == null) return null;
		String trimmed = raw.trim();
		if (trimmed.isEmpty()) return null;
		if (!trimmed.matches("\\d+")) return null;
		try {
			return Long.parseLong(trimmed);
		} catch (NumberFormatException ignored) {
			return null;
		}
	}
}
