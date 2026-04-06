package edu.ntnu.idatt2105.backend.common.controller;

import edu.ntnu.idatt2105.backend.common.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.common.dto.temperaturezone.CreateTemperatureZoneRequest;
import edu.ntnu.idatt2105.backend.common.dto.temperaturezone.TemperatureZoneResponse;
import edu.ntnu.idatt2105.backend.common.service.TemperatureZoneService;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Temperature Zones", description = "Create, fetch, update, and delete reusable temperature zones")
@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/temperature-zones")
public class TemperatureZoneController {

	private final TemperatureZoneService temperatureZoneService;

	public TemperatureZoneController(TemperatureZoneService temperatureZoneService) {
		this.temperatureZoneService = temperatureZoneService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a temperature zone")
	@ApiResponse(responseCode = "201", description = "Temperature zone created")
	public TemperatureZoneResponse createZone(@Valid @RequestBody CreateTemperatureZoneRequest request, Authentication auth) {
		return temperatureZoneService.createZone(request, (JwtAuthenticatedPrincipal) auth.getPrincipal());
	}

	@PutMapping("/{zoneId}")
	@Operation(summary = "Update a temperature zone")
	@ApiResponse(responseCode = "200", description = "Temperature zone updated")
	public TemperatureZoneResponse updateZone(
		@PathVariable Long zoneId,
		@Valid @RequestBody CreateTemperatureZoneRequest request,
		Authentication auth
	) {
		return temperatureZoneService.updateZone(zoneId, request, (JwtAuthenticatedPrincipal) auth.getPrincipal());
	}

	@GetMapping
	@Operation(summary = "Fetch all temperature zones")
	@ApiResponse(responseCode = "200", description = "Temperature zones returned")
	public List<TemperatureZoneResponse> getAllZones(@RequestParam IcModule module, Authentication auth) {
		return temperatureZoneService.getAllZones(module, (JwtAuthenticatedPrincipal) auth.getPrincipal());
	}

	@DeleteMapping("/{zoneId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete a temperature zone")
	@ApiResponse(responseCode = "204", description = "Temperature zone deleted")
	public void deleteZone(@PathVariable Long zoneId, Authentication auth) {
		temperatureZoneService.deleteZone(zoneId, (JwtAuthenticatedPrincipal) auth.getPrincipal());
	}
}
