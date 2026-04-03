package edu.ntnu.idatt2105.backend.common.service;

import java.util.List;

import edu.ntnu.idatt2105.backend.common.dto.task.CreateTaskRequest;
import edu.ntnu.idatt2105.backend.common.dto.task.TaskResponse;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;


public interface TaskService {

	TaskResponse createTask(CreateTaskRequest request, JwtAuthenticatedPrincipal principal);

	List<TaskResponse> getAllTasks(IcModule module, JwtAuthenticatedPrincipal principal);

	TaskResponse getTaskById(Long taskId, JwtAuthenticatedPrincipal principal);

	void deleteTask(Long taskId, JwtAuthenticatedPrincipal principal);
}
