package edu.ntnu.idatt2105.backend.temperature.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import edu.ntnu.idatt2105.backend.temperature.dto.TemperatureZoneResponse;
import edu.ntnu.idatt2105.backend.temperature.model.enums.TemperatureZone;
import edu.ntnu.idatt2105.backend.temperature.service.TemperatureZoneService;
import java.math.BigDecimal;
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
@DisplayName("TemperatureZoneController (/api/temperature-zones)")
class TemperatureZoneControllerTest {

  @Autowired
  private WebApplicationContext context;

  @MockitoBean
  private TemperatureZoneService temperatureZoneService;

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

  private Authentication adminAuth() {
    JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
        userId, orgId, "admin@test.com",
        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
  }

  private Authentication managerAuth() {
    JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
        userId, orgId, "manager@test.com",
        List.of(new SimpleGrantedAuthority("ROLE_MANAGER")));
    return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
  }

  private Authentication staffAuth() {
    JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
        userId, orgId, "staff@test.com",
        List.of(new SimpleGrantedAuthority("ROLE_STAFF")));
    return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
  }

  private TemperatureZoneResponse sampleZone() {
    return new TemperatureZoneResponse(
        1L, IcModule.IC_FOOD, "Main Fridge",
        TemperatureZone.FRIDGE, new BigDecimal("2.00"), new BigDecimal("5.00"));
  }

  @Test
  @DisplayName("POST /api/temperature-zones - admin creates zone and returns 201")
  void createZone_asAdmin_returns201() throws Exception {
    when(temperatureZoneService.createZone(any(), any())).thenReturn(sampleZone());

    mockMvc.perform(post("/api/temperature-zones")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "module": "IC_FOOD",
                  "name": "Main Fridge",
                  "zoneType": "FRIDGE",
                  "targetMin": 2.0,
                  "targetMax": 5.0
                }
                """)
            .with(authentication(adminAuth())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Main Fridge"));
  }

  @Test
  @DisplayName("POST /api/temperature-zones - manager creates zone and returns 201")
  void createZone_asManager_returns201() throws Exception {
    when(temperatureZoneService.createZone(any(), any())).thenReturn(sampleZone());

    mockMvc.perform(post("/api/temperature-zones")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "module": "IC_FOOD",
                  "name": "Main Fridge",
                  "zoneType": "FRIDGE",
                  "targetMin": 2.0,
                  "targetMax": 5.0
                }
                """)
            .with(authentication(managerAuth())))
        .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("POST /api/temperature-zones - staff returns 403")
  void createZone_asStaff_returns403() throws Exception {
    mockMvc.perform(post("/api/temperature-zones")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "module": "IC_FOOD",
                  "name": "Main Fridge",
                  "zoneType": "FRIDGE",
                  "targetMin": 2.0,
                  "targetMax": 5.0
                }
                """)
            .with(authentication(staffAuth())))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("POST /api/temperature-zones - unauthenticated returns 401")
  void createZone_unauthenticated_returns401() throws Exception {
    mockMvc.perform(post("/api/temperature-zones")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "module": "IC_FOOD",
                  "name": "Main Fridge",
                  "zoneType": "FRIDGE",
                  "targetMin": 2.0,
                  "targetMax": 5.0
                }
                """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("POST /api/temperature-zones - missing required fields returns 400")
  void createZone_missingRequiredFields_returns400() throws Exception {
    mockMvc.perform(post("/api/temperature-zones")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}")
            .with(authentication(adminAuth())))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("PUT /api/temperature-zones/{id} - admin updates zone and returns 200")
  void updateZone_asAdmin_returns200() throws Exception {
    when(temperatureZoneService.updateZone(eq(1L), any(), any())).thenReturn(sampleZone());

    mockMvc.perform(put("/api/temperature-zones/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "module": "IC_FOOD",
                  "name": "Updated Fridge",
                  "zoneType": "FRIDGE",
                  "targetMin": 1.0,
                  "targetMax": 4.0
                }
                """)
            .with(authentication(adminAuth())))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("PUT /api/temperature-zones/{id} - staff returns 403")
  void updateZone_asStaff_returns403() throws Exception {
    mockMvc.perform(put("/api/temperature-zones/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "module": "IC_FOOD",
                  "name": "Updated Fridge",
                  "zoneType": "FRIDGE",
                  "targetMin": 1.0,
                  "targetMax": 4.0
                }
                """)
            .with(authentication(staffAuth())))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("PUT /api/temperature-zones/{id} - unauthenticated returns 401")
  void updateZone_unauthenticated_returns401() throws Exception {
    mockMvc.perform(put("/api/temperature-zones/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "module": "IC_FOOD",
                  "name": "Updated Fridge",
                  "zoneType": "FRIDGE",
                  "targetMin": 1.0,
                  "targetMax": 4.0
                }
                """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("GET /api/temperature-zones - admin returns 200 with list")
  void getAllZones_asAdmin_returns200() throws Exception {
    when(temperatureZoneService.getAllZones(any(), any())).thenReturn(List.of(sampleZone()));

    mockMvc.perform(get("/api/temperature-zones")
            .param("module", "IC_FOOD")
            .with(authentication(adminAuth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Main Fridge"));
  }

  @Test
  @DisplayName("GET /api/temperature-zones - manager returns 200")
  void getAllZones_asManager_returns200() throws Exception {
    when(temperatureZoneService.getAllZones(any(), any())).thenReturn(List.of(sampleZone()));

    mockMvc.perform(get("/api/temperature-zones")
            .param("module", "IC_FOOD")
            .with(authentication(managerAuth())))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("GET /api/temperature-zones - staff returns 403")
  void getAllZones_asStaff_returns403() throws Exception {
    mockMvc.perform(get("/api/temperature-zones")
            .param("module", "IC_FOOD")
            .with(authentication(staffAuth())))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("GET /api/temperature-zones - unauthenticated returns 401")
  void getAllZones_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/temperature-zones")
            .param("module", "IC_FOOD"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("DELETE /api/temperature-zones/{id} - admin returns 204")
  void deleteZone_asAdmin_returns204() throws Exception {
    mockMvc.perform(delete("/api/temperature-zones/1")
            .with(authentication(adminAuth())))
        .andExpect(status().isNoContent());

    verify(temperatureZoneService).deleteZone(eq(1L), any());
  }

  @Test
  @DisplayName("DELETE /api/temperature-zones/{id} - manager returns 204")
  void deleteZone_asManager_returns204() throws Exception {
    mockMvc.perform(delete("/api/temperature-zones/1")
            .with(authentication(managerAuth())))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /api/temperature-zones/{id} - staff returns 403")
  void deleteZone_asStaff_returns403() throws Exception {
    mockMvc.perform(delete("/api/temperature-zones/1")
            .with(authentication(staffAuth())))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("DELETE /api/temperature-zones/{id} - unauthenticated returns 401")
  void deleteZone_unauthenticated_returns401() throws Exception {
    mockMvc.perform(delete("/api/temperature-zones/1"))
        .andExpect(status().isUnauthorized());
  }
}
