package edu.ntnu.idatt2105.backend.common.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "activated_tasks")
public class TasksModel extends AuditableEntity {

	@Column(nullable = false)
	private boolean completed;

	@Column(nullable=false)
	private boolean flag;

	@Column(nullable = false)
	private boolean active;

	@Column(length = 255)
	private String meta;

	@Column(name = "completed_at", nullable = false)
	private LocalDateTime completedAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "task_id", nullable = false)
	private TasksModel task;

	@PrePersist
	public void setDefaultCompletedAt() {
		if (completedAt == null) {
			completedAt = LocalDateTime.now();
		}
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public boolean isFlagged() {
		return flag;
	}

	public void setFlag (boolean flag) {
		this.flag = flag;
	}
		
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}

	public TasksModel getTask() {
		return task;
	}

	public void setTask(TasksModel task) {
		this.task = task;
	}

	public String getMeta() {
		return meta;
	}

	public void setMeta(String meta) {
		this.meta = meta;
	}

}
