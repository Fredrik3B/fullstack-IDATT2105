import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ReportFilterBar from '@/components/reports/ReportFilterBar.vue'

function mountBar(props = {}) {
  return mount(ReportFilterBar, {
    props: {
      reportType: 'inspection',
      fromDate: '2024-01-01',
      toDate: '2024-01-31',
      loading: false,
      hasReport: false,
      ...props,
    },
  })
}

describe('ReportFilterBar', () => {

  // ── Report type select ─────────────────────────────────────────────────

  describe('report type select', () => {
    it('renders both report type options', () => {
      const wrapper = mountBar()
      const options = wrapper.findAll('option')
      const values = options.map(o => o.element.value)
      expect(values).toContain('inspection')
      expect(values).toContain('summary')
    })

    it('reflects the reportType prop as the selected value', () => {
      const wrapper = mountBar({ reportType: 'summary' })
      const select = wrapper.find('select')
      expect(select.element.value).toBe('summary')
    })

    it('emits update:reportType when the select changes', async () => {
      const wrapper = mountBar()
      const select = wrapper.find('select')

      await select.setValue('summary')

      expect(wrapper.emitted('update:reportType')).toBeTruthy()
      expect(wrapper.emitted('update:reportType')[0]).toEqual(['summary'])
    })
  })

  // ── Date inputs ────────────────────────────────────────────────────────

  describe('date inputs', () => {
    it('reflects fromDate prop in the from input', () => {
      const wrapper = mountBar({ fromDate: '2024-03-01' })
      const inputs = wrapper.findAll('input[type="date"]')
      expect(inputs[0].element.value).toBe('2024-03-01')
    })

    it('reflects toDate prop in the to input', () => {
      const wrapper = mountBar({ toDate: '2024-03-31' })
      const inputs = wrapper.findAll('input[type="date"]')
      expect(inputs[1].element.value).toBe('2024-03-31')
    })

    it('emits update:fromDate when the from input changes', async () => {
      const wrapper = mountBar()
      const inputs = wrapper.findAll('input[type="date"]')
      await inputs[0].setValue('2024-06-01')

      expect(wrapper.emitted('update:fromDate')).toBeTruthy()
      expect(wrapper.emitted('update:fromDate')[0]).toEqual(['2024-06-01'])
    })

    it('emits update:toDate when the to input changes', async () => {
      const wrapper = mountBar()
      const inputs = wrapper.findAll('input[type="date"]')
      await inputs[1].setValue('2024-06-30')

      expect(wrapper.emitted('update:toDate')).toBeTruthy()
      expect(wrapper.emitted('update:toDate')[0]).toEqual(['2024-06-30'])
    })
  })

  // ── Generate button ────────────────────────────────────────────────────

  describe('generate button', () => {
    it('shows "Generate report" when not loading', () => {
      const wrapper = mountBar({ loading: false })
      expect(wrapper.find('.btn-generate').text()).toBe('Generate report')
    })

    it('shows "Generating..." when loading', () => {
      const wrapper = mountBar({ loading: true })
      expect(wrapper.find('.btn-generate').text()).toBe('Generating...')
    })

    it('is disabled when loading=true', () => {
      const wrapper = mountBar({ loading: true })
      expect(wrapper.find('.btn-generate').element.disabled).toBe(true)
    })

    it('is enabled when loading=false', () => {
      const wrapper = mountBar({ loading: false })
      expect(wrapper.find('.btn-generate').element.disabled).toBe(false)
    })

    it('emits "generate" when clicked', async () => {
      const wrapper = mountBar()
      await wrapper.find('.btn-generate').trigger('click')
      expect(wrapper.emitted('generate')).toBeTruthy()
    })
  })

  // ── Export button ──────────────────────────────────────────────────────

  describe('export button', () => {
    it('is hidden when hasReport=false', () => {
      const wrapper = mountBar({ hasReport: false })
      expect(wrapper.find('.btn-export').exists()).toBe(false)
    })

    it('is visible when hasReport=true', () => {
      const wrapper = mountBar({ hasReport: true })
      expect(wrapper.find('.btn-export').exists()).toBe(true)
    })

    it('emits "export" when clicked', async () => {
      const wrapper = mountBar({ hasReport: true })
      await wrapper.find('.btn-export').trigger('click')
      expect(wrapper.emitted('export')).toBeTruthy()
    })
  })

  // ── Deviation button ───────────────────────────────────────────────────

  describe('deviation button', () => {
    it('is always visible regardless of hasReport', () => {
      expect(mountBar({ hasReport: false }).find('.btn-deviation').exists()).toBe(true)
      expect(mountBar({ hasReport: true }).find('.btn-deviation').exists()).toBe(true)
    })

    it('emits "deviation" when clicked', async () => {
      const wrapper = mountBar()
      await wrapper.find('.btn-deviation').trigger('click')
      expect(wrapper.emitted('deviation')).toBeTruthy()
    })
  })

})
