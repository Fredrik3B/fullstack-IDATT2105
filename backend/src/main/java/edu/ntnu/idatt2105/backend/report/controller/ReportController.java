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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reports")
@RestController
@RequestMapping("/reports")
@AllArgsConstructor
public class ReportController {

  private final ReportService reportService;

  @Operation(summary = "Internal compliance summary")
//  @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
  @GetMapping("/summary")
  public ResponseEntity<InternalSummary> getSummary(
      @RequestParam(required = false) LocalDateTime from,
      @RequestParam(required = false) LocalDateTime to,
      Authentication auth
  ) {

    UUID orgId = ((JwtAuthenticatedPrincipal) auth.getPrincipal()).requireOrganizationId();
    if (from == null) from = LocalDateTime.now().minusMonths(1);
    if (to == null) to = LocalDateTime.now();
    return ResponseEntity.ok(reportService.generateSummary(orgId, from, to));
  }

  @Operation(summary = "Full inspection report data")
//  @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
  @GetMapping("/full-report")
  public ResponseEntity<InspectionReport> getInspection(
      @RequestParam(required = false) LocalDateTime from,
      @RequestParam(required = false) LocalDateTime to,
      Authentication auth
  ) {
    UUID orgId = ((JwtAuthenticatedPrincipal) auth.getPrincipal()).requireOrganizationId();
    if (from == null) from = LocalDateTime.now().minusMonths(1);
    if (to == null) to = LocalDateTime.now();
    return ResponseEntity.ok(reportService.generateInspection(orgId, from, to));
  }

  @Operation(summary = "Download inspection report as PDF")
//  @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
  @GetMapping("/inspection/pdf")
  public ResponseEntity getInspectionPdf(
      @RequestParam(required = false) LocalDateTime from,
      @RequestParam(required = false) LocalDateTime to,
      Authentication auth) {
    UUID orgId = ((JwtAuthenticatedPrincipal) auth.getPrincipal()).requireOrganizationId();
    if (from == null) from = LocalDateTime.now().minusMonths(1);
    if (to == null) to = LocalDateTime.now();

    InspectionReport report = reportService.generateInspection(orgId, from, to);

    // export to pdf

    return ResponseEntity.ok(report);
  }

  @PostMapping("/deviations")
  public ResponseEntity<DeviationCreatedResponse> createDeviation(
      @RequestBody @Valid DeviationReport request, Authentication auth
  ) {
    JwtAuthenticatedPrincipal principal =
        (JwtAuthenticatedPrincipal) auth.getPrincipal();
    DeviationCreatedResponse response = reportService.createDeviationReport(
        request, principal.getUserId(), principal.getOrganizationId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

}
