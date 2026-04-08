import { describe, expect, it } from 'vitest'
import { countTaskStates, getSummaryAriaLabel } from '@/composables/ic-checklists/checklistSummary'
import { recalcCardProgress } from '@/composables/ic-checklists/recalcCardProgress'
import {
  formatTemperatureTarget,
  isTemperatureDeviation,
  isTemperatureTask,
} from '@/composables/ic-checklists/temperature'
import {
  TEMPERATURE_ZONE_OPTIONS,
  formatTemperatureZoneType,
} from '@/composables/ic-checklists/temperatureZoneOptions'
import {
  SECTION_TYPE_OPTIONS,
  formatSectionType,
} from '@/composables/ic-checklists/taskTemplateOptions'

describe('checklist helper utilities', () => {
  describe('countTaskStates', () => {
    it('counts completed, pending, and fallback todo tasks across nested cards', () => {
      const totals = countTaskStates([
        {
          sections: [
            {
              items: [
                { state: 'completed' },
                { state: 'pending' },
                { state: 'todo' },
                { state: 'unknown-state' },
              ],
            },
          ],
        },
        {
          sections: [
            {
              items: [{ state: 'completed' }],
            },
          ],
        },
      ])

      expect(totals).toEqual({
        completed: 2,
        pending: 1,
        todo: 2,
        total: 5,
        completedPct: 40,
        pendingPct: 20,
        todoPct: 40,
      })
    })

    it('returns zero totals for invalid card collections', () => {
      expect(countTaskStates(null)).toEqual({
        completed: 0,
        pending: 0,
        todo: 0,
        total: 0,
        completedPct: 0,
        pendingPct: 0,
        todoPct: 0,
      })
    })
  })

  describe('getSummaryAriaLabel', () => {
    it('builds a label from totals', () => {
      expect(getSummaryAriaLabel({ completed: 4, pending: 2, todo: 1 })).toBe(
        'Completed 4, flagged 2, not started 1',
      )
    })

    it('falls back to zeros when totals are missing', () => {
      expect(getSummaryAriaLabel()).toBe('Completed 0, flagged 0, not started 0')
    })
  })

  describe('recalcCardProgress', () => {
    it('marks partially completed cards as warning with ratio label', () => {
      const card = {
        statusLabel: '0/2 completed',
        sections: [
          { items: [{ state: 'completed' }, { state: 'todo' }] },
        ],
      }

      recalcCardProgress(card)

      expect(card.progress).toBe(50)
      expect(card.statusTone).toBe('warning')
      expect(card.statusLabel).toBe('1/2 completed')
    })

    it('preserves norwegian-style completion wording when fullfort is present', () => {
      const card = {
        statusLabel: '0/1 fullfort',
        sections: [
          { items: [{ state: 'completed' }] },
        ],
      }

      recalcCardProgress(card)

      expect(card.progress).toBe(100)
      expect(card.statusTone).toBe('success')
      expect(card.statusLabel).toBe('Fullfort')
    })

    it('resets empty cards to muted with no progress', () => {
      const card = {
        statusLabel: 'unused',
        sections: [],
      }

      recalcCardProgress(card)

      expect(card.progress).toBeNull()
      expect(card.statusTone).toBe('muted')
    })
  })

  describe('temperature helpers', () => {
    it('detects temperature tasks from multiple markers', () => {
      expect(isTemperatureTask({ type: 'temperature' })).toBe(true)
      expect(isTemperatureTask({ sectionType: 'TEMPERATURE_CONTROL' })).toBe(true)
      expect(isTemperatureTask({ temperatureZoneId: 'zone-1' })).toBe(true)
      expect(isTemperatureTask({ targetMin: 2 })).toBe(true)
      expect(isTemperatureTask({ label: 'Generic task' })).toBe(false)
    })

    it('formats temperature targets for min/max, lower-bound, and upper-bound tasks', () => {
      expect(formatTemperatureTarget({ type: 'temperature', targetMin: 2, targetMax: 4 })).toBe(
        '2-4 C',
      )
      expect(formatTemperatureTarget({ type: 'temperature', targetMin: 60, unit: 'F' })).toBe(
        '>= 60 F',
      )
      expect(formatTemperatureTarget({ type: 'temperature', targetMax: 4 })).toBe('<= 4 C')
      expect(formatTemperatureTarget({ label: 'Not temperature' })).toBe('')
    })

    it('flags deviations when a measurement falls outside configured bounds', () => {
      const task = { type: 'temperature', targetMin: 2, targetMax: 4 }

      expect(isTemperatureDeviation(task, 1.9)).toBe(true)
      expect(isTemperatureDeviation(task, 4.1)).toBe(true)
      expect(isTemperatureDeviation(task, 3)).toBe(false)
      expect(isTemperatureDeviation(task, Number.NaN)).toBe(false)
      expect(isTemperatureDeviation({ label: 'Other' }, 3)).toBe(false)
    })
  })

  describe('option formatters', () => {
    it('exposes the expected zone and section option enums', () => {
      expect(TEMPERATURE_ZONE_OPTIONS).toContain('FRIDGE')
      expect(TEMPERATURE_ZONE_OPTIONS).toContain('HOT_HOLDING')
      expect(SECTION_TYPE_OPTIONS).toContain('OPENING_ROUTINE')
      expect(SECTION_TYPE_OPTIONS).toContain('TEMPERATURE_CONTROL')
    })

    it('formats zone and section enum values into readable labels', () => {
      expect(formatTemperatureZoneType('HOT_HOLDING')).toBe('Hot Holding')
      expect(formatTemperatureZoneType()).toBe('Temperature item')
      expect(formatSectionType('CLEANING_SANITATION')).toBe('Cleaning Sanitation')
      expect(formatSectionType()).toBe('Tasks')
    })
  })
})
