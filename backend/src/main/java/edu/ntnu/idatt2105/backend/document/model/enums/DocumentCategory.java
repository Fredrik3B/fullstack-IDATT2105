package edu.ntnu.idatt2105.backend.document.model.enums;

/**
 * Category classifying the purpose of a compliance document.
 */
public enum DocumentCategory {

  /**
   * Operational guidelines and standard procedures.
   */
  GUIDELINES,

  /**
   * Training materials and learning resources.
   */
  TRAINING,

  /**
   * Official certificates (e.g. hygiene certifications, licences).
   */
  CERTIFICATE,

  /**
   * Audit reports from internal or external inspections.
   */
  AUDIT_REPORT,

  /**
   * HACCP (Hazard Analysis and Critical Control Points) documentation.
   */
  HACCP,

  /**
   * Emergency plans and incident response procedures.
   */
  EMERGENCY,

  /**
   * Created deviation reports
   */
  DEVIATION_REPORT
}
