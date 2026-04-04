package edu.ntnu.idatt2105.backend.report.service;


import edu.ntnu.idatt2105.backend.common.mapper.TaskMapper;
import edu.ntnu.idatt2105.backend.common.model.ChecklistModel;
import edu.ntnu.idatt2105.backend.common.model.TaskTemplate;
import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.common.repository.ChecklistRepository;
import edu.ntnu.idatt2105.backend.common.repository.TasksRepository;
import edu.ntnu.idatt2105.backend.common.repository.TemperatureMeasurementRepository;
import edu.ntnu.idatt2105.backend.exception.ResourceNotFoundException;
import edu.ntnu.idatt2105.backend.report.dto.shared.ChecklistRecord;
import edu.ntnu.idatt2105.backend.report.dto.shared.ChecklistSection;
import edu.ntnu.idatt2105.backend.report.dto.shared.ComplianceStats;
import edu.ntnu.idatt2105.backend.report.dto.InspectionReport;
import edu.ntnu.idatt2105.backend.report.dto.InternalSummary;
import edu.ntnu.idatt2105.backend.report.dto.shared.OrgSection;
import edu.ntnu.idatt2105.backend.report.dto.shared.ReportPeriod;
import edu.ntnu.idatt2105.backend.report.dto.shared.TemperaturePoint;
import edu.ntnu.idatt2105.backend.report.dto.shared.UnresolvedItemDto;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import edu.ntnu.idatt2105.backend.user.model.enums.RoleEnum;
import edu.ntnu.idatt2105.backend.user.repository.OrganizationRepository;
import edu.ntnu.idatt2105.backend.user.repository.UserRepository;
import java.math.BigDecimal;
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

    List<ChecklistRecord> records = checklists.stream()
        .map(cl -> {
          int total = tasksRepository.countByChecklistAndPeriod(cl.getId(), from, to);
          int completed = tasksRepository.countCompletedByChecklistAndPeriod(cl.getId(), from, to);
          int deviated = tasksRepository.countDeviatedByChecklistAndPeriod(cl.getId(), from, to);

          return ChecklistRecord.builder()
              .name(cl.getName())
              .description(cl.getDescription())
              .frequency(cl.getFrequency().name())
              .complianceArea(cl.getComplianceArea())
              .totalTasks(total)
              .completedTasks(completed)
              .deviatedTasks(deviated)
              .completionRate(total > 0 ? (double) completed / total * 100 : 0.0)
              .build();
        })
        .toList();

    return ChecklistSection.builder()
        .totalChecklists(checklists.size())
        .activeChecklists((int) checklists.stream().filter(ChecklistModel::isActive).count())
        .checklists(records)
        .build();
  }

  private List<TemperaturePoint> buildTemperatureLog(UUID orgId, LocalDateTime from, LocalDateTime to) {
    return tempRepository.findByOrgAndPeriod(orgId, from, to).stream()
        .map(m -> {
          TaskTemplate template = m.getTask().getTaskTemplate();
          BigDecimal min = template.getTargetMin();
          BigDecimal max = template.getTargetMax();
          double value = m.getValueC();

          return TemperaturePoint.builder()
              .measuredAt()
              .taskName(template.getTitle())
              .valueC(value)
              .targetMin(min)
              .targetMax(max)
              .withinRange((min == null || value >= min) && (max == null || value <= max))
              .recordedBy(m.getRecordedBy().getFirstName() + " " + m.getRecordedBy().getLastName())
              .build();
        })
        .toList();
  }

  private List<UnresolvedItemDto> buildDeviations(UUID orgId, LocalDateTime from, LocalDateTime to) {
    return tasksRepository.findDeviatedTaskByOrgIdInPeriod(orgId, from, to).stream()
        .map(t -> new UnresolvedItemDto(t.getTaskTemplate().getTitle(), t.getEndedAt()))
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
    OrganizationModel org = organizationRepository.findById(orgId)
        .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

    return InspectionReport.builder()
        .period(new ReportPeriod(from, to))
        .generatedAt(LocalDateTime.now())
        .organization(buildOrgSection(org))
        .checklists(buildChecklistSection(orgId, from, to))
        .temperatureLog(buildTemperatureLog(orgId, from, to))
        .deviations(buildDeviations(orgId, from, to))
        .foodStats(buildStats(orgId, from, to, ComplianceArea.IK_MAT))
        .alcoholStats(buildStats(orgId, from, to, ComplianceArea.IK_ALKOHOL))
        .build();
  }
}