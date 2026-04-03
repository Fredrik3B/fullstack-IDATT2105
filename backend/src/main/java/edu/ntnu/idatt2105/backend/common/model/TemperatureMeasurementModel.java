package edu.ntnu.idatt2105.backend.common.model;

import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "temperature_measurements")
public class TemperatureMeasurementModel extends AuditableEntity {

	@Enumerated(EnumType.STRING)
	@Column(name = "compliance_area", nullable = false, length = 30)
	private ComplianceArea complianceArea;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "checklist_id", nullable = false)
	private ChecklistModel checklist;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "task_id", nullable = false)
	private TasksModel task;

	@Column(name = "period_key", length = 16)
	private String periodKey;

	@Column(name = "value_c", nullable = false, precision = 6, scale = 2)
	private BigDecimal valueC;

	@Column(name = "measured_at", nullable = false)
	private Instant measuredAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "organization_id", nullable = false)
	private OrganizationModel organization;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "recorded_by_user_id", nullable = false)
	private UserModel recordedBy;

	@PrePersist
	public void setDefaultMeasuredAt() {
		if (measuredAt == null) {
			measuredAt = Instant.now();
		}
	}

	public ComplianceArea getComplianceArea() {
		return complianceArea;
	}

	public void setComplianceArea(ComplianceArea complianceArea) {
		this.complianceArea = complianceArea;
	}

	public ChecklistModel getChecklist() {
		return checklist;
	}

	public void setChecklist(ChecklistModel checklist) {
		this.checklist = checklist;
	}

	public TasksModel getTask() {
		return task;
	}

	public void setTask(TasksModel task) {
		this.task = task;
	}

	public String getPeriodKey() {
		return periodKey;
	}

	public void setPeriodKey(String periodKey) {
		this.periodKey = periodKey;
	}

	public BigDecimal getValueC() {
		return valueC;
	}

	public void setValueC(BigDecimal valueC) {
		this.valueC = valueC;
	}

	public Instant getMeasuredAt() {
		return measuredAt;
	}

	public void setMeasuredAt(Instant measuredAt) {
		this.measuredAt = measuredAt;
	}

	public OrganizationModel getOrganization() {
		return organization;
	}

	public void setOrganization(OrganizationModel organization) {
		this.organization = organization;
	}

	public UserModel getRecordedBy() {
		return recordedBy;
	}

	public void setRecordedBy(UserModel recordedBy) {
		this.recordedBy = recordedBy;
	}
}
