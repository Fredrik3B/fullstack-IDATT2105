/**
 * Category options available for document metadata.
 *
 * @type {Array<{value: string, label: string, emptyHint: string}>}
 */
export const CATEGORIES = [
  { value: 'GUIDELINES', label: 'Guidelines', emptyHint: 'Upload company policies and procedures for hygiene and alcohol handling.' },
  { value: 'TRAINING', label: 'Training material', emptyHint: 'Add course material, instructions, and training documents for employees.' },
  { value: 'CERTIFICATE', label: 'Certificates', emptyHint: 'Add employee certificates, e.g. serving license and food safety course.' },
  { value: 'AUDIT_REPORT', label: 'Audit & inspection', emptyHint: 'Store results from external food authority inspections.' },
  { value: 'HACCP', label: 'HACCP / Risk', emptyHint: 'Upload food safety hazard analysis and risk assessment documents.' },
  { value: 'EMERGENCY', label: 'Emergency procedures', emptyHint: 'Add fire evacuation, first aid, and other emergency plans.' },
]

/**
 * Module options available for document metadata.
 *
 * @type {Array<{value: string, label: string}>}
 */
export const MODULES = [
  { value: 'SHARED', label: 'Shared' },
  { value: 'IC_FOOD', label: 'IC-Food' },
  { value: 'IC_ALCOHOL', label: 'IC-Alcohol' },
]

/**
 * Format byte size for compact UI labels.
 *
 * @param {number|null|undefined} bytes
 * @returns {string}
 */
export function formatSize(bytes) {
  if (!bytes) return ''
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

/**
 * Map MIME type to short icon label.
 *
 * @param {string|null|undefined} fileType
 * @returns {'PDF'|'DOC'|'IMG'|'XLS'|'FILE'}
 */
export function fileIconLabel(fileType) {
  if (!fileType) return 'FILE'
  if (fileType.includes('pdf')) return 'PDF'
  if (fileType.includes('word') || fileType.includes('document')) return 'DOC'
  if (fileType.includes('image')) return 'IMG'
  if (fileType.includes('spreadsheet') || fileType.includes('excel')) return 'XLS'
  return 'FILE'
}

/**
 * Map MIME type to CSS icon modifier class.
 *
 * @param {string|null|undefined} fileType
 * @returns {string}
 */
export function fileIconClass(fileType) {
  if (!fileType) return 'doc-icon--file'
  if (fileType.includes('pdf')) return 'doc-icon--pdf'
  if (fileType.includes('word') || fileType.includes('document')) return 'doc-icon--doc'
  if (fileType.includes('image')) return 'doc-icon--img'
  if (fileType.includes('spreadsheet') || fileType.includes('excel')) return 'doc-icon--xls'
  return 'doc-icon--file'
}

/**
 * Map module enum to document badge class.
 *
 * @param {string|null|undefined} module
 * @returns {string}
 */
export function moduleBadgeClass(module) {
  if (module === 'IC_FOOD') return 'doc-badge--food'
  if (module === 'IC_ALCOHOL') return 'doc-badge--alcohol'
  return 'doc-badge--shared'
}

/**
 * Map module enum to display label.
 *
 * @param {string|null|undefined} module
 * @returns {'IC-Food'|'IC-Alcohol'|'Shared'}
 */
export function moduleLabel(module) {
  if (module === 'IC_FOOD') return 'IC-Food'
  if (module === 'IC_ALCOHOL') return 'IC-Alcohol'
  return 'Shared'
}

/**
 * Normalize external document links so missing protocol still opens correctly.
 *
 * @param {string|null|undefined} url
 * @returns {string}
 */
export function normalizeExternalUrl(url) {
  if (!url) return ''
  const value = String(url).trim()
  if (!value) return ''

  // Keep explicit URL schemes untouched (http, https, mailto, etc.)
  if (/^[a-zA-Z][a-zA-Z\d+\-.]*:/.test(value)) return value

  // Protocol-relative URLs like //example.com
  if (value.startsWith('//')) return `https:${value}`

  return `https://${value}`
}
