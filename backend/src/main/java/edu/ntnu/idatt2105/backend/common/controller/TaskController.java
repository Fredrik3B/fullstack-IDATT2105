package edu.ntnu.idatt2105.backend.common.controller;

import edu.ntnu.idatt2105.backend.common.dto.CreateTaskRequest;
import edu.ntnu.idatt2105.backend.common.dto.TaskResponse;
import edu.ntnu.idatt2105.backend.common.service.TaskService;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/tasks")
public class TaskController {

	private final TaskService taskService;

	public TaskController(TaskService taskService) {
		this.taskService = taskService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TaskResponse createTask(@Valid @RequestBody CreateTaskRequest request) {
		return taskService.createTask(request);
	}

	@GetMapping
	public List<TaskResponse> getAllTasks() {
		return taskService.getAllTasks();
	}

	@GetMapping("/{taskId}")
	public TaskResponse getTaskById(@PathVariable Long taskId) {
		return taskService.getTaskById(taskId);
	}

	@DeleteMapping("/{taskId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteTask(@PathVariable Long taskId) {
		taskService.deleteTask(taskId);
	}
}
