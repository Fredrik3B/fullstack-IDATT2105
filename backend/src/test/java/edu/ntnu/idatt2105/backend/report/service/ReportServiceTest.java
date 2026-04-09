package edu.ntnu.idatt2105.backend.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.ntnu.idatt2105.backend.task.mapper.TaskMapper;
import edu.ntnu.idatt2105.backend.checklist.model.ChecklistModel;
import edu.ntnu.idatt2105.backend.task.model.TaskTemplate;
import edu.ntnu.idatt2105.backend.temperature.model.TemperatureMeasurementModel;
import edu.ntnu.idatt2105.backend.temperature.model.TemperatureZoneModel;
import edu.ntnu.idatt2105.backend.task.model.TasksModel;
import edu.ntnu.idatt2105.backend.checklist.model.enums.ChecklistFrequency;
import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.checklist.model.enums.SectionTypes;
import edu.ntnu.idatt2105.backend.temperature.model.enums.TemperatureZone;
import edu.ntnu.idatt2105.backend.checklist.repository.ChecklistRepository;
import edu.ntnu.idatt2105.backend.task.repository.TasksRepository;
import edu.ntnu.idatt2105.backend.temperature.repository.TemperatureMeasurementRepository;
import edu.ntnu.idatt2105.backend.checklist.service.icchecklist.PeriodKeyUtil;
import edu.ntnu.idatt2105.backend.report.dto.DeviationCreatedResponse;
import edu.ntnu.idatt2105.backend.report.dto.DeviationReport;
import edu.ntnu.idatt2105.backend.report.dto.InspectionReport;
import edu.ntnu.idatt2105.backend.report.dto.InternalSummary;
import edu.ntnu.idatt2105.backend.report.model.DeviationReportModel;
import edu.ntnu.idatt2105.backend.shared.enums.DeviationSeverity;
import edu.ntnu.idatt2105.backend.report.dto.shared.ComplianceStats;
import edu.ntnu.idatt2105.backend.report.dto.shared.UnresolvedItemDto;
import edu.ntnu.idatt2105.backend.report.repository.DeviationReportRepository;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import edu.ntnu.idatt2105.backend.user.repository.OrganizationRepository;
import edu.ntnu.idatt2105.backend.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

  @Mock private TasksRepository tasksRepository;
  @Mock private TemperatureMeasurementRepository tempRepository;
  @Mock private ChecklistRepository checklistRepository;
  @Mock private UserRepository userRepository;
  @Mock private OrganizationRepository organizationRepository;
  @Mock private TaskMapper taskMapper;
  @Mock private DeviationReportRepository deviationReportRepository;

  @InjectMocks
  private ReportService reportService;

  private UUID orgId;
  private LocalDateTime from;
  private LocalDateTime to;

  @BeforeEach
  void setUp() {
    orgId = UUID.randomUUID();
    from = LocalDateTime.now().minusMonths(1);
    to = LocalDateTime.now();
  }

  // --- generateSummary ---

  @Test
  void generateSummary_withData_returnsCorrectStats() {
    when(tasksRepository.contTaskInPeriod(eq(orgId), any(), any(), eq(ComplianceArea.IK_MAT))).thenReturn(10);
    when(tasksRepository.countCompletedInPeriod(eq(orgId), any(), any(), eq(ComplianceArea.IK_MAT))).thenReturn(8);
    when(tasksRepository.countDeviatedInPeriod(eq(orgId), any(), any(), eq(ComplianceArea.IK_MAT))).thenReturn(2);
    when(tempRepository.countReadingsInPeriod(eq(orgId), any(), any())).thenReturn(20);
    when(tempRepository.countOutOfRangeInPeriod(eq(orgId), any(), any())).thenReturn(3);

    when(tasksRepository.contTaskInPeriod(eq(orgId), any(), any(), eq(ComplianceArea.IK_ALKOHOL))).thenReturn(5);
    when(tasksRepository.countCompletedInPeriod(eq(orgId), any(), any(), eq(ComplianceArea.IK_ALKOHOL))).thenReturn(5);
    when(tasksRepository.countDeviatedInPeriod(eq(orgId), any(), any(), eq(ComplianceArea.IK_ALKOHOL))).thenReturn(0);

    when(tasksRepository.findDeviatedTaskByOrgIdInPeriod(eq(orgId), any(), any())).thenReturn(List.of());

    InternalSummary summary = reportService.generateSummary(orgId, from, to);

    ComplianceStats foodStats = summary.getFoodStats();
    assertThat(foodStats.getTotalTasks()).isEqualTo(10);
    assertThat(foodStats.getCompletedTasks()).isEqualTo(8);
    assertThat(foodStats.getDeviatedTasks()).isEqualTo(2);
    assertThat(foodStats.getCompletionRate()).isEqualTo(80.0);
    assertThat(foodStats.getTemperatureReadings()).isEqualTo(20);
    assertThat(foodStats.getOutOfRangeReadings()).isEqualTo(3);

    ComplianceStats alcStats = summary.getAlcoholStats();
    assertThat(alcStats.getTotalTasks()).isEqualTo(5);
    assertThat(alcStats.getCompletedTasks()).isEqualTo(5);
    assertThat(alcStats.getCompletionRate()).isEqualTo(100.0);
    assertThat(alcStats.getDeviatedTasks()).isEqualTo(0);
  }

  @Test
  void generateSummary_noTasks_completionRateIsZero() {
    when(tasksRepository.contTaskInPeriod(any(), any(), any(), any())).thenReturn(0);
    when(tasksRepository.countCompletedInPeriod(any(), any(), any(), any())).thenReturn(0);
    when(tasksRepository.countDeviatedInPeriod(any(), any(), any(), any())).thenReturn(0);
    when(tempRepository.countReadingsInPeriod(any(), any(), any())).thenReturn(0);
    when(tempRepository.countOutOfRangeInPeriod(any(), any(), any())).thenReturn(0);
    when(tasksRepository.findDeviatedTaskByOrgIdInPeriod(any(), any(), any())).thenReturn(List.of());

    InternalSummary summary = reportService.generateSummary(orgId, from, to);

    assertThat(summary.getFoodStats().getCompletionRate()).isEqualTo(0.0);
    assertThat(summary.getAlcoholStats().getCompletionRate()).isEqualTo(0.0);
  }

  @Test
  void generateSummary_noTemperatureReadings_outOfRangeRateIsZero() {
    when(tasksRepository.contTaskInPeriod(any(), any(), any(), any())).thenReturn(0);
    when(tasksRepository.countCompletedInPeriod(any(), any(), any(), any())).thenReturn(0);
    when(tasksRepository.countDeviatedInPeriod(any(), any(), any(), any())).thenReturn(0);
    when(tempRepository.countReadingsInPeriod(any(), any(), any())).thenReturn(0);
    when(tempRepository.countOutOfRangeInPeriod(any(), any(), any())).thenReturn(0);
    when(tasksRepository.findDeviatedTaskByOrgIdInPeriod(any(), any(), any())).thenReturn(List.of());

    InternalSummary summary = reportService.generateSummary(orgId, from, to);

    assertThat(summary.getFoodStats().getOutOfRangeRate()).isEqualTo(0.0);
  }

  @Test
  void generateSummary_periodIsSetCorrectly() {
    when(tasksRepository.contTaskInPeriod(any(), any(), any(), any())).thenReturn(0);
    when(tasksRepository.countCompletedInPeriod(any(), any(), any(), any())).thenReturn(0);
    when(tasksRepository.countDeviatedInPeriod(any(), any(), any(), any())).thenReturn(0);
    when(tempRepository.countReadingsInPeriod(any(), any(), any())).thenReturn(0);
    when(tempRepository.countOutOfRangeInPeriod(any(), any(), any())).thenReturn(0);
    when(tasksRepository.findDeviatedTaskByOrgIdInPeriod(any(), any(), any())).thenReturn(List.of());

    InternalSummary summary = reportService.generateSummary(orgId, from, to);

    assertThat(summary.getPeriod().getFrom()).isEqualTo(from);
    assertThat(summary.getPeriod().getTo()).isEqualTo(to);
  }

  @Test
  void generateSummary_withUnresolvedItems_mapsToDtos() {
    when(tasksRepository.contTaskInPeriod(any(), any(), any(), any())).thenReturn(0);
    when(tasksRepository.countCompletedInPeriod(any(), any(), any(), any())).thenReturn(0);
    when(tasksRepository.countDeviatedInPeriod(any(), any(), any(), any())).thenReturn(0);
    when(tempRepository.countReadingsInPeriod(any(), any(), any())).thenReturn(0);
    when(tempRepository.countOutOfRangeInPeriod(any(), any(), any())).thenReturn(0);

    TasksModel deviatedTask = new TasksModel();
    UnresolvedItemDto dto = new UnresolvedItemDto("Fridge temperature check", LocalDateTime.now());

    when(tasksRepository.findDeviatedTaskByOrgIdInPeriod(eq(orgId), any(), any()))
        .thenReturn(List.of(deviatedTask));
    when(taskMapper.toUnresolvedDto(deviatedTask)).thenReturn(dto);

    InternalSummary summary = reportService.generateSummary(orgId, from, to);

    assertThat(summary.getUnresolvedItems()).hasSize(1);
    assertThat(summary.getUnresolvedItems().get(0).getName()).isEqualTo("Fridge temperature check");
    verify(taskMapper).toUnresolvedDto(deviatedTask);
  }

  @Test
  void generateSummary_allTasksCompleted_fullCompletionRate() {
    when(tasksRepository.contTaskInPeriod(eq(orgId), any(), any(), eq(ComplianceArea.IK_MAT))).thenReturn(5);
    when(tasksRepository.countCompletedInPeriod(eq(orgId), any(), any(), eq(ComplianceArea.IK_MAT))).thenReturn(5);
    when(tasksRepository.countDeviatedInPeriod(eq(orgId), any(), any(), eq(ComplianceArea.IK_MAT))).thenReturn(0);
    when(tasksRepository.contTaskInPeriod(eq(orgId), any(), any(), eq(ComplianceArea.IK_ALKOHOL))).thenReturn(0);
    when(tasksRepository.countCompletedInPeriod(eq(orgId), any(), any(), eq(ComplianceArea.IK_ALKOHOL))).thenReturn(0);
    when(tasksRepository.countDeviatedInPeriod(eq(orgId), any(), any(), eq(ComplianceArea.IK_ALKOHOL))).thenReturn(0);
    when(tempRepository.countReadingsInPeriod(any(), any(), any())).thenReturn(0);
    when(tempRepository.countOutOfRangeInPeriod(any(), any(), any())).thenReturn(0);
    when(tasksRepository.findDeviatedTaskByOrgIdInPeriod(any(), any(), any())).thenReturn(List.of());

    InternalSummary summary = reportService.generateSummary(orgId, from, to);

    assertThat(summary.getFoodStats().getCompletionRate()).isEqualTo(100.0);
  }

  @Test
  void generateInspection_aggregatesChecklistPerformanceAndSkipsChecklistsWithoutRuns() {
    OrganizationModel org = organization("North Kitchen");

    ChecklistModel weeklyChecklist = checklist(1L, "Weekly food safety", ComplianceArea.IK_MAT, ChecklistFrequency.WEEKLY);
    ChecklistModel dailyChecklist = checklist(2L, "Daily opening", ComplianceArea.IK_MAT, ChecklistFrequency.DAILY);
    ChecklistModel unusedChecklist = checklist(3L, "Unused checklist", ComplianceArea.IK_ALKOHOL, ChecklistFrequency.MONTHLY);

    TaskTemplate fridgeTemplate = template(11L, "Check fridge", SectionTypes.TEMPERATURE_CONTROL);
    TaskTemplate hygieneTemplate = template(12L, "Sanitize surfaces", SectionTypes.CLEANING_SANITATION);

    TasksModel runOneTaskOne = task(101L, weeklyChecklist, fridgeTemplate, "2026-W13", true, false, from.plusDays(1));
    TasksModel runOneTaskTwo = task(102L, weeklyChecklist, hygieneTemplate, "2026-W13", false, false, from.plusDays(1));
    TasksModel runTwoTaskOne = task(103L, weeklyChecklist, fridgeTemplate, "2026-W14", true, false, from.plusDays(8));
    TasksModel dailyTask = task(104L, dailyChecklist, hygieneTemplate, "2026-03-20", true, false, from.plusDays(2));

    when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
    when(userRepository.findAllByOrganization(org)).thenReturn(List.of());
    when(checklistRepository.findAllByOrganizationId(orgId)).thenReturn(List.of(weeklyChecklist, dailyChecklist, unusedChecklist));
    when(tasksRepository.findAllByOrgIdInPeriodWithRelations(eq(orgId), eq(from), eq(to)))
        .thenReturn(List.of(runOneTaskOne, runOneTaskTwo, runTwoTaskOne, dailyTask));
    when(tasksRepository.findDeviatedTaskByOrgIdInPeriod(eq(orgId), eq(from), eq(to)))
        .thenReturn(List.of(runOneTaskTwo));
    when(tempRepository.findByOrgAndPeriod(eq(orgId), eq(from), eq(to))).thenReturn(List.of());
    stubSummaryCounts();

    InspectionReport report = reportService.generateInspection(orgId, from, to);

    assertThat(report.getChecklists().getChecklists()).hasSize(2);

    var weeklyRecord = report.getChecklists().getChecklists().stream()
        .filter(record -> record.getName().equals("Weekly food safety"))
        .findFirst()
        .orElseThrow();
    assertThat(weeklyRecord.getCompletionsInPeriod()).isEqualTo(2);
    assertThat(weeklyRecord.getExpectedRuns()).isEqualTo(expectedRuns(ChecklistFrequency.WEEKLY));
    assertThat(weeklyRecord.getAverageCompletionRate()).isEqualTo(75.0);
    assertThat(weeklyRecord.getDeviatedTasks()).isEqualTo(1);

    var dailyRecord = report.getChecklists().getChecklists().stream()
        .filter(record -> record.getName().equals("Daily opening"))
        .findFirst()
        .orElseThrow();
    assertThat(dailyRecord.getCompletionsInPeriod()).isEqualTo(1);
    assertThat(dailyRecord.getExpectedRuns()).isEqualTo(expectedRuns(ChecklistFrequency.DAILY));
    assertThat(dailyRecord.getAverageCompletionRate()).isEqualTo(100.0);
  }

  @Test
  void generateInspection_buildsDeviationTrendMissedTasksAndTemperatureZones() {
    OrganizationModel org = organization("North Kitchen");
    ChecklistModel checklist = checklist(4L, "Cold storage", ComplianceArea.IK_MAT, ChecklistFrequency.DAILY);
    TemperatureZoneModel zone = temperatureZone(90L, "Main freezer", TemperatureZone.FREEZER, new BigDecimal("-20"), new BigDecimal("-15"));
    TaskTemplate template = template(21L, "Freezer reading", SectionTypes.TEMPERATURE_CONTROL);
    template.setTemperatureZone(zone);
    template.setTargetMin(new BigDecimal("-20"));
    template.setTargetMax(new BigDecimal("-15"));

    TasksModel deviatedOne = task(201L, checklist, template, "2026-03-10", false, false, from.plusDays(3));
    TasksModel deviatedTwo = task(202L, checklist, template, "2026-03-10", false, false, from.plusDays(3).plusHours(2));
    TasksModel deviatedThree = task(203L, checklist, template, "2026-03-11", false, false, from.plusDays(4));

    UserModel recorder = new UserModel();
    recorder.setFirstName("Alex");
    recorder.setLastName("Manager");

    TemperatureMeasurementModel measurement = new TemperatureMeasurementModel();
    measurement.setTask(deviatedOne);
    measurement.setMeasuredAt(from.plusDays(3).plusHours(1));
    measurement.setValueC(new BigDecimal("-22.5"));
    measurement.setRecordedBy(recorder);

    when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
    when(userRepository.findAllByOrganization(org)).thenReturn(List.of());
    when(checklistRepository.findAllByOrganizationId(orgId)).thenReturn(List.of(checklist));
    when(tasksRepository.findAllByOrgIdInPeriodWithRelations(eq(orgId), eq(from), eq(to)))
        .thenReturn(List.of(deviatedOne, deviatedTwo, deviatedThree));
    when(tasksRepository.findDeviatedTaskByOrgIdInPeriod(eq(orgId), eq(from), eq(to)))
        .thenReturn(List.of(deviatedOne, deviatedTwo, deviatedThree));
    when(tempRepository.findByOrgAndPeriod(eq(orgId), eq(from), eq(to))).thenReturn(List.of(measurement));
    stubSummaryCounts();

    InspectionReport report = reportService.generateInspection(orgId, from, to);

    assertThat(report.getDeviationsByDay()).hasSize(2);
    assertThat(report.getDeviationsByDay().get(0).getCount()).isEqualTo(2);
    assertThat(report.getDeviationsByDay().get(1).getCount()).isEqualTo(1);

    assertThat(report.getMissedTasks()).hasSize(1);
    assertThat(report.getMissedTasks().get(0).getTaskName()).isEqualTo("Freezer reading");
    assertThat(report.getMissedTasks().get(0).getMissedCount()).isEqualTo(3);

    assertThat(report.getTemperatureLog()).hasSize(1);
    assertThat(report.getTemperatureLog().get(0).getZoneId()).isEqualTo(90L);
    assertThat(report.getTemperatureLog().get(0).getZoneName()).isEqualTo("Main freezer");
    assertThat(report.getTemperatureLog().get(0).isWithinRange()).isFalse();
  }

  private void stubSummaryCounts() {
    when(tasksRepository.contTaskInPeriod(eq(orgId), eq(from), eq(to), eq(ComplianceArea.IK_MAT))).thenReturn(4);
    when(tasksRepository.countCompletedInPeriod(eq(orgId), eq(from), eq(to), eq(ComplianceArea.IK_MAT))).thenReturn(2);
    when(tasksRepository.countDeviatedInPeriod(eq(orgId), eq(from), eq(to), eq(ComplianceArea.IK_MAT))).thenReturn(1);
    when(tasksRepository.contTaskInPeriod(eq(orgId), eq(from), eq(to), eq(ComplianceArea.IK_ALKOHOL))).thenReturn(0);
    when(tasksRepository.countCompletedInPeriod(eq(orgId), eq(from), eq(to), eq(ComplianceArea.IK_ALKOHOL))).thenReturn(0);
    when(tasksRepository.countDeviatedInPeriod(eq(orgId), eq(from), eq(to), eq(ComplianceArea.IK_ALKOHOL))).thenReturn(0);
    when(tempRepository.countReadingsInPeriod(eq(orgId), eq(from), eq(to))).thenReturn(1);
    when(tempRepository.countOutOfRangeInPeriod(eq(orgId), eq(from), eq(to))).thenReturn(1);
  }

  private int expectedRuns(ChecklistFrequency frequency) {
    String periodKey = PeriodKeyUtil.currentPeriodKey(frequency, from.toLocalDate());
    int count = 0;

    while (!PeriodKeyUtil.periodStartDate(frequency, periodKey).isAfter(to.toLocalDate())) {
      count++;
      periodKey = PeriodKeyUtil.nextPeriodKey(frequency, periodKey);
    }

    return count;
  }

  private OrganizationModel organization(String name) {
    OrganizationModel organization = new OrganizationModel();
    organization.setId(orgId);
    organization.setName(name);
    organization.setJoinCode("JOIN");
    return organization;
  }

  private ChecklistModel checklist(Long id, String name, ComplianceArea area, ChecklistFrequency frequency) {
    ChecklistModel checklist = new ChecklistModel();
    checklist.setId(id);
    checklist.setName(name);
    checklist.setDescription(name + " description");
    checklist.setComplianceArea(area);
    checklist.setFrequency(frequency);
    checklist.setActive(true);
    checklist.setTaskTemplates(new LinkedHashSet<>());
    return checklist;
  }

  private TaskTemplate template(Long id, String title, SectionTypes sectionType) {
    TaskTemplate template = new TaskTemplate();
    template.setId(id);
    template.setTitle(title);
    template.setSectionType(sectionType);
    template.setComplianceArea(ComplianceArea.IK_MAT);
    template.setOrganisationId(orgId);
    return template;
  }

  private TasksModel task(
      Long id,
      ChecklistModel checklist,
      TaskTemplate template,
      String periodKey,
      boolean completed,
      boolean active,
      LocalDateTime endedAt
  ) {
    TasksModel task = new TasksModel();
    task.setId(id);
    task.setChecklist(checklist);
    task.setTaskTemplate(template);
    task.setPeriodKey(periodKey);
    task.setCompleted(completed);
    task.setActive(active);
    task.setEndedAt(endedAt);
    checklist.getTaskTemplates().add(template);
    return task;
  }

  // ── createDeviationReport ─────────────────────────────────────────────────

  @Test
  void createDeviationReport_savesEntityAndReturnsIdWithCreatedAt() {
    UUID userId = UUID.randomUUID();
    edu.ntnu.idatt2105.backend.user.model.UserModel user = new edu.ntnu.idatt2105.backend.user.model.UserModel();
    user.setId(userId);
    edu.ntnu.idatt2105.backend.user.model.OrganizationModel org = new edu.ntnu.idatt2105.backend.user.model.OrganizationModel();
    org.setId(orgId);

    DeviationReport request = new DeviationReport();
    request.setDeviationName("Cold chain breach");
    request.setSeverity(DeviationSeverity.HIGH);
    request.setOccurredAt(LocalDateTime.of(2026, 4, 1, 10, 0));
    request.setDescription("Fridge temperature exceeded 8°C for 2 hours.");

    UUID reportId = UUID.randomUUID();
    LocalDateTime createdAt = LocalDateTime.of(2026, 4, 9, 12, 0);

    when(organizationRepository.getReferenceById(orgId)).thenReturn(org);
    when(userRepository.getReferenceById(userId)).thenReturn(user);
    when(deviationReportRepository.save(any(DeviationReportModel.class))).thenAnswer(inv -> {
      DeviationReportModel m = inv.getArgument(0);
      m.setId(reportId);
      m.setCreatedAt(createdAt);
      return m;
    });

    DeviationCreatedResponse response = reportService.createDeviationReport(request, userId, orgId);

    assertThat(response.getId()).isEqualTo(reportId);
    assertThat(response.getCreatedAt()).isEqualTo(createdAt);
    verify(deviationReportRepository).save(any(DeviationReportModel.class));
  }

  @Test
  void createDeviationReport_mapsAllFieldsFromRequestToEntity() {
    UUID userId = UUID.randomUUID();
    when(organizationRepository.getReferenceById(orgId))
        .thenReturn(new edu.ntnu.idatt2105.backend.user.model.OrganizationModel());
    when(userRepository.getReferenceById(userId))
        .thenReturn(new edu.ntnu.idatt2105.backend.user.model.UserModel());

    DeviationReport request = new DeviationReport();
    request.setDeviationName("Pest found");
    request.setSeverity(DeviationSeverity.CRITICAL);
    request.setOccurredAt(LocalDateTime.of(2026, 3, 15, 8, 0));
    request.setNoticedBy("Chef Marie");
    request.setReportedTo("Manager Lars");
    request.setDescription("Pest traces found in storage.");
    request.setImmediateAction("Area sealed off.");
    request.setBelievedCause("Damaged door seal.");
    request.setCorrectiveMeasures("Replace door seal.");
    request.setCorrectiveMeasuresDone("Completed on 2026-03-16");

    when(deviationReportRepository.save(any(DeviationReportModel.class))).thenAnswer(inv -> {
      DeviationReportModel m = inv.getArgument(0);
      m.setId(UUID.randomUUID());
      return m;
    });

    reportService.createDeviationReport(request, userId, orgId);

    org.mockito.ArgumentCaptor<DeviationReportModel> captor =
        org.mockito.ArgumentCaptor.forClass(DeviationReportModel.class);
    verify(deviationReportRepository).save(captor.capture());
    DeviationReportModel saved = captor.getValue();

    assertThat(saved.getDeviationName()).isEqualTo("Pest found");
    assertThat(saved.getSeverity()).isEqualTo(DeviationSeverity.CRITICAL);
    assertThat(saved.getNoticedBy()).isEqualTo("Chef Marie");
    assertThat(saved.getReportedTo()).isEqualTo("Manager Lars");
    assertThat(saved.getImmediateAction()).isEqualTo("Area sealed off.");
    assertThat(saved.getBelievedCause()).isEqualTo("Damaged door seal.");
    assertThat(saved.getCorrectiveMeasures()).isEqualTo("Replace door seal.");
    assertThat(saved.getCorrectiveMeasuresDone()).isEqualTo("Completed on 2026-03-16");
  }

  private TemperatureZoneModel temperatureZone(
      Long id,
      String name,
      TemperatureZone zoneType,
      BigDecimal min,
      BigDecimal max
  ) {
    TemperatureZoneModel zone = new TemperatureZoneModel();
    zone.setId(id);
    zone.setName(name);
    zone.setZoneType(zoneType);
    zone.setComplianceArea(ComplianceArea.IK_MAT);
    zone.setOrganizationId(orgId);
    zone.setTargetMin(min);
    zone.setTargetMax(max);
    return zone;
  }
}
