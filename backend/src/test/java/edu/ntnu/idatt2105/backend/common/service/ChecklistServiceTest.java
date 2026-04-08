package edu.ntnu.idatt2105.backend.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.ntnu.idatt2105.backend.checklist.dto.ChecklistWorkbenchStateRequest;
import edu.ntnu.idatt2105.backend.checklist.dto.TaskCompletionRequest;
import edu.ntnu.idatt2105.backend.checklist.model.ChecklistModel;
import edu.ntnu.idatt2105.backend.checklist.service.ChecklistCacheStateService;
import edu.ntnu.idatt2105.backend.task.model.TaskTemplate;
import edu.ntnu.idatt2105.backend.task.model.TasksModel;
import edu.ntnu.idatt2105.backend.checklist.model.enums.ChecklistFrequency;
import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.checklist.model.enums.SectionTypes;
import edu.ntnu.idatt2105.backend.checklist.repository.ChecklistRepository;
import edu.ntnu.idatt2105.backend.task.repository.TaskTemplateRepository;
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

    assertThatThrownBy(() -> checklistService.submitChecklist(2L, principal))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("already been submitted");

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

    var response = checklistService.setTaskCompletion(
        4L,
        500L,
        new TaskCompletionRequest("todo", periodKey, null),
        principal);

    assertThat(task.isCompleted()).isFalse();
    assertThat(task.getEndedAt()).isNull();
    assertThat(response.state()).isEqualTo("todo");
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
}
