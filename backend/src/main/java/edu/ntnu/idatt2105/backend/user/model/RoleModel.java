package edu.ntnu.idatt2105.backend.user.model;

import edu.ntnu.idatt2105.backend.user.model.enums.RoleEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity representing an application role (e.g. ADMIN, MANAGER, STAFF).
 *
 * <p>Role rows are seeded at startup by {@link edu.ntnu.idatt2105.backend.config.TableSeeder}
 * and are referenced by {@link UserModel} via the {@code user_roles} join table.
 */
@Entity
@Getter
@Setter
@Table(name = "roles")
public class RoleModel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, unique = true)
  private RoleEnum name;
}