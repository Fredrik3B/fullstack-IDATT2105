package edu.ntnu.idatt2105.backend.common.model;

import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(
	name = "task_period_states",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_task_period_state",
		columnNames = { "organization_id", "user_id", "task_id", "period_key" }
	)
)
public class TaskPeriodStateModel extends AuditableEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "organization_id", nullable = false)
	private OrganizationModel organization;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private UserModel user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "task_id", nullable = false)
	private TaskModel task;

	@Column(name = "period_key", nullable = false, length = 16)
	private String periodKey;

	@Column(name = "completed_at")
	private Instant completedAt;

	@Column(name = "flagged_at")
	private Instant flaggedAt;

	public OrganizationModel getOrganization() {
		return organization;
	}

	public void setOrganization(OrganizationModel organization) {
		this.organization = organization;
	}

	public UserModel getUser() {
		return user;
	}

	public void setUser(UserModel user) {
		this.user = user;
	}

	public TaskModel getTask() {
		return task;
	}

	public void setTask(TaskModel task) {
		this.task = task;
	}

	public String getPeriodKey() {
		return periodKey;
	}

	public void setPeriodKey(String periodKey) {
		this.periodKey = periodKey;
	}

	public Instant getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(Instant completedAt) {
		this.completedAt = completedAt;
	}

	public Instant getFlaggedAt() {
		return flaggedAt;
	}

	public void setFlaggedAt(Instant flaggedAt) {
		this.flaggedAt = flaggedAt;
	}
}

