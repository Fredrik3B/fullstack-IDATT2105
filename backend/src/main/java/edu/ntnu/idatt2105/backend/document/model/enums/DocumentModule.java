package edu.ntnu.idatt2105.backend.document.model.enums;

/**
 * Identifies which compliance module a document belongs to.
 *
 * <p>Documents marked {@link #SHARED} are visible across all modules.
 */
public enum DocumentModule {

	/** Document is available to all compliance modules. */
	SHARED,

	/** Document is specific to the food-safety (IK-Mat) module. */
	IC_FOOD,

	/** Document is specific to the alcohol-control (IK-Alkohol) module. */
	IC_ALCOHOL
}
