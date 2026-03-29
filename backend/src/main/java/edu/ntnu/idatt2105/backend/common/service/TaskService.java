package edu.ntnu.idatt2105.backend.common.service;

import java.util.List;

import edu.ntnu.idatt2105.backend.common.dto.task.CreateTaskRequest;
import edu.ntnu.idatt2105.backend.common.dto.task.TaskResponse;


public interface TaskService {

	TaskResponse createTask(CreateTaskRequest request);

	List<TaskResponse> getAllTasks();

	TaskResponse getTaskById(Long taskId);

	void deleteTask(Long taskId);
}
