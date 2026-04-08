package edu.ntnu.idatt2105.backend.temperature.service;

import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.CreateTemperatureMeasurementRequest;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.TemperatureMeasurementResponse;
import edu.ntnu.idatt2105.backend.checklist.model.ChecklistModel;
import edu.ntnu.idatt2105.backend.temperature.model.TemperatureMeasurementModel;
import edu.ntnu.idatt2105.backend.task.model.TasksModel;
import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.checklist.repository.ChecklistRepository;
import edu.ntnu.idatt2105.backend.temperature.repository.TemperatureMeasurementRepository;
import edu.ntnu.idatt2105.backend.task.repository.TasksRepository;
import edu.ntnu.idatt2105.backend.checklist.service.ChecklistCacheStateService;
import edu.ntnu.idatt2105.backend.common.service.TemperatureMeasurementService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TemperatureMeasurementServiceImpl implements TemperatureMeasurementService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TemperatureMeasurementServiceImpl.class);

	private final TemperatureMeasurementRepository temperatureMeasurementRepository;
	private final ChecklistRepository checklistRepository;
	private final TasksRepository tasksRepository;
	private final OrganizationRepository organizationRepository;
	private final UserRepository userRepository;
	private final ChecklistCacheStateService checklistCacheStateService;

	public TemperatureMeasurementServiceImpl(
		TemperatureMeasurementRepository temperatureMeasurementRepository,
		ChecklistRepository checklistRepository,
		TasksRepository tasksRepository,
		OrganizationRepository organizationRepository,
		UserRepository userRepository,
		ChecklistCacheStateService checklistCacheStateService
	) {
		this.temperatureMeasurementRepository = temperatureMeasurementRepository;
		this.checklistRepository = checklistRepository;
		this.tasksRepository = tasksRepository;
		this.organizationRepository = organizationRepository;
		this.userRepository = userRepository;
		this.checklistCacheStateService = checklistCacheStateService;
	}

	@Override
	public TemperatureMeasurementResponse createMeasurement(
		CreateTemperatureMeasurementRequest request,
		JwtAuthenticatedPrincipal principal
	) {
		requirePrincipal(principal);
		Objects.requireNonNull(request, "Request cannot be null.");

		UUID orgId = principal.getOrganizationId();
		LOGGER.debug(
			"createMeasurement(orgId={}, userId={}, module={}, checklistId={}, taskId={}, valueC={}, periodKey={})",
			orgId,
			principal.getUserId(),
			request.module(),
			request.checklistId(),
			request.taskId(),
			request.valueC(),
			request.periodKey()
		);
		IcModule module = Objects.requireNonNull(request.module(), "module is required.");
		ComplianceArea area = module.toComplianceArea();

		ChecklistModel checklist = checklistRepository.findByIdAndOrganization_Id(request.checklistId(), orgId)
			.orElseThrow(() -> new IllegalArgumentException("Checklist not found."));

		if (checklist.getComplianceArea() != area) {
			throw new IllegalArgumentException("module does not match checklist.");
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

		TemperatureMeasurementModel model = new TemperatureMeasurementModel();
		model.setComplianceArea(area);
		model.setChecklist(checklist);
		model.setTask(task);
		model.setPeriodKey(resolvedPeriodKey);
		model.setValueC(request.valueC());
		model.setMeasuredAt(request.measuredAt() != null ? request.measuredAt() : LocalDateTime.now());

		OrganizationModel org = organizationRepository.findById(orgId)
			.orElseThrow(() -> new IllegalArgumentException("Organization not found."));
		UserModel user = userRepository.findById(principal.getUserId())
			.orElseThrow(() -> new IllegalArgumentException("User not found."));
		model.setOrganization(org);
		model.setRecordedBy(user);

		TemperatureMeasurementModel saved = temperatureMeasurementRepository.save(model);
		checklistCacheStateService.touch(orgId, area);
		LOGGER.debug("createMeasurement: saved measurementId={}", saved.getId());
		return toResponse(saved, module);
	}

	@Override
	public List<TemperatureMeasurementResponse> fetchMeasurements(
		IcModule module,
		LocalDateTime from,
		LocalDateTime to,
		JwtAuthenticatedPrincipal principal
	) {
		requirePrincipal(principal);
		IcModule safeModule = Objects.requireNonNull(module, "module is required.");

		LocalDateTime fromInstant = from != null ? from : LocalDateTime.MIN;
		LocalDateTime toInstant = to != null ? to : LocalDateTime.now();
		if (toInstant.isBefore(fromInstant)) {
			throw new IllegalArgumentException("to must be after from.");
		}

		List<TemperatureMeasurementModel> rows = temperatureMeasurementRepository
			.findAllByOrganization_IdAndComplianceAreaAndMeasuredAtBetweenOrderByMeasuredAtDesc(
				principal.getOrganizationId(),
				safeModule.toComplianceArea(),
				fromInstant,
				toInstant
			);

		LOGGER.debug("fetchMeasurements(orgId={}, userId={}, module={}): returned {}", principal.getOrganizationId(), principal.getUserId(), safeModule, rows.size());
		return rows.stream().map(row -> toResponse(row, safeModule)).toList();
	}

	private TemperatureMeasurementResponse toResponse(TemperatureMeasurementModel model, IcModule module) {
		return new TemperatureMeasurementResponse(
			model.getId(),
			module,
			model.getChecklist().getId(),
			model.getTask().getId(),
			model.getValueC(),
			model.getMeasuredAt(),
			model.getPeriodKey(),
			isTemperatureDeviation(model)
		);
	}

	private boolean isTemperatureTask(TasksModel task) {
		return task.getTaskTemplate() != null && (
			task.getTaskTemplate().getUnit() != null ||
			task.getTaskTemplate().getTargetMin() != null ||
			task.getTaskTemplate().getTargetMax() != null
		);
	}

	private boolean isTemperatureDeviation(TemperatureMeasurementModel measurement) {
		if (measurement == null || measurement.getTask() == null || measurement.getTask().getTaskTemplate() == null) {
			return false;
		}

		if (measurement.getTask().getTaskTemplate().getTargetMin() != null
			&& measurement.getValueC().compareTo(measurement.getTask().getTaskTemplate().getTargetMin()) < 0) {
			return true;
		}
		if (measurement.getTask().getTaskTemplate().getTargetMax() != null
			&& measurement.getValueC().compareTo(measurement.getTask().getTaskTemplate().getTargetMax()) > 0) {
			return true;
		}
		return false;
	}

	private JwtAuthenticatedPrincipal requirePrincipal(JwtAuthenticatedPrincipal principal) {
		if (principal == null) throw new IllegalArgumentException("Authentication required.");
		if (principal.getOrganizationId() == null) throw new IllegalArgumentException("Organization required.");
		if (principal.getUserId() == null) throw new IllegalArgumentException("User required.");
		return principal;
	}
}
