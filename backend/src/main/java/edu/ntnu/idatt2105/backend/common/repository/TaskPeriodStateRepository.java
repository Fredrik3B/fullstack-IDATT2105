package edu.ntnu.idatt2105.backend.common.repository;

import edu.ntnu.idatt2105.backend.common.model.TaskPeriodStateModel;
import edu.ntnu.idatt2105.backend.common.model.TaskModel;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskPeriodStateRepository extends JpaRepository<TaskPeriodStateModel, Long> {

	Optional<TaskPeriodStateModel> findByOrganization_IdAndUser_IdAndTask_IdAndPeriodKey(
		UUID organizationId,
		UUID userId,
		Long taskId,
		String periodKey
	);

	List<TaskPeriodStateModel> findAllByOrganization_IdAndUser_IdAndTask_IdInAndPeriodKeyIn(
		UUID organizationId,
		UUID userId,
		Collection<Long> taskIds,
		Collection<String> periodKeys
	);

	void deleteAllByTaskIn(Collection<TaskModel> tasks);
}

