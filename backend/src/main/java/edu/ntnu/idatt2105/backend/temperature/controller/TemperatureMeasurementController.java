package edu.ntnu.idatt2105.backend.temperature.controller;

import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import edu.ntnu.idatt2105.backend.temperature.dto.CreateTemperatureMeasurementRequest;
import edu.ntnu.idatt2105.backend.temperature.dto.TemperatureMeasurementResponse;
import edu.ntnu.idatt2105.backend.temperature.service.TemperatureMeasurementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for recording and retrieving temperature measurements.
 *
 * <p>Any authenticated user may log a measurement ({@code POST}) or query the
 * measurement history ({@code GET}). Access is scoped to the caller's organisation.
 */
@Tag(
    name = "Temperature Measurements",
    description = "Create and fetch temperature measurements"
)
@RestController
@AllArgsConstructor
@RequestMapping("/temperature-measurements")
public class TemperatureMeasurementController {

  private final TemperatureMeasurementService temperatureMeasurementService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a temperature measurement")
  @ApiResponse(responseCode = "201", description = "Temperature measurement created")
  public TemperatureMeasurementResponse createMeasurement(
      @Valid @RequestBody CreateTemperatureMeasurementRequest request,
      Authentication auth
  ) {
    JwtAuthenticatedPrincipal principal = JwtAuthenticatedPrincipal.from(auth);
    return temperatureMeasurementService.createMeasurement(request, principal);
  }

  @GetMapping
  @Operation(summary = "Fetch temperature measurements")
  @ApiResponse(responseCode = "200", description = "Temperature measurements returned")
  public List<TemperatureMeasurementResponse> fetchMeasurements(
      @RequestParam IcModule module,
      @RequestParam(required = false) LocalDateTime from,
      @RequestParam(required = false) LocalDateTime to,
      Authentication auth
  ) {
    JwtAuthenticatedPrincipal principal = JwtAuthenticatedPrincipal.from(auth);
    return temperatureMeasurementService.fetchMeasurements(module, from, to, principal);
  }
}
