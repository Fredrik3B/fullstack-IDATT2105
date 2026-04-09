package edu.ntnu.idatt2105.backend.report.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ntnu.idatt2105.backend.report.dto.DeviationCreatedResponse;
import edu.ntnu.idatt2105.backend.report.dto.InspectionReport;
import edu.ntnu.idatt2105.backend.report.dto.InternalSummary;
import edu.ntnu.idatt2105.backend.report.service.ReportService;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@DisplayName("ReportController (/api/reports)")
class ReportControllerTest {

  @Autowired
  private WebApplicationContext context;

  @MockitoBean
  private ReportService reportService;

  private MockMvc mockMvc;

  private final UUID userId = UUID.randomUUID();
  private final UUID orgId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .build();
  }

  private Authentication staffAuth() {
    JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
        userId, orgId, "staff@test.com",
        List.of(new SimpleGrantedAuthority("ROLE_STAFF")));
    return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
  }

  private Authentication adminAuth() {
    JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
        userId, orgId, "admin@test.com",
        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
  }

  // ── GET /api/reports/summary ──────────────────────────────────────────────

  @Test
  @DisplayName("GET /api/reports/summary - authenticated returns 200")
  void getSummary_authenticated_returns200() throws Exception {
    when(reportService.generateSummary(eq(orgId), any(), any()))
        .thenReturn(InternalSummary.builder().build());

    mockMvc.perform(get("/api/reports/summary")
            .with(authentication(adminAuth())))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET /api/reports/summary - staff returns 200")
  void getSummary_asStaff_returns200() throws Exception {
    when(reportService.generateSummary(eq(orgId), any(), any()))
        .thenReturn(InternalSummary.builder().build());

    mockMvc.perform(get("/api/reports/summary")
            .with(authentication(staffAuth())))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET /api/reports/summary - with explicit date range returns 200")
  void getSummary_withDateRange_returns200() throws Exception {
    when(reportService.generateSummary(eq(orgId), any(), any()))
        .thenReturn(InternalSummary.builder().build());

    mockMvc.perform(get("/api/reports/summary")
            .param("from", "2025-01-01T00:00")
            .param("to", "2025-01-31T23:59")
            .with(authentication(adminAuth())))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET /api/reports/summary - unauthenticated returns 401")
  void getSummary_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/reports/summary"))
        .andExpect(status().isUnauthorized());
  }

  // ── GET /api/reports/full-report ──────────────────────────────────────────

  @Test
  @DisplayName("GET /api/reports/full-report - authenticated returns 200")
  void getInspection_authenticated_returns200() throws Exception {
    when(reportService.generateInspection(eq(orgId), any(), any()))
        .thenReturn(InspectionReport.builder().build());

    mockMvc.perform(get("/api/reports/full-report")
            .with(authentication(adminAuth())))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET /api/reports/full-report - unauthenticated returns 401")
  void getInspection_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/reports/full-report"))
        .andExpect(status().isUnauthorized());
  }

  // ── GET /api/reports/inspection/pdf ──────────────────────────────────────

  @Test
  @DisplayName("GET /api/reports/inspection/pdf - authenticated returns 200")
  void getInspectionPdf_authenticated_returns200() throws Exception {
    when(reportService.generateInspection(eq(orgId), any(), any()))
        .thenReturn(InspectionReport.builder().build());

    mockMvc.perform(get("/api/reports/inspection/pdf")
            .with(authentication(adminAuth())))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET /api/reports/inspection/pdf - unauthenticated returns 401")
  void getInspectionPdf_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/reports/inspection/pdf"))
        .andExpect(status().isUnauthorized());
  }

  // ── POST /api/reports/deviations ─────────────────────────────────────────

  @Test
  @DisplayName("POST /api/reports/deviations - authenticated creates report and returns 201")
  void createDeviation_authenticated_returns201() throws Exception {
    when(reportService.createDeviationReport(any(), eq(userId), eq(orgId)))
        .thenReturn(new DeviationCreatedResponse(UUID.randomUUID(), LocalDateTime.now()));

    mockMvc.perform(post("/api/reports/deviations")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "deviationName": "Cold storage too warm",
                  "severity": "MAJOR",
                  "timestamp": "2025-01-15T10:00",
                  "noticedBy": "Jane Doe",
                  "reportedTo": "Manager Smith",
                  "processedBy": "Jane Doe",
                  "description": "Cold storage temperature exceeded 8°C for over 2 hours.",
                  "immediateAction": "Moved products to backup storage",
                  "believedCause": "Compressor failure",
                  "correctiveMeasures": "Called repair service",
                  "correctiveMeasuresDone": "Compressor replaced"
                }
                """)
            .with(authentication(adminAuth())))
        .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("POST /api/reports/deviations - unauthenticated returns 401")
  void createDeviation_unauthenticated_returns401() throws Exception {
    mockMvc.perform(post("/api/reports/deviations")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "deviationName": "Cold storage too warm",
                  "severity": "MAJOR",
                  "timestamp": "2025-01-15T10:00",
                  "noticedBy": "Jane Doe",
                  "reportedTo": "Manager Smith",
                  "processedBy": "Jane Doe",
                  "description": "Cold storage temperature exceeded 8°C.",
                  "immediateAction": "Moved products",
                  "believedCause": "Compressor failure",
                  "correctiveMeasures": "Called repair service",
                  "correctiveMeasuresDone": "Compressor replaced"
                }
                """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("POST /api/reports/deviations - missing required fields returns 400")
  void createDeviation_missingRequiredFields_returns400() throws Exception {
    mockMvc.perform(post("/api/reports/deviations")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}")
            .with(authentication(adminAuth())))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /api/reports/deviations - missing severity returns 400")
  void createDeviation_missingSeverity_returns400() throws Exception {
    mockMvc.perform(post("/api/reports/deviations")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "deviationName": "Cold storage too warm",
                  "timestamp": "2025-01-15T10:00",
                  "noticedBy": "Jane Doe",
                  "reportedTo": "Manager Smith",
                  "processedBy": "Jane Doe",
                  "description": "Description",
                  "immediateAction": "Action",
                  "believedCause": "Cause",
                  "correctiveMeasures": "Measures",
                  "correctiveMeasuresDone": "Done"
                }
                """)
            .with(authentication(adminAuth())))
        .andExpect(status().isBadRequest());
  }
}
