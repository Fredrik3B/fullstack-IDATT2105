package edu.ntnu.idatt2105.backend.user.model;

import edu.ntnu.idatt2105.backend.user.model.enums.JoinOrgStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;


/**
 * JPA entity representing a user's request to join an organisation.
 *
 * <p>A request is created in {@link JoinOrgStatus#PENDING} state when a user
 * submits a join-code. An ADMIN or MANAGER then accepts or declines it, transitioning the status to
 * {@link JoinOrgStatus#ACCEPTED} or {@link JoinOrgStatus#DECLINED}.
 */
@Entity
@Getter
@Setter
@Table(name = "join_requests")
public class JoinRequestModel {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserModel user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id", nullable = false)
  private OrganizationModel organization;

  @Enumerated(EnumType.STRING)
  private JoinOrgStatus status;

  private LocalDateTime createdAt;
  private LocalDateTime resolvedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "resolved_by")
  private UserModel resolvedBy;
}
