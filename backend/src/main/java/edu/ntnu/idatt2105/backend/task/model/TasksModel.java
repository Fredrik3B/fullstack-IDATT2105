package edu.ntnu.idatt2105.backend.task.model;

import edu.ntnu.idatt2105.backend.checklist.model.ChecklistModel;
import edu.ntnu.idatt2105.backend.shared.model.AuditableEntity;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity representing an activated instance of a {@link TaskTemplate} for a specific
 * checklist period.
 *
 * <p>A new instance is created by {@link edu.ntnu.idatt2105.backend.checklist.service.ChecklistService}
 * when a checklist is first loaded for a period. The instance tracks whether the task
 * was completed ({@code completed}), flagged as deviating ({@code flagged}), or
 * still active in the current period ({@code active}).
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
