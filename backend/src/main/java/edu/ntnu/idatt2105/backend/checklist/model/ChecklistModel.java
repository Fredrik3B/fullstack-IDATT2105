package edu.ntnu.idatt2105.backend.checklist.model;

import edu.ntnu.idatt2105.backend.shared.model.AuditableEntity;
import edu.ntnu.idatt2105.backend.task.model.TaskTemplate;
import edu.ntnu.idatt2105.backend.common.model.enums.ChecklistFrequency;
import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
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


@Entity
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ChecklistFrequency getFrequency() {
		return frequency;
	}

	public void setFrequency(ChecklistFrequency frequency) {
		this.frequency = frequency;
	}

	public ComplianceArea getComplianceArea() {
		return complianceArea;
	}

	public void setComplianceArea(ComplianceArea complianceArea) {
		this.complianceArea = complianceArea;
	}

	public String getActivePeriodKey() {
		return activePeriodKey;
	}

	public void setActivePeriodKey(String activePeriodKey) {
		this.activePeriodKey = activePeriodKey;
	}

	public boolean isRecurring() {
		return recurring;
	}

	public void setRecurring(boolean recurring) {
		this.recurring = recurring;
	}

	public boolean isDisplayedOnWorkbench() {
		return displayedOnWorkbench;
	}

	public void setDisplayedOnWorkbench(boolean displayedOnWorkbench) {
		this.displayedOnWorkbench = displayedOnWorkbench;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public OrganizationModel getOrganization() {
		return organization;
	}

	public void setOrganization(OrganizationModel organization) {
		this.organization = organization;
	}

	public Set<TaskTemplate> getTaskTemplates() {
		return taskTemplates;
	}

	public void setTaskTemplates(Set<TaskTemplate> taskTemplates) {
		this.taskTemplates = taskTemplates;
	}
}
