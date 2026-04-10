package edu.ntnu.idatt2105.backend.common.service;

import edu.ntnu.idatt2105.backend.document.dto.DocumentDto;
import edu.ntnu.idatt2105.backend.document.mapper.DocumentMapper;
import edu.ntnu.idatt2105.backend.document.model.DocumentModel;
import edu.ntnu.idatt2105.backend.document.model.enums.DocumentCategory;
import edu.ntnu.idatt2105.backend.document.model.enums.DocumentModule;
import edu.ntnu.idatt2105.backend.document.repository.DocumentRepository;
import edu.ntnu.idatt2105.backend.document.service.DocumentService;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import edu.ntnu.idatt2105.backend.user.repository.OrganizationRepository;
import edu.ntnu.idatt2105.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
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
@DisplayName("Document Service")
class DocumentServiceTest {

    @InjectMocks
    private DocumentService service;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private DocumentMapper documentMapper;

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

    private DocumentDto makeDto(DocumentModel doc) {
        return new DocumentDto(
            doc.getId(),
            doc.getName(),
            doc.getDescription(),
            doc.getCategory(),
            doc.getModule(),
            doc.getExternalUrl(),
            doc.getOriginalFileName(),
            doc.getFileType(),
            doc.getFileSize(),
            doc.getExpiryDate(),
            doc.getUploadedAt(),
            "Test User"
        );
    }


    @Test
    @DisplayName("uploadDocument - file upload: saves file to disk and returns DTO")
    void uploadDocument_withFile_savesFileAndReturnsDTO() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getSize()).thenReturn(1024L);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("content".getBytes()));

        when(userRepository.getReferenceById(principal.getUserId())).thenReturn(user);
        when(organizationRepository.getReferenceById(orgId)).thenReturn(org);

        DocumentModel saved = makeDoc(1L, orgId);
        saved.setOriginalFileName("test.pdf");
        saved.setFileType("application/pdf");
        saved.setFileSize(1024L);
        when(documentRepository.save(any())).thenReturn(saved);
        when(documentMapper.toDto(any(DocumentModel.class))).thenAnswer(invocation -> makeDto(invocation.getArgument(0)));

        DocumentDto result = service.uploadDocument(
                file, null, "Test Doc", "desc",
                DocumentCategory.GUIDELINES, DocumentModule.SHARED, null, principal);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Test Doc");
        verify(documentRepository).save(any(DocumentModel.class));

        Path orgDir = tempDir.resolve(orgId.toString());
        assertThat(Files.list(orgDir).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("uploadDocument - external URL: no file written to disk")
    void uploadDocument_withExternalUrl_doesNotWriteFileToDisk() {
        when(userRepository.getReferenceById(principal.getUserId())).thenReturn(user);
        when(organizationRepository.getReferenceById(orgId)).thenReturn(org);

        DocumentModel saved = makeDoc(1L, orgId);
        saved.setExternalUrl("https://example.com/doc.pdf");
        when(documentRepository.save(any())).thenReturn(saved);

        when(documentMapper.toDto(any(DocumentModel.class))).thenAnswer(invocation -> {
                DocumentModel doc = invocation.getArgument(0);
                return makeDto(doc);
            });

        DocumentDto result = service.uploadDocument(
                null, "https://example.com/doc.pdf", "External Doc", null,
                DocumentCategory.GUIDELINES, DocumentModule.SHARED, null, principal);

        assertThat(result).isNotNull();
        assertThat(tempDir.toFile().list()).isEmpty();
    }

    @Test
    @DisplayName("uploadDocument - no file and no URL: throws 400")
    void uploadDocument_neitherFileNorUrl_throws400() {
        assertThatThrownBy(() ->
                service.uploadDocument(null, null, "name", null,
                        DocumentCategory.GUIDELINES, DocumentModule.SHARED, null, principal))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    @DisplayName("uploadDocument - blank URL string: throws 400")
    void uploadDocument_blankUrl_throws400() {
        assertThatThrownBy(() ->
                service.uploadDocument(null, "   ", "name", null,
                        DocumentCategory.GUIDELINES, DocumentModule.SHARED, null, principal))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    @DisplayName("uploadDocument - user not found: throws 404")
    void uploadDocument_userNotFound_throws404() {
        when(userRepository.getReferenceById(principal.getUserId()))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        assertThatThrownBy(() ->
            service.uploadDocument(null, "https://example.com", "name", null,
                DocumentCategory.GUIDELINES, DocumentModule.SHARED, null, principal))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("404");
    }

    @Test
    @DisplayName("uploadDocument - organization not found: throws 404")
    void uploadDocument_orgNotFound_throws404() {
        when(userRepository.getReferenceById(principal.getUserId())).thenReturn(user);
        when(organizationRepository.getReferenceById(principal.getOrganizationId()))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found"));

        assertThatThrownBy(() ->
                service.uploadDocument(null, "https://example.com", "name", null,
                        DocumentCategory.GUIDELINES, DocumentModule.SHARED, null, principal))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    @DisplayName("getDocuments - no filters: queries all org documents")
    void getDocuments_noFilters_callsFindAllByOrganizationId() {
        when(documentRepository.findAllByOrganizationId(orgId)).thenReturn(List.of());

        service.getDocuments(null, null, principal);

        verify(documentRepository).findAllByOrganizationId(orgId);
        verifyNoMoreInteractions(documentRepository);
    }

    @Test
    @DisplayName("getDocuments - category filter only: queries by category")
    void getDocuments_categoryOnly_callsFindByCategory() {
        when(documentRepository.findAllByOrganizationIdAndCategory(orgId, DocumentCategory.GUIDELINES))
                .thenReturn(List.of());

        service.getDocuments(DocumentCategory.GUIDELINES, null, principal);

        verify(documentRepository).findAllByOrganizationIdAndCategory(orgId, DocumentCategory.GUIDELINES);
        verifyNoMoreInteractions(documentRepository);
    }

    @Test
    @DisplayName("getDocuments - module filter only: queries by module")
    void getDocuments_moduleOnly_callsFindByModule() {
        when(documentRepository.findAllByOrganizationIdAndModule(orgId, DocumentModule.SHARED))
                .thenReturn(List.of());

        service.getDocuments(null, DocumentModule.SHARED, principal);

        verify(documentRepository).findAllByOrganizationIdAndModule(orgId, DocumentModule.SHARED);
        verifyNoMoreInteractions(documentRepository);
    }

    @Test
    @DisplayName("getDocuments - both filters: queries by category and module")
    void getDocuments_bothFilters_callsFindByCategoryAndModule() {
        DocumentModel doc = makeDoc(1L, orgId);
        when(documentRepository.findAllByOrganizationIdAndCategoryAndModule(
            orgId, DocumentCategory.GUIDELINES, DocumentModule.SHARED))
            .thenReturn(List.of(doc));

        when(documentMapper.toDto(any(DocumentModel.class)))
            .thenAnswer(invocation -> makeDto(invocation.getArgument(0)));

        service.getDocuments(DocumentCategory.GUIDELINES, DocumentModule.SHARED, principal);

        verify(documentRepository).findAllByOrganizationIdAndCategoryAndModule(
                orgId, DocumentCategory.GUIDELINES, DocumentModule.SHARED);
        verifyNoMoreInteractions(documentRepository);
    }

    @Test
    @DisplayName("getDocuments - result includes uploadedByName from user entity")
    void getDocuments_mapsToDTOs() {
        DocumentModel doc = makeDoc(1L, orgId);
        when(documentRepository.findAllByOrganizationId(orgId)).thenReturn(List.of(doc));

        when(documentMapper.toDto(any(DocumentModel.class)))
            .thenAnswer(invocation -> makeDto(invocation.getArgument(0)));

        List<DocumentDto> result = service.getDocuments(null, null, principal);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).uploadedByName()).isEqualTo("Test User");
    }


    @Test
    @DisplayName("downloadDocument - own document: returns readable resource")
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
    @DisplayName("downloadDocument - document belongs to another org: throws 403")
    void downloadDocument_wrongOrg_throws403() {
        DocumentModel doc = makeDoc(1L, UUID.randomUUID());
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        assertThatThrownBy(() -> service.downloadDocument(1L, principal))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    @Test
    @DisplayName("downloadDocument - document not found: throws 404")
    void downloadDocument_notFound_throws404() {
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.downloadDocument(99L, principal))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }


    @Test
    @DisplayName("deleteDocument - own document: removes from DB and deletes file from disk")
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
    @DisplayName("deleteDocument - external link without storage path: removes from DB without touching disk")
    void deleteDocument_externalLinkWithoutStoragePath_deletesRepoOnly() {
        DocumentModel doc = makeDoc(1L, orgId);
        doc.setExternalUrl("https://example.com/doc");
        doc.setStoragePath(null);
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        service.deleteDocument(1L, principal);

        verify(documentRepository).delete(doc);
    }

    @Test
    @DisplayName("deleteDocument - document belongs to another org: throws 403, repo not called")
    void deleteDocument_wrongOrg_throws403() {
        DocumentModel doc = makeDoc(1L, UUID.randomUUID());
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        assertThatThrownBy(() -> service.deleteDocument(1L, principal))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
        verify(documentRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteDocument - document not found: throws 404")
    void deleteDocument_notFound_throws404() {
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteDocument(99L, principal))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }
}
