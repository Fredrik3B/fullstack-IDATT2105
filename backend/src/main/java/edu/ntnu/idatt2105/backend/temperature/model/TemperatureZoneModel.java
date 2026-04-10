package edu.ntnu.idatt2105.backend.temperature.model;

import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.shared.model.AuditableEntity;
import edu.ntnu.idatt2105.backend.temperature.model.enums.TemperatureZone;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity representing a named temperature zone within an organisation.
 *
 * <p>A zone captures the acceptable target range ({@code targetMin} / {@code targetMax}).
 * When the range is updated via
 * {@link edu.ntnu.idatt2105.backend.temperature.service.TemperatureZoneService#updateZone}, the new
 * bounds are automatically propagated to all linked
 * {@link edu.ntnu.idatt2105.backend.task.model.TaskTemplate} records so that existing tasks stay in
 * sync.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "temperature_zones")
public class TemperatureZoneModel extends AuditableEntity {

  @Column(nullable = false, length = 120)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "zone_type", nullable = false, length = 30)
  private TemperatureZone zoneType;

  @Enumerated(EnumType.STRING)
  @Column(name = "compliance_area", nullable = false, length = 30)
  private ComplianceArea complianceArea;

  @Column(name = "target_min", precision = 5, scale = 2)
  private BigDecimal targetMin;

  @Column(name = "target_max", precision = 5, scale = 2)
  private BigDecimal targetMax;

  @Column(nullable = false)
  private UUID organizationId;
}
