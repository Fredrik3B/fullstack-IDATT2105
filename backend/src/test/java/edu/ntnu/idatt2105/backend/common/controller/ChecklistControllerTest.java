package edu.ntnu.idatt2105.backend.common.controller;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.ChecklistCardResponse;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.ChecklistTaskItemResponse;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.ChecklistWorkbenchStateRequest;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.CreateChecklistCardRequest;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.TaskCompletionRequest;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.TaskFlagRequest;
import edu.ntnu.idatt2105.backend.common.service.ChecklistService;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
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
@DisplayName("Checklist Controller")
class ChecklistControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private ChecklistService checklistService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UUID userId = UUID.randomUUID();
    private final UUID orgId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    // ── Auth helpers ──────────────────────────────────────────────────────────

    private Authentication userAuth() {
        JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
                userId, orgId, "user@test.com",
                List.of(new SimpleGrantedAuthority("ROLE_STAFF")));
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private ChecklistCardResponse sampleCard() {
        return new ChecklistCardResponse(
                1L, "daily", "2024-01-15", true, true,
                "Fridge Check", "Daily temperature log", "IC_FOOD",
                false, "0/1 completed", "muted", 0, List.of());
    }

    // ── GET /checklists ───────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/checklists - authenticated returns 200 with list")
    void getChecklists_authenticated_returns200() throws Exception {
        when(checklistService.fetchChecklists(any(), any())).thenReturn(List.of(sampleCard()));

        mockMvc.perform(get("/api/checklists")
                        .param("module", "IC_FOOD")
                        .with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Fridge Check"));
    }

    @Test
    @DisplayName("GET /api/checklists - unauthenticated returns 401")
    void getChecklists_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/checklists").param("module", "IC_FOOD"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/checklists - passes module parameter to service")
    void getChecklists_passesModuleToService() throws Exception {
        when(checklistService.fetchChecklists(eq(IcModule.IC_FOOD), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/checklists")
                        .param("module", "IC_FOOD")
                        .with(authentication(userAuth())))
                .andExpect(status().isOk());

        verify(checklistService).fetchChecklists(eq(IcModule.IC_FOOD), any());
    }

    // ── POST /checklists ──────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/checklists - authenticated returns 201 with created card")
    void createChecklist_authenticated_returns201() throws Exception {
        CreateChecklistCardRequest request = new CreateChecklistCardRequest(
                IcModule.IC_FOOD, "daily", "Fridge Check",
                "Daily temperature log", true, true, List.of(1L));

        when(checklistService.createChecklist(any(), any())).thenReturn(sampleCard());

        mockMvc.perform(post("/api/checklists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(authentication(userAuth())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Fridge Check"));
    }

    @Test
    @DisplayName("POST /api/checklists - unauthenticated returns 401")
    void createChecklist_unauthenticated_returns401() throws Exception {
        CreateChecklistCardRequest request = new CreateChecklistCardRequest(
                IcModule.IC_FOOD, "daily", "Fridge Check",
                null, true, true, List.of(1L));

        mockMvc.perform(post("/api/checklists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/checklists - missing required fields returns 400")
    void createChecklist_missingFields_returns400() throws Exception {
        // title is @NotBlank, taskTemplateIds is @NotEmpty
        String invalidBody = "{\"module\":\"IC_FOOD\",\"period\":\"daily\",\"recurring\":true,\"displayedOnWorkbench\":true,\"taskTemplateIds\":[]}";

        mockMvc.perform(post("/api/checklists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody)
                        .with(authentication(userAuth())))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /checklists/{id}/submit ───────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/checklists/{id}/submit - authenticated returns 200")
    void submitChecklist_authenticated_returns200() throws Exception {
        when(checklistService.submitChecklist(eq(1L), any())).thenReturn(sampleCard());

        mockMvc.perform(put("/api/checklists/1/submit")
                        .with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /api/checklists/{id}/submit - unauthenticated returns 401")
    void submitChecklist_unauthenticated_returns401() throws Exception {
        mockMvc.perform(put("/api/checklists/1/submit"))
                .andExpect(status().isUnauthorized());
    }

    // ── PUT /checklists/{id}/workbench ────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/checklists/{id}/workbench - authenticated returns 200")
    void setWorkbenchState_authenticated_returns200() throws Exception {
        ChecklistWorkbenchStateRequest request = new ChecklistWorkbenchStateRequest(true);
        when(checklistService.setChecklistWorkbenchState(eq(1L), any(), any()))
                .thenReturn(sampleCard());

        mockMvc.perform(put("/api/checklists/1/workbench")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(authentication(userAuth())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/checklists/{id}/workbench - unauthenticated returns 401")
    void setWorkbenchState_unauthenticated_returns401() throws Exception {
        mockMvc.perform(put("/api/checklists/1/workbench")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayedOnWorkbench\":true}"))
                .andExpect(status().isUnauthorized());
    }

    // ── PUT /checklists/{id}/tasks/{taskId}/completion ────────────────────────

    @Test
    @DisplayName("PUT /api/checklists/{id}/tasks/{taskId}/completion - authenticated returns 200")
    void setTaskCompletion_authenticated_returns200() throws Exception {
        ChecklistTaskItemResponse taskResponse = new ChecklistTaskItemResponse(
                100L, null, "Fridge temp", null, "temperature", "C",
                null, null, "todo", false, null, null, null, null);
        when(checklistService.setTaskCompletion(eq(1L), eq(100L), any(), any()))
                .thenReturn(taskResponse);

        TaskCompletionRequest request = new TaskCompletionRequest("completed", "2024-01-15", null);

        mockMvc.perform(put("/api/checklists/1/tasks/100/completion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(authentication(userAuth())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/checklists/{id}/tasks/{taskId}/completion - unauthenticated returns 401")
    void setTaskCompletion_unauthenticated_returns401() throws Exception {
        mockMvc.perform(put("/api/checklists/1/tasks/100/completion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"state\":\"completed\",\"periodKey\":\"2024-01-15\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ── PUT /checklists/{id}/tasks/{taskId}/flag ──────────────────────────────

    @Test
    @DisplayName("PUT /api/checklists/{id}/tasks/{taskId}/flag - authenticated returns 200")
    void setTaskFlag_authenticated_returns200() throws Exception {
        ChecklistTaskItemResponse taskResponse = new ChecklistTaskItemResponse(
                100L, null, "Fridge temp", null, "temperature", "C",
                null, null, "pending", true, null, null, null, null);
        when(checklistService.setTaskFlag(eq(1L), eq(100L), any(), any()))
                .thenReturn(taskResponse);

        TaskFlagRequest request = new TaskFlagRequest("pending", "2024-01-15", null);

        mockMvc.perform(put("/api/checklists/1/tasks/100/flag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(authentication(userAuth())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/checklists/{id}/tasks/{taskId}/flag - unauthenticated returns 401")
    void setTaskFlag_unauthenticated_returns401() throws Exception {
        mockMvc.perform(put("/api/checklists/1/tasks/100/flag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"state\":\"pending\",\"periodKey\":\"2024-01-15\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ── DELETE /checklists/{id} ───────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/checklists/{id} - authenticated returns 204")
    void deleteChecklist_authenticated_returns204() throws Exception {
        mockMvc.perform(delete("/api/checklists/1")
                        .with(authentication(userAuth())))
                .andExpect(status().isNoContent());

        verify(checklistService).deleteChecklist(eq(1L), any());
    }

    @Test
    @DisplayName("DELETE /api/checklists/{id} - unauthenticated returns 401")
    void deleteChecklist_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/api/checklists/1"))
                .andExpect(status().isUnauthorized());
    }
}
