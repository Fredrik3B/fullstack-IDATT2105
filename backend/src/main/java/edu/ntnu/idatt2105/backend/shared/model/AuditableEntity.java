package edu.ntnu.idatt2105.backend.shared.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Base class for JPA entities that use an auto-generated {@code Long} primary key.
 *
 * <p>All domain entities that need a surrogate ID should extend this class
 * rather than declaring their own {@code @Id} field.
 */
@Setter
@Getter
@MappedSuperclass
public abstract class AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

}
