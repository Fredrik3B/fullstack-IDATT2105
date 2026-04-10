package edu.ntnu.idatt2105.backend.task.repository;

import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.task.model.TaskTemplate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


/**
 * JPA repository for {@link TaskTemplate}.
 *
 * <p>Custom queries order results by section type then title so the task list
 * is presented consistently in the checklist builder UI.
 */
public interface TaskTemplateRepository extends JpaRepository<TaskTemplate, Long> {

  List<TaskTemplate> findAllByOrganisationIdAndComplianceAreaOrderBySectionTypeAscTitleAsc(
      UUID organisationId,
      ComplianceArea complianceArea
  );

  List<TaskTemplate> findAllByTemperatureZone_Id(Long temperatureZoneId);

  @Query("""
      	select t from TaskTemplate t
      	where t.id in :ids
      	  and t.organisationId = :organisationId
      	  and t.complianceArea = :complianceArea
      	order by t.sectionType asc, t.title asc
      """)
  List<TaskTemplate> findAllByIdInAndOrganisationIdAndComplianceAreaOrdered(
      @Param("ids") Collection<Long> ids,
      @Param("organisationId") UUID organisationId,
      @Param("complianceArea") ComplianceArea complianceArea
  );
}
