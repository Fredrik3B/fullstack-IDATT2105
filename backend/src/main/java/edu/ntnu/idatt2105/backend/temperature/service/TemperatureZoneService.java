package edu.ntnu.idatt2105.backend.temperature.service;

import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import edu.ntnu.idatt2105.backend.checklist.service.ChecklistCacheStateService;
import edu.ntnu.idatt2105.backend.temperature.dto.CreateTemperatureZoneRequest;
import edu.ntnu.idatt2105.backend.temperature.dto.TemperatureZoneResponse;
import edu.ntnu.idatt2105.backend.task.model.TaskTemplate;
import edu.ntnu.idatt2105.backend.temperature.mapper.TemperatureMapper;
import edu.ntnu.idatt2105.backend.temperature.model.TemperatureZoneModel;
import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.task.repository.TaskTemplateRepository;
import edu.ntnu.idatt2105.backend.temperature.repository.TemperatureZoneRepository;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TemperatureZoneService {

	private final TemperatureZoneRepository temperatureZoneRepository;
	private final TaskTemplateRepository taskTemplateRepository;
	private final ChecklistCacheStateService checklistCacheStateService;
	private final TemperatureMapper temperatureMapper;


	public TemperatureZoneResponse createZone(CreateTemperatureZoneRequest request, JwtAuthenticatedPrincipal principal) {
		if (request.targetMin().compareTo(request.targetMax()) > 0) {
			throw new IllegalArgumentException("targetMin cannot be greater than targetMax");
		}

		UUID orgId = principal.requireOrganizationId();
		ComplianceArea area = request.module().toComplianceArea();

		TemperatureZoneModel zone = TemperatureZoneModel.builder()
				.name(request.name().trim())
				.zoneType(request.zoneType())
				.complianceArea(area)
				.targetMin(request.targetMin())
				.targetMax(request.targetMax())
				.organizationId(orgId)
				.build();

		TemperatureZoneModel saved = temperatureZoneRepository.save(zone);
		checklistCacheStateService.touch(orgId, area);
		return temperatureMapper.toZoneResponse(saved);
	}


	public TemperatureZoneResponse updateZone(Long zoneId, CreateTemperatureZoneRequest request, JwtAuthenticatedPrincipal principal) {
		UUID orgId = principal.requireOrganizationId();
		TemperatureZoneModel zone = requireOwnZone(zoneId, orgId);
		ComplianceArea previousArea = zone.getComplianceArea();
		ComplianceArea newArea = request.module().toComplianceArea();

		zone.setName(request.name().trim());
		zone.setZoneType(request.zoneType());
		zone.setComplianceArea(newArea);
		zone.setTargetMin(request.targetMin());
		zone.setTargetMax(request.targetMax());

		TemperatureZoneModel saved = temperatureZoneRepository.save(zone);

		List<TaskTemplate> linked = taskTemplateRepository.findAllByTemperatureZone_Id(zoneId);
		for (TaskTemplate template : linked) {
			template.setTargetMin(saved.getTargetMin());
			template.setTargetMax(saved.getTargetMax());
		}
		if (!linked.isEmpty()) {
			taskTemplateRepository.saveAll(linked);
		}

		checklistCacheStateService.touch(orgId, previousArea);
		if (newArea != previousArea) {
			checklistCacheStateService.touch(orgId, newArea);
		}
		return temperatureMapper.toZoneResponse(saved);
	}

	public List<TemperatureZoneResponse> getAllZones(IcModule module, JwtAuthenticatedPrincipal principal) {
		UUID orgId = principal.requireOrganizationId();
		return temperatureZoneRepository
				.findAllByOrganizationIdAndComplianceAreaOrderByZoneTypeAscNameAsc(orgId, module.toComplianceArea())
				.stream()
				.map(temperatureMapper::toZoneResponse)
				.toList();
	}

	public void deleteZone(Long zoneId, JwtAuthenticatedPrincipal principal) {
		UUID orgId = principal.requireOrganizationId();
		TemperatureZoneModel zone = requireOwnZone(zoneId, orgId);
		if (!taskTemplateRepository.findAllByTemperatureZone_Id(zoneId).isEmpty()) {
			throw new IllegalArgumentException("Temperature zone is still used by one or more tasks.");
		}
		temperatureZoneRepository.delete(zone);
		checklistCacheStateService.touch(orgId, zone.getComplianceArea());
	}

	private TemperatureZoneModel requireOwnZone(Long zoneId, UUID orgId) {
		return temperatureZoneRepository.findByIdAndOrganizationId(zoneId, orgId)
				.orElseThrow(() -> new IllegalArgumentException("Temperature zone not found."));
	}
}
