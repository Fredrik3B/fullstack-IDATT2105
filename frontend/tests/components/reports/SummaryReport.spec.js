import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { vi } from 'vitest'

vi.mock('@/components/reports/ComplianceStatsGrid.vue', () => ({
  default: {
    name: 'ComplianceStatsGrid',
    template: '<div class="compliance-stats-grid"></div>',
    props: ['foodStats', 'alcoholStats'],
  },
}))

import SummaryReport from '@/components/reports/SummaryReport.vue'
import { formatDate, formatDateTime } from '@/components/reports/reportHelpers.js'

// ── Helpers ────────────────────────────────────────────────────────────────

function makeReport(overrides = {}) {
  return {
    period: { from: '2024-01-01', to: '2024-01-31' },
    foodStats: { completedTasks: 8, totalTasks: 10, completionRate: 80, deviatedTasks: 0 },
    alcoholStats: { completedTasks: 5, totalTasks: 5, completionRate: 100, deviatedTasks: 0 },
    unresolvedItems: [],
    ...overrides,
  }
}

function mountReport(overrides = {}) {
  return mount(SummaryReport, {
    props: { report: makeReport(overrides) },
  })
}

describe('SummaryReport', () => {

  // ── Period display ─────────────────────────────────────────────────────

  describe('period display', () => {
    it('shows the formatted from and to dates in the period card', () => {
      const wrapper = mountReport()
      const periodText = wrapper.find('.period-value').text()
      expect(periodText).toContain(formatDate('2024-01-01'))
      expect(periodText).toContain(formatDate('2024-01-31'))
    })

    it('does not show raw ISO strings in the period card', () => {
      const wrapper = mountReport()
      const periodText = wrapper.find('.period-value').text()
      expect(periodText).not.toContain('2024-01-01')
      expect(periodText).not.toContain('2024-01-31')
    })
  })

  // ── ComplianceStatsGrid ────────────────────────────────────────────────

  describe('compliance stats grid', () => {
    it('renders the ComplianceStatsGrid', () => {
      const wrapper = mountReport()
      expect(wrapper.find('.compliance-stats-grid').exists()).toBe(true)
    })

    it('passes foodStats and alcoholStats to ComplianceStatsGrid', () => {
      const foodStats = { completedTasks: 3, totalTasks: 5, completionRate: 60, deviatedTasks: 1 }
      const alcoholStats = { completedTasks: 2, totalTasks: 4, completionRate: 50, deviatedTasks: 0 }
      const wrapper = mount(SummaryReport, {
        props: { report: makeReport({ foodStats, alcoholStats }) },
      })

      const grid = wrapper.findComponent({ name: 'ComplianceStatsGrid' })
      expect(grid.props('foodStats')).toEqual(foodStats)
      expect(grid.props('alcoholStats')).toEqual(alcoholStats)
    })
  })

  // ── Unresolved items ───────────────────────────────────────────────────

  describe('unresolved items', () => {
    it('shows "All clear" when there are no unresolved items', () => {
      const wrapper = mountReport({ unresolvedItems: [] })
      expect(wrapper.text()).toContain('All clear')
      expect(wrapper.text()).toContain('No unresolved items in this period')
    })

    it('does NOT show "All clear" when there are unresolved items', () => {
      const wrapper = mountReport({
        unresolvedItems: [{ name: 'Daily fridge check', notDoneBy: null }],
      })
      expect(wrapper.text()).not.toContain('All clear')
    })

    it('shows the item count in the section heading', () => {
      const wrapper = mountReport({
        unresolvedItems: [
          { name: 'Fridge check', notDoneBy: null },
          { name: 'Temperature log', notDoneBy: null },
        ],
      })
      expect(wrapper.text()).toContain('Unresolved items (2)')
    })

    it('renders each unresolved item name', () => {
      const wrapper = mountReport({
        unresolvedItems: [
          { name: 'Fridge check', notDoneBy: null },
          { name: 'Temperature log', notDoneBy: null },
        ],
      })
      expect(wrapper.text()).toContain('Fridge check')
      expect(wrapper.text()).toContain('Temperature log')
    })

    it('shows formatted notDoneBy datetime when present', () => {
      const timestamp = '2024-01-15T09:00:00'
      const wrapper = mountReport({
        unresolvedItems: [{ name: 'Fridge check', notDoneBy: timestamp }],
      })
      // The component formats via formatDateTime — check the formatted output appears
      expect(wrapper.text()).toContain(formatDateTime(timestamp))
    })

    it('does not show notDoneBy details when the field is absent', () => {
      const wrapper = mountReport({
        unresolvedItems: [{ name: 'Fridge check' }],
      })
      // Should render the item name but no timestamp cell
      const cards = wrapper.findAll('.deviation-card')
      expect(cards).toHaveLength(1)
      expect(cards[0].find('.deviation-details').exists()).toBe(false)
    })
  })

})
