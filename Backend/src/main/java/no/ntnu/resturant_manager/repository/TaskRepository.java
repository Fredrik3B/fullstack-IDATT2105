package no.ntnu.resturant_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import no.ntnu.resturant_manager.model.TaskModel;

public interface TaskRepository extends JpaRepository<TaskModel, Long> {
}
