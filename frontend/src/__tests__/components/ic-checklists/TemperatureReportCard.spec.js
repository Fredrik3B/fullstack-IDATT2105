import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import TemperatureReportCard from '@/components/ic-checklists/TemperatureReportCard.vue'

describe('TemperatureReportCard', () => {
  it('renders an empty state when no temperature tasks are active', () => {
    const wrapper = mount(TemperatureReportCard, {
      props: {
        cards: [
          {
            id: 'card-1',
            sections: [{ items: [{ label: 'Sweep floor', state: 'todo' }] }],
          },
        ],
      },
    })

    expect(wrapper.text()).toContain('No temperature-controlled tasks are active in this period.')
    expect(wrapper.text()).toContain('No deviations')
  })

  it('renders temperature rows with target, values, missing state, and deviation badge', () => {
    const wrapper = mount(TemperatureReportCard, {
      props: {
        cards: [
          {
            id: 'card-1',
            sections: [
              {
                items: [
                  {
                    id: 'temp-1',
                    label: 'Main fridge',
                    type: 'temperature',
                    targetMin: 0,
                    targetMax: 4,
                    latestMeasurement: { valueC: 5.6, deviation: true },
                  },
                  {
                    id: 'temp-2',
                    label: 'Freezer',
                    sectionType: 'TEMPERATURE_CONTROL',
                    targetMax: -18,
                  },
                ],
              },
            ],
          },
        ],
      },
    })

    expect(wrapper.text()).toContain('1 deviations')
    expect(wrapper.text()).toContain('Main fridge')
    expect(wrapper.text()).toContain('Target: 0-4 C')
    expect(wrapper.text()).toContain('5.6 C')
    expect(wrapper.text()).toContain('Freezer')
    expect(wrapper.text()).toContain('Target: <= -18 C')
    expect(wrapper.text()).toContain('No entry')
    expect(wrapper.find('.report-badge').classes()).toContain('danger')
    expect(wrapper.find('.report-row').classes()).toContain('danger')
  })
})
