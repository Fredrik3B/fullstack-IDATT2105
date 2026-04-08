package edu.ntnu.idatt2105.backend.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.checklist.service.ChecklistCacheStateService;
import edu.ntnu.idatt2105.backend.temperature.dto.CreateTemperatureZoneRequest;
import edu.ntnu.idatt2105.backend.temperature.dto.TemperatureZoneResponse;
import edu.ntnu.idatt2105.backend.task.model.TaskTemplate;
import edu.ntnu.idatt2105.backend.temperature.model.TemperatureZoneModel;
import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.temperature.model.enums.TemperatureZone;
import edu.ntnu.idatt2105.backend.task.repository.TaskTemplateRepository;
import edu.ntnu.idatt2105.backend.temperature.repository.TemperatureZoneRepository;
import edu.ntnu.idatt2105.backend.temperature.service.TemperatureZoneService;
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
class TemperatureZoneServiceImplTest {

  @Mock private TemperatureZoneRepository temperatureZoneRepository;
  @Mock private TaskTemplateRepository taskTemplateRepository;
  @Mock private ChecklistCacheStateService checklistCacheStateService;

  @InjectMocks private TemperatureZoneService temperatureZoneService;

  private UUID orgId;
  private JwtAuthenticatedPrincipal principal;

  @BeforeEach
  void setUp() {
    orgId = UUID.randomUUID();
    principal = new JwtAuthenticatedPrincipal(UUID.randomUUID(), orgId, "tester", Collections.emptyList());
  }

  @Test
  void createZone_persistsZoneWithModuleMappedComplianceArea() {
    when(temperatureZoneRepository.save(any(TemperatureZoneModel.class))).thenAnswer(invocation -> {
      TemperatureZoneModel zone = invocation.getArgument(0);
      zone.setId(7L);
      return zone;
    });

    TemperatureZoneResponse response = temperatureZoneService.createZone(
        new CreateTemperatureZoneRequest(
            IcModule.IC_ALCOHOL,
            "Bar cooler",
            TemperatureZone.FRIDGE,
            new BigDecimal("2.0"),
            new BigDecimal("6.0")),
        principal);

    ArgumentCaptor<TemperatureZoneModel> captor = ArgumentCaptor.forClass(TemperatureZoneModel.class);
    verify(temperatureZoneRepository).save(captor.capture());
    TemperatureZoneModel saved = captor.getValue();

    assertThat(saved.getComplianceArea()).isEqualTo(ComplianceArea.IK_ALKOHOL);
    assertThat(saved.getOrganizationId()).isEqualTo(orgId);
    assertThat(response.module()).isEqualTo(IcModule.IC_ALCOHOL);
    assertThat(response.name()).isEqualTo("Bar cooler");
  }

  @Test
  void updateZone_propagatesUpdatedRangeToLinkedTaskTemplates() {
    TemperatureZoneModel zone = new TemperatureZoneModel();
    zone.setId(8L);
    zone.setName("Main fridge");
    zone.setZoneType(TemperatureZone.FRIDGE);
    zone.setComplianceArea(ComplianceArea.IK_MAT);
    zone.setOrganizationId(orgId);

    TaskTemplate template = new TaskTemplate();
    template.setId(100L);
    template.setUnit("C");
    template.setTargetMin(new BigDecimal("1.0"));
    template.setTargetMax(new BigDecimal("4.0"));

    when(temperatureZoneRepository.findByIdAndOrganizationId(8L, orgId)).thenReturn(Optional.of(zone));
    when(temperatureZoneRepository.save(any(TemperatureZoneModel.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(taskTemplateRepository.findAllByTemperatureZone_Id(8L)).thenReturn(List.of(template));

    TemperatureZoneResponse response = temperatureZoneService.updateZone(
        8L,
        new CreateTemperatureZoneRequest(
            IcModule.IC_FOOD,
            "Main fridge",
            TemperatureZone.FRIDGE,
            new BigDecimal("2.0"),
            new BigDecimal("5.0")),
        principal);

    assertThat(template.getTargetMin()).isEqualByComparingTo("2.0");
    assertThat(template.getTargetMax()).isEqualByComparingTo("5.0");
    verify(taskTemplateRepository).saveAll(List.of(template));
    assertThat(response.targetMin()).isEqualByComparingTo("2.0");
  }

  @Test
  void deleteZone_whenZoneStillUsedByTask_rejectsDelete() {
    TemperatureZoneModel zone = new TemperatureZoneModel();
    zone.setId(9L);
    zone.setOrganizationId(orgId);

    when(temperatureZoneRepository.findByIdAndOrganizationId(9L, orgId)).thenReturn(Optional.of(zone));
    when(taskTemplateRepository.findAllByTemperatureZone_Id(9L)).thenReturn(List.of(new TaskTemplate()));

    assertThatThrownBy(() -> temperatureZoneService.deleteZone(9L, principal))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("still used");

    verify(temperatureZoneRepository, never()).delete(any());
  }

  @Test
  void createZone_whenMinGreaterThanMax_rejectsRequest() {
    assertThatThrownBy(() -> temperatureZoneService.createZone(
        new CreateTemperatureZoneRequest(
            IcModule.IC_FOOD,
            "Broken freezer",
            TemperatureZone.FREEZER,
            new BigDecimal("-10.0"),
            new BigDecimal("-18.0")),
        principal))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("targetMin cannot be greater");
  }
}
