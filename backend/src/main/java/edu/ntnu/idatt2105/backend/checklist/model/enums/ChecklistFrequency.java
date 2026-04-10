package edu.ntnu.idatt2105.backend.checklist.model.enums;

/**
 * Repetition frequency for a checklist, determining period-key format and rollover behaviour.
 */
public enum ChecklistFrequency {
  /**
   * Checklist resets every calendar day. Period key format: {@code "YYYY-MM-DD"}.
   */
  DAILY,
  /**
   * Checklist resets every ISO week (Monday–Sunday). Period key format: {@code "YYYY-Www"}.
   */
  WEEKLY,
  /**
   * Checklist resets every calendar month. Period key format: {@code "YYYY-MM"}.
   */
  MONTHLY,
  /**
   * One-off checklist with no automatic rollover.
   */
  AD_HOC
}
