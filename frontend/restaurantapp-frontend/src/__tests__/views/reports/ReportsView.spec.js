import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import ReportsView from '@/views/ReportsView.vue'

// ── Mocks ──────────────────────────────────────────────────────────────────

const mockRouteQuery = vi.fn(() => ({}))

vi.mock('vue-router', () => ({
  useRoute: () => ({ get query() { return mockRouteQuery() } }),
}))

vi.mock('@/api/reports', () => ({
  fetchInspectionReport: vi.fn(),
  fetchSummaryReport: vi.fn(),
}))

vi.mock('@/components/layout/PageHeader.vue', () => ({
  default: {
    template: '<div class="page-header"><slot name="actions" /></div>',
    props: ['eyebrow', 'title', 'description'],
  },
}))

vi.mock('@/components/reports/ReportFilterBar.vue', () => ({
  default: {
    name: 'ReportFilterBar',
    template: `
      <div class="report-filter-bar">
        <button class="btn-generate" @click="$emit('generate')">Generate</button>
        <button class="btn-export" @click="$emit('export')">Export</button>
        <button class="btn-deviation" @click="$emit('deviation')">New Deviation</button>
      </div>
    `,
    props: ['reportType', 'fromDate', 'toDate', 'loading', 'hasReport'],
    emits: ['update:reportType', 'update:fromDate', 'update:toDate', 'generate', 'export', 'deviation'],
  },
}))

vi.mock('@/components/reports/InspectionReport.vue', () => ({
  default: {
    name: 'InspectionReport',
    template: '<div class="inspection-report">Inspection Report</div>',
    props: ['report'],
  },
}))

vi.mock('@/components/reports/SummaryReport.vue', () => ({
  default: {
    name: 'SummaryReport',
    template: '<div class="summary-report">Summary Report</div>',
    props: ['report'],
  },
}))

vi.mock('@/components/reports/DeviationReportForm.vue', () => ({
  default: {
    name: 'DeviationReportForm',
    template: `
      <div class="deviation-report-form">
        <button class="btn-cancel" @click="$emit('cancel')">Cancel</button>
        <button class="btn-submitted" @click="$emit('submitted')">Submit</button>
      </div>
    `,
    emits: ['cancel', 'submitted'],
  },
}))

import { fetchInspectionReport, fetchSummaryReport } from '@/api/reports'

// ── Helpers ────────────────────────────────────────────────────────────────

function mountView() {
  return mount(ReportsView, { attachTo: document.body })
}

function dateString(d) {
  return d.toISOString().slice(0, 10)
}

// ── Setup ──────────────────────────────────────────────────────────────────

beforeEach(() => {
  vi.clearAllMocks()
  mockRouteQuery.mockReturnValue({})
  fetchInspectionReport.mockResolvedValue({ sections: [] })
  fetchSummaryReport.mockResolvedValue({ totalDeviations: 0 })
})

// ── Tests ──────────────────────────────────────────────────────────────────

describe('ReportsView', () => {

  // ── Default state ───────────────────────────────────────────────────────

  describe('default state', () => {
    it('shows the empty-state prompt before any report is generated', async () => {
      const wrapper = mountView()
      await flushPromises()

      expect(wrapper.text()).toContain('No report generated')
      expect(wrapper.find('.inspection-report').exists()).toBe(false)
      expect(wrapper.find('.summary-report').exists()).toBe(false)
    })

    it('defaults fromDate to one month ago and toDate to today', async () => {
      const wrapper = mountView()
      await flushPromises()

      const today = new Date()
      const monthAgo = new Date(today)
      monthAgo.setMonth(monthAgo.getMonth() - 1)

      const insightText = wrapper.find('.insight-value').text()
      expect(insightText).toContain(dateString(monthAgo))
      expect(insightText).toContain(dateString(today))
    })

    it('does not call any report API on mount without autoload', async () => {
      mountView()
      await flushPromises()

      expect(fetchInspectionReport).not.toHaveBeenCalled()
      expect(fetchSummaryReport).not.toHaveBeenCalled()
    })
  })

  // ── loadReport: API calls ───────────────────────────────────────────────

  describe('loadReport — API dispatch', () => {
    it('calls fetchInspectionReport (not summary) when reportType is inspection', async () => {
      const wrapper = mountView()
      await flushPromises()

      await wrapper.find('.btn-generate').trigger('click')
      await flushPromises()

      expect(fetchInspectionReport).toHaveBeenCalledOnce()
      expect(fetchSummaryReport).not.toHaveBeenCalled()
    })

    it('appends T00:00:00 to fromDate and T23:59:59 to toDate in the API call', async () => {
      const wrapper = mountView()
      await flushPromises()

      await wrapper.find('.btn-generate').trigger('click')
      await flushPromises()

      const { from, to } = fetchInspectionReport.mock.calls[0][0]
      // Verify the date part is present and the correct time suffix is appended
      expect(from).toMatch(/^\d{4}-\d{2}-\d{2}T00:00:00$/)
      expect(to).toMatch(/^\d{4}-\d{2}-\d{2}T23:59:59$/)
    })

    it('calls fetchSummaryReport (not inspection) when reportType is summary', async () => {
      const wrapper = mountView()
      await flushPromises()

      // Change report type via v-model before generating
      await wrapper.findComponent({ name: 'ReportFilterBar' }).vm.$emit('update:reportType', 'summary')
      await wrapper.find('.btn-generate').trigger('click')
      await flushPromises()

      expect(fetchSummaryReport).toHaveBeenCalledOnce()
      expect(fetchInspectionReport).not.toHaveBeenCalled()
    })
  })

  // ── loadReport: UI states ───────────────────────────────────────────────

  describe('loadReport — UI states', () => {
    it('shows loading text while the request is in flight', async () => {
      let resolve
      fetchInspectionReport.mockReturnValue(new Promise(r => { resolve = r }))

      const wrapper = mountView()
      await flushPromises()
      await wrapper.find('.btn-generate').trigger('click')
      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('Generating report...')
      expect(wrapper.find('.inspection-report').exists()).toBe(false)

      resolve({ sections: [] })
      await flushPromises()

      expect(wrapper.text()).not.toContain('Generating report...')
    })

    it('shows InspectionReport (and not SummaryReport) after a successful inspection load', async () => {
      const wrapper = mountView()
      await flushPromises()

      await wrapper.find('.btn-generate').trigger('click')
      await flushPromises()

      expect(wrapper.find('.inspection-report').exists()).toBe(true)
      expect(wrapper.find('.summary-report').exists()).toBe(false)
      expect(wrapper.find('.empty-state').exists()).toBe(false)
    })

    it('shows SummaryReport (and not InspectionReport) after a successful summary load', async () => {
      const wrapper = mountView()
      await flushPromises()

      await wrapper.findComponent({ name: 'ReportFilterBar' }).vm.$emit('update:reportType', 'summary')
      await wrapper.find('.btn-generate').trigger('click')
      await flushPromises()

      expect(wrapper.find('.summary-report').exists()).toBe(true)
      expect(wrapper.find('.inspection-report').exists()).toBe(false)
    })

    it('shows error state with message when the request fails', async () => {
      fetchInspectionReport.mockRejectedValue({ message: 'Server unavailable' })

      const wrapper = mountView()
      await flushPromises()
      await wrapper.find('.btn-generate').trigger('click')
      await flushPromises()

      expect(wrapper.text()).toContain('Failed to load report')
      expect(wrapper.text()).toContain('Server unavailable')
      expect(wrapper.find('.inspection-report').exists()).toBe(false)
    })

    it('prefers error.response.data.detail over error.message', async () => {
      fetchInspectionReport.mockRejectedValue({
        response: { data: { detail: 'Specific API reason' } },
        message: 'Generic fallback',
      })

      const wrapper = mountView()
      await flushPromises()
      await wrapper.find('.btn-generate').trigger('click')
      await flushPromises()

      expect(wrapper.text()).toContain('Specific API reason')
      expect(wrapper.text()).not.toContain('Generic fallback')
    })

    it('falls back to "Unknown error" when error has neither detail nor message', async () => {
      fetchInspectionReport.mockRejectedValue({})

      const wrapper = mountView()
      await flushPromises()
      await wrapper.find('.btn-generate').trigger('click')
      await flushPromises()

      expect(wrapper.text()).toContain('Unknown error')
    })

    it('clears a previous error when a new load succeeds', async () => {
      fetchInspectionReport
        .mockRejectedValueOnce({ message: 'First load failed' })
        .mockResolvedValueOnce({ sections: [] })

      const wrapper = mountView()
      await flushPromises()

      await wrapper.find('.btn-generate').trigger('click')
      await flushPromises()
      expect(wrapper.text()).toContain('Failed to load report')

      await wrapper.find('.btn-generate').trigger('click')
      await flushPromises()

      expect(wrapper.text()).not.toContain('Failed to load report')
      expect(wrapper.find('.inspection-report').exists()).toBe(true)
    })
  })

  // ── Preset date ranges ──────────────────────────────────────────────────

  describe('preset date ranges via route query', () => {
    it('sets fromDate to 7 days ago when preset=week', async () => {
      mockRouteQuery.mockReturnValue({ preset: 'week' })

      const wrapper = mountView()
      await flushPromises()

      const expected = new Date()
      expected.setDate(expected.getDate() - 7)

      expect(wrapper.find('.insight-value').text()).toContain(dateString(expected))
    })

    it('sets fromDate to 1 month ago when preset=month', async () => {
      mockRouteQuery.mockReturnValue({ preset: 'month' })

      const wrapper = mountView()
      await flushPromises()

      const expected = new Date()
      expected.setMonth(expected.getMonth() - 1)

      expect(wrapper.find('.insight-value').text()).toContain(dateString(expected))
    })

    it('sets both fromDate and toDate to today when preset=today', async () => {
      mockRouteQuery.mockReturnValue({ preset: 'today' })

      const wrapper = mountView()
      await flushPromises()

      const today = dateString(new Date())
      const insightText = wrapper.find('.insight-value').text()

      expect(insightText).toBe(`${today} – ${today}`)
    })

    it('does not change dates for an unknown preset', async () => {
      mockRouteQuery.mockReturnValue({ preset: 'quarterly' })

      const wrapper = mountView()
      await flushPromises()

      // Should still show the default month-ago range, not today–today
      const today = new Date()
      const monthAgo = new Date(today)
      monthAgo.setMonth(monthAgo.getMonth() - 1)

      expect(wrapper.find('.insight-value').text()).toContain(dateString(monthAgo))
    })
  })

  // ── Route query intent ──────────────────────────────────────────────────

  describe('route query intent on mount', () => {
    it('opens the deviation form when action=deviation', async () => {
      mockRouteQuery.mockReturnValue({ action: 'deviation' })

      const wrapper = mountView()
      await flushPromises()

      expect(wrapper.find('.deviation-report-form').exists()).toBe(true)
    })

    it('switches reportType to summary when reportType=summary in query', async () => {
      mockRouteQuery.mockReturnValue({ reportType: 'summary', autoload: '1' })

      mountView()
      await flushPromises()

      expect(fetchSummaryReport).toHaveBeenCalledOnce()
      expect(fetchInspectionReport).not.toHaveBeenCalled()
    })

    it('auto-loads the report when autoload=1', async () => {
      mockRouteQuery.mockReturnValue({ autoload: '1' })

      mountView()
      await flushPromises()

      expect(fetchInspectionReport).toHaveBeenCalledOnce()
    })

    it('does not auto-load when autoload is absent', async () => {
      mockRouteQuery.mockReturnValue({ reportType: 'summary' })

      mountView()
      await flushPromises()

      expect(fetchInspectionReport).not.toHaveBeenCalled()
      expect(fetchSummaryReport).not.toHaveBeenCalled()
    })

    it('ignores unknown reportType values and keeps inspection as default', async () => {
      mockRouteQuery.mockReturnValue({ reportType: 'invalid', autoload: '1' })

      mountView()
      await flushPromises()

      expect(fetchInspectionReport).toHaveBeenCalledOnce()
      expect(fetchSummaryReport).not.toHaveBeenCalled()
    })
  })

  // ── Deviation form ──────────────────────────────────────────────────────

  describe('deviation form', () => {
    it('opens when the deviation button in the filter bar is clicked', async () => {
      const wrapper = mountView()
      await flushPromises()

      expect(wrapper.find('.deviation-report-form').exists()).toBe(false)
      await wrapper.find('.btn-deviation').trigger('click')
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.deviation-report-form').exists()).toBe(true)
    })

    it('closes when the form emits cancel', async () => {
      const wrapper = mountView()
      await flushPromises()
      await wrapper.find('.btn-deviation').trigger('click')
      await wrapper.vm.$nextTick()

      await wrapper.find('.btn-cancel').trigger('click')
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.deviation-report-form').exists()).toBe(false)
    })

    it('closes when clicking the modal overlay outside the form', async () => {
      const wrapper = mountView()
      await flushPromises()
      await wrapper.find('.btn-deviation').trigger('click')
      await wrapper.vm.$nextTick()

      await wrapper.find('.modal-overlay').trigger('click')
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.deviation-report-form').exists()).toBe(false)
    })

    it('closes and reloads the report when the form emits submitted (report already loaded)', async () => {
      const wrapper = mountView()
      await flushPromises()

      await wrapper.find('.btn-generate').trigger('click')
      await flushPromises()
      expect(fetchInspectionReport).toHaveBeenCalledTimes(1)

      await wrapper.find('.btn-deviation').trigger('click')
      await wrapper.vm.$nextTick()
      await wrapper.find('.btn-submitted').trigger('click')
      await flushPromises()

      expect(wrapper.find('.deviation-report-form').exists()).toBe(false)
      expect(fetchInspectionReport).toHaveBeenCalledTimes(2)
    })

    it('closes but does NOT reload when submitted and no report was previously loaded', async () => {
      const wrapper = mountView()
      await flushPromises()

      await wrapper.find('.btn-deviation').trigger('click')
      await wrapper.vm.$nextTick()
      await wrapper.find('.btn-submitted').trigger('click')
      await flushPromises()

      expect(wrapper.find('.deviation-report-form').exists()).toBe(false)
      expect(fetchInspectionReport).not.toHaveBeenCalled()
    })
  })

  // ── Export / print ──────────────────────────────────────────────────────

  describe('export', () => {
    it('calls window.print when the export button is clicked', async () => {
      const printSpy = vi.spyOn(window, 'print').mockImplementation(() => {})

      const wrapper = mountView()
      await flushPromises()
      await wrapper.find('.btn-export').trigger('click')

      expect(printSpy).toHaveBeenCalledOnce()
      printSpy.mockRestore()
    })
  })

})
