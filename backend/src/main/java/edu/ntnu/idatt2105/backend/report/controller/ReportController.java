package edu.ntnu.idatt2105.backend.report.controller;

import edu.ntnu.idatt2105.backend.report.dto.InspectionReport;
import edu.ntnu.idatt2105.backend.report.dto.InternalSummary;
import edu.ntnu.idatt2105.backend.report.service.ReportService;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
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
  @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
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
}
