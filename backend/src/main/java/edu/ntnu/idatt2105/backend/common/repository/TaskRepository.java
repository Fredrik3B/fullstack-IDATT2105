package edu.ntnu.idatt2105.backend.common.repository;

import edu.ntnu.idatt2105.backend.common.model.TaskModel;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;


public interface TaskRepository extends JpaRepository<TaskModel, Long> {
	@Query("""
		select t from TaskModel t
		where t.active = true and t.checklist.id in :checklistIds
		order by t.checklist.id asc, coalesce(t.sectionOrderIndex, 0) asc, t.orderIndex asc
	""")
	List<TaskModel> findActiveByChecklistIdsOrdered(@Param("checklistIds") Collection<Long> checklistIds);

	List<TaskModel> findAllByChecklist_IdAndActiveTrue(Long checklistId);
}
