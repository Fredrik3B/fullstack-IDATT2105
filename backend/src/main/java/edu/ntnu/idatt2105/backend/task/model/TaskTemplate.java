package edu.ntnu.idatt2105.backend.task.model;

import edu.ntnu.idatt2105.backend.temperature.model.TemperatureZoneModel;
import edu.ntnu.idatt2105.backend.shared.model.AuditableEntity;
import java.math.BigDecimal;
import java.util.UUID;

import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.checklist.model.enums.SectionTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// implement correct relations
@Entity
@Table(name = "template_tasks")
public class TaskTemplate extends AuditableEntity {

	@Column(nullable = false, length = 120)
	private String title;

	@Enumerated(EnumType.STRING)
	@Column(name = "section_type", nullable = false, length = 30)
	private SectionTypes sectionType;

	@Enumerated(EnumType.STRING)
	@Column(name = "compliance_area", nullable = false, length = 30)
	private ComplianceArea complianceArea;

	@Column(length = 10)
	private String unit;

	@Column(length = 255)
	private String meta;

	@Column(name = "target_min", precision = 5, scale = 2)
	private BigDecimal targetMin;

	@Column(name = "target_max", precision = 5, scale = 2)
	private BigDecimal targetMax;

	@Column (nullable = false)
	private UUID organisationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "temperature_zone_id")
	private TemperatureZoneModel temperatureZone;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public SectionTypes getSectionType() {
		return sectionType;
	}

	public void setSectionType(SectionTypes sectionType) {
		this.sectionType = sectionType;
	}

	public ComplianceArea getComplianceArea() {
		return complianceArea;
	}

	public void setComplianceArea(ComplianceArea complianceArea) {
		this.complianceArea = complianceArea;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getMeta() {
		return meta;
	}

	public void setMeta(String meta) {
		this.meta = meta;
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

	public UUID getOrganisationId() {
		return organisationId;
	}

	public void setOrganisationId(UUID organisationId) {
		this.organisationId = organisationId;
	}

	public TemperatureZoneModel getTemperatureZone() {
		return temperatureZone;
	}

	public void setTemperatureZone(TemperatureZoneModel temperatureZone) {
		this.temperatureZone = temperatureZone;
	}
}
