package edu.ntnu.idatt2105.backend.common.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "activated_tasks")
public class TasksModel extends AuditableEntity {

	@Column(nullable = false)
	private boolean completed;

	@Column(nullable = false)
	private boolean flagged;

	@Column(nullable = false)
	private boolean active = true;

	@Column(length = 255)
	private String meta;

	@Column(name = "period_key", nullable = false, length = 16)
	private String periodKey;

	@Column(name = "completed_at")
	private LocalDateTime completedAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "checklist_id", nullable = false)
	private ChecklistModel checklist;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "task_template_id", nullable = false)
	private TaskTemplate taskTemplate;

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public boolean isFlagged() {
		return flagged;
	}

	public void setFlagged(boolean flagged) {
		this.flagged = flagged;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getMeta() {
		return meta;
	}

	public void setMeta(String meta) {
		this.meta = meta;
	}

	public String getPeriodKey() {
		return periodKey;
	}

	public void setPeriodKey(String periodKey) {
		this.periodKey = periodKey;
	}

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}

	public ChecklistModel getChecklist() {
		return checklist;
	}

	public void setChecklist(ChecklistModel checklist) {
		this.checklist = checklist;
	}

	public TaskTemplate getTaskTemplate() {
		return taskTemplate;
	}

	public void setTaskTemplate(TaskTemplate taskTemplate) {
		this.taskTemplate = taskTemplate;
	}
}
