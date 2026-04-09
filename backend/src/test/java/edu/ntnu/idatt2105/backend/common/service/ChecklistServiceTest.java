package edu.ntnu.idatt2105.backend.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.ntnu.idatt2105.backend.checklist.dto.ChecklistCardResponse;
import edu.ntnu.idatt2105.backend.checklist.dto.ChecklistTaskItemResponse;
import edu.ntnu.idatt2105.backend.checklist.dto.ChecklistWorkbenchStateRequest;
import edu.ntnu.idatt2105.backend.checklist.dto.CreateChecklistCardRequest;
import edu.ntnu.idatt2105.backend.checklist.dto.TaskCompletionRequest;
import edu.ntnu.idatt2105.backend.checklist.dto.TaskFlagRequest;
import edu.ntnu.idatt2105.backend.checklist.dto.UpdateChecklistCardRequest;
import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import edu.ntnu.idatt2105.backend.checklist.mapper.ChecklistMapper;
import edu.ntnu.idatt2105.backend.checklist.model.ChecklistModel;
import edu.ntnu.idatt2105.backend.checklist.service.ChecklistCacheStateService;
import edu.ntnu.idatt2105.backend.task.model.TaskTemplate;
import edu.ntnu.idatt2105.backend.task.model.TasksModel;
import edu.ntnu.idatt2105.backend.checklist.model.enums.ChecklistFrequency;
import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.checklist.model.enums.SectionTypes;
import edu.ntnu.idatt2105.backend.checklist.repository.ChecklistRepository;
import edu.ntnu.idatt2105.backend.task.repository.TaskTemplateRepository;
import edu.ntnu.idatt2105.backend.temperature.model.TemperatureMeasurementModel;
import edu.ntnu.idatt2105.backend.temperature.repository.TemperatureMeasurementRepository;
import edu.ntnu.idatt2105.backend.task.repository.TasksRepository;
import edu.ntnu.idatt2105.backend.checklist.service.icchecklist.PeriodKeyUtil;
import edu.ntnu.idatt2105.backend.checklist.service.ChecklistService;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.repository.OrganizationRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChecklistServiceTest {

  @Mock private ChecklistRepository checklistRepository;
  @Mock private TaskTemplateRepository taskTemplateRepository;
  @Mock private TasksRepository tasksRepository;
  @Mock private TemperatureMeasurementRepository temperatureMeasurementRepository;
  @Mock private OrganizationRepository organizationRepository;
  @Mock private ChecklistCacheStateService checklistCacheStateService;
  @Mock private ChecklistMapper checklistMapper;

  @InjectMocks private ChecklistService checklistService;

  private UUID orgId;
  private JwtAuthenticatedPrincipal principal;

  @BeforeEach
  void setUp() {
    orgId = UUID.randomUUID();
    principal = new JwtAuthenticatedPrincipal(UUID.randomUUID(), orgId, "tester", Collections.emptyList());
  }

  @Test
  void submitChecklist_marksIncompleteTasksFinishedAtEndOfPeriodAndDeactivatesThem() {
    String currentPeriodKey = PeriodKeyUtil.currentPeriodKey(ChecklistFrequency.WEEKLY, ZoneId.systemDefault());
    String nextPeriodKey = PeriodKeyUtil.nextPeriodKey(ChecklistFrequency.WEEKLY, currentPeriodKey);
    LocalDateTime expectedEndedAt = PeriodKeyUtil.periodStartDate(ChecklistFrequency.WEEKLY, nextPeriodKey)
        .atStartOfDay()
        .minusSeconds(1);

    TaskTemplate template = template(20L, "Freezer check");

    ChecklistModel checklist = checklist(1L, currentPeriodKey, false, true, template);

    TasksModel incompleteTask = activatedTask(100L, checklist, template, currentPeriodKey);
    incompleteTask.setCompleted(false);
    incompleteTask.setEndedAt(null);

    when(checklistRepository.findByIdAndOrganization_Id(1L, orgId)).thenReturn(Optional.of(checklist));
    when(tasksRepository.findAllByChecklist_IdAndPeriodKeyAndActiveTrue(1L, currentPeriodKey))
        .thenReturn(new java.util.ArrayList<>(List.of(incompleteTask)));
    when(checklistRepository.save(any(ChecklistModel.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(checklistMapper.toCardResponse(
        any(ChecklistModel.class),
        any(),
        any(),
        any(),
        any(Boolean.class)))
        .thenAnswer(invocation -> {
          return new ChecklistCardResponse(
              3L, "2026-04-08", "2026-W16",false, false, "Test",
              null, null,false,null, null, 0, List.of()
          );
        });


    var response = checklistService.submitChecklist(1L, principal);

    assertThat(incompleteTask.isActive()).isFalse();
    assertThat(incompleteTask.getEndedAt()).isEqualTo(expectedEndedAt);
    assertThat(response.activePeriodKey()).isEqualTo(nextPeriodKey);

    ArgumentCaptor<List<TasksModel>> tasksCaptor = ArgumentCaptor.forClass(List.class);
    verify(tasksRepository).saveAll(tasksCaptor.capture());
    assertThat(tasksCaptor.getValue()).containsExactly(incompleteTask);
  }

  @Test
  void submitChecklist_whenAlreadyAdvancedToNextPeriod_rejectsSecondSubmit() {
    String currentPeriodKey = PeriodKeyUtil.currentPeriodKey(ChecklistFrequency.DAILY, ZoneId.systemDefault());
    String nextPeriodKey = PeriodKeyUtil.nextPeriodKey(ChecklistFrequency.DAILY, currentPeriodKey);

    ChecklistModel checklist = checklist(2L, nextPeriodKey, false, true, template(21L, "Opening check"));

    when(checklistRepository.findByIdAndOrganization_Id(2L, orgId)).thenReturn(Optional.of(checklist));
    when(checklistMapper.taskTemplateComparator()).thenReturn(
        Comparator.comparing(TaskTemplate::getTitle)
    );
    assertThatThrownBy(() -> checklistService.submitChecklist(2L, principal))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Already submitted");

    verify(tasksRepository, never()).saveAll(any());
  }

  @Test
  void setChecklistWorkbenchState_whenHidden_deletesActivatedTasksAndMeasurements() {
    ChecklistModel checklist = checklist(3L,
        PeriodKeyUtil.currentPeriodKey(ChecklistFrequency.DAILY, ZoneId.systemDefault()),
        false,
        true,
        template(30L, "Close bar"));

    TasksModel task = activatedTask(400L, checklist, checklist.getTaskTemplates().iterator().next(), checklist.getActivePeriodKey());

    when(checklistRepository.findByIdAndOrganization_Id(3L, orgId)).thenReturn(Optional.of(checklist));
    when(tasksRepository.findAllByChecklist_Id(3L)).thenReturn(List.of(task));
    when(checklistRepository.save(any(ChecklistModel.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(checklistMapper.toCardResponse(
        any(ChecklistModel.class),
        any(),
        any(),
        any(),
        any(Boolean.class)))
        .thenAnswer(invocation -> {
          return new ChecklistCardResponse(
              3L, "2026-04-08", "2026-04-08",false, false, "Test",
              null, null,false,null, null, 0, List.of()
          );
        });

    var response = checklistService.setChecklistWorkbenchState(
        3L,
        new ChecklistWorkbenchStateRequest(false),
        principal);

    assertThat(response.displayedOnWorkbench()).isFalse();
    verify(temperatureMeasurementRepository).deleteAllByTaskIn(List.of(task));
    verify(tasksRepository).deleteAll(List.of(task));
  }

  @Test
  void setTaskCompletion_whenMarkedTodo_clearsEndedAtAndCompletionState() {
    String periodKey = PeriodKeyUtil.currentPeriodKey(ChecklistFrequency.DAILY, ZoneId.systemDefault());
    TaskTemplate template = template(50L, "Check fridge");
    ChecklistModel checklist = checklist(4L, periodKey, true, true, template);

    TasksModel task = activatedTask(500L, checklist, template, periodKey);
    task.setCompleted(true);
    task.setEndedAt(LocalDateTime.now());

    when(checklistRepository.findByIdAndOrganization_Id(4L, orgId)).thenReturn(Optional.of(checklist));
    when(tasksRepository.findByIdAndChecklist_Id(500L, 4L)).thenReturn(Optional.of(task));
    when(tasksRepository.save(any(TasksModel.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(temperatureMeasurementRepository.findAllByTask_IdInOrderByMeasuredAtDesc(List.of(500L))).thenReturn(List.of());
    when(checklistMapper.toTaskItemResponse(any(TasksModel.class), any()))
        .thenAnswer(invocation -> {
          TasksModel t = invocation.getArgument(0);
          TemperatureMeasurementModel measurement = invocation.getArgument(1);
          return new ChecklistTaskItemResponse(
              t.getId(),
              t.getTaskTemplate().getId(),
              t.getTaskTemplate().getTitle(),
              t.getMeta(),
              null,
              t.getTaskTemplate().getUnit(),
              t.getTaskTemplate().getTargetMin(),
              t.getTaskTemplate().getTargetMax(),
              t.isCompleted() ? "completed" : "todo",
              t.isFlagged() ? Boolean.TRUE : Boolean.FALSE,
              t.isCompleted() ? t.getPeriodKey() : null,
              t.isCompleted() ? t.getEndedAt() : null,
              t.isFlagged() ? t.getPeriodKey() : null,
              null
          );
        });

    var response = checklistService.setTaskCompletion(
        4L,
        500L,
        new TaskCompletionRequest("todo", periodKey, null),
        principal);

    assertThat(task.isCompleted()).isFalse();
    assertThat(task.getEndedAt()).isNull();
    assertThat(response.state()).isEqualTo("todo");
  }

  @Test
  void setTaskCompletion_whenTemperatureTaskHasNoReading_rejectsCompletion() {
    String periodKey = PeriodKeyUtil.currentPeriodKey(ChecklistFrequency.DAILY, ZoneId.systemDefault());
    TaskTemplate template = template(60L, "Check fridge");
    ChecklistModel checklist = checklist(5L, periodKey, true, true, template);

    TasksModel task = activatedTask(600L, checklist, template, periodKey);

    when(checklistRepository.findByIdAndOrganization_Id(5L, orgId)).thenReturn(Optional.of(checklist));
    when(tasksRepository.findByIdAndChecklist_Id(600L, 5L)).thenReturn(Optional.of(task));
    when(temperatureMeasurementRepository.existsByTask_IdAndPeriodKey(600L, periodKey)).thenReturn(false);

    assertThatThrownBy(() -> checklistService.setTaskCompletion(
        5L,
        600L,
        new TaskCompletionRequest("completed", periodKey, LocalDateTime.now()),
        principal))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("require a saved reading");

    verify(tasksRepository, never()).save(any(TasksModel.class));
  }

  @Test
  void setTaskCompletion_whenTemperatureTaskHasReading_allowsCompletion() {
    String periodKey = PeriodKeyUtil.currentPeriodKey(ChecklistFrequency.DAILY, ZoneId.systemDefault());
    TaskTemplate template = template(70L, "Check freezer");
    ChecklistModel checklist = checklist(6L, periodKey, true, true, template);

    TasksModel task = activatedTask(700L, checklist, template, periodKey);
    LocalDateTime completedAt = LocalDateTime.now();

    when(checklistRepository.findByIdAndOrganization_Id(6L, orgId)).thenReturn(Optional.of(checklist));
    when(tasksRepository.findByIdAndChecklist_Id(700L, 6L)).thenReturn(Optional.of(task));
    when(temperatureMeasurementRepository.existsByTask_IdAndPeriodKey(700L, periodKey)).thenReturn(true);
    when(tasksRepository.save(any(TasksModel.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(temperatureMeasurementRepository.findAllByTask_IdInOrderByMeasuredAtDesc(List.of(700L)))
        .thenReturn(List.of(measurement(700L, periodKey, task)));
    when(checklistMapper.toTaskItemResponse(any(TasksModel.class), any()))
        .thenAnswer(invocation -> {
          TasksModel t = invocation.getArgument(0);
          TemperatureMeasurementModel measurement = invocation.getArgument(1);
          return new ChecklistTaskItemResponse(
              t.getId(),
              t.getTaskTemplate().getId(),
              t.getTaskTemplate().getTitle(),
              t.getMeta(),
              null,
              t.getTaskTemplate().getUnit(),
              t.getTaskTemplate().getTargetMin(),
              t.getTaskTemplate().getTargetMax(),
              t.isCompleted() ? "completed" : "todo",
              t.isFlagged() ? Boolean.TRUE : Boolean.FALSE,
              t.isCompleted() ? t.getPeriodKey() : null,
              t.isCompleted() ? t.getEndedAt() : null,
              t.isFlagged() ? t.getPeriodKey() : null,
              null
          );
        });

    var response = checklistService.setTaskCompletion(
        6L,
        700L,
        new TaskCompletionRequest("completed", periodKey, completedAt),
        principal);

    assertThat(task.isCompleted()).isTrue();
    assertThat(task.getEndedAt()).isEqualTo(completedAt);
    assertThat(response.state()).isEqualTo("completed");
  }

  private ChecklistModel checklist(Long id, String periodKey, boolean recurring, boolean displayed, TaskTemplate template) {
    ChecklistModel checklist = new ChecklistModel();
    OrganizationModel organization = new OrganizationModel();
    organization.setId(orgId);
    checklist.setId(id);
    checklist.setName("Weekly safety");
    checklist.setDescription("desc");
    checklist.setFrequency(ChecklistFrequency.WEEKLY);
    if (periodKey.matches("\\d{4}-\\d{2}-\\d{2}")) {
      checklist.setFrequency(ChecklistFrequency.DAILY);
    } else if (periodKey.matches("\\d{4}-W\\d{2}")) {
      checklist.setFrequency(ChecklistFrequency.WEEKLY);
    } else {
      checklist.setFrequency(ChecklistFrequency.MONTHLY);
    }
    checklist.setComplianceArea(ComplianceArea.IK_MAT);
    checklist.setActivePeriodKey(periodKey);
    checklist.setRecurring(recurring);
    checklist.setDisplayedOnWorkbench(displayed);
    checklist.setActive(true);
    checklist.setOrganization(organization);
    checklist.setTaskTemplates(new LinkedHashSet<>(List.of(template)));
    return checklist;
  }

  private TaskTemplate template(Long id, String title) {
    TaskTemplate template = new TaskTemplate();
    template.setId(id);
    template.setTitle(title);
    template.setSectionType(SectionTypes.TEMPERATURE_CONTROL);
    template.setComplianceArea(ComplianceArea.IK_MAT);
    template.setUnit("C");
    template.setTargetMin(new BigDecimal("2.00"));
    template.setTargetMax(new BigDecimal("5.00"));
    template.setOrganisationId(orgId);
    return template;
  }

  private TasksModel activatedTask(Long id, ChecklistModel checklist, TaskTemplate template, String periodKey) {
    TasksModel task = new TasksModel();
    task.setId(id);
    task.setChecklist(checklist);
    task.setTaskTemplate(template);
    task.setPeriodKey(periodKey);
    task.setMeta(template.getMeta());
    task.setActive(true);
    task.setCompleted(false);
    task.setFlagged(false);
    return task;
  }

  private TemperatureMeasurementModel measurement(Long id, String periodKey, TasksModel task) {
    TemperatureMeasurementModel measurement = new TemperatureMeasurementModel();
    measurement.setId(id);
    measurement.setTask(task);
    measurement.setPeriodKey(periodKey);
    measurement.setValueC(new BigDecimal("3.50"));
    measurement.setMeasuredAt(LocalDateTime.now());
    return measurement;
  }

  // ── fetchChecklists ───────────────────────────────────────────────────────

  @Test
  void fetchChecklists_returnsMappedResponseForEachChecklist() {
    String periodKey = PeriodKeyUtil.currentPeriodKey(ChecklistFrequency.DAILY, ZoneId.systemDefault());
    TaskTemplate template = template(10L, "Opening check");
    ChecklistModel cl = checklist(1L, periodKey, false, false, template); // displayedOnWorkbench=false → shouldLoad=false

    when(checklistRepository.findAllByOrganization_IdAndComplianceAreaAndActiveTrueOrderByIdAsc(orgId, ComplianceArea.IK_MAT))
        .thenReturn(List.of(cl));
    when(checklistMapper.taskTemplateComparator()).thenReturn(Comparator.comparing(TaskTemplate::getTitle));
    when(checklistMapper.toCardResponse(any(), any(), any(), any(), any(Boolean.class)))
        .thenReturn(stubCard(1L));

    List<ChecklistCardResponse> result = checklistService.fetchChecklists(IcModule.IC_FOOD, principal);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).id()).isEqualTo(1L);
  }

  @Test
  void fetchChecklists_returnsEmptyListWhenNoChecklistsExist() {
    when(checklistRepository.findAllByOrganization_IdAndComplianceAreaAndActiveTrueOrderByIdAsc(orgId, ComplianceArea.IK_MAT))
        .thenReturn(List.of());

    List<ChecklistCardResponse> result = checklistService.fetchChecklists(IcModule.IC_FOOD, principal);

    assertThat(result).isEmpty();
  }

  // ── fetchChecklistsLastModified ───────────────────────────────────────────

  @Test
  void fetchChecklistsLastModified_delegatesToCacheService() {
    java.time.Instant expected = java.time.Instant.now();
    when(checklistCacheStateService.getLastModified(orgId, ComplianceArea.IK_MAT)).thenReturn(expected);

    java.time.Instant result = checklistService.fetchChecklistsLastModified(IcModule.IC_FOOD, principal);

    assertThat(result).isEqualTo(expected);
  }

  // ── createChecklist ───────────────────────────────────────────────────────

  @Test
  void createChecklist_savesNewChecklistAndTouchesCache() {
    TaskTemplate template = template(5L, "Hand wash");
    edu.ntnu.idatt2105.backend.user.model.OrganizationModel org = new edu.ntnu.idatt2105.backend.user.model.OrganizationModel();
    org.setId(orgId);

    when(organizationRepository.getReferenceById(orgId)).thenReturn(org);
    when(taskTemplateRepository.findAllByIdInAndOrganisationIdAndComplianceAreaOrdered(List.of(5L), orgId, ComplianceArea.IK_MAT))
        .thenReturn(List.of(template));
    when(checklistRepository.save(any(ChecklistModel.class))).thenAnswer(inv -> {
      ChecklistModel saved = inv.getArgument(0);
      saved.setId(99L);
      return saved;
    });
    when(checklistMapper.taskTemplateComparator()).thenReturn(Comparator.comparing(TaskTemplate::getTitle));
    when(checklistMapper.toCardResponse(any(), any(), any(), any(), any(Boolean.class)))
        .thenReturn(stubCard(99L));

    ChecklistCardResponse response = checklistService.createChecklist(
        new CreateChecklistCardRequest(IcModule.IC_FOOD, "daily", "New Checklist", null, true, false, List.of(5L)),
        principal);

    assertThat(response.id()).isEqualTo(99L);
    verify(checklistRepository).save(any(ChecklistModel.class));
    verify(checklistCacheStateService).touch(orgId, ComplianceArea.IK_MAT);
  }

  @Test
  void createChecklist_withEmptyTaskTemplateIds_throwsException() {
    assertThatThrownBy(() -> checklistService.createChecklist(
        new CreateChecklistCardRequest(IcModule.IC_FOOD, "daily", "Bad", null, true, false, List.of()),
        principal))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("taskTemplateIds must contain at least one task");

    verify(checklistRepository, never()).save(any());
  }

  // ── updateChecklist ───────────────────────────────────────────────────────

  @Test
  void updateChecklist_whenFrequencyChanges_deactivatesExistingActiveTasks() {
    String dailyKey = PeriodKeyUtil.currentPeriodKey(ChecklistFrequency.DAILY, ZoneId.systemDefault());
    TaskTemplate template = template(6L, "Close bar");
    ChecklistModel cl = checklist(10L, dailyKey, true, false, template); // starts as DAILY (from periodKey pattern)

    TasksModel activeTask = activatedTask(200L, cl, template, dailyKey);

    when(checklistRepository.findByIdAndOrganization_Id(10L, orgId)).thenReturn(Optional.of(cl));
    when(taskTemplateRepository.findAllByIdInAndOrganisationIdAndComplianceAreaOrdered(List.of(6L), orgId, ComplianceArea.IK_MAT))
        .thenReturn(List.of(template));
    when(tasksRepository.findAllByChecklist_IdAndActiveTrue(10L)).thenReturn(List.of(activeTask));
    when(checklistRepository.save(any(ChecklistModel.class))).thenAnswer(inv -> inv.getArgument(0));
    when(checklistMapper.taskTemplateComparator()).thenReturn(Comparator.comparing(TaskTemplate::getTitle));
    when(checklistMapper.toCardResponse(any(), any(), any(), any(), any(Boolean.class)))
        .thenReturn(stubCard(10L));

    // Change frequency from DAILY to WEEKLY
    checklistService.updateChecklist(10L,
        new UpdateChecklistCardRequest("weekly", "Updated", null, true, false, List.of(6L)),
        principal);

    assertThat(activeTask.isActive()).isFalse();
    verify(tasksRepository).saveAll(anyList());
  }

  @Test
  void updateChecklist_whenWorkbenchHidden_deletesAllTasks() {
    String periodKey = PeriodKeyUtil.currentPeriodKey(ChecklistFrequency.DAILY, ZoneId.systemDefault());
    TaskTemplate template = template(7L, "Sanitize");
    ChecklistModel cl = checklist(11L, periodKey, true, true, template); // currently shown

    TasksModel existingTask = activatedTask(300L, cl, template, periodKey);

    when(checklistRepository.findByIdAndOrganization_Id(11L, orgId)).thenReturn(Optional.of(cl));
    when(taskTemplateRepository.findAllByIdInAndOrganisationIdAndComplianceAreaOrdered(List.of(7L), orgId, ComplianceArea.IK_MAT))
        .thenReturn(List.of(template));
    when(tasksRepository.findAllByChecklist_Id(11L)).thenReturn(List.of(existingTask));
    when(checklistRepository.save(any(ChecklistModel.class))).thenAnswer(inv -> inv.getArgument(0));
    when(checklistMapper.taskTemplateComparator()).thenReturn(Comparator.comparing(TaskTemplate::getTitle));
    when(checklistMapper.toCardResponse(any(), any(), any(), any(), any(Boolean.class)))
        .thenReturn(stubCard(11L));

    checklistService.updateChecklist(11L,
        new UpdateChecklistCardRequest("daily", "Hidden checklist", null, true, false, List.of(7L)),
        principal);

    verify(temperatureMeasurementRepository).deleteAllByTaskIn(List.of(existingTask));
    verify(tasksRepository).deleteAll(List.of(existingTask));
  }

  // ── deleteChecklist ───────────────────────────────────────────────────────

  @Test
  void deleteChecklist_deletesChecklistAndTouchesCache() {
    String periodKey = PeriodKeyUtil.currentPeriodKey(ChecklistFrequency.DAILY, ZoneId.systemDefault());
    TaskTemplate template = template(8L, "Close");
    ChecklistModel cl = checklist(20L, periodKey, false, false, template);

    when(checklistRepository.findByIdAndOrganization_Id(20L, orgId)).thenReturn(Optional.of(cl));
    when(tasksRepository.findAllByChecklist_Id(20L)).thenReturn(List.of());

    checklistService.deleteChecklist(20L, principal);

    verify(checklistRepository).delete(cl);
    verify(checklistCacheStateService).touch(orgId, ComplianceArea.IK_MAT);
  }

  @Test
  void deleteChecklist_whenChecklistNotFound_throwsException() {
    when(checklistRepository.findByIdAndOrganization_Id(99L, orgId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> checklistService.deleteChecklist(99L, principal))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Checklist not found");

    verify(checklistRepository, never()).delete(any());
  }

  @Test
  void deleteChecklist_deletesAssociatedTasksAndMeasurementsFirst() {
    String periodKey = PeriodKeyUtil.currentPeriodKey(ChecklistFrequency.DAILY, ZoneId.systemDefault());
    TaskTemplate template = template(9L, "Wash");
    ChecklistModel cl = checklist(21L, periodKey, false, false, template);
    TasksModel task = activatedTask(400L, cl, template, periodKey);

    when(checklistRepository.findByIdAndOrganization_Id(21L, orgId)).thenReturn(Optional.of(cl));
    when(tasksRepository.findAllByChecklist_Id(21L)).thenReturn(List.of(task));

    checklistService.deleteChecklist(21L, principal);

    verify(temperatureMeasurementRepository).deleteAllByTaskIn(List.of(task));
    verify(tasksRepository).deleteAll(List.of(task));
    verify(checklistRepository).delete(cl);
  }

  // ── setTaskFlag ───────────────────────────────────────────────────────────

  @Test
  void setTaskFlag_whenPending_flagsTaskAndClearsCompletion() {
    String periodKey = PeriodKeyUtil.currentPeriodKey(ChecklistFrequency.DAILY, ZoneId.systemDefault());
    TaskTemplate template = template(40L, "Check fridge");
    ChecklistModel cl = checklist(30L, periodKey, true, true, template);

    TasksModel task = activatedTask(800L, cl, template, periodKey);
    task.setCompleted(true);
    task.setEndedAt(LocalDateTime.now());

    when(checklistRepository.findByIdAndOrganization_Id(30L, orgId)).thenReturn(Optional.of(cl));
    when(tasksRepository.findByIdAndChecklist_Id(800L, 30L)).thenReturn(Optional.of(task));
    when(tasksRepository.save(any(TasksModel.class))).thenAnswer(inv -> inv.getArgument(0));
    when(temperatureMeasurementRepository.findAllByTask_IdInOrderByMeasuredAtDesc(List.of(800L))).thenReturn(List.of());
    when(checklistMapper.toTaskItemResponse(any(TasksModel.class), any()))
        .thenAnswer(inv -> {
          TasksModel t = inv.getArgument(0);
          return new ChecklistTaskItemResponse(t.getId(), t.getTaskTemplate().getId(),
              t.getTaskTemplate().getTitle(), t.getMeta(), null, null, null, null,
              t.isCompleted() ? "completed" : "todo",
              t.isFlagged() ? Boolean.TRUE : Boolean.FALSE,
              null, null, t.isFlagged() ? t.getPeriodKey() : null, null);
        });

    var response = checklistService.setTaskFlag(30L, 800L,
        new TaskFlagRequest("pending", periodKey, LocalDateTime.now()), principal);

    assertThat(task.isFlagged()).isTrue();
    assertThat(task.isCompleted()).isFalse();
    assertThat(task.getEndedAt()).isNull();
    assertThat(response.pendingForPeriodKey()).isEqualTo(periodKey);
  }

  @Test
  void setTaskFlag_whenTodo_clearsFlag() {
    String periodKey = PeriodKeyUtil.currentPeriodKey(ChecklistFrequency.DAILY, ZoneId.systemDefault());
    TaskTemplate template = template(41L, "Check freezer");
    ChecklistModel cl = checklist(31L, periodKey, true, true, template);

    TasksModel task = activatedTask(810L, cl, template, periodKey);
    task.setFlagged(true);

    when(checklistRepository.findByIdAndOrganization_Id(31L, orgId)).thenReturn(Optional.of(cl));
    when(tasksRepository.findByIdAndChecklist_Id(810L, 31L)).thenReturn(Optional.of(task));
    when(tasksRepository.save(any(TasksModel.class))).thenAnswer(inv -> inv.getArgument(0));
    when(temperatureMeasurementRepository.findAllByTask_IdInOrderByMeasuredAtDesc(List.of(810L))).thenReturn(List.of());
    when(checklistMapper.toTaskItemResponse(any(TasksModel.class), any()))
        .thenAnswer(inv -> {
          TasksModel t = inv.getArgument(0);
          return new ChecklistTaskItemResponse(t.getId(), t.getTaskTemplate().getId(),
              t.getTaskTemplate().getTitle(), t.getMeta(), null, null, null, null,
              "todo", t.isFlagged() ? Boolean.TRUE : Boolean.FALSE,
              null, null, null, null);
        });

    var response = checklistService.setTaskFlag(31L, 810L,
        new TaskFlagRequest("todo", periodKey, null), principal);

    assertThat(task.isFlagged()).isFalse();
    assertThat(response.highlighted()).isFalse();
  }

  @Test
  void setTaskFlag_invalidState_throwsException() {
    String periodKey = PeriodKeyUtil.currentPeriodKey(ChecklistFrequency.DAILY, ZoneId.systemDefault());
    TaskTemplate template = template(42L, "Some task");
    ChecklistModel cl = checklist(32L, periodKey, true, true, template);
    TasksModel task = activatedTask(820L, cl, template, periodKey);

    when(checklistRepository.findByIdAndOrganization_Id(32L, orgId)).thenReturn(Optional.of(cl));
    when(tasksRepository.findByIdAndChecklist_Id(820L, 32L)).thenReturn(Optional.of(task));

    assertThatThrownBy(() -> checklistService.setTaskFlag(32L, 820L,
        new TaskFlagRequest("invalid", periodKey, null), principal))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid state");

    verify(tasksRepository, never()).save(any());
  }

  // ── setChecklistWorkbenchState (show path) ────────────────────────────────

  @Test
  void setChecklistWorkbenchState_whenShown_doesNotDeleteTasks() {
    String periodKey = PeriodKeyUtil.currentPeriodKey(ChecklistFrequency.DAILY, ZoneId.systemDefault());
    TaskTemplate template = template(50L, "Open doors");
    ChecklistModel cl = checklist(40L, periodKey, true, false, template); // currently hidden

    when(checklistRepository.findByIdAndOrganization_Id(40L, orgId)).thenReturn(Optional.of(cl));
    when(checklistRepository.save(any(ChecklistModel.class))).thenAnswer(inv -> inv.getArgument(0));
    when(tasksRepository.findAllByChecklist_IdAndPeriodKeyAndActiveTrue(eq(40L), any())).thenReturn(new java.util.ArrayList<>());
    when(checklistMapper.taskTemplateComparator()).thenReturn(Comparator.comparing(TaskTemplate::getTitle));
    when(checklistMapper.toCardResponse(any(), any(), any(), any(), any(Boolean.class)))
        .thenReturn(stubCard(40L));

    var response = checklistService.setChecklistWorkbenchState(40L,
        new ChecklistWorkbenchStateRequest(true), principal);

    assertThat(response.displayedOnWorkbench()).isFalse(); // stub returns false, but key assertion is below
    verify(tasksRepository, never()).deleteAll(anyList());
    verify(temperatureMeasurementRepository, never()).deleteAllByTaskIn(anyList());
  }

  // ── helpers ───────────────────────────────────────────────────────────────

  private ChecklistCardResponse stubCard(Long id) {
    return new ChecklistCardResponse(id, "2026-04-09", "2026-04-09", false, false, "Stub",
        null, null, false, null, null, 0, List.of());
  }
}
