import { describe, it, expect } from 'vitest'
import {
  formatSize,
  fileIconLabel,
  fileIconClass,
  moduleBadgeClass,
  moduleLabel,
  CATEGORIES,
  MODULES,
} from '@/components/documents/documentHelpers'

// ── Tests ──────────────────────────────────────────────────────────────────

describe('documentHelpers', () => {

  // ── CATEGORIES / MODULES constants ─────────────────────────────────────

  describe('CATEGORIES', () => {
    it('contains 7 entries', () => {
      expect(CATEGORIES).toHaveLength(7)
    })

    it('each entry has value, label, and emptyHint', () => {
      CATEGORIES.forEach(cat => {
        expect(cat).toHaveProperty('value')
        expect(cat).toHaveProperty('label')
        expect(cat).toHaveProperty('emptyHint')
      })
    })

    it('includes CERTIFICATE category', () => {
      expect(CATEGORIES.some(c => c.value === 'CERTIFICATE')).toBe(true)
    })

    it('includes DEVIATION_REPORT category', () => {
      expect(CATEGORIES.some(c => c.value === 'DEVIATION_REPORT')).toBe(true)
    })
  })

  describe('MODULES', () => {
    it('contains SHARED, IC_FOOD, IC_ALCOHOL', () => {
      const values = MODULES.map(m => m.value)
      expect(values).toContain('SHARED')
      expect(values).toContain('IC_FOOD')
      expect(values).toContain('IC_ALCOHOL')
    })
  })

  // ── formatSize ──────────────────────────────────────────────────────────

  describe('formatSize', () => {
    it('returns empty string for falsy input', () => {
      expect(formatSize(0)).toBe('')
      expect(formatSize(null)).toBe('')
      expect(formatSize(undefined)).toBe('')
    })

    it('formats bytes under 1 KB as "X B"', () => {
      expect(formatSize(512)).toBe('512 B')
      expect(formatSize(1)).toBe('1 B')
    })

    it('formats bytes in KB range', () => {
      expect(formatSize(1024)).toBe('1.0 KB')
      expect(formatSize(2048)).toBe('2.0 KB')
      expect(formatSize(1536)).toBe('1.5 KB')
    })

    it('formats bytes in MB range', () => {
      expect(formatSize(1024 * 1024)).toBe('1.0 MB')
      expect(formatSize(1024 * 1024 * 2.5)).toBe('2.5 MB')
    })
  })

  // ── fileIconLabel ───────────────────────────────────────────────────────

  describe('fileIconLabel', () => {
    it('returns "FILE" for null/undefined', () => {
      expect(fileIconLabel(null)).toBe('FILE')
      expect(fileIconLabel(undefined)).toBe('FILE')
    })

    it('returns "PDF" for pdf mime type', () => {
      expect(fileIconLabel('application/pdf')).toBe('PDF')
    })

    it('returns "DOC" for word document', () => {
      expect(fileIconLabel('application/vnd.openxmlformats-officedocument.wordprocessingml.document')).toBe('DOC')
      expect(fileIconLabel('application/msword')).toBe('DOC')
    })

    it('returns "IMG" for image types', () => {
      expect(fileIconLabel('image/png')).toBe('IMG')
      expect(fileIconLabel('image/jpeg')).toBe('IMG')
    })

    it('returns "XLS" for spreadsheet types', () => {
      expect(fileIconLabel('application/vnd.ms-excel')).toBe('XLS')
      expect(fileIconLabel('application/spreadsheet')).toBe('XLS')
    })

    it('returns "FILE" for unknown types', () => {
      expect(fileIconLabel('application/zip')).toBe('FILE')
      expect(fileIconLabel('text/plain')).toBe('FILE')
    })
  })

  // ── fileIconClass ───────────────────────────────────────────────────────

  describe('fileIconClass', () => {
    it('returns "doc-icon--file" for null/undefined', () => {
      expect(fileIconClass(null)).toBe('doc-icon--file')
    })

    it('returns "doc-icon--pdf" for pdf', () => {
      expect(fileIconClass('application/pdf')).toBe('doc-icon--pdf')
    })

    it('returns "doc-icon--doc" for word', () => {
      expect(fileIconClass('application/msword')).toBe('doc-icon--doc')
    })

    it('returns "doc-icon--img" for image', () => {
      expect(fileIconClass('image/png')).toBe('doc-icon--img')
    })

    it('returns "doc-icon--xls" for spreadsheet', () => {
      expect(fileIconClass('application/vnd.ms-excel')).toBe('doc-icon--xls')
      expect(fileIconClass('application/spreadsheet')).toBe('doc-icon--xls')
    })

    it('returns "doc-icon--file" for unknown', () => {
      expect(fileIconClass('text/csv')).toBe('doc-icon--file')
    })
  })

  // ── moduleBadgeClass ────────────────────────────────────────────────────

  describe('moduleBadgeClass', () => {
    it('returns food class for IC_FOOD', () => {
      expect(moduleBadgeClass('IC_FOOD')).toBe('doc-badge--food')
    })

    it('returns alcohol class for IC_ALCOHOL', () => {
      expect(moduleBadgeClass('IC_ALCOHOL')).toBe('doc-badge--alcohol')
    })

    it('returns shared class for SHARED and unknown values', () => {
      expect(moduleBadgeClass('SHARED')).toBe('doc-badge--shared')
      expect(moduleBadgeClass('OTHER')).toBe('doc-badge--shared')
      expect(moduleBadgeClass(null)).toBe('doc-badge--shared')
    })
  })

  // ── moduleLabel ─────────────────────────────────────────────────────────

  describe('moduleLabel', () => {
    it('returns "IC-Food" for IC_FOOD', () => {
      expect(moduleLabel('IC_FOOD')).toBe('IC-Food')
    })

    it('returns "IC-Alcohol" for IC_ALCOHOL', () => {
      expect(moduleLabel('IC_ALCOHOL')).toBe('IC-Alcohol')
    })

    it('returns "Shared" for anything else', () => {
      expect(moduleLabel('SHARED')).toBe('Shared')
      expect(moduleLabel(null)).toBe('Shared')
      expect(moduleLabel(undefined)).toBe('Shared')
    })
  })

})
