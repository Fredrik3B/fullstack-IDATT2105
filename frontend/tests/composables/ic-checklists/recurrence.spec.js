import { describe, expect, it } from 'vitest'
import {
  deriveDisplayCards,
  getCardPeriodEnum,
  getPeriodEnd,
  getPeriodKey,
  isPeriodExpired,
  normalizePeriodEnum,
  periodEnumToLabel,
  seedCompletionMetaForCurrentPeriod,
} from '@/composables/ic-checklists/recurrence'

describe('recurrence utilities', () => {
  describe('normalizePeriodEnum and periodEnumToLabel', () => {
    it('normalizes labels, prefixes, and enums', () => {
      expect(normalizePeriodEnum('Daily')).toBe('daily')
      expect(normalizePeriodEnum('weekly')).toBe('weekly')
      expect(normalizePeriodEnum('Month plan')).toBe('monthly')
      expect(normalizePeriodEnum('')).toBeNull()
      expect(normalizePeriodEnum('yearly')).toBeNull()
    })

    it('converts enums to a user-facing label with daily fallback', () => {
      expect(periodEnumToLabel('weekly')).toBe('Weekly')
      expect(periodEnumToLabel('monthly')).toBe('Monthly')
      expect(periodEnumToLabel('unexpected')).toBe('Daily')
    })
  })

  describe('getPeriodKey and getPeriodEnd', () => {
    it('builds daily, weekly, and monthly period keys', () => {
      const date = new Date('2026-01-01T10:00:00Z')

      expect(getPeriodKey('daily', date)).toBe('2026-01-01')
      expect(getPeriodKey('weekly', date)).toBe('2026-W01')
      expect(getPeriodKey('monthly', date)).toBe('2026-01')
    })

    it('returns the next period boundary for each recurrence type', () => {
      const dailyEnd = getPeriodEnd('daily', '2026-04-07')
      const weeklyEnd = getPeriodEnd('weekly', '2026-W15')
      const monthlyEnd = getPeriodEnd('monthly', '2026-04')

      expect(dailyEnd).not.toBeNull()
      expect(weeklyEnd).not.toBeNull()
      expect(monthlyEnd).not.toBeNull()

      expect(dailyEnd?.getFullYear()).toBe(2026)
      expect(dailyEnd?.getMonth()).toBe(3)
      expect(dailyEnd?.getDate()).toBe(8)
      expect(dailyEnd?.getHours()).toBe(0)

      expect(weeklyEnd?.getFullYear()).toBe(2026)
      expect(weeklyEnd?.getMonth()).toBe(3)
      expect(weeklyEnd?.getDate()).toBe(13)
      expect(weeklyEnd?.getHours()).toBe(0)

      expect(monthlyEnd?.getFullYear()).toBe(2026)
      expect(monthlyEnd?.getMonth()).toBe(4)
      expect(monthlyEnd?.getDate()).toBe(1)
      expect(monthlyEnd?.getHours()).toBe(0)
      expect(getPeriodEnd('weekly', 'bad-key')).toBeNull()
    })
  })

  describe('isPeriodExpired', () => {
    it('treats periods as expired once the end boundary is reached', () => {
      expect(isPeriodExpired('daily', '2026-04-07', new Date(2026, 3, 8, 0, 0, 0))).toBe(true)
      expect(isPeriodExpired('daily', '2026-04-07', new Date(2026, 3, 7, 23, 59, 59))).toBe(false)
      expect(isPeriodExpired('monthly', 'bad-key', new Date('2026-04-07T00:00:00Z'))).toBe(false)
    })
  })

  describe('getCardPeriodEnum', () => {
    it('reads the period from direct fields or subtitle text', () => {
      expect(getCardPeriodEnum({ period: 'weekly' })).toBe('weekly')
      expect(getCardPeriodEnum({ recurrence: 'Monthly' })).toBe('monthly')
      expect(getCardPeriodEnum({ repeat: 'Daily' })).toBe('daily')
      expect(getCardPeriodEnum({ subtitle: 'Weekly opening routine' })).toBe('weekly')
      expect(getCardPeriodEnum({ subtitle: 'No schedule listed' })).toBeNull()
      expect(getCardPeriodEnum(null)).toBeNull()
    })
  })

  describe('seedCompletionMetaForCurrentPeriod', () => {
    it('seeds completed tasks with current period metadata without overwriting existing values', () => {
      const now = new Date('2026-04-07T09:30:00Z')
      const cards = [
        {
          period: 'weekly',
          sections: [
            {
              items: [
                { state: 'completed' },
                {
                  state: 'completed',
                  completedForPeriodKey: 'custom-key',
                  completedAt: '2026-04-01T10:00:00Z',
                },
                { state: 'todo' },
              ],
            },
          ],
        },
      ]

      seedCompletionMetaForCurrentPeriod(cards, now)

      expect(cards[0].sections[0].items[0]).toMatchObject({
        completedForPeriodKey: '2026-W15',
        completedAt: '2026-04-07T09:30:00.000Z',
      })
      expect(cards[0].sections[0].items[1]).toMatchObject({
        completedForPeriodKey: 'custom-key',
        completedAt: '2026-04-01T10:00:00Z',
      })
      expect(cards[0].sections[0].items[2]).not.toHaveProperty('completedForPeriodKey')
    })
  })

  describe('deriveDisplayCards', () => {
    it('resets stale completed and pending task state for a new period', () => {
      const now = new Date('2026-04-07T09:30:00Z')
      const cards = [
        {
          id: 'daily-card',
          period: 'daily',
          sections: [
            {
              items: [
                {
                  id: 'completed-old',
                  state: 'completed',
                  highlighted: true,
                  completedForPeriodKey: '2026-04-06',
                },
                {
                  id: 'pending-old',
                  state: 'pending',
                  highlighted: true,
                  pendingForPeriodKey: '2026-04-06',
                },
                {
                  id: 'current',
                  state: 'completed',
                  highlighted: true,
                  completedForPeriodKey: '2026-04-07',
                },
              ],
            },
          ],
        },
      ]

      const [displayCard] = deriveDisplayCards(cards, { now })

      expect(displayCard.__sourceIndex).toBe(0)
      expect(displayCard.sections[0].items[0]).toMatchObject({
        state: 'todo',
        highlighted: false,
      })
      expect(displayCard.sections[0].items[1]).toMatchObject({
        state: 'todo',
        highlighted: false,
      })
      expect(displayCard.sections[0].items[2]).toMatchObject({
        state: 'completed',
        highlighted: true,
      })
      expect(cards[0].sections[0].items[0].state).toBe('completed')
    })

    it('filters cards by the selected active period while keeping cards with unknown recurrence', () => {
      const cards = [
        { id: 'daily-card', period: 'daily', sections: [] },
        { id: 'weekly-card', period: 'weekly', sections: [] },
        { id: 'unknown-card', sections: [] },
      ]

      const displayCards = deriveDisplayCards(cards, {
        activePeriodLabel: 'Weekly',
        now: new Date('2026-04-07T09:30:00Z'),
      })

      expect(displayCards.map((card) => card.id)).toEqual(['weekly-card', 'unknown-card'])
    })
  })
})
