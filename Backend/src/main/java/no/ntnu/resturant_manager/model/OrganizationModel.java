package no.ntnu.resturant_manager.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "organizations")
public class OrganizationModel extends AuditableEntity {

	@Column(nullable = false, length = 150)
	private String name;

	@Column(name = "organization_number", length = 30)
	private String organizationNumber;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOrganizationNumber() {
		return organizationNumber;
	}

	public void setOrganizationNumber(String organizationNumber) {
		this.organizationNumber = organizationNumber;
	}
}
