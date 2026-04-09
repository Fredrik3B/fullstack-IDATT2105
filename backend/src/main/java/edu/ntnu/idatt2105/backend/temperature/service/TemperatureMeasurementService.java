package edu.ntnu.idatt2105.backend.temperature.service;

import edu.ntnu.idatt2105.backend.temperature.dto.CreateTemperatureMeasurementRequest;
import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import edu.ntnu.idatt2105.backend.temperature.dto.TemperatureMeasurementResponse;
import edu.ntnu.idatt2105.backend.checklist.model.ChecklistModel;
import edu.ntnu.idatt2105.backend.checklist.service.ChecklistCacheStateService;
import edu.ntnu.idatt2105.backend.temperature.mapper.TemperatureMapper;
import edu.ntnu.idatt2105.backend.temperature.model.TemperatureMeasurementModel;
import edu.ntnu.idatt2105.backend.task.model.TasksModel;
import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.checklist.repository.ChecklistRepository;
import edu.ntnu.idatt2105.backend.temperature.repository.TemperatureMeasurementRepository;
import edu.ntnu.idatt2105.backend.task.repository.TasksRepository;
import edu.ntnu.idatt2105.backend.checklist.service.icchecklist.PeriodKeyUtil;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import edu.ntnu.idatt2105.backend.user.repository.OrganizationRepository;
import edu.ntnu.idatt2105.backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for recording and retrieving temperature measurements.
 *
 * <p>A measurement is always tied to an active checklist task ({@code task.active == true})
 * that is classified as a temperature-control task (has a unit or target range set on its
 * template). If the caller supplies an explicit {@code periodKey} it must match the task's
 * own period key; otherwise the task's period key is used automatically.
 */
@Service
@AllArgsConstructor
public class TemperatureMeasurementService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TemperatureMeasurementService.class);

	private final TemperatureMeasurementRepository temperatureMeasurementRepository;
	private final ChecklistRepository checklistRepository;
	private final TasksRepository tasksRepository;
	private final OrganizationRepository organizationRepository;
	private final UserRepository userRepository;
	private final ChecklistCacheStateService checklistCacheStateService;
	private final TemperatureMapper temperatureMapper;

	/**
	 * Records a new temperature measurement against an active checklist task.
	 *
	 * <p>Validates that the checklist belongs to the caller's organisation, the task is active,
	 * and the task is a temperature-control task. If a {@code periodKey} is supplied it must
	 * match the task's own period key.
	 *
	 * @param request   measurement data including module, checklist/task IDs, value, and optional period key
	 * @param principal the authenticated principal used to resolve the organisation and recorded-by user
	 * @return the persisted measurement as a response DTO with deviation flag
	 * @throws IllegalArgumentException if validation fails (checklist/task not found, wrong module, inactive task, etc.)
	 */
	public TemperatureMeasurementResponse createMeasurement(
		CreateTemperatureMeasurementRequest request,
		JwtAuthenticatedPrincipal principal
	) {
		UUID orgId = principal.requireOrganizationId();
		ComplianceArea area = request.module().toComplianceArea();
		LOGGER.info(
			"createMeasurement(orgId={}, userId={}, module={}, checklistId={}, taskId={}, valueC={}, periodKey={})",
			orgId,
			principal.getUserId(),
			request.module(),
			request.checklistId(),
			request.taskId(),
			request.valueC(),
			request.periodKey()
		);

		ChecklistModel checklist = checklistRepository.findByIdAndOrganization_Id(request.checklistId(), orgId)
				.orElseThrow(() -> new IllegalArgumentException("Checklist not found."));
		if (checklist.getComplianceArea() != area) {
			throw new IllegalArgumentException("Module does not match checklist.");
		}

		TasksModel task = tasksRepository.findByIdAndChecklist_Id(request.taskId(), checklist.getId())
			.orElseThrow(() -> new IllegalArgumentException("Activated task not found."));
		if (!task.isActive()) {
			throw new IllegalArgumentException("Temperature can only be logged for active checklist tasks.");
		}
		if (!isTemperatureTask(task)) {
			throw new IllegalArgumentException("Temperature can only be logged for temperature-controlled tasks.");
		}

		String resolvedPeriodKey = task.getPeriodKey();
		if (request.periodKey() != null && !request.periodKey().isBlank()) {
			PeriodKeyUtil.validatePeriodKey(request.periodKey(), checklist.getFrequency());
			String requestedPeriodKey = request.periodKey().trim();
			if (!Objects.equals(requestedPeriodKey, resolvedPeriodKey)) {
				throw new IllegalArgumentException("periodKey does not match activated task.");
			}
			resolvedPeriodKey = requestedPeriodKey;
		}

		TemperatureMeasurementModel model = TemperatureMeasurementModel.builder()
				.complianceArea(area)
				.checklist(checklist)
				.task(task)
				.periodKey(resolvedPeriodKey)
				.valueC(request.valueC())
				.measuredAt(request.measuredAt() != null ? request.measuredAt() : LocalDateTime.now())
				.organization(organizationRepository.getReferenceById(orgId))
				.recordedBy(userRepository.getReferenceById(principal.getUserId()))
				.build();

		TemperatureMeasurementModel saved = temperatureMeasurementRepository.save(model);
		checklistCacheStateService.touch(orgId, area);
		LOGGER.info("Measurement saved: id={} orgId={} taskId={}", saved.getId(), orgId, request.taskId());
		return temperatureMapper.toMeasurementResponse(saved, request.module());
	}

	/**
	 * Returns temperature measurements for the caller's organisation filtered by module and date range.
	 *
	 * <p>If {@code from} is {@code null} it defaults to {@link java.time.LocalDateTime#MIN};
	 * if {@code to} is {@code null} it defaults to now. Results are ordered by {@code measuredAt} descending.
	 *
	 * @param module    the IC module whose compliance area scopes the query
	 * @param from      inclusive lower bound for {@code measuredAt} (nullable)
	 * @param to        inclusive upper bound for {@code measuredAt} (nullable)
	 * @param principal the authenticated principal used to resolve the organisation
	 * @return list of measurement response DTOs ordered newest first
	 * @throws IllegalArgumentException if {@code to} is before {@code from}
	 */
	public List<TemperatureMeasurementResponse> fetchMeasurements(
		IcModule module,
		LocalDateTime from,
		LocalDateTime to,
		JwtAuthenticatedPrincipal principal
	) {
		UUID orgId = principal.requireOrganizationId();

		LocalDateTime safeFrom = from != null ? from : LocalDateTime.MIN;
		LocalDateTime safeTo = to != null ? to : LocalDateTime.now();
		if (safeTo.isBefore(safeFrom)) {
			throw new IllegalArgumentException("'to' must be after 'from'.");
		}

		return temperatureMeasurementRepository
				.findAllByOrganization_IdAndComplianceAreaAndMeasuredAtBetweenOrderByMeasuredAtDesc(
						orgId, module.toComplianceArea(), safeFrom, safeTo)
				.stream()
				.map(row -> temperatureMapper.toMeasurementResponse(row, module))
				.toList();
	}

	/**
	 * Returns {@code true} if the task's template has any temperature-control fields set
	 * (unit, targetMin, or targetMax).
	 *
	 * @param task the activated task to inspect
	 * @return {@code true} if the task is a temperature-control task
	 */
	private boolean isTemperatureTask(TasksModel task) {
		return task.getTaskTemplate() != null && (
			task.getTaskTemplate().getUnit() != null ||
			task.getTaskTemplate().getTargetMin() != null ||
			task.getTaskTemplate().getTargetMax() != null
		);
	}
}
