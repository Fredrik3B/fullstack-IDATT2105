package edu.ntnu.idatt2105.backend.shared.enums;

/**
 * Severity levels used when classifying a deviation in a report.
 *
 * <p>Values are ordered from least to most severe and are displayed
 * in the deviation report UI to help prioritise corrective actions.
 */
public enum DeviationSeverity {
  /** Minor issue with negligible risk. */
  LOW,
  /** Noteworthy issue that should be addressed. */
  MEDIUM,
  /** Serious issue requiring prompt corrective action. */
  HIGH,
  /** Immediate action required; significant safety or compliance risk. */
  CRITICAL
}
