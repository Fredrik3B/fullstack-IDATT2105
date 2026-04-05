import { describe, it, expect } from 'vitest'
import {
  normalizePeriodEnum,
  periodEnumToLabel,
  getPeriodKey,
  getPeriodEnd,
  isPeriodExpired,
  getCardPeriodEnum,
  deriveDisplayCards,
} from '@/features/ic-checklists/recurrence'

// ── normalizePeriodEnum ──────────────────────────────────────────────────────

describe('normalizePeriodEnum', () => {
  it('returns null for empty string', () => {
    expect(normalizePeriodEnum('')).toBeNull()
  })

  it('returns null for null', () => {
    expect(normalizePeriodEnum(null)).toBeNull()
  })

  it('returns null for undefined', () => {
    expect(normalizePeriodEnum(undefined)).toBeNull()
  })

  it('normalizes exact lowercase values', () => {
    expect(normalizePeriodEnum('daily')).toBe('daily')
    expect(normalizePeriodEnum('weekly')).toBe('weekly')
    expect(normalizePeriodEnum('monthly')).toBe('monthly')
  })

  it('normalizes Title-case label values', () => {
    expect(normalizePeriodEnum('Daily')).toBe('daily')
    expect(normalizePeriodEnum('Weekly')).toBe('weekly')
    expect(normalizePeriodEnum('Monthly')).toBe('monthly')
  })

  it('normalizes prefix-based inputs', () => {
    expect(normalizePeriodEnum('days')).toBe('daily')
    expect(normalizePeriodEnum('weeks')).toBe('weekly')
    expect(normalizePeriodEnum('months')).toBe('monthly')
  })

  it('returns null for unknown strings', () => {
    expect(normalizePeriodEnum('yearly')).toBeNull()
    expect(normalizePeriodEnum('hourly')).toBeNull()
  })
})

// ── periodEnumToLabel ────────────────────────────────────────────────────────

describe('periodEnumToLabel', () => {
  it('returns "Daily" for daily', () => {
    expect(periodEnumToLabel('daily')).toBe('Daily')
  })

  it('returns "Weekly" for weekly', () => {
    expect(periodEnumToLabel('weekly')).toBe('Weekly')
  })

  it('returns "Monthly" for monthly', () => {
    expect(periodEnumToLabel('monthly')).toBe('Monthly')
  })

  it('defaults to "Daily" for null', () => {
    expect(periodEnumToLabel(null)).toBe('Daily')
  })

  it('defaults to "Daily" for an unknown value', () => {
    expect(periodEnumToLabel('yearly')).toBe('Daily')
  })
})

// ── getPeriodKey ─────────────────────────────────────────────────────────────

describe('getPeriodKey', () => {
  it('returns a YYYY-MM-DD key for daily period', () => {
    const date = new Date(2024, 0, 15) // Jan 15, 2024
    expect(getPeriodKey('daily', date)).toBe('2024-01-15')
  })

  it('returns a YYYY-Www key for weekly period', () => {
    const date = new Date(2024, 0, 15) // Week 3 of 2024
    const key = getPeriodKey('weekly', date)
    expect(key).toMatch(/^\d{4}-W\d{2}$/)
    expect(key).toBe('2024-W03')
  })

  it('returns a YYYY-MM key for monthly period', () => {
    const date = new Date(2024, 0, 15) // Jan 2024
    expect(getPeriodKey('monthly', date)).toBe('2024-01')
  })

  it('defaults to daily when period is null', () => {
    const date = new Date(2024, 5, 10) // June 10, 2024
    expect(getPeriodKey(null, date)).toBe('2024-06-10')
  })

  it('pads single-digit month and day with zeros', () => {
    const date = new Date(2024, 2, 5) // March 5, 2024
    expect(getPeriodKey('daily', date)).toBe('2024-03-05')
  })
})

// ── getPeriodEnd ─────────────────────────────────────────────────────────────

describe('getPeriodEnd', () => {
  it('returns end of day for a daily period (next day)', () => {
    const end = getPeriodEnd('daily', '2024-01-15')
    expect(end).toBeInstanceOf(Date)
    expect(end.getFullYear()).toBe(2024)
    expect(end.getMonth()).toBe(0)
    expect(end.getDate()).toBe(16)
  })

  it('returns end of week for a weekly period (7 days later)', () => {
    const end = getPeriodEnd('weekly', '2024-W03')
    expect(end).toBeInstanceOf(Date)
    // Week 3 of 2024 starts on Jan 15; end is Jan 22
    expect(end.getFullYear()).toBe(2024)
    expect(end.getMonth()).toBe(0)
    expect(end.getDate()).toBe(22)
  })

  it('returns start of next month for a monthly period', () => {
    const end = getPeriodEnd('monthly', '2024-01')
    expect(end).toBeInstanceOf(Date)
    expect(end.getFullYear()).toBe(2024)
    expect(end.getMonth()).toBe(1) // February
    expect(end.getDate()).toBe(1)
  })

  it('returns null for an invalid period key', () => {
    expect(getPeriodEnd('daily', '')).toBeNull()
    expect(getPeriodEnd('daily', null)).toBeNull()
  })

  it('handles December to January month rollover', () => {
    const end = getPeriodEnd('monthly', '2024-12')
    expect(end.getFullYear()).toBe(2025)
    expect(end.getMonth()).toBe(0) // January
  })
})

// ── isPeriodExpired ──────────────────────────────────────────────────────────

describe('isPeriodExpired', () => {
  it('returns true when current time is past the period end', () => {
    const pastNow = new Date(2024, 0, 20) // after Jan 15 daily period end
    expect(isPeriodExpired('daily', '2024-01-15', pastNow)).toBe(true)
  })

  it('returns false when current time is before the period end', () => {
    const futureNow = new Date(2024, 0, 14) // before Jan 15 daily period
    expect(isPeriodExpired('daily', '2024-01-15', futureNow)).toBe(false)
  })

  it('returns false for an invalid period key', () => {
    expect(isPeriodExpired('daily', '', new Date())).toBe(false)
  })

  it('returns false for null period key', () => {
    expect(isPeriodExpired('daily', null, new Date())).toBe(false)
  })
})

// ── getCardPeriodEnum ────────────────────────────────────────────────────────

describe('getCardPeriodEnum', () => {
  it('returns null for null input', () => {
    expect(getCardPeriodEnum(null)).toBeNull()
  })

  it('returns null for non-object input', () => {
    expect(getCardPeriodEnum('daily')).toBeNull()
  })

  it('reads period from card.period property', () => {
    expect(getCardPeriodEnum({ period: 'weekly' })).toBe('weekly')
  })

  it('reads period from card.recurrence property', () => {
    expect(getCardPeriodEnum({ recurrence: 'monthly' })).toBe('monthly')
  })

  it('reads period from card.repeat property', () => {
    expect(getCardPeriodEnum({ repeat: 'daily' })).toBe('daily')
  })

  it('falls back to subtitle text for daily', () => {
    expect(getCardPeriodEnum({ subtitle: 'This is a daily checklist' })).toBe('daily')
  })

  it('falls back to subtitle text for weekly', () => {
    expect(getCardPeriodEnum({ subtitle: 'Weekly temperature log' })).toBe('weekly')
  })

  it('falls back to subtitle text for monthly', () => {
    expect(getCardPeriodEnum({ subtitle: 'Monthly compliance check' })).toBe('monthly')
  })

  it('returns null when no period information is available', () => {
    expect(getCardPeriodEnum({ title: 'My Checklist' })).toBeNull()
  })
})

// ── deriveDisplayCards ───────────────────────────────────────────────────────

describe('deriveDisplayCards', () => {
  it('returns empty array for non-array input', () => {
    expect(deriveDisplayCards(null)).toEqual([])
    expect(deriveDisplayCards(undefined)).toEqual([])
  })

  it('returns all cards when no activePeriodLabel filter is set', () => {
    const cards = [{ period: 'daily' }, { period: 'weekly' }]
    const result = deriveDisplayCards(cards)
    expect(result).toHaveLength(2)
  })

  it('filters cards by activePeriodLabel', () => {
    const cards = [{ period: 'daily', sections: [] }, { period: 'weekly', sections: [] }]
    const result = deriveDisplayCards(cards, { activePeriodLabel: 'Daily' })
    expect(result).toHaveLength(1)
    expect(getCardPeriodEnum(result[0])).toBe('daily')
  })

  it('adds __sourceIndex to each card', () => {
    const cards = [{ period: 'daily', sections: [] }, { period: 'weekly', sections: [] }]
    const result = deriveDisplayCards(cards)
    expect(result[0].__sourceIndex).toBe(0)
    expect(result[1].__sourceIndex).toBe(1)
  })

  it('resets completed task state when completedForPeriodKey does not match current period', () => {
    const cards = [
      {
        period: 'daily',
        activePeriodKey: '2024-01-15',
        sections: [
          {
            items: [
              { state: 'completed', completedForPeriodKey: '2024-01-14' }, // stale
            ],
          },
        ],
      },
    ]
    const result = deriveDisplayCards(cards)
    expect(result[0].sections[0].items[0].state).toBe('todo')
  })

  it('keeps completed task state when completedForPeriodKey matches current period', () => {
    const cards = [
      {
        period: 'daily',
        activePeriodKey: '2024-01-15',
        sections: [
          {
            items: [
              { state: 'completed', completedForPeriodKey: '2024-01-15' },
            ],
          },
        ],
      },
    ]
    const result = deriveDisplayCards(cards)
    expect(result[0].sections[0].items[0].state).toBe('completed')
  })

  it('resets pending task state when pendingForPeriodKey does not match current period', () => {
    const cards = [
      {
        period: 'daily',
        activePeriodKey: '2024-01-15',
        sections: [
          {
            items: [
              { state: 'pending', pendingForPeriodKey: '2024-01-14' }, // stale
            ],
          },
        ],
      },
    ]
    const result = deriveDisplayCards(cards)
    expect(result[0].sections[0].items[0].state).toBe('todo')
  })
})
