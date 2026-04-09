package edu.ntnu.idatt2105.backend.checklist.model;

import edu.ntnu.idatt2105.backend.shared.model.AuditableEntity;
import edu.ntnu.idatt2105.backend.task.model.TaskTemplate;
import edu.ntnu.idatt2105.backend.checklist.model.enums.ChecklistFrequency;
import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * JPA entity representing a checklist belonging to an organisation.
 *
 * <p>A checklist groups a set of {@link edu.ntnu.idatt2105.backend.task.model.TaskTemplate}s
 * and tracks the current active period. The {@code activePeriodKey} advances each time the
 * checklist is submitted, enabling historical task records while always presenting a fresh
 * period on the workbench.
 */
@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "checklists")
public class ChecklistModel extends AuditableEntity {

	@Column(nullable = false, length = 120)
	private String name;

	@Column(length = 1000)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ChecklistFrequency frequency;

	@Enumerated(EnumType.STRING)
	@Column(name = "compliance_area", nullable = false, length = 30)
	private ComplianceArea complianceArea;

	@Column(name = "active_period_key", nullable = false, length = 16)
	private String activePeriodKey;

	@Column(name = "recurring", nullable = false)
	private boolean recurring = true;

	@Column(name = "displayed_on_workbench", nullable = false)
	private boolean displayedOnWorkbench = true;

	@Column(nullable = false)
	private boolean active = true;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "organization_id", nullable = false)
	private OrganizationModel organization;

	@ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST })
	@JoinTable(
		name = "checklist_task_templates",
		joinColumns = @JoinColumn(name = "checklist_id"),
		inverseJoinColumns = @JoinColumn(name = "task_template_id")
	)
	private Set<TaskTemplate> taskTemplates = new LinkedHashSet<>();

}
