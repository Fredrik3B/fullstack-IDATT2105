/**
 * Supported backend zone types for temperature tasks.
 *
 * @type {string[]}
 */
export const TEMPERATURE_ZONE_OPTIONS = [
  'FRIDGE',
  'FREEZER',
  'HOT_HOLDING',
  'COLD_STORAGE',
  'RECEIVING',
]

/**
 * Format enum-like zone type into readable UI text.
 *
 * @param {string|null|undefined} zoneType
 * @returns {string}
 */
export function formatTemperatureZoneType(zoneType) {
  if (!zoneType) return 'Temperature item'
  return String(zoneType)
    .split('_')
    .map((part) => part.charAt(0) + part.slice(1).toLowerCase())
    .join(' ')
}
