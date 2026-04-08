package edu.ntnu.idatt2105.backend.task.controller;

import edu.ntnu.idatt2105.backend.task.dto.CreateTaskRequest;
import edu.ntnu.idatt2105.backend.task.dto.TaskResponse;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.common.service.TaskService;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Tasks", description = "Create, fetch, and delete tasks")
@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/tasks")
public class TaskController {

	private final TaskService taskService;

	public TaskController(TaskService taskService) {
		this.taskService = taskService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	@Operation(summary = "Create a task")
	@ApiResponse(responseCode = "201", description = "Task created")
	public TaskResponse createTask(@Valid @RequestBody CreateTaskRequest request, Authentication auth) {
		return taskService.createTask(request, JwtAuthenticatedPrincipal.from(auth));
	}

	@PutMapping("/{taskId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	@Operation(summary = "Update a task")
	@ApiResponse(responseCode = "200", description = "Task updated")
	public TaskResponse updateTask(
		@PathVariable Long taskId,
		@Valid @RequestBody CreateTaskRequest request,
		Authentication auth
	) {
		return taskService.updateTask(taskId, request, JwtAuthenticatedPrincipal.from(auth));
	}

	@GetMapping
	@Operation(summary = "Fetch all tasks")
	@ApiResponse(responseCode = "200", description = "Tasks returned")
	public List<TaskResponse> getAllTasks(@RequestParam IcModule module, Authentication auth) {
		return taskService.getAllTasks(module, JwtAuthenticatedPrincipal.from(auth));
	}

	@GetMapping("/{taskId}")
	@Operation(summary = "Fetch a task by id")
	@ApiResponse(responseCode = "200", description = "Task returned")
	public TaskResponse getTaskById(@PathVariable Long taskId, Authentication auth) {
		return taskService.getTaskById(taskId, JwtAuthenticatedPrincipal.from(auth));
	}

	@DeleteMapping("/{taskId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	@Operation(summary = "Delete a task")
	@ApiResponse(responseCode = "204", description = "Task deleted")
	public void deleteTask(@PathVariable Long taskId, Authentication auth) {
		taskService.deleteTask(taskId, JwtAuthenticatedPrincipal.from(auth));
	}
}
