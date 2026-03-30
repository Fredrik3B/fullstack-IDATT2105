package edu.ntnu.idatt2105.backend.common.model;

import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.common.model.enums.DeviationSeverity;
import edu.ntnu.idatt2105.backend.common.model.enums.DeviationStatus;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
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


@Entity
@Table(name = "deviations")
public class DeviationModel extends AuditableEntity {

	@Column(nullable = false, length = 150)
	private String title;

	@Column(nullable = false, length = 2000)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private DeviationSeverity severity;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private DeviationStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "compliance_area", nullable = false, length = 30)
	private ComplianceArea complianceArea;

	@Column(name = "reported_at", nullable = false)
	private LocalDateTime reportedAt;

	@Column(name = "resolved_at")
	private LocalDateTime resolvedAt;

	@Column(name = "resolution_notes", length = 1500)
	private String resolutionNotes;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "organization_id", nullable = false)
	private OrganizationModel organization;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "reported_by_user_id", nullable = false)
	private UserModel reportedBy;

	@PrePersist
	public void setDefaults() {
		if (reportedAt == null) {
			reportedAt = LocalDateTime.now();
		}
		if (status == null) {
			status = DeviationStatus.OPEN;
		}
	}

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

	public DeviationSeverity getSeverity() {
		return severity;
	}

	public void setSeverity(DeviationSeverity severity) {
		this.severity = severity;
	}

	public DeviationStatus getStatus() {
		return status;
	}

	public void setStatus(DeviationStatus status) {
		this.status = status;
	}

	public ComplianceArea getComplianceArea() {
		return complianceArea;
	}

	public void setComplianceArea(ComplianceArea complianceArea) {
		this.complianceArea = complianceArea;
	}

	public LocalDateTime getReportedAt() {
		return reportedAt;
	}

	public void setReportedAt(LocalDateTime reportedAt) {
		this.reportedAt = reportedAt;
	}

	public LocalDateTime getResolvedAt() {
		return resolvedAt;
	}

	public void setResolvedAt(LocalDateTime resolvedAt) {
		this.resolvedAt = resolvedAt;
	}

	public String getResolutionNotes() {
		return resolutionNotes;
	}

	public void setResolutionNotes(String resolutionNotes) {
		this.resolutionNotes = resolutionNotes;
	}

	public OrganizationModel getOrganization() {
		return organization;
	}

	public void setOrganization(OrganizationModel organization) {
		this.organization = organization;
	}

	public UserModel getReportedBy() {
		return reportedBy;
	}

	public void setReportedBy(UserModel reportedBy) {
		this.reportedBy = reportedBy;
	}
}
