package edu.ntnu.idatt2105.backend.common.repository;

import edu.ntnu.idatt2105.backend.common.model.DocumentModel;
import edu.ntnu.idatt2105.backend.common.model.enums.DocumentCategory;
import edu.ntnu.idatt2105.backend.common.model.enums.DocumentModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<DocumentModel, Long> {

    List<DocumentModel> findAllByOrganizationId(UUID organizationId);

    List<DocumentModel> findAllByOrganizationIdAndCategory(UUID organizationId, DocumentCategory category);

    List<DocumentModel> findAllByOrganizationIdAndModule(UUID organizationId, DocumentModule module);

    List<DocumentModel> findAllByOrganizationIdAndCategoryAndModule(UUID organizationId, DocumentCategory category, DocumentModule module);
}
