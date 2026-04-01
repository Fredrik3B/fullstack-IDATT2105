package edu.ntnu.idatt2105.backend.common.controller;

import edu.ntnu.idatt2105.backend.common.dto.icchecklist.CreateTemperatureMeasurementRequest;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.TemperatureMeasurementResponse;
import edu.ntnu.idatt2105.backend.common.service.TemperatureMeasurementService;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(
	name = "Temperature Measurements",
	description = "Create and fetch temperature measurements"
)
@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/temperature-measurements")
public class TemperatureMeasurementController {

	private static final Logger LOGGER = LoggerFactory.getLogger(TemperatureMeasurementController.class);

	private final TemperatureMeasurementService temperatureMeasurementService;

	public TemperatureMeasurementController(TemperatureMeasurementService temperatureMeasurementService) {
		this.temperatureMeasurementService = temperatureMeasurementService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a temperature measurement")
	@ApiResponse(responseCode = "201", description = "Temperature measurement created")
	public TemperatureMeasurementResponse createMeasurement(
		@Valid @RequestBody CreateTemperatureMeasurementRequest request,
		Authentication auth
	) {
		JwtAuthenticatedPrincipal principal = (JwtAuthenticatedPrincipal) auth.getPrincipal();
		LOGGER.info(
			"IC create temperature measurement: orgId={} userId={} module={} checklistId={} taskId={} valueC={} measuredAt={} periodKey={}",
			principal.getOrganizationId(),
			principal.getUserId(),
			request.module(),
			request.checklistId(),
			request.taskId(),
			request.valueC(),
			request.measuredAt(),
			request.periodKey()
		);
		TemperatureMeasurementResponse created = temperatureMeasurementService.createMeasurement(request, principal);
		LOGGER.info("IC create temperature measurement: created measurementId={}", created.id());
		return created;
	}

	@GetMapping
	@Operation(summary = "Fetch temperature measurements")
	@ApiResponse(responseCode = "200", description = "Temperature measurements returned")
	public List<TemperatureMeasurementResponse> fetchMeasurements(
		@RequestParam IcModule module,
		@RequestParam(required = false) Instant from,
		@RequestParam(required = false) Instant to,
		Authentication auth
	) {
		JwtAuthenticatedPrincipal principal = (JwtAuthenticatedPrincipal) auth.getPrincipal();
		LOGGER.info(
			"IC fetch temperature measurements: orgId={} userId={} module={} from={} to={}",
			principal.getOrganizationId(),
			principal.getUserId(),
			module,
			from,
			to
		);
		List<TemperatureMeasurementResponse> rows = temperatureMeasurementService.fetchMeasurements(module, from, to, principal);
		LOGGER.info("IC fetch temperature measurements: returned {} rows", rows.size());
		return rows;
	}
}
