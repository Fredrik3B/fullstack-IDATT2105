package edu.ntnu.idatt2105.backend.report.model;

import edu.ntnu.idatt2105.backend.shared.enums.DeviationSeverity;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.model.UserModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "deviation_reports")
public class DeviationReportModel {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id", nullable = false)
  private OrganizationModel organization;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reported_by_user_id", nullable = false)
  private UserModel reportedByUser;

  @Column(nullable = false, length = 200)
  private String deviationName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private DeviationSeverity severity;

  @Column(nullable = false)
  private LocalDateTime occurredAt;

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