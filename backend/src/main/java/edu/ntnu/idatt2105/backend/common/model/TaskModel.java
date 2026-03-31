package edu.ntnu.idatt2105.backend.common.model;

import edu.ntnu.idatt2105.backend.common.model.enums.ChecklistTaskType;
import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tasks")
public class TaskModel extends AuditableEntity {

	@Column(nullable = false, length = 120)
	private String title;

	@Column(length = 255)
	private String description;

	@Column(name = "order_index", nullable = false)
	private int orderIndex;

	@Column(name = "section_title", length = 120)
	private String sectionTitle;

	@Column(name = "section_order_index")
	private Integer sectionOrderIndex;

	@Enumerated(EnumType.STRING)
	@Column(name = "task_type", length = 30)
	private ChecklistTaskType taskType;

	@Column(length = 10)
	private String unit;

	@Column(name = "target_min", precision = 5, scale = 2)
	private BigDecimal targetMin;

	@Column(name = "target_max", precision = 5, scale = 2)
	private BigDecimal targetMax;

	@Column(nullable = false)
	private boolean requiredTask = true;

	@Column(nullable = false)
	private boolean active = true;

	@Column (nullable = false)
	private int organisationId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "checklist_id", nullable = false)
	private ChecklistModel checklist;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getOrderIndex() {
		return orderIndex;
	}

	public void setOrderIndex(int orderIndex) {
		this.orderIndex = orderIndex;
	}

	public String getSectionTitle() {
		return sectionTitle;
	}

	public void setSectionTitle(String sectionTitle) {
		this.sectionTitle = sectionTitle;
	}

	public Integer getSectionOrderIndex() {
		return sectionOrderIndex;
	}

	public void setSectionOrderIndex(Integer sectionOrderIndex) {
		this.sectionOrderIndex = sectionOrderIndex;
	}

	public ChecklistTaskType getTaskType() {
		return taskType;
	}

	public void setTaskType(ChecklistTaskType taskType) {
		this.taskType = taskType;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public BigDecimal getTargetMin() {
		return targetMin;
	}

	public void setTargetMin(BigDecimal targetMin) {
		this.targetMin = targetMin;
	}

	public BigDecimal getTargetMax() {
		return targetMax;
	}

	public void setTargetMax(BigDecimal targetMax) {
		this.targetMax = targetMax;
	}

	public boolean isRequiredTask() {
		return requiredTask;
	}

	public void setRequiredTask(boolean requiredTask) {
		this.requiredTask = requiredTask;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public ChecklistModel getChecklist() {
		return checklist;
	}

	public void setChecklist(ChecklistModel checklist) {
		this.checklist = checklist;
	}

	public int getOrganisationId() {
		return organisationId;
	}

	public void setOrganisationId(int organisationId) {
		this.organisationId = organisationId;
	}
}
