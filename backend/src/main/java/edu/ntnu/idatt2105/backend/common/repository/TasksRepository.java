package edu.ntnu.idatt2105.backend.common.repository;

import edu.ntnu.idatt2105.backend.common.model.TasksModel;
import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import java.time.LocalDate;
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

	List<TasksModel> findAllByTaskTemplate_IdInAndActiveTrue(Collection<Long> taskTemplateIds);

	Optional<TasksModel> findByIdAndChecklist_Id(Long activatedTaskId, Long checklistId);

	Optional<TasksModel> findByChecklist_IdAndTaskTemplate_IdAndPeriodKey(
		Long checklistId,
		Long taskTemplateId,
		String periodKey
	);

	List<TasksModel> findAllByChecklist_IdAndPeriodKeyAndActiveTrue(Long checklistId, String periodKey);



	@Query("SELECT COUNT(t) FROM TasksModel t " +
			"WHERE t.checklist.organization.id = :orgId " +
			"AND t.endedAt BETWEEN :from AND :to " +
			"AND t.checklist.complianceArea = :area")
  int contTaskInPeriod(@Param("orgId") UUID orgId,
			@Param("from") LocalDate from, @Param("to") LocalDate to,
			@Param("area") ComplianceArea area);


	@Query("SELECT COUNT(t) FROM TasksModel t " +
			"WHERE t.checklist.organization.id = :orgId " +
			"AND t.completed = true " +
			"AND t.endedAt BETWEEN :from AND :to " +
			"AND t.checklist.complianceArea = :area")
	int countCompletedInPeriod(@Param("orgId") UUID orgId,
			@Param("from") LocalDate from, @Param("to") LocalDate to,
			@Param("area") ComplianceArea area);

	@Query("SELECT COUNT(t) FROM TasksModel t " +
			"WHERE t.checklist.organization.id = :orgId " +
			"AND t.completed = false " +
			"AND t.active = false " +
			"AND t.endedAt BETWEEN :from AND :to " +
			"AND t.checklist.complianceArea = :area")
	int countFlaggedInPeriod(@Param("orgId") UUID orgId,
			@Param("from") LocalDate from, @Param("to") LocalDate to,
			@Param("area") ComplianceArea area);
}
