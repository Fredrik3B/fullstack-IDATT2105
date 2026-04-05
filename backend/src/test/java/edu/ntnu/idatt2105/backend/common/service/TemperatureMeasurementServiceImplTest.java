package edu.ntnu.idatt2105.backend.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.ntnu.idatt2105.backend.common.dto.icchecklist.CreateTemperatureMeasurementRequest;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.TemperatureMeasurementResponse;
import edu.ntnu.idatt2105.backend.common.model.ChecklistModel;
import edu.ntnu.idatt2105.backend.common.model.TaskTemplate;
import edu.ntnu.idatt2105.backend.common.model.TasksModel;
import edu.ntnu.idatt2105.backend.common.model.TemperatureMeasurementModel;
import edu.ntnu.idatt2105.backend.common.model.enums.ChecklistFrequency;
import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.common.repository.ChecklistRepository;
import edu.ntnu.idatt2105.backend.common.repository.TasksRepository;
import edu.ntnu.idatt2105.backend.common.repository.TemperatureMeasurementRepository;
import edu.ntnu.idatt2105.backend.common.service.impl.TemperatureMeasurementServiceImpl;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import edu.ntnu.idatt2105.backend.user.repository.OrganizationRepository;
import edu.ntnu.idatt2105.backend.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
@DisplayName("TemperatureMeasurementServiceImpl")
class TemperatureMeasurementServiceImplTest {

    @InjectMocks
    private TemperatureMeasurementServiceImpl service;

    @Mock
    private TemperatureMeasurementRepository temperatureMeasurementRepository;

    @Mock
    private ChecklistRepository checklistRepository;

    @Mock
    private TasksRepository tasksRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    private final UUID userId = UUID.randomUUID();
    private final UUID orgId = UUID.randomUUID();
    private JwtAuthenticatedPrincipal principal;
    private OrganizationModel org;
    private UserModel user;
    private ChecklistModel checklist;
    private TaskTemplate taskTemplate;
    private TasksModel task;

    @BeforeEach
    void setUp() {
        principal = new JwtAuthenticatedPrincipal(
                userId, orgId, "user@test.com",
                List.of(new SimpleGrantedAuthority("ROLE_STAFF")));

        org = new OrganizationModel();
        org.setId(orgId);
        org.setName("Test Org");
        org.setJoinCode("CODE");

        user = new UserModel();
        user.setId(userId);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("user@test.com");
        user.setPassword("hashed");

        taskTemplate = new TaskTemplate();
        taskTemplate.setId(10L);
        taskTemplate.setTitle("Fridge temp");
        taskTemplate.setUnit("C");
        taskTemplate.setTargetMin(new BigDecimal("2.00"));
        taskTemplate.setTargetMax(new BigDecimal("8.00"));
        taskTemplate.setSectionType(edu.ntnu.idatt2105.backend.common.model.enums.SectionTypes.TEMPERATURE_CONTROL);
        taskTemplate.setComplianceArea(ComplianceArea.IK_MAT);
        taskTemplate.setOrganisationId(orgId);

        checklist = new ChecklistModel();
        checklist.setId(1L);
        checklist.setName("Food Safety");
        checklist.setFrequency(ChecklistFrequency.DAILY);
        checklist.setComplianceArea(ComplianceArea.IK_MAT);
        checklist.setActivePeriodKey("2024-01-15");
        checklist.setOrganization(org);

        task = new TasksModel();
        task.setId(100L);
        task.setChecklist(checklist);
        task.setTaskTemplate(taskTemplate);
        task.setActive(true);
        task.setCompleted(false);
        task.setFlagged(false);
        task.setPeriodKey("2024-01-15");
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private TemperatureMeasurementModel makeSavedMeasurement(BigDecimal valueC) {
        TemperatureMeasurementModel saved = new TemperatureMeasurementModel();
        saved.setId(99L);
        saved.setComplianceArea(ComplianceArea.IK_MAT);
        saved.setChecklist(checklist);
        saved.setTask(task);
        saved.setPeriodKey("2024-01-15");
        saved.setValueC(valueC);
        saved.setMeasuredAt(LocalDateTime.now());
        saved.setOrganization(org);
        saved.setRecordedBy(user);
        return saved;
    }

    // ── createMeasurement ─────────────────────────────────────────────────────

    @Test
    @DisplayName("createMeasurement - valid request saves and returns response")
    void createMeasurement_success() {
        BigDecimal valueC = new BigDecimal("5.00");
        CreateTemperatureMeasurementRequest request = new CreateTemperatureMeasurementRequest(
                IcModule.IC_FOOD, checklist.getId(), task.getId(), valueC, null, null);

        when(checklistRepository.findByIdAndOrganization_Id(checklist.getId(), orgId))
                .thenReturn(Optional.of(checklist));
        when(tasksRepository.findByIdAndChecklist_Id(task.getId(), checklist.getId()))
                .thenReturn(Optional.of(task));
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        TemperatureMeasurementModel saved = makeSavedMeasurement(valueC);
        when(temperatureMeasurementRepository.save(any())).thenReturn(saved);

        TemperatureMeasurementResponse response = service.createMeasurement(request, principal);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.valueC()).isEqualByComparingTo(valueC);
        assertThat(response.deviation()).isFalse();
        verify(temperatureMeasurementRepository).save(any(TemperatureMeasurementModel.class));
    }

    @Test
    @DisplayName("createMeasurement - flags deviation when value is below targetMin")
    void createMeasurement_belowMin_isDeviation() {
        BigDecimal valueC = new BigDecimal("1.00"); // below min of 2.00
        CreateTemperatureMeasurementRequest request = new CreateTemperatureMeasurementRequest(
                IcModule.IC_FOOD, checklist.getId(), task.getId(), valueC, null, null);

        when(checklistRepository.findByIdAndOrganization_Id(checklist.getId(), orgId))
                .thenReturn(Optional.of(checklist));
        when(tasksRepository.findByIdAndChecklist_Id(task.getId(), checklist.getId()))
                .thenReturn(Optional.of(task));
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        TemperatureMeasurementModel saved = makeSavedMeasurement(valueC);
        when(temperatureMeasurementRepository.save(any())).thenReturn(saved);

        TemperatureMeasurementResponse response = service.createMeasurement(request, principal);

        assertThat(response.deviation()).isTrue();
    }

    @Test
    @DisplayName("createMeasurement - flags deviation when value is above targetMax")
    void createMeasurement_aboveMax_isDeviation() {
        BigDecimal valueC = new BigDecimal("9.00"); // above max of 8.00
        CreateTemperatureMeasurementRequest request = new CreateTemperatureMeasurementRequest(
                IcModule.IC_FOOD, checklist.getId(), task.getId(), valueC, null, null);

        when(checklistRepository.findByIdAndOrganization_Id(checklist.getId(), orgId))
                .thenReturn(Optional.of(checklist));
        when(tasksRepository.findByIdAndChecklist_Id(task.getId(), checklist.getId()))
                .thenReturn(Optional.of(task));
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        TemperatureMeasurementModel saved = makeSavedMeasurement(valueC);
        when(temperatureMeasurementRepository.save(any())).thenReturn(saved);

        TemperatureMeasurementResponse response = service.createMeasurement(request, principal);

        assertThat(response.deviation()).isTrue();
    }

    @Test
    @DisplayName("createMeasurement - throws when checklist not found")
    void createMeasurement_checklistNotFound_throws() {
        CreateTemperatureMeasurementRequest request = new CreateTemperatureMeasurementRequest(
                IcModule.IC_FOOD, 999L, task.getId(), BigDecimal.ONE, null, null);

        when(checklistRepository.findByIdAndOrganization_Id(999L, orgId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createMeasurement(request, principal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Checklist not found");
    }

    @Test
    @DisplayName("createMeasurement - throws when module does not match checklist compliance area")
    void createMeasurement_moduleMismatch_throws() {
        // Checklist is IC_MAT, but we pass IC_ALCOHOL
        CreateTemperatureMeasurementRequest request = new CreateTemperatureMeasurementRequest(
                IcModule.IC_ALCOHOL, checklist.getId(), task.getId(), BigDecimal.ONE, null, null);

        when(checklistRepository.findByIdAndOrganization_Id(checklist.getId(), orgId))
                .thenReturn(Optional.of(checklist));

        assertThatThrownBy(() -> service.createMeasurement(request, principal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("module does not match");
    }

    @Test
    @DisplayName("createMeasurement - throws when task not found in checklist")
    void createMeasurement_taskNotFound_throws() {
        CreateTemperatureMeasurementRequest request = new CreateTemperatureMeasurementRequest(
                IcModule.IC_FOOD, checklist.getId(), 999L, BigDecimal.ONE, null, null);

        when(checklistRepository.findByIdAndOrganization_Id(checklist.getId(), orgId))
                .thenReturn(Optional.of(checklist));
        when(tasksRepository.findByIdAndChecklist_Id(999L, checklist.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createMeasurement(request, principal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("task not found");
    }

    @Test
    @DisplayName("createMeasurement - throws when task is not active")
    void createMeasurement_inactiveTask_throws() {
        task.setActive(false);
        CreateTemperatureMeasurementRequest request = new CreateTemperatureMeasurementRequest(
                IcModule.IC_FOOD, checklist.getId(), task.getId(), BigDecimal.ONE, null, null);

        when(checklistRepository.findByIdAndOrganization_Id(checklist.getId(), orgId))
                .thenReturn(Optional.of(checklist));
        when(tasksRepository.findByIdAndChecklist_Id(task.getId(), checklist.getId()))
                .thenReturn(Optional.of(task));

        assertThatThrownBy(() -> service.createMeasurement(request, principal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("active");
    }

    @Test
    @DisplayName("createMeasurement - throws when task is not a temperature task")
    void createMeasurement_nonTemperatureTask_throws() {
        // Remove temperature-related fields from template
        taskTemplate.setUnit(null);
        taskTemplate.setTargetMin(null);
        taskTemplate.setTargetMax(null);

        CreateTemperatureMeasurementRequest request = new CreateTemperatureMeasurementRequest(
                IcModule.IC_FOOD, checklist.getId(), task.getId(), BigDecimal.ONE, null, null);

        when(checklistRepository.findByIdAndOrganization_Id(checklist.getId(), orgId))
                .thenReturn(Optional.of(checklist));
        when(tasksRepository.findByIdAndChecklist_Id(task.getId(), checklist.getId()))
                .thenReturn(Optional.of(task));

        assertThatThrownBy(() -> service.createMeasurement(request, principal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("temperature");
    }

    @Test
    @DisplayName("createMeasurement - throws when principal is null")
    void createMeasurement_nullPrincipal_throws() {
        CreateTemperatureMeasurementRequest request = new CreateTemperatureMeasurementRequest(
                IcModule.IC_FOOD, 1L, 1L, BigDecimal.ONE, null, null);

        assertThatThrownBy(() -> service.createMeasurement(request, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("createMeasurement - throws when principal has no organization")
    void createMeasurement_noOrg_throws() {
        JwtAuthenticatedPrincipal noOrgPrincipal = new JwtAuthenticatedPrincipal(
                userId, null, "user@test.com", List.of());
        CreateTemperatureMeasurementRequest request = new CreateTemperatureMeasurementRequest(
                IcModule.IC_FOOD, 1L, 1L, BigDecimal.ONE, null, null);

        assertThatThrownBy(() -> service.createMeasurement(request, noOrgPrincipal))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── fetchMeasurements ─────────────────────────────────────────────────────

    @Test
    @DisplayName("fetchMeasurements - returns mapped responses for the organization")
    void fetchMeasurements_returnsResults() {
        TemperatureMeasurementModel m = makeSavedMeasurement(new BigDecimal("5.00"));
        LocalDateTime from = LocalDateTime.now().minusDays(7);
        LocalDateTime to = LocalDateTime.now();

        when(temperatureMeasurementRepository
                .findAllByOrganization_IdAndComplianceAreaAndMeasuredAtBetweenOrderByMeasuredAtDesc(
                        orgId, ComplianceArea.IK_MAT, from, to))
                .thenReturn(List.of(m));

        List<TemperatureMeasurementResponse> results = service.fetchMeasurements(
                IcModule.IC_FOOD, from, to, principal);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).id()).isEqualTo(99L);
    }

    @Test
    @DisplayName("fetchMeasurements - returns empty list when no records match")
    void fetchMeasurements_empty() {
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();

        when(temperatureMeasurementRepository
                .findAllByOrganization_IdAndComplianceAreaAndMeasuredAtBetweenOrderByMeasuredAtDesc(
                        orgId, ComplianceArea.IK_MAT, from, to))
                .thenReturn(List.of());

        List<TemperatureMeasurementResponse> results = service.fetchMeasurements(
                IcModule.IC_FOOD, from, to, principal);

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("fetchMeasurements - throws when 'to' is before 'from'")
    void fetchMeasurements_toBeforeFrom_throws() {
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = from.minusDays(1);

        assertThatThrownBy(() -> service.fetchMeasurements(IcModule.IC_FOOD, from, to, principal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("to must be after from");
    }

    @Test
    @DisplayName("fetchMeasurements - uses LocalDateTime.MIN when from is null")
    void fetchMeasurements_nullFrom_usesMin() {
        LocalDateTime to = LocalDateTime.now();

        when(temperatureMeasurementRepository
                .findAllByOrganization_IdAndComplianceAreaAndMeasuredAtBetweenOrderByMeasuredAtDesc(
                        orgId, ComplianceArea.IK_MAT, LocalDateTime.MIN, to))
                .thenReturn(List.of());

        List<TemperatureMeasurementResponse> results = service.fetchMeasurements(
                IcModule.IC_FOOD, null, to, principal);

        assertThat(results).isEmpty();
        verify(temperatureMeasurementRepository)
                .findAllByOrganization_IdAndComplianceAreaAndMeasuredAtBetweenOrderByMeasuredAtDesc(
                        orgId, ComplianceArea.IK_MAT, LocalDateTime.MIN, to);
    }

    @Test
    @DisplayName("fetchMeasurements - throws when module is null")
    void fetchMeasurements_nullModule_throws() {
        assertThatThrownBy(() ->
                service.fetchMeasurements(null, null, null, principal))
                .isInstanceOf(NullPointerException.class);
    }
}
