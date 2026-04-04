package edu.ntnu.idatt2105.backend.report.service;


import edu.ntnu.idatt2105.backend.common.mapper.TaskMapper;
import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.common.repository.ChecklistRepository;
import edu.ntnu.idatt2105.backend.common.repository.TasksRepository;
import edu.ntnu.idatt2105.backend.common.repository.TemperatureMeasurementRepository;
import edu.ntnu.idatt2105.backend.report.dto.shared.ComplianceStats;
import edu.ntnu.idatt2105.backend.report.dto.InspectionReport;
import edu.ntnu.idatt2105.backend.report.dto.InternalSummary;
import edu.ntnu.idatt2105.backend.report.dto.shared.ReportPeriod;
import edu.ntnu.idatt2105.backend.report.dto.shared.UnresolvedItemDto;
import edu.ntnu.idatt2105.backend.user.repository.OrganizationRepository;
import edu.ntnu.idatt2105.backend.user.repository.UserRepository;
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
  private final TaskMapper taskMapper;

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


  public InternalSummary generateSummary(UUID orgId, LocalDateTime from, LocalDateTime to) {
    return InternalSummary.builder()
        .period(new ReportPeriod(from, to))
        .matStats(buildStats(orgId, from, to, ComplianceArea.IK_MAT))
        .alkoholStats(buildStats(orgId, from, to, ComplianceArea.IK_ALKOHOL))
        .unresolvedItems(getUnresolvedItems(orgId, from, to))
        .build();
  }

  public InspectionReport generateInspection(UUID orgId, LocalDateTime from, LocalDateTime to) {
//    OrganizationModel org = orgRepository.findById(orgId)
//        .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
//
//    return InspectionReport.builder()
//        .period(new ReportPeriod(from, to))
//        .organization(buildOrgSection(org))
//        .checklistEvidence(buildChecklistEvidence(orgId, from, to))
//        .temperatureLog(buildFullTemperatureLog(orgId, from, to))
//        .deviationRegister(buildDeviationRegister(orgId, from, to))
//        .generatedAt(LocalDateTime.now())
//        .build();
    throw new RuntimeException("Not implemented");
  }
}