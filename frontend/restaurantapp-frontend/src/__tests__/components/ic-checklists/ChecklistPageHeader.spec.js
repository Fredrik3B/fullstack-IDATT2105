import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import ChecklistPageHeader from '@/components/ic-checklists/ChecklistPageHeader.vue'

function makeProps(overrides = {}) {
  return {
    moduleLabel: 'Internal Control',
    title: 'Food safety checklists',
    dateLabel: 'Wednesday 08 April 2026',
    activePeriod: 'Weekly',
    ...overrides,
  }
}

describe('ChecklistPageHeader', () => {
  it('renders the title, module context, and fallback summary hint', () => {
    const wrapper = mount(ChecklistPageHeader, {
      props: makeProps(),
    })

    expect(wrapper.text()).toContain('Internal Control')
    expect(wrapper.text()).toContain('Food safety checklists')
    expect(wrapper.text()).toContain('Wednesday 08 April 2026')
    expect(wrapper.text()).toContain('Filter the workbench and manage reusable checklists.')
    expect(wrapper.findAll('.period-button').map((button) => button.text())).toEqual([
      'Daily',
      'Weekly',
      'Monthly',
    ])
    expect(wrapper.findAll('.period-button')[1].classes()).toContain('active')
  })

  it('emits period changes and management actions when controls are enabled', async () => {
    const wrapper = mount(ChecklistPageHeader, {
      props: makeProps({
        summaryHint: '2 checklists need attention',
        canManageChecklists: true,
        canManageTaskPool: true,
      }),
    })

    await wrapper.findAll('.period-button')[2].trigger('click')
    await wrapper.find('.create-button').trigger('click')
    await wrapper.findAll('.ghost-button')[0].trigger('click')
    await wrapper.findAll('.ghost-button')[1].trigger('click')
    await wrapper.find('.secondary-button').trigger('click')

    expect(wrapper.text()).toContain('2 checklists need attention')
    expect(wrapper.emitted('update:activePeriod')).toEqual([['Monthly']])
    expect(wrapper.emitted('create')).toEqual([[]])
    expect(wrapper.emitted('open-library')).toEqual([[]])
    expect(wrapper.emitted('refresh')).toEqual([[]])
    expect(wrapper.emitted('manage-tasks')).toEqual([[]])
  })

  it('shows a disabled loading refresh state', () => {
    const wrapper = mount(ChecklistPageHeader, {
      props: makeProps({
        canManageChecklists: true,
        isRefreshing: true,
      }),
    })

    const refreshButton = wrapper.findAll('.ghost-button')[1]
    expect(refreshButton.text()).toBe('Refreshing...')
    expect(refreshButton.attributes('disabled')).toBeDefined()
    expect(refreshButton.classes()).toContain('loading')
  })
})
