package edu.ntnu.idatt2105.backend.common.repository;

import edu.ntnu.idatt2105.backend.document.model.DocumentModel;
import edu.ntnu.idatt2105.backend.common.model.enums.DocumentCategory;
import edu.ntnu.idatt2105.backend.common.model.enums.DocumentModule;
import edu.ntnu.idatt2105.backend.document.repository.DocumentRepository;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import edu.ntnu.idatt2105.backend.user.repository.OrganizationRepository;
import edu.ntnu.idatt2105.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("Document Repository")
class DocumentRepositoryTest {

    @Autowired
    private DocumentRepository repository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    private OrganizationModel orgA;
    private OrganizationModel orgB;
    private UserModel user;

    @BeforeEach
    void setUp() {
        orgA = new OrganizationModel();
        orgA.setName("Org A");
        orgA.setJoinCode("AAAA");
        organizationRepository.save(orgA);

        orgB = new OrganizationModel();
        orgB.setName("Org B");
        orgB.setJoinCode("BBBB");
        organizationRepository.save(orgB);

        user = new UserModel();
        user.setEmail("doctest@test.com");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setOrganization(orgA);
        userRepository.save(user);
    }

    private DocumentModel createDoc(OrganizationModel org, DocumentCategory category, DocumentModule module) {
        DocumentModel doc = new DocumentModel();
        doc.setName("Doc");
        doc.setCategory(category);
        doc.setModule(module);
        doc.setUploadedBy(user);
        doc.setOrganization(org);
        return repository.save(doc);
    }

    @Test
    @DisplayName("findAllByOrganizationId - returns only documents belonging to the given org")
    void findAllByOrganizationId_returnsOnlyOwnOrg() {
        createDoc(orgA, DocumentCategory.GUIDELINES, DocumentModule.SHARED);
        createDoc(orgA, DocumentCategory.TRAINING, DocumentModule.IC_FOOD);
        createDoc(orgB, DocumentCategory.GUIDELINES, DocumentModule.SHARED);

        List<DocumentModel> result = repository.findAllByOrganizationId(orgA.getId());

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(d -> d.getOrganization().getId().equals(orgA.getId()));
    }

    @Test
    @DisplayName("findAllByOrganizationId - org with no documents returns empty list")
    void findAllByOrganizationId_emptyOrg_returnsEmpty() {
        List<DocumentModel> result = repository.findAllByOrganizationId(orgB.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAllByOrganizationIdAndCategory - returns only documents matching the category")
    void findAllByOrganizationIdAndCategory_filtersCategory() {
        createDoc(orgA, DocumentCategory.GUIDELINES, DocumentModule.SHARED);
        createDoc(orgA, DocumentCategory.TRAINING, DocumentModule.SHARED);

        List<DocumentModel> result = repository.findAllByOrganizationIdAndCategory(
                orgA.getId(), DocumentCategory.GUIDELINES);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo(DocumentCategory.GUIDELINES);
    }

    @Test
    @DisplayName("findAllByOrganizationIdAndModule - returns only documents matching the module")
    void findAllByOrganizationIdAndModule_filtersModule() {
        createDoc(orgA, DocumentCategory.GUIDELINES, DocumentModule.SHARED);
        createDoc(orgA, DocumentCategory.GUIDELINES, DocumentModule.IC_FOOD);

        List<DocumentModel> result = repository.findAllByOrganizationIdAndModule(
                orgA.getId(), DocumentModule.SHARED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModule()).isEqualTo(DocumentModule.SHARED);
    }

    @Test
    @DisplayName("findAllByOrganizationIdAndCategoryAndModule - returns only exact category+module match")
    void findAllByOrganizationIdAndCategoryAndModule_filtersBoth() {
        createDoc(orgA, DocumentCategory.GUIDELINES, DocumentModule.SHARED);
        createDoc(orgA, DocumentCategory.GUIDELINES, DocumentModule.IC_FOOD);
        createDoc(orgA, DocumentCategory.TRAINING, DocumentModule.SHARED);

        List<DocumentModel> result = repository.findAllByOrganizationIdAndCategoryAndModule(
                orgA.getId(), DocumentCategory.GUIDELINES, DocumentModule.SHARED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo(DocumentCategory.GUIDELINES);
        assertThat(result.get(0).getModule()).isEqualTo(DocumentModule.SHARED);
    }

    @Test
    @DisplayName("findAllByOrganizationIdAndCategory - does not return documents from other orgs")
    void findAllByOrganizationIdAndCategory_doesNotReturnOtherOrg() {
        createDoc(orgA, DocumentCategory.GUIDELINES, DocumentModule.SHARED);
        createDoc(orgB, DocumentCategory.GUIDELINES, DocumentModule.SHARED);

        List<DocumentModel> result = repository.findAllByOrganizationIdAndCategory(
                orgA.getId(), DocumentCategory.GUIDELINES);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrganization().getId()).isEqualTo(orgA.getId());
    }
}
