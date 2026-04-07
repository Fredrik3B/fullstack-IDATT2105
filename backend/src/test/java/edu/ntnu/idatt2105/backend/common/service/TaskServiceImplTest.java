package edu.ntnu.idatt2105.backend.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.ntnu.idatt2105.backend.common.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.common.dto.task.CreateTaskRequest;
import edu.ntnu.idatt2105.backend.common.dto.task.TaskResponse;
import edu.ntnu.idatt2105.backend.common.model.ChecklistModel;
import edu.ntnu.idatt2105.backend.common.model.TaskTemplate;
import edu.ntnu.idatt2105.backend.common.model.TemperatureZoneModel;
import edu.ntnu.idatt2105.backend.common.model.TasksModel;
import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.common.model.enums.SectionTypes;
import edu.ntnu.idatt2105.backend.common.model.enums.TemperatureZone;
import edu.ntnu.idatt2105.backend.common.repository.ChecklistRepository;
import edu.ntnu.idatt2105.backend.common.repository.TaskTemplateRepository;
import edu.ntnu.idatt2105.backend.common.repository.TemperatureMeasurementRepository;
import edu.ntnu.idatt2105.backend.common.repository.TemperatureZoneRepository;
import edu.ntnu.idatt2105.backend.common.repository.TasksRepository;
import edu.ntnu.idatt2105.backend.common.service.impl.TaskServiceImpl;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import java.math.BigDecimal;
import java.util.Collections;
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
class TaskServiceImplTest {

  @Mock private TaskTemplateRepository taskTemplateRepository;
  @Mock private ChecklistRepository checklistRepository;
  @Mock private TasksRepository tasksRepository;
  @Mock private TemperatureMeasurementRepository temperatureMeasurementRepository;
  @Mock private TemperatureZoneRepository temperatureZoneRepository;
  @Mock private ChecklistCacheStateService checklistCacheStateService;

  @InjectMocks private TaskServiceImpl taskService;

  private UUID orgId;
  private JwtAuthenticatedPrincipal principal;

  @BeforeEach
  void setUp() {
    orgId = UUID.randomUUID();
    principal = new JwtAuthenticatedPrincipal(UUID.randomUUID(), orgId, "tester", Collections.emptyList());
  }

  @Test
  void createTask_temperatureControlCopiesRangeAndZoneFromSelectedTemperatureItem() {
    TemperatureZoneModel zone = new TemperatureZoneModel();
    zone.setId(44L);
    zone.setName("Main freezer");
    zone.setZoneType(TemperatureZone.FREEZER);
    zone.setComplianceArea(ComplianceArea.IK_MAT);
    zone.setOrganizationId(orgId);
    zone.setTargetMin(new BigDecimal("-20.00"));
    zone.setTargetMax(new BigDecimal("-15.00"));

    when(temperatureZoneRepository.findByIdAndOrganizationIdAndComplianceArea(44L, orgId, ComplianceArea.IK_MAT))
        .thenReturn(Optional.of(zone));
    when(taskTemplateRepository.save(any(TaskTemplate.class))).thenAnswer(invocation -> {
      TaskTemplate template = invocation.getArgument(0);
      template.setId(77L);
      return template;
    });

    TaskResponse response = taskService.createTask(
        new CreateTaskRequest(
            IcModule.IC_FOOD,
            "Check freezer at opening",
            "Use calibrated thermometer",
            SectionTypes.TEMPERATURE_CONTROL,
            44L,
            null,
            null),
        principal);

    ArgumentCaptor<TaskTemplate> captor = ArgumentCaptor.forClass(TaskTemplate.class);
    verify(taskTemplateRepository).save(captor.capture());
    TaskTemplate saved = captor.getValue();

    assertThat(saved.getTemperatureZone()).isSameAs(zone);
    assertThat(saved.getUnit()).isEqualTo("C");
    assertThat(saved.getTargetMin()).isEqualByComparingTo("-20.00");
    assertThat(saved.getTargetMax()).isEqualByComparingTo("-15.00");
    assertThat(response.temperatureZoneId()).isEqualTo(44L);
    assertThat(response.temperatureZoneName()).isEqualTo("Main freezer");
  }

  @Test
  void createTask_temperatureControlWithoutTemperatureZoneIsRejected() {
    CreateTaskRequest request = new CreateTaskRequest(
        IcModule.IC_FOOD,
        "Check fridge",
        null,
        SectionTypes.TEMPERATURE_CONTROL,
        null,
        null,
        null);

    assertThatThrownBy(() -> taskService.createTask(request, principal))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("temperatureZoneId is required");

    verify(taskTemplateRepository, never()).save(any(TaskTemplate.class));
  }

  @Test
  void deleteTask_removesTaskFromChecklistsAndDeletesActivatedTaskData() {
    TaskTemplate template = new TaskTemplate();
    template.setId(9L);
    template.setOrganisationId(orgId);
    template.setComplianceArea(ComplianceArea.IK_MAT);
    template.setTitle("Wash hands");

    ChecklistModel checklist = new ChecklistModel();
    checklist.setTaskTemplates(new java.util.LinkedHashSet<>(List.of(template)));

    TasksModel activatedTask = new TasksModel();
    activatedTask.setId(101L);
    activatedTask.setTaskTemplate(template);

    when(taskTemplateRepository.findById(9L)).thenReturn(Optional.of(template));
    when(checklistRepository.findAllByOrganization_IdOrderByIdAsc(orgId)).thenReturn(List.of(checklist));
    when(tasksRepository.findAllByTaskTemplate_Id(9L)).thenReturn(List.of(activatedTask));

    taskService.deleteTask(9L, principal);

    assertThat(checklist.getTaskTemplates()).isEmpty();
    verify(checklistRepository).save(checklist);
    verify(temperatureMeasurementRepository).deleteAllByTaskIn(List.of(activatedTask));
    verify(tasksRepository).deleteAll(List.of(activatedTask));
    verify(taskTemplateRepository).delete(template);
  }

  @Test
  void updateTask_updatesExistingActivatedTaskMetaToMatchTemplateMeta() {
    TaskTemplate template = new TaskTemplate();
    template.setId(13L);
    template.setOrganisationId(orgId);
    template.setComplianceArea(ComplianceArea.IK_MAT);
    template.setSectionType(SectionTypes.HYGIENE);
    template.setTitle("Old title");
    template.setMeta("old meta");

    TasksModel activatedTask = new TasksModel();
    activatedTask.setId(300L);
    activatedTask.setTaskTemplate(template);
    activatedTask.setMeta("old meta");

    when(taskTemplateRepository.findById(13L)).thenReturn(Optional.of(template));
    when(tasksRepository.findAllByTaskTemplate_Id(13L)).thenReturn(List.of(activatedTask));
    when(taskTemplateRepository.save(any(TaskTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

    TaskResponse response = taskService.updateTask(
        13L,
        new CreateTaskRequest(
            IcModule.IC_FOOD,
            "Updated hygiene task",
            "new meta",
            SectionTypes.HYGIENE,
            null,
            null,
            null),
        principal);

    assertThat(activatedTask.getMeta()).isEqualTo("new meta");
    verify(tasksRepository).saveAll(eq(List.of(activatedTask)));
    assertThat(response.meta()).isEqualTo("new meta");
    assertThat(response.title()).isEqualTo("Updated hygiene task");
  }
}
