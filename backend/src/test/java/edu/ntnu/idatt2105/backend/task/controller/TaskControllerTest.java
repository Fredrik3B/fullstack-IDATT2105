package edu.ntnu.idatt2105.backend.task.controller;

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

import edu.ntnu.idatt2105.backend.checklist.model.enums.SectionTypes;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import edu.ntnu.idatt2105.backend.shared.enums.IcModule;
import edu.ntnu.idatt2105.backend.task.dto.TaskResponse;
import edu.ntnu.idatt2105.backend.task.service.TaskService;
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
@DisplayName("TaskController (/api/tasks)")
class TaskControllerTest {

  @Autowired
  private WebApplicationContext context;

  @MockitoBean
  private TaskService taskService;

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

  private TaskResponse sampleTask() {
    return new TaskResponse(1L, IcModule.IC_FOOD, "Check fridge temperature",
        null, SectionTypes.HYGIENE, null, null, null, null, null, null);
  }

  // ── POST /api/tasks ───────────────────────────────────────────────────────

  @Test
  @DisplayName("POST /api/tasks - admin creates task and returns 201")
  void createTask_asAdmin_returns201() throws Exception {
    when(taskService.createTask(any(), any())).thenReturn(sampleTask());

    mockMvc.perform(post("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "module": "IC_FOOD",
                  "title": "Check fridge temperature",
                  "sectionType": "HYGIENE"
                }
                """)
            .with(authentication(adminAuth())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("Check fridge temperature"));
  }

  @Test
  @DisplayName("POST /api/tasks - manager creates task and returns 201")
  void createTask_asManager_returns201() throws Exception {
    when(taskService.createTask(any(), any())).thenReturn(sampleTask());

    mockMvc.perform(post("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "module": "IC_FOOD",
                  "title": "Check fridge temperature",
                  "sectionType": "HYGIENE"
                }
                """)
            .with(authentication(managerAuth())))
        .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("POST /api/tasks - staff returns 403")
  void createTask_asStaff_returns403() throws Exception {
    mockMvc.perform(post("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "module": "IC_FOOD",
                  "title": "Check fridge temperature",
                  "sectionType": "HYGIENE"
                }
                """)
            .with(authentication(staffAuth())))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("POST /api/tasks - unauthenticated returns 401")
  void createTask_unauthenticated_returns401() throws Exception {
    mockMvc.perform(post("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "module": "IC_FOOD",
                  "title": "Check fridge temperature",
                  "sectionType": "HYGIENE"
                }
                """))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("POST /api/tasks - missing required fields returns 400")
  void createTask_missingRequiredFields_returns400() throws Exception {
    mockMvc.perform(post("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}")
            .with(authentication(adminAuth())))
        .andExpect(status().isBadRequest());
  }

  // ── PUT /api/tasks/{taskId} ───────────────────────────────────────────────

  @Test
  @DisplayName("PUT /api/tasks/{id} - admin updates task and returns 200")
  void updateTask_asAdmin_returns200() throws Exception {
    when(taskService.updateTask(eq(1L), any(), any())).thenReturn(sampleTask());

    mockMvc.perform(put("/api/tasks/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "module": "IC_FOOD",
                  "title": "Updated task title",
                  "sectionType": "HYGIENE"
                }
                """)
            .with(authentication(adminAuth())))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("PUT /api/tasks/{id} - staff returns 403")
  void updateTask_asStaff_returns403() throws Exception {
    mockMvc.perform(put("/api/tasks/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "module": "IC_FOOD",
                  "title": "Updated task title",
                  "sectionType": "HYGIENE"
                }
                """)
            .with(authentication(staffAuth())))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("PUT /api/tasks/{id} - unauthenticated returns 401")
  void updateTask_unauthenticated_returns401() throws Exception {
    mockMvc.perform(put("/api/tasks/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "module": "IC_FOOD",
                  "title": "Updated task title",
                  "sectionType": "HYGIENE"
                }
                """))
        .andExpect(status().isUnauthorized());
  }

  // ── GET /api/tasks ────────────────────────────────────────────────────────

  @Test
  @DisplayName("GET /api/tasks - authenticated returns 200 with list")
  void getAllTasks_authenticated_returns200() throws Exception {
    when(taskService.getAllTasks(any(), any())).thenReturn(List.of(sampleTask()));

    mockMvc.perform(get("/api/tasks")
            .param("module", "IC_FOOD")
            .with(authentication(staffAuth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].title").value("Check fridge temperature"));
  }

  @Test
  @DisplayName("GET /api/tasks - unauthenticated returns 401")
  void getAllTasks_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/tasks")
            .param("module", "IC_FOOD"))
        .andExpect(status().isUnauthorized());
  }

  // ── GET /api/tasks/{taskId} ───────────────────────────────────────────────

  @Test
  @DisplayName("GET /api/tasks/{id} - authenticated returns 200")
  void getTaskById_authenticated_returns200() throws Exception {
    when(taskService.getTaskById(eq(1L), any())).thenReturn(sampleTask());

    mockMvc.perform(get("/api/tasks/1")
            .with(authentication(staffAuth())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1));
  }

  @Test
  @DisplayName("GET /api/tasks/{id} - unauthenticated returns 401")
  void getTaskById_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/tasks/1"))
        .andExpect(status().isUnauthorized());
  }

  // ── DELETE /api/tasks/{taskId} ────────────────────────────────────────────

  @Test
  @DisplayName("DELETE /api/tasks/{id} - admin returns 204")
  void deleteTask_asAdmin_returns204() throws Exception {
    mockMvc.perform(delete("/api/tasks/1")
            .with(authentication(adminAuth())))
        .andExpect(status().isNoContent());

    verify(taskService).deleteTask(eq(1L), any());
  }

  @Test
  @DisplayName("DELETE /api/tasks/{id} - manager returns 204")
  void deleteTask_asManager_returns204() throws Exception {
    mockMvc.perform(delete("/api/tasks/1")
            .with(authentication(managerAuth())))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /api/tasks/{id} - staff returns 403")
  void deleteTask_asStaff_returns403() throws Exception {
    mockMvc.perform(delete("/api/tasks/1")
            .with(authentication(staffAuth())))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("DELETE /api/tasks/{id} - unauthenticated returns 401")
  void deleteTask_unauthenticated_returns401() throws Exception {
    mockMvc.perform(delete("/api/tasks/1"))
        .andExpect(status().isUnauthorized());
  }
}
