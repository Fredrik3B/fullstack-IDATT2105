package edu.ntnu.idatt2105.backend.report.controller;

import edu.ntnu.idatt2105.backend.report.dto.DeviationCreatedResponse;
import edu.ntnu.idatt2105.backend.report.dto.DeviationReport;
import edu.ntnu.idatt2105.backend.report.dto.InspectionReport;
import edu.ntnu.idatt2105.backend.report.dto.InternalSummary;
import edu.ntnu.idatt2105.backend.report.service.ReportService;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for generating compliance reports and filing deviation reports.
 *
 * <p>Provides three report endpoints (internal summary, full inspection, PDF export)
 * and a deviation-report creation endpoint. All endpoints are scoped to the caller's
 * organisation. The {@code from}/{@code to} query parameters default to the last 30 days
 * when omitted.
 */
@Tag(name = "Reports")
@RestController
@AllArgsConstructor
@RequestMapping("/reports")
public class ReportController {

  private final ReportService reportService;

  @Operation(summary = "Internal compliance summary")
  @GetMapping("/summary")
  public ResponseEntity<InternalSummary> getSummary(
      @RequestParam(required = false) LocalDateTime from,
      @RequestParam(required = false) LocalDateTime to,
      Authentication auth
  ) {

    UUID orgId = JwtAuthenticatedPrincipal.from(auth).requireOrganizationId();
    if (from == null) from = LocalDateTime.now().minusMonths(1);
    if (to == null) to = LocalDateTime.now();
    return ResponseEntity.ok(reportService.generateSummary(orgId, from, to));
  }

  @Operation(summary = "Full inspection report data")
  @GetMapping("/full-report")
  public ResponseEntity<InspectionReport> getInspection(
      @RequestParam(required = false) LocalDateTime from,
      @RequestParam(required = false) LocalDateTime to,
      Authentication auth
  ) {
    UUID orgId = JwtAuthenticatedPrincipal.from(auth).requireOrganizationId();
    if (from == null) from = LocalDateTime.now().minusMonths(1);
    if (to == null) to = LocalDateTime.now();
    return ResponseEntity.ok(reportService.generateInspection(orgId, from, to));
  }

  @Operation(summary = "Create a deviation report")
  @PostMapping("/deviations")
  public ResponseEntity<DeviationCreatedResponse> createDeviation(
      @RequestBody @Valid DeviationReport request, Authentication auth
  ) {
    JwtAuthenticatedPrincipal principal =
        JwtAuthenticatedPrincipal.from(auth);
    DeviationCreatedResponse response = reportService.createDeviationReport(
        request, principal.getUserId(), principal.getOrganizationId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

}
