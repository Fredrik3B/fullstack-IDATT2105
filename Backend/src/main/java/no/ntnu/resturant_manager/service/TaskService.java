package no.ntnu.resturant_manager.service;

import java.util.List;

import no.ntnu.resturant_manager.dto.CreateTaskRequest;
import no.ntnu.resturant_manager.dto.TaskResponse;

public interface TaskService {

	TaskResponse createTask(CreateTaskRequest request);

	List<TaskResponse> getAllTasks();

	TaskResponse getTaskById(Long taskId);

	void deleteTask(Long taskId);
}
