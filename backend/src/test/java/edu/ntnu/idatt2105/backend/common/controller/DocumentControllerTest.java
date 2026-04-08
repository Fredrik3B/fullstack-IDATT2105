package edu.ntnu.idatt2105.backend.common.controller;

import edu.ntnu.idatt2105.backend.document.dto.DocumentDTO;
import edu.ntnu.idatt2105.backend.document.model.enums.DocumentCategory;
import edu.ntnu.idatt2105.backend.document.model.enums.DocumentModule;
import edu.ntnu.idatt2105.backend.common.service.DocumentService;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DisplayName("Document Controller")
class DocumentControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private DocumentService documentService;

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

    // ── Auth helpers ──────────────────────────────────────────────────────────

    private Authentication adminAuth() {
        JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
                userId, orgId, "admin@test.com",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private Authentication memberAuth() {
        JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(
                userId, orgId, "member@test.com",
                List.of(new SimpleGrantedAuthority("ROLE_MEMBER")));
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private DocumentDTO sampleDTO() {
        return new DocumentDTO(1L, "Test Doc", null, DocumentCategory.GUIDELINES, DocumentModule.SHARED,
                null, "test.pdf", "application/pdf", 100L, null, LocalDateTime.now(), "Admin User");
    }

    // ── POST /documents ───────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/documents - admin gets 201")
    void uploadDocument_asAdmin_returns201() throws Exception {
        when(documentService.uploadDocument(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(sampleDTO());

        mockMvc.perform(multipart("/api/documents")
                        .file(new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes()))
                        .param("name", "Test Doc")
                        .param("category", "GUIDELINES")
                        .param("module", "SHARED")
                        .with(authentication(adminAuth())))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/documents - unauthenticated gets 401")
    void uploadDocument_unauthenticated_returns401() throws Exception {
        mockMvc.perform(multipart("/api/documents")
                        .file(new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes()))
                        .param("name", "Test Doc")
                        .param("category", "GUIDELINES")
                        .param("module", "SHARED"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/documents - member (no ADMIN/MANAGER role) gets 403")
    void uploadDocument_asMember_returns403() throws Exception {
        mockMvc.perform(multipart("/api/documents")
                        .file(new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes()))
                        .param("name", "Test Doc")
                        .param("category", "GUIDELINES")
                        .param("module", "SHARED")
                        .with(authentication(memberAuth())))
                .andExpect(status().isForbidden());
    }

    // ── GET /api/documents ────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/documents - authenticated returns 200 with list")
    void getDocuments_authenticated_returns200WithList() throws Exception {
        when(documentService.getDocuments(any(), any(), any())).thenReturn(List.of(sampleDTO()));

        mockMvc.perform(get("/api/documents")
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Doc"));
    }

    @Test
    @DisplayName("GET /api/documents - unauthenticated gets 401")
    void getDocuments_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/documents - category filter is passed to service")
    void getDocuments_withCategoryFilter_passesFilterToService() throws Exception {
        when(documentService.getDocuments(any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/documents")
                        .param("category", "GUIDELINES")
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk());

        verify(documentService).getDocuments(
                org.mockito.ArgumentMatchers.eq(DocumentCategory.GUIDELINES),
                org.mockito.ArgumentMatchers.isNull(),
                any());
    }

    // ── GET /api/documents/{id}/download ──────────────────────────────────────

    @Test
    @DisplayName("GET /api/documents/{id}/download - returns 200 with Content-Disposition header")
    void downloadDocument_authenticated_returns200WithContentDisposition() throws Exception {
        org.springframework.core.io.ByteArrayResource resource =
                new org.springframework.core.io.ByteArrayResource("file content".getBytes());
        when(documentService.downloadDocument(anyLong(), any())).thenReturn(resource);
        when(documentService.getDocumentContentType(anyLong(), any())).thenReturn("application/pdf");
        when(documentService.getDocumentFileName(anyLong(), any())).thenReturn("test.pdf");

        mockMvc.perform(get("/api/documents/1/download")
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("test.pdf")));
    }

    @Test
    @DisplayName("GET /api/documents/{id}/download - unauthenticated gets 401")
    void downloadDocument_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/documents/1/download"))
                .andExpect(status().isUnauthorized());
    }

    // ── DELETE /api/documents/{id} ────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/documents/{id} - admin gets 204 and service is called")
    void deleteDocument_asAdmin_returns204() throws Exception {
        mockMvc.perform(delete("/api/documents/1")
                        .with(authentication(adminAuth())))
                .andExpect(status().isNoContent());

        verify(documentService).deleteDocument(org.mockito.ArgumentMatchers.eq(1L), any());
    }

    @Test
    @DisplayName("DELETE /api/documents/{id} - unauthenticated gets 401")
    void deleteDocument_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/api/documents/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/documents/{id} - member (no ADMIN/MANAGER role) gets 403")
    void deleteDocument_asMember_returns403() throws Exception {
        mockMvc.perform(delete("/api/documents/1")
                        .with(authentication(memberAuth())))
                .andExpect(status().isForbidden());
    }
}
