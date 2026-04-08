package edu.ntnu.idatt2105.backend.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.CreateTemperatureMeasurementRequest;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.TemperatureMeasurementResponse;
import edu.ntnu.idatt2105.backend.checklist.model.ChecklistModel;
import edu.ntnu.idatt2105.backend.checklist.service.ChecklistCacheStateService;
import edu.ntnu.idatt2105.backend.task.model.TaskTemplate;
import edu.ntnu.idatt2105.backend.temperature.model.TemperatureMeasurementModel;
import edu.ntnu.idatt2105.backend.task.model.TasksModel;
import edu.ntnu.idatt2105.backend.checklist.model.enums.ChecklistFrequency;
import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.checklist.model.enums.SectionTypes;
import edu.ntnu.idatt2105.backend.checklist.repository.ChecklistRepository;
import edu.ntnu.idatt2105.backend.temperature.repository.TemperatureMeasurementRepository;
import edu.ntnu.idatt2105.backend.task.repository.TasksRepository;
import edu.ntnu.idatt2105.backend.temperature.service.TemperatureMeasurementServiceImpl;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import edu.ntnu.idatt2105.backend.user.repository.OrganizationRepository;
import edu.ntnu.idatt2105.backend.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
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
class TemperatureMeasurementServiceImplTest {

  @Mock private TemperatureMeasurementRepository temperatureMeasurementRepository;
  @Mock private ChecklistRepository checklistRepository;
  @Mock private TasksRepository tasksRepository;
  @Mock private OrganizationRepository organizationRepository;
  @Mock private UserRepository userRepository;
  @Mock private ChecklistCacheStateService checklistCacheStateService;

  @InjectMocks private TemperatureMeasurementServiceImpl temperatureMeasurementService;

  private UUID orgId;
  private UUID userId;
  private JwtAuthenticatedPrincipal principal;

  @BeforeEach
  void setUp() {
    orgId = UUID.randomUUID();
    userId = UUID.randomUUID();
    principal = new JwtAuthenticatedPrincipal(userId, orgId, "tester", Collections.emptyList());
  }

  @Test
  void createMeasurement_persistsMeasurementAndMarksDeviationWhenOutOfRange() {
    ChecklistModel checklist = checklist(1L, ComplianceArea.IK_MAT);
    TaskTemplate template = temperatureTemplate();
    TasksModel task = task(10L, checklist, template, "2026-04-07", true);
    OrganizationModel organization = new OrganizationModel();
    organization.setId(orgId);
    UserModel user = new UserModel();
    user.setId(userId);
    user.setFirstName("Olivia");
    user.setLastName("Cook");

    when(checklistRepository.findByIdAndOrganization_Id(1L, orgId)).thenReturn(Optional.of(checklist));
    when(tasksRepository.findByIdAndChecklist_Id(10L, 1L)).thenReturn(Optional.of(task));
    when(organizationRepository.findById(orgId)).thenReturn(Optional.of(organization));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(temperatureMeasurementRepository.save(any(TemperatureMeasurementModel.class))).thenAnswer(invocation -> {
      TemperatureMeasurementModel measurement = invocation.getArgument(0);
      measurement.setId(500L);
      return measurement;
    });

    TemperatureMeasurementResponse response = temperatureMeasurementService.createMeasurement(
        new CreateTemperatureMeasurementRequest(
            IcModule.IC_FOOD,
            1L,
            10L,
            new BigDecimal("6.5"),
            LocalDateTime.of(2026, 4, 7, 9, 30),
            "2026-04-07"),
        principal);

    ArgumentCaptor<TemperatureMeasurementModel> captor = ArgumentCaptor.forClass(TemperatureMeasurementModel.class);
    verify(temperatureMeasurementRepository).save(captor.capture());
    TemperatureMeasurementModel saved = captor.getValue();

    assertThat(saved.getPeriodKey()).isEqualTo("2026-04-07");
    assertThat(saved.getOrganization()).isSameAs(organization);
    assertThat(saved.getRecordedBy()).isSameAs(user);
    assertThat(response.deviation()).isTrue();
    assertThat(response.taskId()).isEqualTo(10L);
  }

  @Test
  void createMeasurement_rejectsWhenRequestedPeriodKeyDoesNotMatchTaskPeriod() {
    ChecklistModel checklist = checklist(1L, ComplianceArea.IK_MAT);
    TasksModel task = task(10L, checklist, temperatureTemplate(), "2026-04-07", true);

    when(checklistRepository.findByIdAndOrganization_Id(1L, orgId)).thenReturn(Optional.of(checklist));
    when(tasksRepository.findByIdAndChecklist_Id(10L, 1L)).thenReturn(Optional.of(task));

    assertThatThrownBy(() -> temperatureMeasurementService.createMeasurement(
        new CreateTemperatureMeasurementRequest(
            IcModule.IC_FOOD,
            1L,
            10L,
            new BigDecimal("3.0"),
            LocalDateTime.now(),
            "2026-04-08"),
        principal))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("periodKey does not match");
  }

  @Test
  void createMeasurement_rejectsNonTemperatureTasks() {
    ChecklistModel checklist = checklist(1L, ComplianceArea.IK_MAT);
    TaskTemplate template = new TaskTemplate();
    template.setId(22L);
    template.setTitle("Wash hands");
    template.setSectionType(SectionTypes.HYGIENE);
    TasksModel task = task(10L, checklist, template, "2026-04-07", true);

    when(checklistRepository.findByIdAndOrganization_Id(1L, orgId)).thenReturn(Optional.of(checklist));
    when(tasksRepository.findByIdAndChecklist_Id(10L, 1L)).thenReturn(Optional.of(task));

    assertThatThrownBy(() -> temperatureMeasurementService.createMeasurement(
        new CreateTemperatureMeasurementRequest(
            IcModule.IC_FOOD,
            1L,
            10L,
            new BigDecimal("3.0"),
            LocalDateTime.now(),
            "2026-04-07"),
        principal))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("temperature-controlled");
  }

  @Test
  void fetchMeasurements_validatesToAfterFrom() {
    assertThatThrownBy(() -> temperatureMeasurementService.fetchMeasurements(
        IcModule.IC_FOOD,
        LocalDateTime.of(2026, 4, 8, 0, 0),
        LocalDateTime.of(2026, 4, 7, 0, 0),
        principal))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("to must be after from");
  }

  private ChecklistModel checklist(Long id, ComplianceArea area) {
    ChecklistModel checklist = new ChecklistModel();
    checklist.setId(id);
    checklist.setName("Checklist");
    checklist.setComplianceArea(area);
    checklist.setFrequency(ChecklistFrequency.DAILY);
    return checklist;
  }

  private TaskTemplate temperatureTemplate() {
    TaskTemplate template = new TaskTemplate();
    template.setId(20L);
    template.setTitle("Check fridge");
    template.setSectionType(SectionTypes.TEMPERATURE_CONTROL);
    template.setUnit("C");
    template.setTargetMin(new BigDecimal("2.0"));
    template.setTargetMax(new BigDecimal("5.0"));
    return template;
  }

  private TasksModel task(Long id, ChecklistModel checklist, TaskTemplate template, String periodKey, boolean active) {
    TasksModel task = new TasksModel();
    task.setId(id);
    task.setChecklist(checklist);
    task.setTaskTemplate(template);
    task.setPeriodKey(periodKey);
    task.setActive(active);
    return task;
  }
}
