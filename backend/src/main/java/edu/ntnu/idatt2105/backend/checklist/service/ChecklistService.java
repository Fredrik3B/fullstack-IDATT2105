package edu.ntnu.idatt2105.backend.checklist.service;

import edu.ntnu.idatt2105.backend.checklist.dto.*;
import edu.ntnu.idatt2105.backend.checklist.mapper.ChecklistMapper;
import edu.ntnu.idatt2105.backend.checklist.model.ChecklistModel;
import edu.ntnu.idatt2105.backend.checklist.model.enums.ChecklistFrequency;
import edu.ntnu.idatt2105.backend.checklist.repository.ChecklistRepository;
import edu.ntnu.idatt2105.backend.checklist.service.icchecklist.PeriodKeyUtil;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import edu.ntnu.idatt2105.backend.task.model.TaskTemplate;
import edu.ntnu.idatt2105.backend.task.model.TasksModel;
import edu.ntnu.idatt2105.backend.task.repository.TaskTemplateRepository;
import edu.ntnu.idatt2105.backend.task.repository.TasksRepository;
import edu.ntnu.idatt2105.backend.temperature.model.TemperatureMeasurementModel;
import edu.ntnu.idatt2105.backend.temperature.repository.TemperatureMeasurementRepository;
import edu.ntnu.idatt2105.backend.user.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChecklistService {

	private final ChecklistRepository checklistRepository;
	private final TaskTemplateRepository taskTemplateRepository;
	private final TasksRepository tasksRepository;
	private final TemperatureMeasurementRepository temperatureMeasurementRepository;
	private final OrganizationRepository organizationRepository;
	private final ChecklistCacheStateService checklistCacheStateService;
	private final ChecklistMapper checklistMapper;


	public List<ChecklistCardResponse> fetchChecklists(IcModule module, JwtAuthenticatedPrincipal principal) {
		UUID orgId = principal.requireOrganizationId();
		return checklistRepository
				.findAllByOrganization_IdAndComplianceAreaAndActiveTrueOrderByIdAsc(orgId, module.toComplianceArea())
				.stream()
				.map(this::sortTemplates)
				.map(this::buildCardResponse)
				.toList();
	}

	public Instant fetchChecklistsLastModified(IcModule module, JwtAuthenticatedPrincipal principal) {
		return checklistCacheStateService.getLastModified(principal.requireOrganizationId(), module.toComplianceArea());
	}

	@Transactional
	public ChecklistCardResponse createChecklist(CreateChecklistCardRequest request, JwtAuthenticatedPrincipal principal) {
		UUID orgId = principal.requireOrganizationId();
		ComplianceArea area = request.module().toComplianceArea();
		ChecklistFrequency frequency = PeriodKeyUtil.periodToFrequency(request.period());

		ChecklistModel checklist = ChecklistModel.builder()
				.name(request.title().trim())
				.description(trimToNull(request.subtitle()))
				.frequency(frequency)
				.complianceArea(area)
				.activePeriodKey(PeriodKeyUtil.currentPeriodKey(frequency, ZoneId.systemDefault()))
				.recurring(Boolean.TRUE.equals(request.recurring()))
				.displayedOnWorkbench(Boolean.TRUE.equals(request.displayedOnWorkbench()))
				.organization(organizationRepository.getReferenceById(orgId))
				.taskTemplates(resolveSelectedTemplates(request.taskTemplateIds(), orgId, area))
				.build();

		ChecklistModel saved = checklistRepository.save(checklist);
		checklistCacheStateService.touch(orgId, area);
		return buildCardResponse(sortTemplates(saved));
	}

	@Transactional
	public ChecklistCardResponse updateChecklist(Long id, UpdateChecklistCardRequest request, JwtAuthenticatedPrincipal principal) {
		UUID orgId = principal.requireOrganizationId();
		ChecklistModel cl = requireOwn(id, orgId);
		ChecklistFrequency prevFreq = cl.getFrequency();

		cl.setName(request.title().trim());
		cl.setDescription(trimToNull(request.subtitle()));
		cl.setFrequency(PeriodKeyUtil.periodToFrequency(request.period()));
		cl.setRecurring(request.recurring() == null || request.recurring());
		cl.setDisplayedOnWorkbench(request.displayedOnWorkbench() == null || request.displayedOnWorkbench());

		if (!Objects.equals(prevFreq, cl.getFrequency())) {
			cl.setActivePeriodKey(PeriodKeyUtil.currentPeriodKey(cl.getFrequency(), ZoneId.systemDefault()));
			deactivateTasks(cl.getId());
		}
		cl.setTaskTemplates(resolveSelectedTemplates(request.taskTemplateIds(), orgId, cl.getComplianceArea()));
		if (!cl.isDisplayedOnWorkbench()) deleteAllTasks(cl.getId());

		checklistCacheStateService.touch(orgId, cl.getComplianceArea());
		return buildCardResponse(sortTemplates(checklistRepository.save(cl)));
	}

	@Transactional
	public ChecklistCardResponse setChecklistWorkbenchState(Long id, ChecklistWorkbenchStateRequest request, JwtAuthenticatedPrincipal principal) {
		UUID orgId = principal.requireOrganizationId();
		ChecklistModel cl = requireOwn(id, orgId);
		cl.setDisplayedOnWorkbench(Boolean.TRUE.equals(request.displayedOnWorkbench()));
		if (!cl.isDisplayedOnWorkbench()) deleteAllTasks(cl.getId());

		checklistCacheStateService.touch(orgId, cl.getComplianceArea());
		return buildCardResponse(sortTemplates(checklistRepository.save(cl)));
	}

	@Transactional
	public void deleteChecklist(Long id, JwtAuthenticatedPrincipal principal) {
		UUID orgId = principal.requireOrganizationId();
		ChecklistModel cl = requireOwn(id, orgId);
		deleteAllTasks(cl.getId());
		checklistRepository.delete(cl);
		checklistCacheStateService.touch(orgId, cl.getComplianceArea());
	}

	@Transactional
	public ChecklistTaskItemResponse setTaskCompletion(Long checklistId, Long taskId, TaskCompletionRequest request, JwtAuthenticatedPrincipal principal) {
		UUID orgId = principal.requireOrganizationId();
		ChecklistModel cl = requireOwn(checklistId, orgId);
		TasksModel task = requireActiveTask(taskId, checklistId, request.periodKey(), cl.getFrequency());

		String state = request.state().trim().toLowerCase();
		if (!state.equals("completed") && !state.equals("todo")) {
			throw new IllegalArgumentException("Invalid state: " + request.state());
		}

		if (state.equals("completed") && isTemperatureTemplate(task.getTaskTemplate())) {
			boolean hasMeasurement = temperatureMeasurementRepository.existsByTask_IdAndPeriodKey(task.getId(), task.getPeriodKey());
			if (!hasMeasurement) {
				throw new IllegalArgumentException("Temperature tasks require a saved reading before completion.");
			}
		}

		task.setCompleted(state.equals("completed"));
		task.setFlagged(false);
		task.setEndedAt(state.equals("completed") ? request.completedAt() : null);

		checklistCacheStateService.touch(orgId, cl.getComplianceArea());
		TasksModel saved = tasksRepository.save(task);
		return checklistMapper.toTaskItemResponse(saved, latestMeasurement(saved.getId()));
	}

	@Transactional
	public ChecklistTaskItemResponse setTaskFlag(Long checklistId, Long taskId, TaskFlagRequest request, JwtAuthenticatedPrincipal principal) {
		UUID orgId = principal.requireOrganizationId();
		ChecklistModel cl = requireOwn(checklistId, orgId);
		TasksModel task = requireActiveTask(taskId, checklistId, request.periodKey(), cl.getFrequency());

		String state = request.state().trim().toLowerCase();
		if (!state.equals("pending") && !state.equals("todo")) {
			throw new IllegalArgumentException("Invalid state: " + request.state());
		}

		task.setFlagged(state.equals("pending"));
		if (task.isFlagged()) { task.setCompleted(false); task.setEndedAt(null); }

		checklistCacheStateService.touch(orgId, cl.getComplianceArea());
		TasksModel saved = tasksRepository.save(task);
		return checklistMapper.toTaskItemResponse(saved, latestMeasurement(saved.getId()));
	}

	@Transactional
	public ChecklistCardResponse submitChecklist(Long checklistId, JwtAuthenticatedPrincipal principal) {
		UUID orgId = principal.requireOrganizationId();
		ChecklistModel cl = synchronizePeriodState(requireOwn(checklistId, orgId));
		sortTemplates(cl);

		if (cl.getTaskTemplates() == null || cl.getTaskTemplates().isEmpty()) {
			throw new IllegalArgumentException("Checklist must contain at least one task.");
		}

		String activeKey = resolveActivePeriodKey(cl);
		String currentKey = PeriodKeyUtil.currentPeriodKey(cl.getFrequency(), ZoneId.systemDefault());
		if (!Objects.equals(activeKey, currentKey)) {
			throw new IllegalStateException("Already submitted for the current period.");
		}

		List<TasksModel> tasks = ensureTasksForPeriod(cl);
		if (tasks.isEmpty()) throw new IllegalArgumentException("No active tasks to submit.");

		tasks.forEach(t -> {
			if (t.getEndedAt() == null) {
				t.setEndedAt(PeriodKeyUtil.periodStartDate(cl.getFrequency(),
								PeriodKeyUtil.nextPeriodKey(cl.getFrequency(), t.getPeriodKey()))
						.atStartOfDay().minusSeconds(1));
			}
			t.setActive(false);
		});
		tasksRepository.saveAll(tasks);

		cl.setActivePeriodKey(PeriodKeyUtil.nextPeriodKey(cl.getFrequency(), activeKey));
		checklistCacheStateService.touch(orgId, cl.getComplianceArea());
		return buildCardResponse(sortTemplates(checklistRepository.save(cl)));
	}


	private ChecklistCardResponse buildCardResponse(ChecklistModel cl) {
		ChecklistModel synced = synchronizePeriodState(cl);
		String activeKey = resolveActivePeriodKey(synced);
		String currentKey = PeriodKeyUtil.currentPeriodKey(synced.getFrequency(), ZoneId.systemDefault());
		boolean shouldLoad = synced.isDisplayedOnWorkbench()
				&& (synced.isRecurring() || Objects.equals(activeKey, currentKey));

		List<TasksModel> tasks = shouldLoad ? ensureTasksForPeriod(synced) : List.of();
		return checklistMapper.toCardResponse(synced, activeKey, tasks, latestMeasurements(tasks), shouldLoad);
	}


	private ChecklistModel synchronizePeriodState(ChecklistModel cl) {
		String activeKey = resolveActivePeriodKey(cl);
		String currentKey = PeriodKeyUtil.currentPeriodKey(cl.getFrequency(), ZoneId.systemDefault());

		if (PeriodKeyUtil.periodStartDate(cl.getFrequency(), activeKey)
				.isBefore(PeriodKeyUtil.periodStartDate(cl.getFrequency(), currentKey))) {
			deactivateTasks(cl.getId());
			cl.setActivePeriodKey(currentKey);
			return sortTemplates(checklistRepository.save(cl));
		}
		return cl;
	}

	private String resolveActivePeriodKey(ChecklistModel cl) {
		if (cl.getActivePeriodKey() == null || cl.getActivePeriodKey().isBlank()) {
			cl.setActivePeriodKey(PeriodKeyUtil.currentPeriodKey(cl.getFrequency(), ZoneId.systemDefault()));
			checklistRepository.save(cl);
		}
		return cl.getActivePeriodKey();
	}


	private List<TasksModel> ensureTasksForPeriod(ChecklistModel cl) {
		String periodKey = resolveActivePeriodKey(cl);
		List<TasksModel> existing = tasksRepository.findAllByChecklist_IdAndPeriodKeyAndActiveTrue(cl.getId(), periodKey);

		Map<Long, TasksModel> byTemplateId = new LinkedHashMap<>();
		existing.forEach(t -> {
			if (t.getTaskTemplate() != null && t.getTaskTemplate().getId() != null)
				byTemplateId.put(t.getTaskTemplate().getId(), t);
		});

		List<TasksModel> toSave = new ArrayList<>();
		Set<Long> selectedIds = new LinkedHashSet<>();

		for (TaskTemplate tmpl : cl.getTaskTemplates()) {
			if (tmpl.getId() == null) continue;
			selectedIds.add(tmpl.getId());
			if (!byTemplateId.containsKey(tmpl.getId())) {
				toSave.add(TasksModel.builder()
						.checklist(cl).taskTemplate(tmpl).meta(tmpl.getMeta())
						.periodKey(periodKey).active(true).completed(false).flagged(false)
						.build());
			}
		}

		existing.stream()
				.filter(t -> t.getTaskTemplate() == null || !selectedIds.contains(t.getTaskTemplate().getId()))
				.forEach(t -> { t.setActive(false); toSave.add(t); });

		if (!toSave.isEmpty()) tasksRepository.saveAll(toSave);

		List<TasksModel> active = tasksRepository.findAllByChecklist_IdAndPeriodKeyAndActiveTrue(cl.getId(), periodKey);
		active.sort(Comparator
				.comparing((TasksModel t) -> checklistMapper.sectionLabel(t.getTaskTemplate().getSectionType()), String.CASE_INSENSITIVE_ORDER)
				.thenComparing(t -> t.getTaskTemplate().getTitle(), String.CASE_INSENSITIVE_ORDER));
		return active;
	}

	private void deactivateTasks(Long checklistId) {
		List<TasksModel> active = tasksRepository.findAllByChecklist_IdAndActiveTrue(checklistId);
		if (active.isEmpty()) return;
		active.forEach(t -> t.setActive(false));
		tasksRepository.saveAll(active);
	}

	private void deleteAllTasks(Long checklistId) {
		List<TasksModel> tasks = tasksRepository.findAllByChecklist_Id(checklistId);
		if (tasks.isEmpty()) return;
		temperatureMeasurementRepository.deleteAllByTaskIn(tasks);
		tasksRepository.deleteAll(tasks);
	}

	private Map<Long, TemperatureMeasurementModel> latestMeasurements(List<TasksModel> tasks) {
		List<Long> ids = tasks.stream().map(TasksModel::getId).filter(Objects::nonNull).toList();
		if (ids.isEmpty()) return Map.of();

		Map<Long, TemperatureMeasurementModel> result = new LinkedHashMap<>();
		temperatureMeasurementRepository.findAllByTask_IdInOrderByMeasuredAtDesc(ids)
				.forEach(m -> result.putIfAbsent(m.getTask().getId(), m));
		return result;
	}

	private TemperatureMeasurementModel latestMeasurement(Long taskId) {
		if (taskId == null) return null;
		return temperatureMeasurementRepository.findAllByTask_IdInOrderByMeasuredAtDesc(List.of(taskId))
				.stream().findFirst().orElse(null);
	}


	private ChecklistModel requireOwn(Long id, UUID orgId) {
		return checklistRepository.findByIdAndOrganization_Id(id, orgId)
				.orElseThrow(() -> new IllegalArgumentException("Checklist not found."));
	}

	private TasksModel requireActiveTask(Long taskId, Long checklistId, String periodKey, ChecklistFrequency freq) {
		PeriodKeyUtil.validatePeriodKey(periodKey, freq);
		String key = periodKey.trim();
		TasksModel task = tasksRepository.findByIdAndChecklist_Id(taskId, checklistId)
				.orElseThrow(() -> new IllegalArgumentException("Activated task not found."));
		if (!task.isActive()) throw new IllegalArgumentException("Task is not active.");
		if (!Objects.equals(task.getPeriodKey(), key)) throw new IllegalArgumentException("Task does not belong to requested period.");
		return task;
	}

	private Set<TaskTemplate> resolveSelectedTemplates(List<Long> ids, UUID orgId, ComplianceArea area) {
		if (ids == null || ids.isEmpty()) throw new IllegalArgumentException("taskTemplateIds must contain at least one task.");
		List<TaskTemplate> selected = taskTemplateRepository.findAllByIdInAndOrganisationIdAndComplianceAreaOrdered(ids, orgId, area);
		if (selected.size() != ids.stream().filter(Objects::nonNull).distinct().count())
			throw new IllegalArgumentException("One or more selected tasks were not found.");
		return new LinkedHashSet<>(selected);
	}

	private ChecklistModel sortTemplates(ChecklistModel cl) {
		List<TaskTemplate> sorted = new ArrayList<>(cl.getTaskTemplates());
		sorted.sort(checklistMapper.taskTemplateComparator());
		cl.setTaskTemplates(new LinkedHashSet<>(sorted));
		return cl;
	}

	private String trimToNull(String v) {
		if (v == null) return null;
		String t = v.trim();
		return t.isEmpty() ? null : t;
	}

	private boolean isTemperatureTemplate(TaskTemplate template) {
		return template.getUnit() != null || template.getTargetMin() != null || template.getTargetMax() != null;
	}
}