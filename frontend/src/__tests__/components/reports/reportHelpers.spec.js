import { describe, it, expect } from 'vitest'
import {
  formatDate,
  formatDateTime,
  formatRate,
  rateClass,
  formatTemperatureRange,
} from '@/components/reports/reportHelpers.js'

describe('reportHelpers', () => {

  // ── formatDate ─────────────────────────────────────────────────────────

  describe('formatDate', () => {
    it('returns empty string for null', () => {
      expect(formatDate(null)).toBe('')
    })

    it('returns empty string for undefined', () => {
      expect(formatDate(undefined)).toBe('')
    })

    it('returns empty string for empty string', () => {
      expect(formatDate('')).toBe('')
    })

    it('includes the day, month name, and year for a valid date', () => {
      const result = formatDate('2024-06-15')
      expect(result).toContain('2024')
      expect(result).toContain('15')
      // en-GB locale produces the full month name
      expect(result).toMatch(/June|Jun/)
    })

    it('formats January correctly', () => {
      const result = formatDate('2024-01-01')
      expect(result).toContain('2024')
      expect(result).toMatch(/January|Jan/)
    })

    it('does not return the raw ISO string', () => {
      expect(formatDate('2024-06-15')).not.toContain('2024-06-15')
    })
  })

  // ── formatDateTime ─────────────────────────────────────────────────────

  describe('formatDateTime', () => {
    it('returns empty string for null', () => {
      expect(formatDateTime(null)).toBe('')
    })

    it('returns empty string for undefined', () => {
      expect(formatDateTime(undefined)).toBe('')
    })

    it('returns empty string for empty string', () => {
      expect(formatDateTime('')).toBe('')
    })

    it('includes the year and abbreviated month for a valid datetime', () => {
      const result = formatDateTime('2024-03-20T14:30:00')
      expect(result).toContain('2024')
      expect(result).toMatch(/Mar/)
    })

    it('includes hours and minutes', () => {
      const result = formatDateTime('2024-03-20T14:30:00')
      expect(result).toContain('14')
      expect(result).toContain('30')
    })

    it('does not return the raw ISO string', () => {
      expect(formatDateTime('2024-03-20T14:30:00')).not.toContain('2024-03-20T14:30:00')
    })
  })

  // ── formatRate ─────────────────────────────────────────────────────────

  describe('formatRate', () => {
    it('returns "0" for null', () => {
      expect(formatRate(null)).toBe('0')
    })

    it('returns "0" for undefined', () => {
      expect(formatRate(undefined)).toBe('0')
    })

    it('returns "0" for NaN', () => {
      expect(formatRate(NaN)).toBe('0')
    })

    it('formats zero to one decimal place', () => {
      expect(formatRate(0)).toBe('0.0')
    })

    it('formats an integer to one decimal place', () => {
      expect(formatRate(75)).toBe('75.0')
    })

    it('rounds to one decimal place', () => {
      expect(formatRate(75.16)).toBe('75.2')
      expect(formatRate(75.14)).toBe('75.1')
    })

    it('formats 100 correctly', () => {
      expect(formatRate(100)).toBe('100.0')
    })
  })

  // ── rateClass ──────────────────────────────────────────────────────────

  describe('rateClass', () => {
    it('returns "stat-ok" for rate exactly 90', () => {
      expect(rateClass(90)).toBe('stat-ok')
    })

    it('returns "stat-ok" for rate above 90', () => {
      expect(rateClass(100)).toBe('stat-ok')
      expect(rateClass(95.5)).toBe('stat-ok')
    })

    it('returns "stat-warn" for rate between 1 and 89.9', () => {
      expect(rateClass(50)).toBe('stat-warn')
      expect(rateClass(1)).toBe('stat-warn')
      expect(rateClass(89.9)).toBe('stat-warn')
    })

    it('returns "" for rate of exactly 0', () => {
      expect(rateClass(0)).toBe('')
    })

    it('returns "" for null or undefined', () => {
      expect(rateClass(null)).toBe('')
      expect(rateClass(undefined)).toBe('')
    })
  })

  // ── formatTemperatureRange ─────────────────────────────────────────────

  describe('formatTemperatureRange', () => {
    it('returns empty string for null point', () => {
      expect(formatTemperatureRange(null)).toBe('')
    })

    it('returns empty string for undefined point', () => {
      expect(formatTemperatureRange(undefined)).toBe('')
    })

    it('returns empty string when both min and max are absent', () => {
      expect(formatTemperatureRange({})).toBe('')
    })

    it('returns empty string when both min and max are null', () => {
      expect(formatTemperatureRange({ targetMin: null, targetMax: null })).toBe('')
    })

    it('returns "min°C - max°C" when both min and max are set', () => {
      expect(formatTemperatureRange({ targetMin: 2, targetMax: 8 })).toBe('2°C - 8°C')
    })

    it('returns ">= min°C" when only min is set', () => {
      expect(formatTemperatureRange({ targetMin: 65 })).toBe('>= 65°C')
    })

    it('returns "<= max°C" when only max is set', () => {
      expect(formatTemperatureRange({ targetMax: 4 })).toBe('<= 4°C')
    })

    it('prefers the range format (min and max) over the one-sided formats', () => {
      // Both present → range wins
      const result = formatTemperatureRange({ targetMin: 2, targetMax: 8 })
      expect(result).not.toContain('>=')
      expect(result).not.toContain('<=')
      expect(result).toBe('2°C - 8°C')
    })
  })

})
