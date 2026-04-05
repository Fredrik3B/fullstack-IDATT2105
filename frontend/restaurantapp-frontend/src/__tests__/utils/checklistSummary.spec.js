import { describe, it, expect } from 'vitest'
import { countTaskStates, getSummaryAriaLabel } from '@/features/ic-checklists/checklistSummary'

describe('countTaskStates', () => {
  it('returns all-zero counts for an empty array', () => {
    const result = countTaskStates([])
    expect(result).toEqual({ completed: 0, pending: 0, todo: 0, total: 0, completedPct: 0, pendingPct: 0, todoPct: 0 })
  })

  it('returns all-zero counts for a non-array input', () => {
    expect(countTaskStates(null)).toEqual({ completed: 0, pending: 0, todo: 0, total: 0, completedPct: 0, pendingPct: 0, todoPct: 0 })
    expect(countTaskStates(undefined)).toEqual({ completed: 0, pending: 0, todo: 0, total: 0, completedPct: 0, pendingPct: 0, todoPct: 0 })
  })

  it('counts a single completed task', () => {
    const cards = [{ sections: [{ items: [{ state: 'completed' }] }] }]
    const result = countTaskStates(cards)
    expect(result.completed).toBe(1)
    expect(result.pending).toBe(0)
    expect(result.todo).toBe(0)
    expect(result.total).toBe(1)
    expect(result.completedPct).toBe(100)
  })

  it('counts a single pending task', () => {
    const cards = [{ sections: [{ items: [{ state: 'pending' }] }] }]
    const result = countTaskStates(cards)
    expect(result.pending).toBe(1)
    expect(result.completed).toBe(0)
    expect(result.todo).toBe(0)
  })

  it('treats unknown state as todo', () => {
    const cards = [{ sections: [{ items: [{ state: 'unknown' }] }] }]
    const result = countTaskStates(cards)
    expect(result.todo).toBe(1)
  })

  it('treats missing state as todo', () => {
    const cards = [{ sections: [{ items: [{}] }] }]
    const result = countTaskStates(cards)
    expect(result.todo).toBe(1)
  })

  it('counts across multiple sections and cards', () => {
    const cards = [
      {
        sections: [
          { items: [{ state: 'completed' }, { state: 'pending' }] },
          { items: [{ state: 'todo' }] },
        ],
      },
      {
        sections: [{ items: [{ state: 'completed' }, { state: 'completed' }] }],
      },
    ]
    const result = countTaskStates(cards)
    expect(result.completed).toBe(3)
    expect(result.pending).toBe(1)
    expect(result.todo).toBe(1)
    expect(result.total).toBe(5)
  })

  it('calculates correct percentages', () => {
    const cards = [
      {
        sections: [
          {
            items: [
              { state: 'completed' },
              { state: 'completed' },
              { state: 'pending' },
              { state: 'todo' },
            ],
          },
        ],
      },
    ]
    const result = countTaskStates(cards)
    expect(result.completedPct).toBe(50)
    expect(result.pendingPct).toBe(25)
    expect(result.todoPct).toBe(25)
  })

  it('returns zero percentages when total is 0', () => {
    const result = countTaskStates([])
    expect(result.completedPct).toBe(0)
    expect(result.pendingPct).toBe(0)
    expect(result.todoPct).toBe(0)
  })

  it('handles cards without sections gracefully', () => {
    const cards = [{ sections: null }, { sections: undefined }, {}]
    const result = countTaskStates(cards)
    expect(result.total).toBe(0)
  })

  it('handles sections without items gracefully', () => {
    const cards = [{ sections: [{ items: null }, { items: undefined }, {}] }]
    const result = countTaskStates(cards)
    expect(result.total).toBe(0)
  })
})

describe('getSummaryAriaLabel', () => {
  it('returns correct aria label for given totals', () => {
    const label = getSummaryAriaLabel({ completed: 3, pending: 1, todo: 2 })
    expect(label).toBe('Completed 3, flagged 1, not started 2')
  })

  it('returns zeros when totals is null', () => {
    const label = getSummaryAriaLabel(null)
    expect(label).toBe('Completed 0, flagged 0, not started 0')
  })

  it('returns zeros when totals is undefined', () => {
    const label = getSummaryAriaLabel(undefined)
    expect(label).toBe('Completed 0, flagged 0, not started 0')
  })

  it('returns correct label for all-zero totals', () => {
    const label = getSummaryAriaLabel({ completed: 0, pending: 0, todo: 0 })
    expect(label).toBe('Completed 0, flagged 0, not started 0')
  })
})
