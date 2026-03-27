package edu.ntnu.idatt2105.backend.common.repository;

import edu.ntnu.idatt2105.backend.common.model.TaskLogModel;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TaskLogRepository extends JpaRepository<TaskLogModel, Long> {
}
