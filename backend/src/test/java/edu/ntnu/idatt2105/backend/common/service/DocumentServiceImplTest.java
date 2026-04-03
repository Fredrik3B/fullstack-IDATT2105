package edu.ntnu.idatt2105.backend.common.service;

import edu.ntnu.idatt2105.backend.common.dto.document.DocumentDTO;
import edu.ntnu.idatt2105.backend.common.model.DocumentModel;
import edu.ntnu.idatt2105.backend.common.model.enums.DocumentCategory;
import edu.ntnu.idatt2105.backend.common.model.enums.DocumentModule;
import edu.ntnu.idatt2105.backend.common.repository.DocumentRepository;
import edu.ntnu.idatt2105.backend.common.service.impl.DocumentServiceImpl;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import edu.ntnu.idatt2105.backend.user.repository.OrganizationRepository;
import edu.ntnu.idatt2105.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @InjectMocks
    private DocumentServiceImpl service;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @TempDir
    Path tempDir;

    private final UUID userId = UUID.randomUUID();
    private final UUID orgId = UUID.randomUUID();
    private JwtAuthenticatedPrincipal principal;
    private UserModel user;
    private OrganizationModel org;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "storagePath", tempDir.toString());

        user = new UserModel();
        user.setId(userId);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("user@test.com");
        user.setPassword("password");

        org = new OrganizationModel();
        org.setId(orgId);
        org.setName("Test Org");
        org.setJoinCode("JOIN123");

        principal = new JwtAuthenticatedPrincipal(userId, orgId, "user@test.com", List.of());
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private DocumentModel makeDoc(Long id, UUID docOrgId) {
        OrganizationModel docOrg = new OrganizationModel();
        docOrg.setId(docOrgId);
        docOrg.setName("Org");
        docOrg.setJoinCode("CODE");

        DocumentModel doc = new DocumentModel();
        doc.setId(id);
        doc.setName("Test Doc");
        doc.setCategory(DocumentCategory.GUIDELINES);
        doc.setModule(DocumentModule.SHARED);
        doc.setUploadedBy(user);
        doc.setOrganization(docOrg);
        doc.setUploadedAt(LocalDateTime.now());
        return doc;
    }

    // ── uploadDocument ────────────────────────────────────────────────────────

    @Test
    void uploadDocument_withFile_savesFileAndReturnsDTO() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getSize()).thenReturn(1024L);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("content".getBytes()));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));

        DocumentModel saved = makeDoc(1L, orgId);
        saved.setOriginalFileName("test.pdf");
        saved.setFileType("application/pdf");
        saved.setFileSize(1024L);
        when(documentRepository.save(any())).thenReturn(saved);

        DocumentDTO result = service.uploadDocument(
                file, null, "Test Doc", "desc",
                DocumentCategory.GUIDELINES, DocumentModule.SHARED, null, principal);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Test Doc");
        verify(documentRepository).save(any(DocumentModel.class));

        // File was physically written to disk under orgId directory
        Path orgDir = tempDir.resolve(orgId.toString());
        assertThat(Files.list(orgDir).count()).isEqualTo(1);
    }

    @Test
    void uploadDocument_withExternalUrl_doesNotWriteFileToDisk() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));

        DocumentModel saved = makeDoc(1L, orgId);
        saved.setExternalUrl("https://example.com/doc.pdf");
        when(documentRepository.save(any())).thenReturn(saved);

        DocumentDTO result = service.uploadDocument(
                null, "https://example.com/doc.pdf", "External Doc", null,
                DocumentCategory.GUIDELINES, DocumentModule.SHARED, null, principal);

        assertThat(result).isNotNull();
        assertThat(tempDir.toFile().list()).isEmpty();
    }

    @Test
    void uploadDocument_neitherFileNorUrl_throws400() {
        assertThatThrownBy(() ->
                service.uploadDocument(null, null, "name", null,
                        DocumentCategory.GUIDELINES, DocumentModule.SHARED, null, principal))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    void uploadDocument_blankUrl_throws400() {
        assertThatThrownBy(() ->
                service.uploadDocument(null, "   ", "name", null,
                        DocumentCategory.GUIDELINES, DocumentModule.SHARED, null, principal))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    void uploadDocument_userNotFound_throws404() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.uploadDocument(null, "https://example.com", "name", null,
                        DocumentCategory.GUIDELINES, DocumentModule.SHARED, null, principal))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    void uploadDocument_orgNotFound_throws404() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.uploadDocument(null, "https://example.com", "name", null,
                        DocumentCategory.GUIDELINES, DocumentModule.SHARED, null, principal))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    // ── getDocuments ──────────────────────────────────────────────────────────

    @Test
    void getDocuments_noFilters_callsFindAllByOrganizationId() {
        when(documentRepository.findAllByOrganizationId(orgId)).thenReturn(List.of());

        service.getDocuments(null, null, principal);

        verify(documentRepository).findAllByOrganizationId(orgId);
        verifyNoMoreInteractions(documentRepository);
    }

    @Test
    void getDocuments_categoryOnly_callsFindByCategory() {
        when(documentRepository.findAllByOrganizationIdAndCategory(orgId, DocumentCategory.GUIDELINES))
                .thenReturn(List.of());

        service.getDocuments(DocumentCategory.GUIDELINES, null, principal);

        verify(documentRepository).findAllByOrganizationIdAndCategory(orgId, DocumentCategory.GUIDELINES);
        verifyNoMoreInteractions(documentRepository);
    }

    @Test
    void getDocuments_moduleOnly_callsFindByModule() {
        when(documentRepository.findAllByOrganizationIdAndModule(orgId, DocumentModule.SHARED))
                .thenReturn(List.of());

        service.getDocuments(null, DocumentModule.SHARED, principal);

        verify(documentRepository).findAllByOrganizationIdAndModule(orgId, DocumentModule.SHARED);
        verifyNoMoreInteractions(documentRepository);
    }

    @Test
    void getDocuments_bothFilters_callsFindByCategoryAndModule() {
        when(documentRepository.findAllByOrganizationIdAndCategoryAndModule(
                orgId, DocumentCategory.GUIDELINES, DocumentModule.SHARED))
                .thenReturn(List.of());

        service.getDocuments(DocumentCategory.GUIDELINES, DocumentModule.SHARED, principal);

        verify(documentRepository).findAllByOrganizationIdAndCategoryAndModule(
                orgId, DocumentCategory.GUIDELINES, DocumentModule.SHARED);
        verifyNoMoreInteractions(documentRepository);
    }

    @Test
    void getDocuments_mapsToDTOs() {
        DocumentModel doc = makeDoc(1L, orgId);
        when(documentRepository.findAllByOrganizationId(orgId)).thenReturn(List.of(doc));

        List<DocumentDTO> result = service.getDocuments(null, null, principal);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).uploadedByName()).isEqualTo("Test User");
    }

    // ── downloadDocument ──────────────────────────────────────────────────────

    @Test
    void downloadDocument_success() throws IOException {
        Path file = tempDir.resolve("test.pdf");
        Files.writeString(file, "PDF content");

        DocumentModel doc = makeDoc(1L, orgId);
        doc.setStoragePath(file.toAbsolutePath().toString());
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        Resource resource = service.downloadDocument(1L, principal);

        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
    }

    @Test
    void downloadDocument_wrongOrg_throws403() {
        DocumentModel doc = makeDoc(1L, UUID.randomUUID());
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        assertThatThrownBy(() -> service.downloadDocument(1L, principal))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    @Test
    void downloadDocument_notFound_throws404() {
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.downloadDocument(99L, principal))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    // ── deleteDocument ────────────────────────────────────────────────────────

    @Test
    void deleteDocument_deletesFromRepoAndDisk() throws IOException {
        Path file = tempDir.resolve("todelete.pdf");
        Files.writeString(file, "content");

        DocumentModel doc = makeDoc(1L, orgId);
        doc.setStoragePath(file.toAbsolutePath().toString());
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        service.deleteDocument(1L, principal);

        verify(documentRepository).delete(doc);
        assertThat(Files.notExists(file)).isTrue();
    }

    @Test
    void deleteDocument_wrongOrg_throws403() {
        DocumentModel doc = makeDoc(1L, UUID.randomUUID());
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        assertThatThrownBy(() -> service.deleteDocument(1L, principal))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
        verify(documentRepository, never()).delete(any());
    }

    @Test
    void deleteDocument_notFound_throws404() {
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteDocument(99L, principal))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }
}
