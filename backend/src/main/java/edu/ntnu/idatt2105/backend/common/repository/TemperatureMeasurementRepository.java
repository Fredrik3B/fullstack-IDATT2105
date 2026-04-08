package edu.ntnu.idatt2105.backend.common.repository;

import edu.ntnu.idatt2105.backend.common.model.TemperatureMeasurementModel;
import edu.ntnu.idatt2105.backend.common.model.TasksModel;
import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TemperatureMeasurementRepository extends JpaRepository<TemperatureMeasurementModel, Long> {

	List<TemperatureMeasurementModel> findAllByOrganization_IdAndComplianceAreaAndMeasuredAtBetweenOrderByMeasuredAtDesc(
		UUID organizationId,
		ComplianceArea complianceArea,
		LocalDateTime from,
		LocalDateTime to
	);

	List<TemperatureMeasurementModel> findAllByTask_IdInOrderByMeasuredAtDesc(Collection<Long> taskIds);

	boolean existsByTask_IdAndPeriodKey(Long taskId, String periodKey);

	void deleteAllByTaskIn(Collection<TasksModel> tasks);


	@Query("SELECT COUNT(m) FROM TemperatureMeasurementModel m " +
			"WHERE m.organization.id = :orgId " +
			"AND m.measuredAt BETWEEN :from AND :to ")
	int countReadingsInPeriod(UUID orgId, LocalDateTime from, LocalDateTime to);

	@Query("SELECT COUNT(m) FROM TemperatureMeasurementModel m " +
			"WHERE m.organization.id = :orgId " +
			"AND m.measuredAt BETWEEN :from AND :to " +
			"AND (m.valueC < m.task.taskTemplate.targetMin OR m.valueC > m.task.taskTemplate.targetMax)")
	int countOutOfRangeInPeriod(UUID orgId, LocalDateTime from, LocalDateTime to);

	@Query("SELECT m FROM TemperatureMeasurementModel m " +
			"JOIN FETCH m.task t " +
			"JOIN FETCH t.taskTemplate " +
			"JOIN FETCH m.recordedBy " +
			"WHERE m.organization.id = :orgId " +
			"AND m.measuredAt BETWEEN :from AND :to " +
			"ORDER BY m.measuredAt")
	List<TemperatureMeasurementModel> findByOrgAndPeriod(@Param("orgId") UUID orgId,
			@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
