package edu.ntnu.idatt2105.backend.checklist.model;

import edu.ntnu.idatt2105.backend.shared.enums.ComplianceArea;
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
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity that stores the last-modified timestamp for a single organisation/module combination.
 *
 * <p>There is at most one row per {@code (organization_id, compliance_area)} pair.
 * It is updated by {@link edu.ntnu.idatt2105.backend.checklist.service.ChecklistCacheStateService}
 * on every mutating checklist operation so that HTTP conditional-GET clients can detect changes.
 */
@Setter
@Getter
@Entity
@Table(
    name = "checklist_module_state",
    uniqueConstraints = @UniqueConstraint(columnNames = {"organization_id", "compliance_area"})
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

}
