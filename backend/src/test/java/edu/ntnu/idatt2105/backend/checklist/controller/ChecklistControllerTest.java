package edu.ntnu.idatt2105.backend.checklist.controller;

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

import edu.ntnu.idatt2105.backend.checklist.dto.ChecklistCardResponse;
import edu.ntnu.idatt2105.backend.checklist.dto.ChecklistTaskItemResponse;
import edu.ntnu.idatt2105.backend.checklist.service.ChecklistService;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import java.time.Instant;
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
@DisplayName("ChecklistController (/api/checklists)")
class ChecklistControllerTest {

  @Autowired
  private WebApplicationContext context;

  @MockitoBean
  private ChecklistService checklistService;

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

  private ChecklistCardResponse sampleChecklist() {
    return new ChecklistCardResponse(1L, "DAILY", "2025-W01", true, true,
        "Opening Checklist", null, "IC_FOOD", false, "In progress", "neutral", 0, List.of());
  }

  private ChecklistTaskItemResponse sampleTaskItem() {
    return new ChecklistTaskItemResponse(10L, 5L, "Check fridge temp", null,
        "TEMPERATURE_CONTROL", "C", null, null, "todo", false, null, null, null, null);
  }

  // ── POST /api/checklists ──────────────────────────────────────────────────

  @Test
  @DisplayName("POST /api/checklists - admin creates checklist and returns 201")
  void createChecklist_asAdmin_returns201() throws Exception {
    when(checklistService.createChecklist(any(), any())).thenReturn(sampleChecklist());

    mockMvc.perform(post("/api/checklists")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "module": "IC_FOOD",
                  "period": "DAILY",
                  "title": "Opening Checklist",
                  "recurring": true,
                  "displayedOnWorkbench": true,
                  "taskTemplateIds": [1, 2]
                }
                """)
            .with(authentication(adminAuth())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("Opening Checklist"));
  }

  @Test
  @DisplayName("POST /api/checklists - manager creates checklist and returns 201")
  void createChecklist_asManager_returns201() throws Exception {
    when(checklistService.createChecklist(any(), any())).thenReturn(sampleChecklist());

    mockMvc.perform(post("/api/checklists")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "module": "IC_FOOD",
                  "period": "DAILY",
                  "title": "Opening Checklist",
                  "recurring": true,
                  "displayedOnWorkbench": true,
                  "taskTemplateIds": [1]
                }
                """)
            .with(authentication(managerAuth())))
        .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("POST /api/checklists - staff returns 403")
  void createChecklist_asStaff_returns403() throws Exception {
    mockMvc.perform(post("/api/checklists")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "module": "IC_FOOD",
                  "period": "DAILY",
                  "title": "Opening Checklist",
                  "recurring": true,
                  "displayedOnWorkbench": true,
                  "taskTemplateIds": [1]
                }
                """)
            .with(authentication(staffAuth())))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("POST /api/checklists - unauthenticated returns 401")
  void createChecklist_unauthenticated_returns401() throws Exception {
    mockMvc.perform(post("/api/checklists")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "module": "IC_FOOD",
                  "period": "DAILY",
                  "title": "Opening Checklist",
                  "recurring": true,
                  "displayedOnWorkbench": true,
                  "taskTemplateIds": [1]
                }
                """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("POST /api/checklists - missing required fields returns 400")
  void createChecklist_missingRequiredFields_returns400() throws Exception {
    mockMvc.perform(post("/api/checklists")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}")
            .with(authentication(adminAuth())))
        .andExpect(status().isBadRequest());
  }

  // ── GET /api/checklists ───────────────────────────────────────────────────

  @Test
  @DisplayName("GET /api/checklists - authenticated returns 200 with list")
  void getChecklists_authenticated_returns200() throws Exception {
    when(checklistService.fetchChecklistsLastModified(any(), any())).thenReturn(Instant.EPOCH);
    when(checklistService.fetchChecklists(any(), any())).thenReturn(List.of(sampleChecklist()));

    mockMvc.perform(get("/api/checklists")
            .param("module", "IC_FOOD")
            .with(authentication(staffAuth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].title").value("Opening Checklist"));
  }

  @Test
  @DisplayName("GET /api/checklists - unauthenticated returns 401")
  void getChecklists_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/checklists")
            .param("module", "IC_FOOD"))
        .andExpect(status().isUnauthorized());
  }

  // ── PUT /api/checklists/{checklistId} ─────────────────────────────────────

  @Test
  @DisplayName("PUT /api/checklists/{id} - admin updates checklist and returns 200")
  void updateChecklist_asAdmin_returns200() throws Exception {
    when(checklistService.updateChecklist(eq(1L), any(), any())).thenReturn(sampleChecklist());

    mockMvc.perform(put("/api/checklists/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "period": "DAILY",
                  "title": "Updated Checklist",
                  "taskTemplateIds": [1, 2]
                }
                """)
            .with(authentication(adminAuth())))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("PUT /api/checklists/{id} - staff returns 403")
  void updateChecklist_asStaff_returns403() throws Exception {
    mockMvc.perform(put("/api/checklists/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "period": "DAILY",
                  "title": "Updated Checklist",
                  "taskTemplateIds": [1]
                }
                """)
            .with(authentication(staffAuth())))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("PUT /api/checklists/{id} - unauthenticated returns 401")
  void updateChecklist_unauthenticated_returns401() throws Exception {
    mockMvc.perform(put("/api/checklists/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "period": "DAILY",
                  "title": "Updated Checklist",
                  "taskTemplateIds": [1]
                }
                """))
        .andExpect(status().isUnauthorized());
  }

  // ── PUT /api/checklists/{id}/tasks/{taskId}/completion ────────────────────

  @Test
  @DisplayName("PUT /api/checklists/{id}/tasks/{taskId}/completion - staff returns 200")
  void setTaskCompletion_asStaff_returns200() throws Exception {
    when(checklistService.setTaskCompletion(eq(1L), eq(10L), any(), any()))
        .thenReturn(sampleTaskItem());

    mockMvc.perform(put("/api/checklists/1/tasks/10/completion")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "state": "completed",
                  "periodKey": "2025-W01"
                }
                """)
            .with(authentication(staffAuth())))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("PUT /api/checklists/{id}/tasks/{taskId}/completion - unauthenticated returns 401")
  void setTaskCompletion_unauthenticated_returns401() throws Exception {
    mockMvc.perform(put("/api/checklists/1/tasks/10/completion")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "state": "completed",
                  "periodKey": "2025-W01"
                }
                """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("PUT /api/checklists/{id}/tasks/{taskId}/completion - missing fields returns 400")
  void setTaskCompletion_missingFields_returns400() throws Exception {
    mockMvc.perform(put("/api/checklists/1/tasks/10/completion")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}")
            .with(authentication(staffAuth())))
        .andExpect(status().isBadRequest());
  }

  // ── PUT /api/checklists/{id}/tasks/{taskId}/flag ──────────────────────────

  @Test
  @DisplayName("PUT /api/checklists/{id}/tasks/{taskId}/flag - staff returns 200")
  void setTaskFlag_asStaff_returns200() throws Exception {
    when(checklistService.setTaskFlag(eq(1L), eq(10L), any(), any()))
        .thenReturn(sampleTaskItem());

    mockMvc.perform(put("/api/checklists/1/tasks/10/flag")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "state": "pending",
                  "periodKey": "2025-W01"
                }
                """)
            .with(authentication(staffAuth())))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("PUT /api/checklists/{id}/tasks/{taskId}/flag - unauthenticated returns 401")
  void setTaskFlag_unauthenticated_returns401() throws Exception {
    mockMvc.perform(put("/api/checklists/1/tasks/10/flag")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "state": "pending",
                  "periodKey": "2025-W01"
                }
                """))
        .andExpect(status().isUnauthorized());
  }

  // ── PUT /api/checklists/{id}/submit ───────────────────────────────────────

  @Test
  @DisplayName("PUT /api/checklists/{id}/submit - staff returns 200")
  void submitChecklist_asStaff_returns200() throws Exception {
    when(checklistService.submitChecklist(eq(1L), any())).thenReturn(sampleChecklist());

    mockMvc.perform(put("/api/checklists/1/submit")
            .with(authentication(staffAuth())))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("PUT /api/checklists/{id}/submit - unauthenticated returns 401")
  void submitChecklist_unauthenticated_returns401() throws Exception {
    mockMvc.perform(put("/api/checklists/1/submit"))
        .andExpect(status().isUnauthorized());
  }

  // ── PUT /api/checklists/{id}/workbench ────────────────────────────────────

  @Test
  @DisplayName("PUT /api/checklists/{id}/workbench - admin returns 200")
  void setWorkbenchState_asAdmin_returns200() throws Exception {
    when(checklistService.setChecklistWorkbenchState(eq(1L), any(), any()))
        .thenReturn(sampleChecklist());

    mockMvc.perform(put("/api/checklists/1/workbench")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"displayedOnWorkbench": true}
                """)
            .with(authentication(adminAuth())))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("PUT /api/checklists/{id}/workbench - staff returns 403")
  void setWorkbenchState_asStaff_returns403() throws Exception {
    mockMvc.perform(put("/api/checklists/1/workbench")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"displayedOnWorkbench": true}
                """)
            .with(authentication(staffAuth())))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("PUT /api/checklists/{id}/workbench - unauthenticated returns 401")
  void setWorkbenchState_unauthenticated_returns401() throws Exception {
    mockMvc.perform(put("/api/checklists/1/workbench")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"displayedOnWorkbench": true}
                """))
        .andExpect(status().isUnauthorized());
  }

  // ── DELETE /api/checklists/{checklistId} ──────────────────────────────────

  @Test
  @DisplayName("DELETE /api/checklists/{id} - admin returns 204")
  void deleteChecklist_asAdmin_returns204() throws Exception {
    mockMvc.perform(delete("/api/checklists/1")
            .with(authentication(adminAuth())))
        .andExpect(status().isNoContent());

    verify(checklistService).deleteChecklist(eq(1L), any());
  }

  @Test
  @DisplayName("DELETE /api/checklists/{id} - manager returns 204")
  void deleteChecklist_asManager_returns204() throws Exception {
    mockMvc.perform(delete("/api/checklists/1")
            .with(authentication(managerAuth())))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /api/checklists/{id} - staff returns 403")
  void deleteChecklist_asStaff_returns403() throws Exception {
    mockMvc.perform(delete("/api/checklists/1")
            .with(authentication(staffAuth())))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("DELETE /api/checklists/{id} - unauthenticated returns 401")
  void deleteChecklist_unauthenticated_returns401() throws Exception {
    mockMvc.perform(delete("/api/checklists/1"))
        .andExpect(status().isUnauthorized());
  }
}
