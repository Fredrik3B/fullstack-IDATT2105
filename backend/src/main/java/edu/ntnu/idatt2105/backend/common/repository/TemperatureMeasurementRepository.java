package edu.ntnu.idatt2105.backend.common.repository;

import edu.ntnu.idatt2105.backend.common.model.TemperatureMeasurementModel;
import edu.ntnu.idatt2105.backend.common.model.TasksModel;
import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemperatureMeasurementRepository extends JpaRepository<TemperatureMeasurementModel, Long> {

	List<TemperatureMeasurementModel> findAllByOrganization_IdAndComplianceAreaAndMeasuredAtBetweenOrderByMeasuredAtDesc(
		UUID organizationId,
		ComplianceArea complianceArea,
		LocalDateTime from,
		LocalDateTime to
	);

	List<TemperatureMeasurementModel> findAllByTask_IdInOrderByMeasuredAtDesc(Collection<Long> taskIds);

	void deleteAllByTaskIn(Collection<TasksModel> tasks);
}
