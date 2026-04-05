import { describe, it, expect } from 'vitest'
import { recalcCardProgress } from '@/features/ic-checklists/recalcCardProgress'

describe('recalcCardProgress', () => {
  it('does nothing when card is null', () => {
    expect(() => recalcCardProgress(null)).not.toThrow()
  })

  it('does nothing when card is undefined', () => {
    expect(() => recalcCardProgress(undefined)).not.toThrow()
  })

  it('sets progress to null and statusTone to "muted" when there are no tasks', () => {
    const card = { sections: [] }
    recalcCardProgress(card)
    expect(card.progress).toBeNull()
    expect(card.statusTone).toBe('muted')
  })

  it('sets progress to null when sections is missing', () => {
    const card = {}
    recalcCardProgress(card)
    expect(card.progress).toBeNull()
    expect(card.statusTone).toBe('muted')
  })

  it('calculates 0% progress when no tasks are completed', () => {
    const card = {
      sections: [{ items: [{ state: 'todo' }, { state: 'todo' }] }],
    }
    recalcCardProgress(card)
    expect(card.progress).toBe(0)
    expect(card.statusTone).toBe('muted')
  })

  it('calculates 100% progress when all tasks are completed', () => {
    const card = {
      sections: [{ items: [{ state: 'completed' }, { state: 'completed' }] }],
    }
    recalcCardProgress(card)
    expect(card.progress).toBe(100)
    expect(card.statusTone).toBe('success')
  })

  it('calculates partial progress and sets statusTone to "warning"', () => {
    const card = {
      sections: [{ items: [{ state: 'completed' }, { state: 'todo' }, { state: 'todo' }, { state: 'todo' }] }],
    }
    recalcCardProgress(card)
    expect(card.progress).toBe(25)
    expect(card.statusTone).toBe('warning')
  })

  it('sets statusLabel to "Completed" when all tasks done', () => {
    const card = {
      sections: [{ items: [{ state: 'completed' }] }],
    }
    recalcCardProgress(card)
    expect(card.statusLabel).toBe('Completed')
  })

  it('sets statusLabel to "Fullfort" when all tasks done and card uses Norwegian label', () => {
    const card = {
      statusLabel: 'fullfort',
      sections: [{ items: [{ state: 'completed' }] }],
    }
    recalcCardProgress(card)
    expect(card.statusLabel).toBe('Fullfort')
  })

  it('sets statusLabel to "X/Y completed" for partial progress', () => {
    const card = {
      sections: [{ items: [{ state: 'completed' }, { state: 'todo' }] }],
    }
    recalcCardProgress(card)
    expect(card.statusLabel).toBe('1/2 completed')
  })

  it('counts tasks across multiple sections', () => {
    const card = {
      sections: [
        { items: [{ state: 'completed' }, { state: 'completed' }] },
        { items: [{ state: 'todo' }, { state: 'todo' }] },
      ],
    }
    recalcCardProgress(card)
    expect(card.progress).toBe(50)
    expect(card.statusTone).toBe('warning')
  })

  it('handles sections with null items without throwing', () => {
    const card = { sections: [{ items: null }] }
    recalcCardProgress(card)
    expect(card.progress).toBeNull()
    expect(card.statusTone).toBe('muted')
  })

  it('rounds progress to nearest integer', () => {
    const card = {
      sections: [{ items: [{ state: 'completed' }, { state: 'todo' }, { state: 'todo' }] }],
    }
    recalcCardProgress(card)
    expect(card.progress).toBe(33)
  })
})
