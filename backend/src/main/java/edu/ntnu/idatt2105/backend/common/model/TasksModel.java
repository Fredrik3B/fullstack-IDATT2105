package edu.ntnu.idatt2105.backend.common.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
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

	@Column(name = "ended_at")
	private LocalDateTime endedAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "checklist_id", nullable = false)
	private ChecklistModel checklist;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "task_template_id", nullable = false)
	private TaskTemplate taskTemplate;
}
