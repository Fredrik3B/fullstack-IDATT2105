package edu.ntnu.idatt2105.backend.shared.enums;

/**
 * Frontend-facing module identifier that maps to a {@link ComplianceArea}.
 *
 * <p>API clients use these values to scope requests to a specific internal-control
 * module without needing to know the Norwegian-language {@link ComplianceArea} constants.
 */
public enum IcModule {
  /** Food-safety internal control module (maps to {@link ComplianceArea#IK_MAT}). */
  IC_FOOD,
  /** Alcohol-control internal control module (maps to {@link ComplianceArea#IK_ALKOHOL}). */
  IC_ALCOHOL;

  /**
   * Converts a {@link ComplianceArea} to its corresponding {@link IcModule}.
   *
   * @param complianceArea the compliance area to convert
   * @return the matching {@link IcModule}
   */
  public static IcModule fromComplianceArea(ComplianceArea complianceArea) {
    return complianceArea == ComplianceArea.IK_ALKOHOL ? IC_ALCOHOL : IC_FOOD;
  }

  /**
   * Converts this module to its underlying {@link ComplianceArea}.
   *
   * @return the corresponding {@link ComplianceArea}
   */
  public ComplianceArea toComplianceArea() {
    return switch (this) {
      case IC_FOOD -> ComplianceArea.IK_MAT;
      case IC_ALCOHOL -> ComplianceArea.IK_ALKOHOL;
    };
  }
}

