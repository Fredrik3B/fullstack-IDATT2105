package edu.ntnu.idatt2105.backend.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.ntnu.idatt2105.backend.common.model.ChecklistModuleState;
import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.common.repository.ChecklistModuleStateRepository;
import edu.ntnu.idatt2105.backend.common.service.impl.ChecklistCacheStateServiceImpl;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChecklistCacheStateServiceImplTest {

  @Mock private ChecklistModuleStateRepository checklistModuleStateRepository;

  @InjectMocks private ChecklistCacheStateServiceImpl checklistCacheStateService;

  @Test
  void getLastModified_bootstrapsStateWhenModuleHasNoExistingCacheRow() {
    UUID orgId = UUID.randomUUID();

    when(checklistModuleStateRepository.findByOrganizationIdAndComplianceArea(orgId, ComplianceArea.IK_MAT))
        .thenReturn(Optional.empty());
    when(checklistModuleStateRepository.save(any(ChecklistModuleState.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var lastModified = checklistCacheStateService.getLastModified(orgId, ComplianceArea.IK_MAT);

    assertThat(lastModified).isNotNull();

    ArgumentCaptor<ChecklistModuleState> captor = ArgumentCaptor.forClass(ChecklistModuleState.class);
    verify(checklistModuleStateRepository).save(captor.capture());
    assertThat(captor.getValue().getOrganizationId()).isEqualTo(orgId);
    assertThat(captor.getValue().getComplianceArea()).isEqualTo(ComplianceArea.IK_MAT);
    assertThat(captor.getValue().getModifiedAt()).isNotNull();
  }

  @Test
  void touch_advancesTimestampBeyondPreviousHttpSecondBoundary() {
    UUID orgId = UUID.randomUUID();
    LocalDateTime previousModifiedAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    ChecklistModuleState existing = new ChecklistModuleState();
    existing.setOrganizationId(orgId);
    existing.setComplianceArea(ComplianceArea.IK_MAT);
    existing.setModifiedAt(previousModifiedAt);

    when(checklistModuleStateRepository.findByOrganizationIdAndComplianceArea(orgId, ComplianceArea.IK_MAT))
        .thenReturn(Optional.of(existing));
    when(checklistModuleStateRepository.save(any(ChecklistModuleState.class))).thenAnswer(invocation -> invocation.getArgument(0));

    checklistCacheStateService.touch(orgId, ComplianceArea.IK_MAT);

    ArgumentCaptor<ChecklistModuleState> captor = ArgumentCaptor.forClass(ChecklistModuleState.class);
    verify(checklistModuleStateRepository).save(captor.capture());
    assertThat(captor.getValue().getModifiedAt())
        .isAfterOrEqualTo(previousModifiedAt.plusSeconds(1));
  }
}
