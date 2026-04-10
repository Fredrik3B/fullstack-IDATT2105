package edu.ntnu.idatt2105.backend.report.repository;

import edu.ntnu.idatt2105.backend.report.model.DeviationReportModel;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository for {@link DeviationReportModel}.
 */
public interface DeviationReportRepository extends JpaRepository<DeviationReportModel, UUID> {

  List<DeviationReportModel> findAllByOrganizationId(UUID organizationId);
}
