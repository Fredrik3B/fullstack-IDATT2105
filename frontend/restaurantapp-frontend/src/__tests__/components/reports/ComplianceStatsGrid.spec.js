import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ComplianceStatsGrid from '@/components/reports/ComplianceStatsGrid.vue'

// ── Helpers ────────────────────────────────────────────────────────────────

function makeFoodStats(overrides = {}) {
  return {
    completedTasks: 8,
    totalTasks: 10,
    completionRate: 80,
    deviatedTasks: 0,
    temperatureReadings: null,
    outOfRangeReadings: 0,
    ...overrides,
  }
}

function makeAlcoholStats(overrides = {}) {
  return {
    completedTasks: 5,
    totalTasks: 5,
    completionRate: 100,
    deviatedTasks: 0,
    ...overrides,
  }
}

function mountGrid(foodOverrides = {}, alcoholOverrides = {}) {
  return mount(ComplianceStatsGrid, {
    props: {
      foodStats: makeFoodStats(foodOverrides),
      alcoholStats: makeAlcoholStats(alcoholOverrides),
    },
  })
}

describe('ComplianceStatsGrid', () => {

  // ── Food stats block ───────────────────────────────────────────────────

  describe('food stats', () => {
    it('shows completed/total task count for food', () => {
      const wrapper = mountGrid({ completedTasks: 7, totalTasks: 10 })
      const foodBlock = wrapper.findAll('.stat-block')[0]
      expect(foodBlock.text()).toContain('7/10')
    })

    it('shows the food completion rate formatted to one decimal', () => {
      const wrapper = mountGrid({ completionRate: 85.5 })
      const foodBlock = wrapper.findAll('.stat-block')[0]
      expect(foodBlock.text()).toContain('85.5%')
    })

    it('applies stat-ok class to food rate when >= 90', () => {
      const wrapper = mountGrid({ completionRate: 95 })
      const foodBlock = wrapper.findAll('.stat-block')[0]
      const rateEl = foodBlock.find('.stat-number.stat-ok')
      expect(rateEl.exists()).toBe(true)
      expect(rateEl.text()).toContain('95.0%')
    })

    it('applies stat-warn class to food rate when between 1 and 89', () => {
      const wrapper = mountGrid({ completionRate: 60 })
      const foodBlock = wrapper.findAll('.stat-block')[0]
      expect(foodBlock.find('.stat-number.stat-warn').exists()).toBe(true)
    })

    it('shows food deviation count', () => {
      const wrapper = mountGrid({ deviatedTasks: 3 })
      const foodBlock = wrapper.findAll('.stat-block')[0]
      expect(foodBlock.text()).toContain('3')
    })

    it('applies stat-danger class to food deviations when > 0', () => {
      const wrapper = mountGrid({ deviatedTasks: 2 })
      const foodBlock = wrapper.findAll('.stat-block')[0]
      // The deviation stat-number gets stat-danger
      const deviationEl = foodBlock.findAll('.stat-number').find(el =>
        el.classes('stat-danger')
      )
      expect(deviationEl).toBeTruthy()
    })

    it('does NOT apply stat-danger to food deviations when 0', () => {
      const wrapper = mountGrid({ deviatedTasks: 0 })
      const foodBlock = wrapper.findAll('.stat-block')[0]
      expect(foodBlock.find('.stat-danger').exists()).toBe(false)
    })
  })

  // ── Temperature readings (food only) ───────────────────────────────────

  describe('temperature readings', () => {
    it('hides the temperature readings stat when foodStats.temperatureReadings is null', () => {
      const wrapper = mountGrid({ temperatureReadings: null })
      const foodBlock = wrapper.findAll('.stat-block')[0]
      expect(foodBlock.text()).not.toContain('Temp. out of range')
    })

    it('shows temperature readings when foodStats.temperatureReadings is not null', () => {
      const wrapper = mountGrid({ temperatureReadings: 20, outOfRangeReadings: 2 })
      const foodBlock = wrapper.findAll('.stat-block')[0]
      expect(foodBlock.text()).toContain('Temp. out of range')
      expect(foodBlock.text()).toContain('2/20')
    })

    it('applies stat-danger to out-of-range count when > 0', () => {
      const wrapper = mountGrid({ temperatureReadings: 20, outOfRangeReadings: 3 })
      const foodBlock = wrapper.findAll('.stat-block')[0]
      expect(foodBlock.find('.stat-danger').exists()).toBe(true)
    })

    it('does NOT apply stat-danger when out-of-range count is 0', () => {
      const wrapper = mountGrid({ temperatureReadings: 20, outOfRangeReadings: 0 })
      const foodBlock = wrapper.findAll('.stat-block')[0]
      expect(foodBlock.find('.stat-danger').exists()).toBe(false)
    })
  })

  // ── Alcohol stats block ────────────────────────────────────────────────

  describe('alcohol stats', () => {
    it('shows completed/total task count for alcohol', () => {
      const wrapper = mountGrid({}, { completedTasks: 3, totalTasks: 6 })
      const alcoholBlock = wrapper.findAll('.stat-block')[1]
      expect(alcoholBlock.text()).toContain('3/6')
    })

    it('shows the alcohol completion rate formatted to one decimal', () => {
      const wrapper = mountGrid({}, { completionRate: 50 })
      const alcoholBlock = wrapper.findAll('.stat-block')[1]
      expect(alcoholBlock.text()).toContain('50.0%')
    })

    it('applies stat-ok class to alcohol rate when >= 90', () => {
      const wrapper = mountGrid({}, { completionRate: 100 })
      const alcoholBlock = wrapper.findAll('.stat-block')[1]
      expect(alcoholBlock.find('.stat-number.stat-ok').exists()).toBe(true)
    })

    it('shows alcohol deviation count and applies stat-danger when > 0', () => {
      const wrapper = mountGrid({}, { deviatedTasks: 1 })
      const alcoholBlock = wrapper.findAll('.stat-block')[1]
      expect(alcoholBlock.find('.stat-danger').exists()).toBe(true)
    })

    it('does not have a temperature readings section', () => {
      const wrapper = mountGrid({}, {})
      const alcoholBlock = wrapper.findAll('.stat-block')[1]
      // Alcohol block has exactly 3 stat-items (tasks, rate, deviations)
      expect(alcoholBlock.findAll('.stat-item')).toHaveLength(3)
    })
  })

})
