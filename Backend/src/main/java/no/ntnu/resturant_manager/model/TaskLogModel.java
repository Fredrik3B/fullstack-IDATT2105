package no.ntnu.resturant_manager.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "task_logs")
public class TaskLogModel extends AuditableEntity {

	@Column(nullable = false)
	private boolean completed;

	@Column(length = 1000)
	private String notes;

	@Column(name = "completed_at", nullable = false)
	private LocalDateTime completedAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "organization_id", nullable = false)
	private OrganizationModel organization;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "task_id", nullable = false)
	private TaskModel task;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "completed_by_user_id", nullable = false)
	private UserModel completedBy;

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

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}

	public OrganizationModel getOrganization() {
		return organization;
	}

	public void setOrganization(OrganizationModel organization) {
		this.organization = organization;
	}

	public TaskModel getTask() {
		return task;
	}

	public void setTask(TaskModel task) {
		this.task = task;
	}

	public UserModel getCompletedBy() {
		return completedBy;
	}

	public void setCompletedBy(UserModel completedBy) {
		this.completedBy = completedBy;
	}
}
