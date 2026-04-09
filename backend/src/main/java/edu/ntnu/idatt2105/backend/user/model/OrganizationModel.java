package edu.ntnu.idatt2105.backend.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity representing a restaurant or business organisation.
 *
 * <p>Each organisation has a unique {@code joinCode} that staff members
 * use to request membership. The code is generated from the organisation
 * name during creation.
 */
@Entity
@Getter
@Setter
@Table(name = "organizations")
public class OrganizationModel {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String joinCode;
}
