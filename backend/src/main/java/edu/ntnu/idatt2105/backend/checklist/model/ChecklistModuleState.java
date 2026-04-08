package edu.ntnu.idatt2105.backend.checklist.model;

import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
	name = "checklist_module_state",
	uniqueConstraints = @UniqueConstraint(columnNames = { "organization_id", "compliance_area" })
)
public class ChecklistModuleState {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "organization_id", nullable = false)
	private UUID organizationId;

	@Enumerated(EnumType.STRING)
	@Column(name = "compliance_area", nullable = false, length = 30)
	private ComplianceArea complianceArea;

	@Column(name = "modified_at", nullable = false)
	private LocalDateTime modifiedAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UUID getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(UUID organizationId) {
		this.organizationId = organizationId;
	}

	public ComplianceArea getComplianceArea() {
		return complianceArea;
	}

	public void setComplianceArea(ComplianceArea complianceArea) {
		this.complianceArea = complianceArea;
	}

	public LocalDateTime getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(LocalDateTime modifiedAt) {
		this.modifiedAt = modifiedAt;
	}
}
