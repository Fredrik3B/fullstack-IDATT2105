package edu.ntnu.idatt2105.backend.common.service.impl;

import edu.ntnu.idatt2105.backend.common.dto.CreateTaskRequest;
import edu.ntnu.idatt2105.backend.common.dto.TaskResponse;
import edu.ntnu.idatt2105.backend.common.model.ChecklistModel;
import edu.ntnu.idatt2105.backend.common.model.TaskModel;
import edu.ntnu.idatt2105.backend.common.repository.ChecklistRepository;
import edu.ntnu.idatt2105.backend.common.repository.TaskRepository;
import edu.ntnu.idatt2105.backend.common.service.TaskService;
import java.util.List;

import org.springframework.stereotype.Service;



@Service
public class TaskServiceImpl implements TaskService {

	private final TaskRepository taskRepository;
	private final ChecklistRepository checklistRepository;

	public TaskServiceImpl(TaskRepository taskRepository, ChecklistRepository checklistRepository) {
		this.taskRepository = taskRepository;
		this.checklistRepository = checklistRepository;
	}

	@Override
	public TaskResponse createTask(CreateTaskRequest request) {
		validateRequest(request);

		ChecklistModel checklist = checklistRepository.findById(request.checklistId())
			.orElseThrow(() -> new IllegalArgumentException("Checklist not found with id: " + request.checklistId()));

		TaskModel task = new TaskModel();
		task.setTitle(request.title().trim());
		task.setDescription(trimToNull(request.description()));
		Integer orderIndex = request.orderIndex();
		task.setOrderIndex(orderIndex != null ? orderIndex : 0);
		task.setRequiredTask(request.requiredTask() == null || request.requiredTask());
		task.setActive(request.active() == null || request.active());
		task.setOrganisationId(request.organisationId());
		task.setChecklist(checklist);

		return toResponse(taskRepository.save(task));
	}

	@Override
	public List<TaskResponse> getAllTasks() {
		return taskRepository.findAll().stream()
			.map(this::toResponse)
			.toList();
	}

	@Override
	public TaskResponse getTaskById(Long taskId) {
		TaskModel task = taskRepository.findById(taskId)
			.orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));

		return toResponse(task);
	}

	@Override
	public void deleteTask(Long taskId) {
		if (!taskRepository.existsById(taskId)) {
			throw new IllegalArgumentException("Task not found with id: " + taskId);
		}
		taskRepository.deleteById(taskId);
	}

	private TaskResponse toResponse(TaskModel task) {
		return new TaskResponse(
			task.getId(),
			task.getTitle(),
			task.getDescription(),
			task.getOrderIndex(),
			task.isRequiredTask(),
			task.isActive(),
			task.getChecklist() != null ? task.getChecklist().getId() : null
		);
	}

	private void validateRequest(CreateTaskRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Task request cannot be null.");
		}
		if (!hasText(request.title())) {
			throw new IllegalArgumentException("Task title is required.");
		}
		if (request.checklistId() == null) {
			throw new IllegalArgumentException("Checklist id is required.");
		}
		if (request.orderIndex() != null && request.orderIndex() < 0) {
			throw new IllegalArgumentException("Order index cannot be negative.");
		}
		if (request.organisationId() == null) {
			throw new IllegalArgumentException("Organisation id is required.");
		}
		if (request.organisationId() < 1) {
			throw new IllegalArgumentException("Organisation id must be greater than 0.");
		}
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private String trimToNull(String value) {
		return hasText(value) ? value.trim() : null;
	}
}
