package edu.ntnu.idatt2105.backend.common.service.impl;

import edu.ntnu.idatt2105.backend.common.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.common.dto.temperaturezone.CreateTemperatureZoneRequest;
import edu.ntnu.idatt2105.backend.common.dto.temperaturezone.TemperatureZoneResponse;
import edu.ntnu.idatt2105.backend.common.model.TaskTemplate;
import edu.ntnu.idatt2105.backend.common.model.TemperatureZoneModel;
import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.common.repository.TaskTemplateRepository;
import edu.ntnu.idatt2105.backend.common.repository.TemperatureZoneRepository;
import edu.ntnu.idatt2105.backend.common.service.ChecklistCacheStateService;
import edu.ntnu.idatt2105.backend.common.service.TemperatureZoneService;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TemperatureZoneServiceImpl implements TemperatureZoneService {

	private final TemperatureZoneRepository temperatureZoneRepository;
	private final TaskTemplateRepository taskTemplateRepository;
	private final ChecklistCacheStateService checklistCacheStateService;

	public TemperatureZoneServiceImpl(
		TemperatureZoneRepository temperatureZoneRepository,
		TaskTemplateRepository taskTemplateRepository,
		ChecklistCacheStateService checklistCacheStateService
	) {
		this.temperatureZoneRepository = temperatureZoneRepository;
		this.taskTemplateRepository = taskTemplateRepository;
		this.checklistCacheStateService = checklistCacheStateService;
	}

	@Override
	public TemperatureZoneResponse createZone(CreateTemperatureZoneRequest request, JwtAuthenticatedPrincipal principal) {
		JwtAuthenticatedPrincipal safePrincipal = requirePrincipal(principal);
		validateRequest(request);

		TemperatureZoneModel zone = new TemperatureZoneModel();
		zone.setName(request.name().trim());
		zone.setZoneType(request.zoneType());
		zone.setComplianceArea(requireModule(request.module()).toComplianceArea());
		zone.setTargetMin(request.targetMin());
		zone.setTargetMax(request.targetMax());
		zone.setOrganizationId(safePrincipal.getOrganizationId());

		TemperatureZoneResponse response = toResponse(temperatureZoneRepository.save(zone));
		checklistCacheStateService.touch(safePrincipal.getOrganizationId(), zone.getComplianceArea());
		return response;
	}

	@Override
	public TemperatureZoneResponse updateZone(Long zoneId, CreateTemperatureZoneRequest request, JwtAuthenticatedPrincipal principal) {
		JwtAuthenticatedPrincipal safePrincipal = requirePrincipal(principal);
		validateRequest(request);
		TemperatureZoneModel zone = getZone(zoneId, safePrincipal.getOrganizationId());
		ComplianceArea previousComplianceArea = zone.getComplianceArea();

		zone.setName(request.name().trim());
		zone.setZoneType(request.zoneType());
		zone.setComplianceArea(requireModule(request.module()).toComplianceArea());
		zone.setTargetMin(request.targetMin());
		zone.setTargetMax(request.targetMax());

		TemperatureZoneModel savedZone = temperatureZoneRepository.save(zone);
		List<TaskTemplate> linkedTemplates = taskTemplateRepository.findAllByTemperatureZone_Id(zoneId);
		for (TaskTemplate template : linkedTemplates) {
			template.setUnit("C");
			template.setTargetMin(savedZone.getTargetMin());
			template.setTargetMax(savedZone.getTargetMax());
		}
		if (!linkedTemplates.isEmpty()) {
			taskTemplateRepository.saveAll(linkedTemplates);
		}
		checklistCacheStateService.touch(safePrincipal.getOrganizationId(), previousComplianceArea);
		if (savedZone.getComplianceArea() != previousComplianceArea) {
			checklistCacheStateService.touch(safePrincipal.getOrganizationId(), savedZone.getComplianceArea());
		}

		return toResponse(savedZone);
	}

	@Override
	public List<TemperatureZoneResponse> getAllZones(IcModule module, JwtAuthenticatedPrincipal principal) {
		JwtAuthenticatedPrincipal safePrincipal = requirePrincipal(principal);
		ComplianceArea complianceArea = requireModule(module).toComplianceArea();
		return temperatureZoneRepository
			.findAllByOrganizationIdAndComplianceAreaOrderByZoneTypeAscNameAsc(safePrincipal.getOrganizationId(), complianceArea)
			.stream()
			.map(this::toResponse)
			.toList();
	}

	@Override
	public void deleteZone(Long zoneId, JwtAuthenticatedPrincipal principal) {
		JwtAuthenticatedPrincipal safePrincipal = requirePrincipal(principal);
		TemperatureZoneModel zone = getZone(zoneId, safePrincipal.getOrganizationId());
		if (!taskTemplateRepository.findAllByTemperatureZone_Id(zoneId).isEmpty()) {
			throw new IllegalArgumentException("Temperature zone is still used by one or more tasks.");
		}
		temperatureZoneRepository.delete(zone);
		checklistCacheStateService.touch(safePrincipal.getOrganizationId(), zone.getComplianceArea());
	}

	private TemperatureZoneModel getZone(Long zoneId, UUID organizationId) {
		return temperatureZoneRepository.findByIdAndOrganizationId(zoneId, organizationId)
			.orElseThrow(() -> new IllegalArgumentException("Temperature zone not found."));
	}

	private TemperatureZoneResponse toResponse(TemperatureZoneModel zone) {
		return new TemperatureZoneResponse(
			zone.getId(),
			toModule(zone.getComplianceArea()),
			zone.getName(),
			zone.getZoneType(),
			zone.getTargetMin(),
			zone.getTargetMax()
		);
	}

	private JwtAuthenticatedPrincipal requirePrincipal(JwtAuthenticatedPrincipal principal) {
		if (principal == null) throw new IllegalArgumentException("Authentication required.");
		if (principal.getOrganizationId() == null) throw new IllegalArgumentException("Organization required.");
		return principal;
	}

	private IcModule requireModule(IcModule module) {
		if (module == null) throw new IllegalArgumentException("module is required.");
		return module;
	}

	private void validateRequest(CreateTemperatureZoneRequest request) {
		if (request == null) throw new IllegalArgumentException("Temperature zone request cannot be null.");
		if (request.module() == null) throw new IllegalArgumentException("module is required.");
		if (request.zoneType() == null) throw new IllegalArgumentException("zoneType is required.");
		if (request.name() == null || request.name().trim().isEmpty()) {
			throw new IllegalArgumentException("Temperature zone name is required.");
		}
		if (request.targetMin().compareTo(request.targetMax()) > 0) {
			throw new IllegalArgumentException("targetMin cannot be greater than targetMax.");
		}
	}

	private IcModule toModule(ComplianceArea complianceArea) {
		if (complianceArea == ComplianceArea.IK_ALKOHOL) {
			return IcModule.IC_ALCOHOL;
		}
		return IcModule.IC_FOOD;
	}
}
