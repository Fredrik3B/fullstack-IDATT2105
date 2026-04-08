package edu.ntnu.idatt2105.backend.task.repository;

import edu.ntnu.idatt2105.backend.task.model.TasksModel;
import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TasksRepository extends JpaRepository<TasksModel, Long> {

	List<TasksModel> findAllByTaskTemplate_Id(Long taskTemplateId);

	List<TasksModel> findAllByChecklist_IdAndActiveTrue(Long checklistId);

	List<TasksModel> findAllByChecklist_Id(Long checklistId);

	Optional<TasksModel> findByIdAndChecklist_Id(Long activatedTaskId, Long checklistId);

	List<TasksModel> findAllByChecklist_IdAndPeriodKeyAndActiveTrue(Long checklistId, String periodKey);

	@Query("SELECT COUNT(t) FROM TasksModel t " +
			"WHERE t.checklist.organization.id = :orgId " +
			"AND t.endedAt BETWEEN :from AND :to " +
			"AND t.checklist.complianceArea = :area")
  int contTaskInPeriod(@Param("orgId") UUID orgId,
			@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
			@Param("area") ComplianceArea area);


	@Query("SELECT COUNT(t) FROM TasksModel t " +
			"WHERE t.checklist.organization.id = :orgId " +
			"AND t.completed = true " +
			"AND t.endedAt BETWEEN :from AND :to " +
			"AND t.checklist.complianceArea = :area")
	int countCompletedInPeriod(@Param("orgId") UUID orgId,
			@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
			@Param("area") ComplianceArea area);

	@Query("SELECT COUNT(t) FROM TasksModel t " +
			"WHERE t.checklist.organization.id = :orgId " +
			"AND t.completed = false " +
			"AND t.active = false " +
			"AND t.endedAt BETWEEN :from AND :to " +
			"AND t.checklist.complianceArea = :area")
	int countDeviatedInPeriod(@Param("orgId") UUID orgId,
			@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
			@Param("area") ComplianceArea area);

	@Query("SELECT t FROM TasksModel t " +
			"WHERE t.checklist.organization.id = :orgId " +
			"AND t.completed = false " +
			"AND t.active = false " +
			"AND t.endedAt BETWEEN :from AND :to ")
	List<TasksModel> findDeviatedTaskByOrgIdInPeriod(@Param("orgId") UUID orgId,
			@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("SELECT DISTINCT t FROM TasksModel t " +
			"JOIN FETCH t.checklist c " +
			"JOIN FETCH t.taskTemplate tt " +
			"WHERE c.organization.id = :orgId " +
			"AND t.endedAt BETWEEN :from AND :to")
	List<TasksModel> findAllByOrgIdInPeriodWithRelations(
		@Param("orgId") UUID orgId,
		@Param("from") LocalDateTime from,
		@Param("to") LocalDateTime to
	);
}
