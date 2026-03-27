package edu.ntnu.idatt2105.backend.common.service;

import edu.ntnu.idatt2105.backend.common.dto.CreateTaskRequest;
import edu.ntnu.idatt2105.backend.common.dto.TaskResponse;
import java.util.List;


public interface TaskService {

	TaskResponse createTask(CreateTaskRequest request);

	List<TaskResponse> getAllTasks();

	TaskResponse getTaskById(Long taskId);

	void deleteTask(Long taskId);
}
