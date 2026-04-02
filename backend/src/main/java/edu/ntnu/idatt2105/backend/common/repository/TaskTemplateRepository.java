package edu.ntnu.idatt2105.backend.common.repository;

import edu.ntnu.idatt2105.backend.common.model.TaskTemplate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface TaskTemplateRepository extends JpaRepository<TaskTemplate, Long> {
	@Query("""
		select t from TaskTemplate t
		where t.checklist.id in :checklistIds
		order by t.checklist.id asc, coalesce(t.sectionTitle, '') asc, t.id asc
	""")
	List<TaskTemplate> findActiveByChecklistIdsOrdered(@Param("checklistIds") Collection<Long> checklistIds);

	@Query("""
		select t from TaskTemplate t
		where t.checklist.id = :checklistId
		order by coalesce(t.sectionTitle, '') asc, t.id asc
	""")
	List<TaskTemplate> findAllByChecklist_IdAndActiveTrue(@Param("checklistId") Long checklistId);

	List<TaskTemplate> findAllByChecklist_IdOrderBySectionTitleAscIdAsc(Long checklistId);
}
