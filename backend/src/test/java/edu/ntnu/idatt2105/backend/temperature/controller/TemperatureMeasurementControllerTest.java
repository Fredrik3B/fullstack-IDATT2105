package edu.ntnu.idatt2105.backend.temperature.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import edu.ntnu.idatt2105.backend.temperature.dto.TemperatureMeasurementResponse;
import edu.ntnu.idatt2105.backend.temperature.service.TemperatureMeasurementService;
import java.math.BigDecimal;
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
@DisplayName("TemperatureMeasurementController (/api/temperature-measurements)")
class TemperatureMeasurementControllerTest {

  @Autowired
  private WebApplicationContext context;

  @MockitoBean
  private TemperatureMeasurementService temperatureMeasurementService;

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

  private TemperatureMeasurementResponse sampleMeasurement() {
    return new TemperatureMeasurementResponse(
        1L, IcModule.IC_FOOD, 10L, 5L,
        new BigDecimal("4.5"), LocalDateTime.now(), "2025-W01", false);
  }

  @Test
  @DisplayName("POST /api/temperature-measurements - staff creates measurement and returns 201")
  void createMeasurement_asStaff_returns201() throws Exception {
    when(temperatureMeasurementService.createMeasurement(any(), any())).thenReturn(sampleMeasurement());

    mockMvc.perform(post("/api/temperature-measurements")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "module": "IC_FOOD",
                  "checklistId": 10,
                  "taskId": 5,
                  "valueC": 4.5
                }
                """)
            .with(authentication(staffAuth())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.valueC").value(4.5));
  }

  @Test
  @DisplayName("POST /api/temperature-measurements - admin creates measurement and returns 201")
  void createMeasurement_asAdmin_returns201() throws Exception {
    when(temperatureMeasurementService.createMeasurement(any(), any())).thenReturn(sampleMeasurement());

    mockMvc.perform(post("/api/temperature-measurements")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "module": "IC_FOOD",
                  "checklistId": 10,
                  "taskId": 5,
                  "valueC": 4.5
                }
                """)
            .with(authentication(adminAuth())))
        .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("POST /api/temperature-measurements - unauthenticated returns 401")
  void createMeasurement_unauthenticated_returns401() throws Exception {
    mockMvc.perform(post("/api/temperature-measurements")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "module": "IC_FOOD",
                  "checklistId": 10,
                  "taskId": 5,
                  "valueC": 4.5
                }
                """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("POST /api/temperature-measurements - missing required fields returns 400")
  void createMeasurement_missingRequiredFields_returns400() throws Exception {
    mockMvc.perform(post("/api/temperature-measurements")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}")
            .with(authentication(staffAuth())))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /api/temperature-measurements - missing valueC returns 400")
  void createMeasurement_missingValueC_returns400() throws Exception {
    mockMvc.perform(post("/api/temperature-measurements")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "module": "IC_FOOD",
                  "checklistId": 10,
                  "taskId": 5
                }
                """)
            .with(authentication(staffAuth())))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("GET /api/temperature-measurements - staff returns 200 with list")
  void fetchMeasurements_asStaff_returns200() throws Exception {
    when(temperatureMeasurementService.fetchMeasurements(any(), isNull(), isNull(), any()))
        .thenReturn(List.of(sampleMeasurement()));

    mockMvc.perform(get("/api/temperature-measurements")
            .param("module", "IC_FOOD")
            .with(authentication(staffAuth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].valueC").value(4.5));
  }

  @Test
  @DisplayName("GET /api/temperature-measurements - with date range returns 200")
  void fetchMeasurements_withDateRange_returns200() throws Exception {
    when(temperatureMeasurementService.fetchMeasurements(any(), any(), any(), any()))
        .thenReturn(List.of(sampleMeasurement()));

    mockMvc.perform(get("/api/temperature-measurements")
            .param("module", "IC_FOOD")
            .param("from", "2025-01-01T00:00:00")
            .param("to", "2025-01-31T23:59:59")
            .with(authentication(staffAuth())))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET /api/temperature-measurements - unauthenticated returns 401")
  void fetchMeasurements_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/temperature-measurements")
            .param("module", "IC_FOOD"))
        .andExpect(status().isUnauthorized());
  }
}
