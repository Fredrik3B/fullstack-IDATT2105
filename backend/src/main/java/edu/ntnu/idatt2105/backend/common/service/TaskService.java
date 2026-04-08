package edu.ntnu.idatt2105.backend.common.service;

import java.util.List;

import edu.ntnu.idatt2105.backend.task.dto.CreateTaskRequest;
import edu.ntnu.idatt2105.backend.task.dto.TaskResponse;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;


public interface TaskService {

	TaskResponse createTask(CreateTaskRequest request, JwtAuthenticatedPrincipal principal);

	TaskResponse updateTask(Long taskId, CreateTaskRequest request, JwtAuthenticatedPrincipal principal);

	List<TaskResponse> getAllTasks(IcModule module, JwtAuthenticatedPrincipal principal);

	TaskResponse getTaskById(Long taskId, JwtAuthenticatedPrincipal principal);

	void deleteTask(Long taskId, JwtAuthenticatedPrincipal principal);
}
