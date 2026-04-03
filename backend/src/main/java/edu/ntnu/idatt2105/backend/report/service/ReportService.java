package edu.ntnu.idatt2105.backend.report.service;


import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.common.repository.ChecklistRepository;
import edu.ntnu.idatt2105.backend.common.repository.TasksRepository;
import edu.ntnu.idatt2105.backend.common.repository.TemperatureMeasurementRepository;
import edu.ntnu.idatt2105.backend.exception.ResourceNotFoundException;
import edu.ntnu.idatt2105.backend.report.dto.ComplianceStats;
import edu.ntnu.idatt2105.backend.report.dto.InspectionReport;
import edu.ntnu.idatt2105.backend.report.dto.InternalSummary;
import edu.ntnu.idatt2105.backend.report.dto.ReportPeriod;
import edu.ntnu.idatt2105.backend.report.dto.UnresolvedItemDto;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.repository.OrganizationRepository;
import edu.ntnu.idatt2105.backend.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ReportService {

  private final TasksRepository tasksRepository;
  private final TemperatureMeasurementRepository tempRepository;
  private final ChecklistRepository checklistRepository;
  private final UserRepository userRepository;
  private final OrganizationRepository organizationRepository;

  private ComplianceStats buildStats(UUID orgId, LocalDate from, LocalDate to, ComplianceArea area) {
    int total = tasksRepository.contTaskInPeriod(orgId, from, to, area);
    int completed = tasksRepository.countCompletedInPeriod(orgId, from, to, area);
    int flagged = tasksRepository.countFlaggedInPeriod(orgId, from, to, area);

    return ComplianceStats.builder()
        .totalTasks(total)
        .completedTasks(completed)
        .flaggedTasks(flagged)
        .completionRate((double) completed / total * 100)
        .build();
  }

  private List<UnresolvedItemDto> getUnresolvedItems(UUID orgId, LocalDate from, LocalDate to) {
    tasksRepository.findDeviatedTaskByOrgIdInPeriod(orgId, from, to);
  }


  public InternalSummary generateSummary(UUID orgId, LocalDate from, LocalDate to) {
    return InternalSummary.builder()
        .period(new ReportPeriod(from, to))
        .matStats(buildStats(orgId, from, to, ComplianceArea.IK_MAT))
        .alkoholStats(buildStats(orgId, from, to, ComplianceArea.IK_ALKOHOL))
        .unresolvedItems(getUnresolvedItems(orgId, from, to))
        .build();
  }

  public InspectionReport generateInspection(UUID orgId, LocalDate from, LocalDate to) {
    OrganizationModel org = orgRepository.findById(orgId)
        .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

    return InspectionReport.builder()
        .period(new ReportPeriod(from, to))
        .organization(buildOrgSection(org))
        .checklistEvidence(buildChecklistEvidence(orgId, from, to))
        .temperatureLog(buildFullTemperatureLog(orgId, from, to))
        .deviationRegister(buildDeviationRegister(orgId, from, to))
        .generatedAt(LocalDateTime.now())
        .build();
  }
}