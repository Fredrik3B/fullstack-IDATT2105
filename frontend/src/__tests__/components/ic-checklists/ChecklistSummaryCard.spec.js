import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import ChecklistSummaryCard from '@/components/ic-checklists/ChecklistSummaryCard.vue'

describe('ChecklistSummaryCard', () => {
  it('renders totals, legend counts, and percentage widths from checklist cards', () => {
    const wrapper = mount(ChecklistSummaryCard, {
      props: {
        cards: [
          {
            sections: [
              {
                items: [
                  { state: 'completed' },
                  { state: 'pending' },
                  { state: 'todo' },
                  { state: 'completed' },
                ],
              },
            ],
          },
        ],
      },
    })

    expect(wrapper.text()).toContain('4 tasks')
    expect(wrapper.text()).toContain('Completed')
    expect(wrapper.text()).toContain('Flagged')
    expect(wrapper.text()).toContain('Not started')
    expect(wrapper.find('.summary-bar').attributes('aria-label')).toBe(
      'Completed 2, flagged 1, not started 1',
    )

    const segments = wrapper.findAll('.summary-segment')
    expect(segments[0].attributes('style')).toContain('width: 50%')
    expect(segments[1].attributes('style')).toContain('width: 25%')
    expect(segments[2].attributes('style')).toContain('width: 25%')
  })

  it('falls back to zero totals when no cards are provided', () => {
    const wrapper = mount(ChecklistSummaryCard, {
      props: {
        cards: [],
      },
    })

    expect(wrapper.text()).toContain('0 tasks')
    expect(wrapper.find('.summary-bar').attributes('aria-label')).toBe(
      'Completed 0, flagged 0, not started 0',
    )
    wrapper.findAll('.summary-segment').forEach((segment) => {
      expect(segment.attributes('style')).toContain('width: 0%')
    })
  })
})
