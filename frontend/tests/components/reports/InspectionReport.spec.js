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

vi.mock('@/components/ui/DeviationTrendChart.vue', () => ({
  default: {
    name: 'DeviationTrendChart',
    template: '<div class="deviation-trend-chart"></div>',
    props: ['points'],
  },
}))

vi.mock('@/components/ui/TemperatureChart.vue', () => ({
  default: {
    name: 'TemperatureChart',
    template: '<div class="temperature-chart"></div>',
    props: ['log'],
  },
}))

import InspectionReport from '@/components/reports/InspectionReport.vue'
import { formatDate, formatDateTime } from '@/components/reports/reportHelpers.js'

// ── Helpers ────────────────────────────────────────────────────────────────

function makeChecklist(overrides = {}) {
  return {
    name: 'Daily Food Check',
    complianceArea: 'IK_MAT',
    frequency: 'DAILY',
    completionsInPeriod: 5,
    expectedRuns: 7,
    averageCompletionRate: 85,
    deviatedTasks: 0,
    ...overrides,
  }
}

function makeReport(overrides = {}) {
  return {
    organization: {
      name: 'Pizza Palace',
      adminNames: ['Alice'],
      managerNames: ['Bob'],
      totalStaff: 10,
    },
    period: { from: '2024-01-01', to: '2024-01-31' },
    generatedAt: '2024-01-31T12:00:00',
    foodStats: { completedTasks: 8, totalTasks: 10, completionRate: 80, deviatedTasks: 0 },
    alcoholStats: { completedTasks: 5, totalTasks: 5, completionRate: 100, deviatedTasks: 0 },
    checklists: { checklists: [] },
    temperatureLog: [],
    deviationsByDay: [],
    missedTasks: [],
    ...overrides,
  }
}

function mountReport(overrides = {}) {
  return mount(InspectionReport, {
    props: { report: makeReport(overrides) },
  })
}

describe('InspectionReport', () => {

  // ── Organization header ────────────────────────────────────────────────

  describe('organization header', () => {
    it('shows the organization name', () => {
      const wrapper = mountReport()
      expect(wrapper.find('.report-org-name').text()).toBe('Pizza Palace')
    })

    it('shows the formatted period', () => {
      const wrapper = mountReport()
      const periodText = wrapper.find('.report-period').text()
      expect(periodText).toContain(formatDate('2024-01-01'))
      expect(periodText).toContain(formatDate('2024-01-31'))
    })

    it('does not show raw ISO period strings', () => {
      const wrapper = mountReport()
      const periodText = wrapper.find('.report-period').text()
      expect(periodText).not.toContain('2024-01-01')
    })

    it('shows the formatted generatedAt timestamp', () => {
      const wrapper = mountReport()
      expect(wrapper.find('.report-generated').text()).toContain(formatDateTime('2024-01-31T12:00:00'))
    })

    it('shows admin names comma-separated', () => {
      const wrapper = mountReport({
        organization: { name: 'X', adminNames: ['Alice', 'Charlie'], managerNames: [], totalStaff: 5 },
      })
      const adminsEl = wrapper.findAll('.org-detail-value')[0]
      expect(adminsEl.text()).toContain('Alice')
      expect(adminsEl.text()).toContain('Charlie')
    })

    it('shows "None" when adminNames is empty', () => {
      const wrapper = mountReport({
        organization: { name: 'X', adminNames: [], managerNames: [], totalStaff: 5 },
      })
      const adminsEl = wrapper.findAll('.org-detail-value')[0]
      expect(adminsEl.text()).toBe('None')
    })

    it('shows total staff count', () => {
      const wrapper = mountReport({
        organization: { name: 'X', adminNames: [], managerNames: [], totalStaff: 12 },
      })
      const staffEl = wrapper.findAll('.org-detail-value')[2]
      expect(staffEl.text()).toBe('12')
    })
  })

  // ── Checklist insight cards ────────────────────────────────────────────

  describe('checklist insight cards', () => {
    it('shows 0 total completions when checklists array is empty', () => {
      const wrapper = mountReport({ checklists: { checklists: [] } })
      const cards = wrapper.findAll('.insight-card')
      expect(cards[0].find('.insight-value').text()).toBe('0')
    })

    it('sums completionsInPeriod across all checklists', () => {
      const wrapper = mountReport({
        checklists: {
          checklists: [
            makeChecklist({ completionsInPeriod: 4 }),
            makeChecklist({ completionsInPeriod: 6 }),
          ],
        },
      })
      const cards = wrapper.findAll('.insight-card')
      expect(cards[0].find('.insight-value').text()).toBe('10')
    })

    it('shows 0% avg completion rate when no checklists', () => {
      const wrapper = mountReport({ checklists: { checklists: [] } })
      const cards = wrapper.findAll('.insight-card')
      expect(cards[1].find('.insight-value').text()).toBe('0.0%')
    })

    it('shows correct average completion rate across checklists', () => {
      const wrapper = mountReport({
        checklists: {
          checklists: [
            makeChecklist({ averageCompletionRate: 80 }),
            makeChecklist({ averageCompletionRate: 60 }),
          ],
        },
      })
      const cards = wrapper.findAll('.insight-card')
      expect(cards[1].find('.insight-value').text()).toBe('70.0%')
    })
  })

  // ── Checklist table ────────────────────────────────────────────────────

  describe('checklist performance table', () => {
    it('shows "No checklists in this period" when checklists array is empty', () => {
      const wrapper = mountReport({ checklists: { checklists: [] } })
      expect(wrapper.text()).toContain('No checklists in this period')
    })

    it('renders a row for each checklist', () => {
      const wrapper = mountReport({
        checklists: {
          checklists: [
            makeChecklist({ name: 'Food Check' }),
            makeChecklist({ name: 'Alcohol Check', complianceArea: 'IK_ALKOHOL' }),
          ],
        },
      })
      expect(wrapper.text()).toContain('Food Check')
      expect(wrapper.text()).toContain('Alcohol Check')
    })

    it('shows Food badge for IK_MAT area', () => {
      const wrapper = mountReport({
        checklists: { checklists: [makeChecklist({ complianceArea: 'IK_MAT' })] },
      })
      const badge = wrapper.find('.module-badge--food')
      expect(badge.exists()).toBe(true)
      expect(badge.text()).toBe('Food')
    })

    it('shows Alcohol badge for non-IK_MAT area', () => {
      const wrapper = mountReport({
        checklists: { checklists: [makeChecklist({ complianceArea: 'IK_ALKOHOL' })] },
      })
      const badge = wrapper.find('.module-badge--alcohol')
      expect(badge.exists()).toBe(true)
      expect(badge.text()).toBe('Alcohol')
    })
  })

  // ── Deviations by checklist table ──────────────────────────────────────

  describe('deviations by checklist table', () => {
    it('shows "No checklist deviations" when no checklists have deviations', () => {
      const wrapper = mountReport({
        checklists: {
          checklists: [makeChecklist({ deviatedTasks: 0 })],
        },
      })
      expect(wrapper.text()).toContain('No checklist deviations in this period')
    })

    it('only shows checklists that have deviatedTasks > 0', () => {
      const wrapper = mountReport({
        checklists: {
          checklists: [
            makeChecklist({ name: 'Has Deviations', deviatedTasks: 3 }),
            makeChecklist({ name: 'Clean', deviatedTasks: 0 }),
          ],
        },
      })
      // The deviation insight table is the second .report-table
      const tables = wrapper.findAll('.report-table')
      const deviationTable = tables[1]
      expect(deviationTable.text()).toContain('Has Deviations')
      expect(deviationTable.text()).not.toContain('Clean')
    })

    it('sorts deviation rows by deviatedTasks descending', () => {
      const wrapper = mountReport({
        checklists: {
          checklists: [
            makeChecklist({ name: 'Low', deviatedTasks: 1 }),
            makeChecklist({ name: 'High', deviatedTasks: 5 }),
            makeChecklist({ name: 'Mid', deviatedTasks: 3 }),
          ],
        },
      })
      const tables = wrapper.findAll('.report-table')
      const deviationTable = tables[1]
      const rows = deviationTable.findAll('.table-row--insights')
      const names = rows.map(r => r.find('.cell-name').text())
      expect(names).toEqual(['High', 'Mid', 'Low'])
    })

    it('uses alphabetical order as tiebreak when deviatedTasks are equal', () => {
      const wrapper = mountReport({
        checklists: {
          checklists: [
            makeChecklist({ name: 'Zebra', deviatedTasks: 2 }),
            makeChecklist({ name: 'Alpha', deviatedTasks: 2 }),
          ],
        },
      })
      const tables = wrapper.findAll('.report-table')
      const deviationTable = tables[1]
      const rows = deviationTable.findAll('.table-row--insights')
      const names = rows.map(r => r.find('.cell-name').text())
      expect(names).toEqual(['Alpha', 'Zebra'])
    })

    it('caps the deviation table at 8 rows', () => {
      const checklists = Array.from({ length: 10 }, (_, i) =>
        makeChecklist({ name: `Check ${i}`, deviatedTasks: i + 1 }),
      )
      const wrapper = mountReport({ checklists: { checklists } })
      const tables = wrapper.findAll('.report-table')
      const deviationTable = tables[1]
      expect(deviationTable.findAll('.table-row--insights')).toHaveLength(8)
    })
  })

  // ── Temperature section ────────────────────────────────────────────────

  describe('temperature trends section', () => {
    it('hides the temperature section when temperatureLog is empty', () => {
      const wrapper = mountReport({ temperatureLog: [] })
      expect(wrapper.text()).not.toContain('Temperature trends by zone')
    })

    it('shows the temperature section when temperatureLog has entries', () => {
      const wrapper = mountReport({
        temperatureLog: [{
          zoneId: 1, zoneName: 'Cold Room', taskName: 'Cold Room',
          targetMin: 2, targetMax: 8, measuredAt: '2024-01-10T08:00:00',
        }],
      })
      expect(wrapper.text()).toContain('Temperature trends by zone')
    })

    it('groups readings by zoneId into a single zone card', () => {
      const wrapper = mountReport({
        temperatureLog: [
          { zoneId: 1, zoneName: 'Cold Room', taskName: 'Cold Room', targetMin: 2, targetMax: 8, measuredAt: '2024-01-10T08:00:00' },
          { zoneId: 1, zoneName: 'Cold Room', taskName: 'Cold Room', targetMin: 2, targetMax: 8, measuredAt: '2024-01-10T14:00:00' },
        ],
      })
      expect(wrapper.findAll('.temperature-zone-card')).toHaveLength(1)
    })

    it('creates separate zone cards for entries with different zoneIds', () => {
      const wrapper = mountReport({
        temperatureLog: [
          { zoneId: 1, zoneName: 'Cold Room', taskName: 'Cold Room', targetMin: 2, targetMax: 8, measuredAt: '2024-01-10T08:00:00' },
          { zoneId: 2, zoneName: 'Freezer', taskName: 'Freezer', targetMin: -20, targetMax: -15, measuredAt: '2024-01-10T08:00:00' },
        ],
      })
      expect(wrapper.findAll('.temperature-zone-card')).toHaveLength(2)
    })

    it('shows zone reading count', () => {
      const wrapper = mountReport({
        temperatureLog: [
          { zoneId: 1, zoneName: 'Cold Room', taskName: 'Cold Room', targetMin: 2, targetMax: 8, measuredAt: '2024-01-10T08:00:00' },
          { zoneId: 1, zoneName: 'Cold Room', taskName: 'Cold Room', targetMin: 2, targetMax: 8, measuredAt: '2024-01-10T14:00:00' },
        ],
      })
      expect(wrapper.find('.temperature-zone-meta').text()).toContain('2 readings')
    })

    it('sorts zone cards alphabetically by label', () => {
      const wrapper = mountReport({
        temperatureLog: [
          { zoneId: 2, zoneName: 'Zebra Zone', taskName: 'Z', targetMin: null, targetMax: null, measuredAt: '2024-01-10T08:00:00' },
          { zoneId: 1, zoneName: 'Alpha Zone', taskName: 'A', targetMin: null, targetMax: null, measuredAt: '2024-01-10T08:00:00' },
        ],
      })
      const titles = wrapper.findAll('.temperature-zone-title').map(el => el.text())
      expect(titles[0]).toContain('Alpha Zone')
      expect(titles[1]).toContain('Zebra Zone')
    })
  })

  // ── Deviation trend chart ──────────────────────────────────────────────

  describe('deviation trend section', () => {
    it('shows "No deviation trend" message when deviationsByDay is empty', () => {
      const wrapper = mountReport({ deviationsByDay: [] })
      expect(wrapper.text()).toContain('No deviation trend in this period')
    })

    it('renders DeviationTrendChart when deviationsByDay has entries', () => {
      const wrapper = mountReport({
        deviationsByDay: [{ date: '2024-01-10', count: 2 }],
      })
      expect(wrapper.find('.deviation-trend-chart').exists()).toBe(true)
    })
  })

  // ── Most missed tasks table ────────────────────────────────────────────

  describe('most missed tasks table', () => {
    it('shows "No missed tasks" when missedTasks is empty', () => {
      const wrapper = mountReport({ missedTasks: [] })
      expect(wrapper.text()).toContain('No missed tasks in this period')
    })

    it('renders each missed task name and checklist', () => {
      const wrapper = mountReport({
        missedTasks: [
          { taskName: 'Check fridge', checklistName: 'Daily Food', complianceArea: 'IK_MAT', missedCount: 3 },
          { taskName: 'Check ID', checklistName: 'Alcohol', complianceArea: 'IK_ALKOHOL', missedCount: 1 },
        ],
      })
      expect(wrapper.text()).toContain('Check fridge')
      expect(wrapper.text()).toContain('Daily Food')
      expect(wrapper.text()).toContain('Check ID')
    })

    it('applies stat-danger to missed count when > 0', () => {
      const wrapper = mountReport({
        missedTasks: [
          { taskName: 'Check fridge', checklistName: 'Daily Food', complianceArea: 'IK_MAT', missedCount: 3 },
        ],
      })
      // The missed task table is the last .report-table
      const tables = wrapper.findAll('.report-table')
      const missedTable = tables[tables.length - 1]
      expect(missedTable.find('.stat-danger').exists()).toBe(true)
    })
  })

})
