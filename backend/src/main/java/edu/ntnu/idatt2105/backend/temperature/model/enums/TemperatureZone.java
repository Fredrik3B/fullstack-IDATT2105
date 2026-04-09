package edu.ntnu.idatt2105.backend.temperature.model.enums;

/**
 * Physical type of a temperature zone used in food-safety monitoring.
 *
 * <p>The zone type is stored on both the {@link edu.ntnu.idatt2105.backend.temperature.model.TemperatureZoneModel}
 * and propagated to task templates so reports can group readings by zone category.
 */
public enum TemperatureZone {

	/** Standard refrigerator, typically +2 °C to +8 °C. */
	FRIDGE,

	/** Freezer unit, typically -18 °C or below. */
	FREEZER,

	/** Hot-holding equipment that keeps cooked food above 60 °C. */
	HOT_HOLDING,

	/** Cold-storage room, usually between 0 °C and +5 °C. */
	COLD_STORAGE,

	/** Goods-receiving area where incoming deliveries are temperature-checked. */
	RECEIVING
}
