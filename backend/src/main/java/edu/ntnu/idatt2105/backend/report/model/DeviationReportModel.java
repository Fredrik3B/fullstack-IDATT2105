package edu.ntnu.idatt2105.backend.report.model;

import edu.ntnu.idatt2105.backend.common.model.enums.DeviationSeverity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "deviation_reports")
public class DeviationReportModel {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private UUID organizationId;

  @Column(nullable = false)
  private UUID reportedByUserId;

  @Column(nullable = false, length = 200)
  private String deviationName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private DeviationSeverity severity;

  @Column(nullable = false)
  private LocalDateTime timestamp;

  @Column(nullable = false)
  private String noticedBy;

  @Column(nullable = false)
  private String reportedTo;

  @Column(nullable = false)
  private String processedBy;

  @Column(nullable = false, length = 2000)
  private String description;

  @Column(nullable = false, length = 2000)
  private String immediateAction;

  @Column(nullable = false, length = 2000)
  private String believedCause;

  @Column(nullable = false, length = 2000)
  private String correctiveMeasures;

  @Column(nullable = false, length = 2000)
  private String correctiveMeasuresDone;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }
}