package edu.ntnu.idatt2105.backend.common.model;

import java.math.BigDecimal;
import java.util.UUID;

import edu.ntnu.idatt2105.backend.common.model.enums.SectionTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "template_tasks")
public class TaskTemplate extends AuditableEntity {

	@Column(nullable = false, length = 120)
	private String title;

	@Column(name = "section_title", length = 120)
	private String sectionTitle;

	@Enumerated(EnumType.STRING)
	@Column(name = "section_type", length = 30)
	private SectionTypes sectionType;

	@Column(length = 10)
	private String unit;

	@Column(name = "target_min", precision = 5, scale = 2)
	private BigDecimal targetMin;

	@Column(name = "target_max", precision = 5, scale = 2)
	private BigDecimal targetMax;

	@Column (nullable = false)
	private UUID organisationId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "checklist_id", nullable = false)
	private ChecklistModel checklist;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}


	public String getSectionTitle() {
		return sectionTitle;
	}

	public void setSectionTitle(String sectionTitle) {
		this.sectionTitle = sectionTitle;
	}


	public SectionTypes getSectionType() {
		return sectionType;
	}

	public void setSectionType(SectionTypes sectionType) {
		this.sectionType = sectionType;
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

	public ChecklistModel getChecklist() {
		return checklist;
	}

	public void setChecklist(ChecklistModel checklist) {
		this.checklist = checklist;
	}

	public UUID getOrganisationId() {
		return organisationId;
	}

	public void setOrganisationId(UUID organisationId) {
		this.organisationId = organisationId;
	}
}
