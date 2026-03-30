package edu.ntnu.idatt2105.backend.user.model;

import edu.ntnu.idatt2105.backend.user.model.enums.JoinOrgStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "join_requests")
public class JoinRequestModel {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private UUID userId;

  @Column(nullable = false)
  private UUID organizationId;

  @Enumerated(EnumType.STRING)
  private JoinOrgStatus status;

  private LocalDateTime createdAt;
  private LocalDateTime resolvedAt;
  private UUID resolvedBy;
}
