export const SECTION_TYPE_OPTIONS = [
  'CLEANING_SANITATION',
  'CLOSING_ROUTINE',
  'FOOD_STORAGE',
  'HYGIENE',
  'OPENING_ROUTINE',
  'SAFETY_COMPLIANCE',
  'SERVICE_QUALITY',
  'TEMPERATURE_CONTROL'
]

export function formatSectionType(sectionType) {
  if (!sectionType) return 'Tasks'
  return String(sectionType)
    .split('_')
    .map((part) => part.charAt(0) + part.slice(1).toLowerCase())
    .join(' ')
}
