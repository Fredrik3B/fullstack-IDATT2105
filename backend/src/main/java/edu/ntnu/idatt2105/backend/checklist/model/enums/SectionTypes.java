package edu.ntnu.idatt2105.backend.checklist.model.enums;

/**
 * Categories used to group task templates within a checklist section.
 *
 * <p>Each constant is formatted into a human-readable section heading by
 * {@link edu.ntnu.idatt2105.backend.checklist.mapper.ChecklistMapper#sectionLabel}.
 */
public enum SectionTypes {
  /**
   * Personal and surface hygiene tasks.
   */
  HYGIENE,
  /**
   * Correct storage of perishable and dry goods.
   */
  FOOD_STORAGE,
  /**
   * Refrigerator, freezer, and food temperature checks.
   */
  TEMPERATURE_CONTROL,
  /**
   * Cleaning schedules and sanitisation procedures.
   */
  CLEANING_SANITATION,
  /**
   * Guest experience and service standard tasks.
   */
  SERVICE_QUALITY,
  /**
   * Tasks performed when opening the establishment.
   */
  OPENING_ROUTINE,
  /**
   * Tasks performed when closing the establishment.
   */
  CLOSING_ROUTINE,
  /**
   * Health, safety, and regulatory compliance checks.
   */
  SAFETY_COMPLIANCE
}
