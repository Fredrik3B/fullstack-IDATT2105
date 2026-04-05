package edu.ntnu.idatt2105.backend.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.ntnu.idatt2105.backend.common.mapper.TaskMapper;
import edu.ntnu.idatt2105.backend.common.model.TasksModel;
import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.common.repository.ChecklistRepository;
import edu.ntnu.idatt2105.backend.common.repository.TasksRepository;
import edu.ntnu.idatt2105.backend.common.repository.TemperatureMeasurementRepository;
import edu.ntnu.idatt2105.backend.report.dto.InternalSummary;
import edu.ntnu.idatt2105.backend.report.dto.shared.ComplianceStats;
import edu.ntnu.idatt2105.backend.report.dto.shared.UnresolvedItemDto;
import edu.ntnu.idatt2105.backend.user.repository.OrganizationRepository;
import edu.ntnu.idatt2105.backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
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
}