package edu.ntnu.idatt2105.backend.common.service;

import edu.ntnu.idatt2105.backend.common.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.common.dto.temperaturezone.CreateTemperatureZoneRequest;
import edu.ntnu.idatt2105.backend.common.dto.temperaturezone.TemperatureZoneResponse;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import java.util.List;

public interface TemperatureZoneService {
	TemperatureZoneResponse createZone(CreateTemperatureZoneRequest request, JwtAuthenticatedPrincipal principal);

	TemperatureZoneResponse updateZone(Long zoneId, CreateTemperatureZoneRequest request, JwtAuthenticatedPrincipal principal);

	List<TemperatureZoneResponse> getAllZones(IcModule module, JwtAuthenticatedPrincipal principal);

	void deleteZone(Long zoneId, JwtAuthenticatedPrincipal principal);
}
