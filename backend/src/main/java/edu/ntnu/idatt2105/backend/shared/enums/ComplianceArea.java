package edu.ntnu.idatt2105.backend.shared.enums;

/**
 * Identifies which Norwegian regulatory framework a checklist or task falls under.
 *
 * <p>Used to partition checklists by module so that food-safety and
 * alcohol-control data are kept separate.
 */
public enum ComplianceArea {
  /**
   * Norwegian food-safety internal-control regulation (IK-mat).
   */
  IK_MAT,
  /**
   * Norwegian alcohol-control internal-control regulation (IK-alkohol).
   */
  IK_ALKOHOL,
  /**
   * Cross-cutting compliance items not tied to a specific regulation.
   */
  GENERAL
}
