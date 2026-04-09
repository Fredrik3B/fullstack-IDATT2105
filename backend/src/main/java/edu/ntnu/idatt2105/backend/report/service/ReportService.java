package edu.ntnu.idatt2105.backend.report.service;


import edu.ntnu.idatt2105.backend.task.mapper.TaskMapper;
import edu.ntnu.idatt2105.backend.checklist.model.ChecklistModel;
import edu.ntnu.idatt2105.backend.task.model.TaskTemplate;
import edu.ntnu.idatt2105.backend.task.model.TasksModel;
import edu.ntnu.idatt2105.backend.temperature.model.TemperatureZoneModel;
import edu.ntnu.idatt2105.backend.checklist.model.enums.ChecklistFrequency;
import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.checklist.repository.ChecklistRepository;
import edu.ntnu.idatt2105.backend.task.repository.TasksRepository;
import edu.ntnu.idatt2105.backend.temperature.repository.TemperatureMeasurementRepository;
import edu.ntnu.idatt2105.backend.checklist.service.icchecklist.PeriodKeyUtil;
import edu.ntnu.idatt2105.backend.exception.ResourceNotFoundException;
import edu.ntnu.idatt2105.backend.report.dto.DeviationCreatedResponse;
import edu.ntnu.idatt2105.backend.report.dto.DeviationReport;
import edu.ntnu.idatt2105.backend.report.dto.shared.ChecklistRecord;
import edu.ntnu.idatt2105.backend.report.dto.shared.ChecklistSection;
import edu.ntnu.idatt2105.backend.report.dto.shared.ComplianceStats;
import edu.ntnu.idatt2105.backend.report.dto.shared.DeviationDayPoint;
import edu.ntnu.idatt2105.backend.report.dto.InspectionReport;
import edu.ntnu.idatt2105.backend.report.dto.InternalSummary;
import edu.ntnu.idatt2105.backend.report.dto.shared.MissedTaskRecord;
import edu.ntnu.idatt2105.backend.report.dto.shared.OrgSection;
import edu.ntnu.idatt2105.backend.report.dto.shared.ReportPeriod;
import edu.ntnu.idatt2105.backend.report.dto.shared.TemperaturePoint;
import edu.ntnu.idatt2105.backend.report.dto.shared.UnresolvedItemDto;
import edu.ntnu.idatt2105.backend.report.model.DeviationReportModel;
import edu.ntnu.idatt2105.backend.report.repository.DeviationReportRepository;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import edu.ntnu.idatt2105.backend.user.model.enums.RoleEnum;
import edu.ntnu.idatt2105.backend.user.repository.OrganizationRepository;
import edu.ntnu.idatt2105.backend.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service for generating compliance reports and persisting deviation reports.
 *
 * <p>The two main report types are:
 * <ul>
 *   <li>{@link #generateSummary} — a lightweight dashboard summary with task and temperature statistics</li>
 *   <li>{@link #generateInspection} — a full inspection report including checklist records,
 *       temperature log, missed-task breakdown, and per-day deviation counts</li>
 * </ul>
 * All report data is derived from existing task, temperature, checklist, and user records.
 * No data is written during report generation.
 */
@Service
@AllArgsConstructor
public class ReportService {

  private final TasksRepository tasksRepository;
  private final TemperatureMeasurementRepository tempRepository;
  private final ChecklistRepository checklistRepository;
  private final UserRepository userRepository;
  private final OrganizationRepository organizationRepository;
  private final TaskMapper taskMapper;
  private final DeviationReportRepository deviationReportRepository;

  private ComplianceStats buildStats(UUID orgId, LocalDateTime from, LocalDateTime to, ComplianceArea area) {
    int total = tasksRepository.contTaskInPeriod(orgId, from, to, area);
    int completed = tasksRepository.countCompletedInPeriod(orgId, from, to, area);
    int flagged = tasksRepository.countDeviatedInPeriod(orgId, from, to, area);
    int tempReadings = tempRepository.countReadingsInPeriod(orgId, from, to);
    int tempOutOfRange = tempRepository.countOutOfRangeInPeriod(orgId, from, to);

    return ComplianceStats.builder()
        .totalTasks(total)
        .completedTasks(completed)
        .deviatedTasks(flagged)
        .completionRate(total > 0 ? (double) completed / total * 100 : 0.0)
        .temperatureReadings(tempReadings)
        .outOfRangeReadings(tempOutOfRange)
        .outOfRangeRate(tempReadings > 0 ? (double) tempOutOfRange / tempReadings * 100 : 0.0)
        .build();
  }

  private List<UnresolvedItemDto> getUnresolvedItems(UUID orgId, LocalDateTime from, LocalDateTime to) {
    return tasksRepository.findDeviatedTaskByOrgIdInPeriod(orgId, from, to).stream()
        .map(taskMapper::toUnresolvedDto)
        .toList();
  }

  private List<String> getUsersByRole(List<UserModel> users, RoleEnum role) {
    return users.stream()
        .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName() == role))
        .map(u -> u.getFirstName() + " " + u.getLastName())
        .toList();
  }

  private OrgSection buildOrgSection(OrganizationModel org) {
    List<UserModel> orgUsers = userRepository.findAllByOrganization(org);

    List<String> admins = getUsersByRole(orgUsers, RoleEnum.ADMIN);
    List<String> managers = getUsersByRole(orgUsers, RoleEnum.MANAGER);

    return OrgSection.builder()
        .name(org.getName())
        .adminNames(admins)
        .managerNames(managers)
        .totalStaff(orgUsers.size())
        .build();
  }

  private ChecklistSection buildChecklistSection(UUID orgId, LocalDateTime from, LocalDateTime to) {
    List<ChecklistModel> checklists = checklistRepository.findAllByOrganizationId(orgId);
    List<TasksModel> tasksInPeriod = tasksRepository.findAllByOrgIdInPeriodWithRelations(orgId, from, to);
    Map<Long, List<TasksModel>> tasksByChecklist = tasksInPeriod.stream()
        .collect(Collectors.groupingBy(task -> task.getChecklist().getId()));

    List<ChecklistRecord> records = checklists.stream()
        .map(cl -> buildChecklistRecord(cl, tasksByChecklist.getOrDefault(cl.getId(), List.of()), from, to))
        .filter(r -> r.getCompletionsInPeriod() > 0)
        .sorted(Comparator.comparing(ChecklistRecord::getName, String.CASE_INSENSITIVE_ORDER))
        .toList();

    return ChecklistSection.builder()
        .totalChecklists(checklists.size())
        .activeChecklists((int) checklists.stream().filter(ChecklistModel::isActive).count())
        .checklists(records)
        .build();
  }

  private ChecklistRecord buildChecklistRecord(ChecklistModel cl, List<TasksModel> tasks, LocalDateTime from, LocalDateTime to) {
    Map<String, List<TasksModel>> byPeriod = tasks.stream().collect(Collectors.groupingBy(TasksModel::getPeriodKey));
    int total = tasks.size();
    int completed = (int) tasks.stream().filter(TasksModel::isCompleted).count();
    int deviated = (int) tasks.stream().filter(t -> !t.isCompleted() && !t.isActive()).count();

    double avgRate = byPeriod.isEmpty() ? 0.0
        : byPeriod.values().stream()
            .mapToDouble(run -> {
              long done = run.stream().filter(TasksModel::isCompleted).count();
              return run.isEmpty() ? 0.0 : (double) done / run.size() * 100;
            })
            .average().orElse(0.0);

    return ChecklistRecord.builder()
        .name(cl.getName())
        .description(cl.getDescription())
        .frequency(cl.getFrequency().name())
        .complianceArea(cl.getComplianceArea())
        .completionsInPeriod(byPeriod.size())
        .expectedRuns(countExpectedRuns(cl.getFrequency(), from, to))
        .totalTasks(total)
        .completedTasks(completed)
        .deviatedTasks(deviated)
        .completionRate(total > 0 ? (double) completed / total * 100 : 0.0)
        .averageCompletionRate(avgRate)
        .build();
  }

  private int countExpectedRuns(ChecklistFrequency frequency, LocalDateTime from, LocalDateTime to) {
    if (from == null || to == null || from.isAfter(to)) {
      return 0;
    }

    ZoneId zone = ZoneId.systemDefault();
    ChecklistFrequency safeFrequency = frequency != null ? frequency : ChecklistFrequency.DAILY;
    LocalDate fromDate = from.atZone(zone).toLocalDate();
    LocalDate toDate = to.atZone(zone).toLocalDate();
    String periodKey = PeriodKeyUtil.currentPeriodKey(safeFrequency, fromDate);
    int count = 0;

    while (!PeriodKeyUtil.periodStartDate(safeFrequency, periodKey).isAfter(toDate)) {
      count++;
      periodKey = PeriodKeyUtil.nextPeriodKey(safeFrequency, periodKey);
    }

    return count;
  }

  private List<TemperaturePoint> buildTemperatureLog(UUID orgId, LocalDateTime from, LocalDateTime to) {
    return tempRepository.findByOrgAndPeriod(orgId, from, to).stream()
        .map(m -> {
          TaskTemplate template = m.getTask().getTaskTemplate();
          TemperatureZoneModel zone = template.getTemperatureZone();
          BigDecimal min = template.getTargetMin();
          BigDecimal max = template.getTargetMax();

          return TemperaturePoint.builder()
              .measuredAt(m.getMeasuredAt())
              .zoneId(zone != null ? zone.getId() : null)
              .zoneName(zone != null ? zone.getName() : template.getTitle())
              .zoneType(zone != null ? zone.getZoneType() : null)
              .taskName(template.getTitle())
              .valueC(m.getValueC())
              .targetMin(min)
              .targetMax(max)
              .withinRange((min == null || m.getValueC().compareTo(min) >= 0) &&
                  (max == null || m.getValueC().compareTo(max) <= 0))
              .recordedBy(m.getRecordedBy().getFirstName() + " " + m.getRecordedBy().getLastName())
              .build();
        })
        .toList();
  }

  private List<UnresolvedItemDto> buildDeviations(List<TasksModel> tasks) {
    return tasks.stream()
        .map(t -> new UnresolvedItemDto(t.getTaskTemplate().getTitle(), t.getEndedAt()))
        .toList();
  }

  private List<DeviationDayPoint> buildDeviationsByDay(List<TasksModel> tasks) {
    return tasks.stream()
        .filter(task -> task.getEndedAt() != null)
        .collect(Collectors.groupingBy(task -> task.getEndedAt().toLocalDate(), Collectors.counting()))
        .entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(entry -> DeviationDayPoint.builder()
            .date(entry.getKey())
            .count(entry.getValue().intValue())
            .build())
        .toList();
  }

  private List<MissedTaskRecord> buildMissedTasks(List<TasksModel> missedTasks) {
    return missedTasks.stream()
        .collect(Collectors.groupingBy(
            task -> task.getChecklist().getName() + "::" + task.getTaskTemplate().getTitle(),
            Collectors.collectingAndThen(Collectors.toList(), tasks -> {
              TasksModel sample = tasks.getFirst();
              return MissedTaskRecord.builder()
                  .taskName(sample.getTaskTemplate().getTitle())
                  .checklistName(sample.getChecklist().getName())
                  .complianceArea(sample.getChecklist().getComplianceArea())
                  .missedCount(tasks.size())
                  .build();
            })
        ))
        .values().stream()
        .sorted(Comparator.comparing(MissedTaskRecord::getMissedCount).reversed()
            .thenComparing(MissedTaskRecord::getTaskName, String.CASE_INSENSITIVE_ORDER))
        .limit(8)
        .toList();
  }

  /**
   * Generates a lightweight internal compliance summary for the given organisation and period.
   *
   * @param orgId the organisation to generate the summary for
   * @param from  inclusive start of the period
   * @param to    inclusive end of the period
   * @return the internal summary DTO
   */
  public InternalSummary generateSummary(UUID orgId, LocalDateTime from, LocalDateTime to) {
    return InternalSummary.builder()
        .period(new ReportPeriod(from, to))
        .foodStats(buildStats(orgId, from, to, ComplianceArea.IK_MAT))
        .alcoholStats(buildStats(orgId, from, to, ComplianceArea.IK_ALKOHOL))
        .unresolvedItems(getUnresolvedItems(orgId, from, to))
        .build();
  }

  /**
   * Generates a full inspection report for the given organisation and period.
   *
   * @param orgId the organisation to generate the report for
   * @param from  inclusive start of the period
   * @param to    inclusive end of the period
   * @return the inspection report DTO
   * @throws edu.ntnu.idatt2105.backend.exception.ResourceNotFoundException if the organisation is not found
   */
  public InspectionReport generateInspection(UUID orgId, LocalDateTime from, LocalDateTime to) {
    OrganizationModel org = organizationRepository.findById(orgId)
        .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

    List<TasksModel> deviatedTasks = tasksRepository.findDeviatedTaskByOrgIdInPeriod(orgId, from, to);

    return InspectionReport.builder()
        .period(new ReportPeriod(from, to))
        .generatedAt(LocalDateTime.now())
        .organization(buildOrgSection(org))
        .checklists(buildChecklistSection(orgId, from, to))
        .temperatureLog(buildTemperatureLog(orgId, from, to))
        .deviationsByDay(buildDeviationsByDay(deviatedTasks))
        .missedTasks(buildMissedTasks(deviatedTasks))
        .deviations(buildDeviations(deviatedTasks))
        .foodStats(buildStats(orgId, from, to, ComplianceArea.IK_MAT))
        .alcoholStats(buildStats(orgId, from, to, ComplianceArea.IK_ALKOHOL))
        .build();
    }

  /**
   * Persists a new deviation report filed by the given user.
   *
   * @param request        the deviation report data
   * @param userId         ID of the user filing the report
   * @param organizationId ID of the organisation the report belongs to
   * @return the created response containing the new report's UUID and creation timestamp
   */
  public DeviationCreatedResponse createDeviationReport(
      DeviationReport request, UUID userId, UUID organizationId
  ) {
    DeviationReportModel entity = DeviationReportModel.builder()
        .organization(organizationRepository.getReferenceById(organizationId))
        .reportedByUser(userRepository.getReferenceById(userId))
        .deviationName(request.getDeviationName())
        .severity(request.getSeverity())
        .occurredAt(request.getOccurredAt())
        .noticedBy(request.getNoticedBy())
        .reportedTo(request.getReportedTo())
        .processedBy(request.getProcessedBy())
        .description(request.getDescription())
        .immediateAction(request.getImmediateAction())
        .believedCause(request.getBelievedCause())
        .correctiveMeasures(request.getCorrectiveMeasures())
        .correctiveMeasuresDone(request.getCorrectiveMeasuresDone())
        .build();

    DeviationReportModel saved = deviationReportRepository.save(entity);
    return new DeviationCreatedResponse(saved.getId(), saved.getCreatedAt());
  }
}
