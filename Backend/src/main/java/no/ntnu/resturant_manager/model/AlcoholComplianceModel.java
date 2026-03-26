package no.ntnu.resturant_manager.model;

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
import no.ntnu.resturant_manager.model.enums.AlcoholComplianceType;

@Entity
@Table(name = "alcohol_compliance_records")
public class AlcoholComplianceModel extends AuditableEntity {

	@Enumerated(EnumType.STRING)
	@Column(name = "compliance_type", nullable = false, length = 40)
	private AlcoholComplianceType complianceType;

	@Column(nullable = false)
	private boolean compliant;

	@Column(nullable = false, length = 1000)
	private String details;

	@Column(length = 1000)
	private String notes;

	@Column(name = "performed_at", nullable = false)
	private LocalDateTime performedAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "organization_id", nullable = false)
	private OrganizationModel organization;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "performed_by_user_id", nullable = false)
	private UserModel performedBy;

	@PrePersist
	public void setDefaultPerformedAt() {
		if (performedAt == null) {
			performedAt = LocalDateTime.now();
		}
	}

	public AlcoholComplianceType getComplianceType() {
		return complianceType;
	}

	public void setComplianceType(AlcoholComplianceType complianceType) {
		this.complianceType = complianceType;
	}

	public boolean isCompliant() {
		return compliant;
	}

	public void setCompliant(boolean compliant) {
		this.compliant = compliant;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public LocalDateTime getPerformedAt() {
		return performedAt;
	}

	public void setPerformedAt(LocalDateTime performedAt) {
		this.performedAt = performedAt;
	}

	public OrganizationModel getOrganization() {
		return organization;
	}

	public void setOrganization(OrganizationModel organization) {
		this.organization = organization;
	}

	public UserModel getPerformedBy() {
		return performedBy;
	}

	public void setPerformedBy(UserModel performedBy) {
		this.performedBy = performedBy;
	}
}
