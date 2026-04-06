export const CATEGORIES = [
  { value: 'GUIDELINES', label: 'Guidelines', emptyHint: 'Upload company policies and procedures for hygiene and alcohol handling.' },
  { value: 'TRAINING', label: 'Training material', emptyHint: 'Add course material, instructions, and training documents for employees.' },
  { value: 'CERTIFICATE', label: 'Certificates', emptyHint: 'Add employee certificates, e.g. serving license and food safety course.' },
  { value: 'AUDIT_REPORT', label: 'Audit & inspection', emptyHint: 'Store results from external food authority inspections.' },
  { value: 'HACCP', label: 'HACCP / Risk', emptyHint: 'Upload food safety hazard analysis and risk assessment documents.' },
  { value: 'EMERGENCY', label: 'Emergency procedures', emptyHint: 'Add fire evacuation, first aid, and other emergency plans.' },
]

export const MODULES = [
  { value: 'SHARED', label: 'Shared' },
  { value: 'IC_FOOD', label: 'IC-Food' },
  { value: 'IC_ALCOHOL', label: 'IC-Alcohol' },
]

export function formatSize(bytes) {
  if (!bytes) return ''
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

export function fileIconLabel(fileType) {
  if (!fileType) return 'FILE'
  if (fileType.includes('pdf')) return 'PDF'
  if (fileType.includes('word') || fileType.includes('document')) return 'DOC'
  if (fileType.includes('image')) return 'IMG'
  if (fileType.includes('spreadsheet') || fileType.includes('excel')) return 'XLS'
  return 'FILE'
}

export function fileIconClass(fileType) {
  if (!fileType) return 'doc-icon--file'
  if (fileType.includes('pdf')) return 'doc-icon--pdf'
  if (fileType.includes('word') || fileType.includes('document')) return 'doc-icon--doc'
  if (fileType.includes('image')) return 'doc-icon--img'
  if (fileType.includes('spreadsheet') || fileType.includes('excel')) return 'doc-icon--xls'
  return 'doc-icon--file'
}

export function moduleBadgeClass(module) {
  if (module === 'IC_FOOD') return 'doc-badge--food'
  if (module === 'IC_ALCOHOL') return 'doc-badge--alcohol'
  return 'doc-badge--shared'
}

export function moduleLabel(module) {
  if (module === 'IC_FOOD') return 'IC-Food'
  if (module === 'IC_ALCOHOL') return 'IC-Alcohol'
  return 'Shared'
}