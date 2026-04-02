package edu.ntnu.idatt2105.backend.common.repository;

import edu.ntnu.idatt2105.backend.common.model.TasksModel;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TasksRepository extends JpaRepository<TasksModel, Long> {

	List<TasksModel> findAllByChecklist_IdAndActiveTrue(Long checklistId);

	List<TasksModel> findAllByChecklist_Id(Long checklistId);

	List<TasksModel> findAllByTaskTemplate_IdInAndActiveTrue(Collection<Long> taskTemplateIds);

	Optional<TasksModel> findByIdAndChecklist_Id(Long activatedTaskId, Long checklistId);

	Optional<TasksModel> findByChecklist_IdAndTaskTemplate_IdAndPeriodKey(
		Long checklistId,
		Long taskTemplateId,
		String periodKey
	);

	List<TasksModel> findAllByChecklist_IdAndPeriodKeyAndActiveTrue(Long checklistId, String periodKey);
}
