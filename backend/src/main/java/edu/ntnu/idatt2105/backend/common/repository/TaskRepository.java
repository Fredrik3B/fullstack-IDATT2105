package edu.ntnu.idatt2105.backend.common.repository;

import edu.ntnu.idatt2105.backend.common.model.TaskModel;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TaskRepository extends JpaRepository<TaskModel, Long> {
}
