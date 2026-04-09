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

/**
 * Service for managing temperature zones within an organisation.
 *
 * <p>When a zone's target range is updated, the new bounds are automatically propagated to
 * all linked {@link edu.ntnu.idatt2105.backend.task.model.TaskTemplate} records so that
 * existing checklist tasks stay in sync. Every mutating operation invalidates the checklist
 * cache state for the affected compliance area.
 */
@Service
@AllArgsConstructor
public class TemperatureZoneService {

	private final TemperatureZoneRepository temperatureZoneRepository;
	private final TaskTemplateRepository taskTemplateRepository;
	private final ChecklistCacheStateService checklistCacheStateService;
	private final TemperatureMapper temperatureMapper;


	/**
	 * Creates a new temperature zone for the caller's organisation.
	 *
	 * @param request   zone creation data including name, type, module, and target range
	 * @param principal the authenticated principal used to resolve the organisation
	 * @return the persisted zone as a response DTO
	 * @throws IllegalArgumentException if {@code targetMin} is greater than {@code targetMax}
	 */
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


	/**
	 * Updates an existing temperature zone and propagates the new target range to all
	 * linked task templates.
	 *
	 * @param zoneId    the ID of the zone to update
	 * @param request   updated zone data
	 * @param principal the authenticated principal; must own the zone
	 * @return the updated zone as a response DTO
	 * @throws IllegalArgumentException if the zone is not found for the caller's organisation
	 */
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

	/**
	 * Returns all temperature zones for the caller's organisation in the given module,
	 * ordered by zone type then name.
	 *
	 * @param module    the IC module whose compliance area filters the zones
	 * @param principal the authenticated principal used to resolve the organisation
	 * @return list of zone response DTOs, sorted by zone type then name
	 */
	public List<TemperatureZoneResponse> getAllZones(IcModule module, JwtAuthenticatedPrincipal principal) {
		UUID orgId = principal.requireOrganizationId();
		return temperatureZoneRepository
				.findAllByOrganizationIdAndComplianceAreaOrderByZoneTypeAscNameAsc(orgId, module.toComplianceArea())
				.stream()
				.map(temperatureMapper::toZoneResponse)
				.toList();
	}

	/**
	 * Deletes a temperature zone, provided no task template still references it.
	 *
	 * @param zoneId    the ID of the zone to delete
	 * @param principal the authenticated principal; must own the zone
	 * @throws IllegalArgumentException if the zone is not found or is still referenced by tasks
	 */
	public void deleteZone(Long zoneId, JwtAuthenticatedPrincipal principal) {
		UUID orgId = principal.requireOrganizationId();
		TemperatureZoneModel zone = requireOwnZone(zoneId, orgId);
		if (!taskTemplateRepository.findAllByTemperatureZone_Id(zoneId).isEmpty()) {
			throw new IllegalArgumentException("Temperature zone is still used by one or more tasks.");
		}
		temperatureZoneRepository.delete(zone);
		checklistCacheStateService.touch(orgId, zone.getComplianceArea());
	}

	/**
	 * Loads a zone and asserts it belongs to the given organisation.
	 *
	 * @param zoneId the zone ID to look up
	 * @param orgId  the organisation that must own the zone
	 * @return the zone entity
	 * @throws IllegalArgumentException if the zone does not exist or belongs to another organisation
	 */
	private TemperatureZoneModel requireOwnZone(Long zoneId, UUID orgId) {
		return temperatureZoneRepository.findByIdAndOrganizationId(zoneId, orgId)
				.orElseThrow(() -> new IllegalArgumentException("Temperature zone not found."));
	}
}
