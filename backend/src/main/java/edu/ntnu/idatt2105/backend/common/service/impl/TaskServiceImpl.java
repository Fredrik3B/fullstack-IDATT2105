package edu.ntnu.idatt2105.backend.common.service.impl;

import edu.ntnu.idatt2105.backend.common.dto.task.CreateTaskRequest;
import edu.ntnu.idatt2105.backend.common.dto.task.TaskResponse;
import edu.ntnu.idatt2105.backend.common.model.ChecklistModel;
import edu.ntnu.idatt2105.backend.common.model.TaskTemplate;
import edu.ntnu.idatt2105.backend.common.repository.ChecklistRepository;
import edu.ntnu.idatt2105.backend.common.repository.TaskTemplateRepository;
import edu.ntnu.idatt2105.backend.common.service.TaskService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TaskServiceImpl implements TaskService {

	private final TaskTemplateRepository taskTemplateRepository;
	private final ChecklistRepository checklistRepository;

	public TaskServiceImpl(TaskTemplateRepository taskTemplateRepository, ChecklistRepository checklistRepository) {
		this.taskTemplateRepository = taskTemplateRepository;
		this.checklistRepository = checklistRepository;
	}

	@Override
	public TaskResponse createTask(CreateTaskRequest request) {
		validateRequest(request);

		ChecklistModel checklist = checklistRepository.findById(request.checklistId())
			.orElseThrow(() -> new IllegalArgumentException("Checklist not found with id: " + request.checklistId()));

		TaskTemplate template = new TaskTemplate();
		template.setTitle(request.title().trim());
		template.setSectionTitle(trimToNull(request.sectionTitle()));
		template.setSectionType(request.sectionType());
		template.setUnit(trimToNull(request.unit()));
		template.setTargetMin(request.targetMin());
		template.setTargetMax(request.targetMax());
		template.setChecklist(checklist);
		template.setOrganisationId(checklist.getOrganization().getId());

		return toResponse(taskTemplateRepository.save(template));
	}

	@Override
	public List<TaskResponse> getAllTasks() {
		return taskTemplateRepository.findAll().stream()
			.map(this::toResponse)
			.toList();
	}

	@Override
	public TaskResponse getTaskById(Long taskId) {
		TaskTemplate template = taskTemplateRepository.findById(taskId)
			.orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));

		return toResponse(template);
	}

	@Override
	public void deleteTask(Long taskId) {
		if (!taskTemplateRepository.existsById(taskId)) {
			throw new IllegalArgumentException("Task not found with id: " + taskId);
		}
		taskTemplateRepository.deleteById(taskId);
	}

	private TaskResponse toResponse(TaskTemplate template) {
		return new TaskResponse(
			template.getId(),
			template.getTitle(),
			template.getSectionTitle(),
			template.getSectionType(),
			template.getUnit(),
			template.getTargetMin(),
			template.getTargetMax(),
			template.getChecklist() != null ? template.getChecklist().getId() : null
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
		if (request.targetMin() != null && request.targetMax() != null
			&& request.targetMin().compareTo(request.targetMax()) > 0) {
			throw new IllegalArgumentException("targetMin cannot be greater than targetMax.");
		}
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private String trimToNull(String value) {
		return hasText(value) ? value.trim() : null;
	}
}
