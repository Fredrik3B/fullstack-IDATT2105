package no.ntnu.resturant_manager.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import no.ntnu.resturant_manager.model.enums.TemperatureZone;

@Entity
@Table(name = "temperature_logs")
public class TemperatureLogModel extends AuditableEntity {

	@Column(nullable = false, length = 120)
	private String location;

	@Column(nullable = false, precision = 5, scale = 2)
	private BigDecimal value;

	@Enumerated(EnumType.STRING)
	@Column(name = "temperature_zone", nullable = false, length = 30)
	private TemperatureZone temperatureZone;

	@Column(name = "minimum_allowed", precision = 5, scale = 2)
	private BigDecimal minimumAllowed;

	@Column(name = "maximum_allowed", precision = 5, scale = 2)
	private BigDecimal maximumAllowed;

	@Column(length = 100)
	private String notes;

	@Column(name = "recorded_at", nullable = false)
	private LocalDateTime recordedAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "organization_id", nullable = false)
	private OrganizationModel organization;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "recorded_by_user_id", nullable = false)
	private UserModel recordedBy;

	@PrePersist
	public void setDefaultRecordedAt() {
		if (recordedAt == null) {
			recordedAt = LocalDateTime.now();
		}
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public TemperatureZone getTemperatureZone() {
		return temperatureZone;
	}

	public void setTemperatureZone(TemperatureZone temperatureZone) {
		this.temperatureZone = temperatureZone;
	}

	public BigDecimal getMinimumAllowed() {
		return minimumAllowed;
	}

	public void setMinimumAllowed(BigDecimal minimumAllowed) {
		this.minimumAllowed = minimumAllowed;
	}

	public BigDecimal getMaximumAllowed() {
		return maximumAllowed;
	}

	public void setMaximumAllowed(BigDecimal maximumAllowed) {
		this.maximumAllowed = maximumAllowed;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public LocalDateTime getRecordedAt() {
		return recordedAt;
	}

	public void setRecordedAt(LocalDateTime recordedAt) {
		this.recordedAt = recordedAt;
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
