package edu.ntnu.idatt2105.backend.common.service;

import edu.ntnu.idatt2105.backend.common.dto.icchecklist.CreateTemperatureMeasurementRequest;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.TemperatureMeasurementResponse;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface TemperatureMeasurementService {
	TemperatureMeasurementResponse createMeasurement(
		CreateTemperatureMeasurementRequest request,
		JwtAuthenticatedPrincipal principal
	);

	List<TemperatureMeasurementResponse> fetchMeasurements(
		IcModule module,
		LocalDateTime from,
		LocalDateTime to,
		JwtAuthenticatedPrincipal principal
	);
}

