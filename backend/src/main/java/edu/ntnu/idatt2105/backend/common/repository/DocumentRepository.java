package edu.ntnu.idatt2105.backend.common.repository;

import edu.ntnu.idatt2105.backend.common.model.DocumentModel;
import edu.ntnu.idatt2105.backend.common.model.enums.DocumentCategory;
import edu.ntnu.idatt2105.backend.common.model.enums.DocumentModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<DocumentModel, Long> {

    List<DocumentModel> findAllByOrganizationId(Long organizationId);

    List<DocumentModel> findAllByOrganizationIdAndCategory(Long organizationId, DocumentCategory category);

    List<DocumentModel> findAllByOrganizationIdAndModule(Long organizationId, DocumentModule module);

    List<DocumentModel> findAllByOrganizationIdAndCategoryAndModule(Long organizationId, DocumentCategory category, DocumentModule module);
}
