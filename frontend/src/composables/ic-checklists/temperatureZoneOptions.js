export const TEMPERATURE_ZONE_OPTIONS = [
  'FRIDGE',
  'FREEZER',
  'HOT_HOLDING',
  'COLD_STORAGE',
  'RECEIVING',
]

export function formatTemperatureZoneType(zoneType) {
  if (!zoneType) return 'Temperature item'
  return String(zoneType)
    .split('_')
    .map((part) => part.charAt(0) + part.slice(1).toLowerCase())
    .join(' ')
}
