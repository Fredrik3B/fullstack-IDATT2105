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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity representing a reusable task definition that can be included in checklists.
 *
 * <p>Temperature-control tasks carry additional fields ({@code unit}, {@code targetMin},
 * {@code targetMax}, {@code temperatureZone}) that drive the temperature measurement workflow.
 * Non-temperature tasks leave these fields null.
 */
@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

}
