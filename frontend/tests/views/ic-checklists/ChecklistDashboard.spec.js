import { describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ChecklistDashboard from '@/components/ic-checklists/ChecklistDashboard.vue'

vi.mock('@/components/ic-checklists/ChecklistPageHeader.vue', () => ({
  default: {
    name: 'ChecklistPageHeader',
    template: '<div class="header-stub" />',
  },
}))

vi.mock('@/components/ic-checklists/ChecklistSummaryCard.vue', () => ({
  default: {
    name: 'ChecklistSummaryCard',
    template: '<div class="summary-stub" />',
  },
}))

vi.mock('@/components/ic-checklists/TemperatureReportCard.vue', () => ({
  default: {
    name: 'TemperatureReportCard',
    template: '<div class="temperature-report-stub" />',
  },
}))

vi.mock('@/components/ic-checklists/ChecklistCard.vue', () => ({
  default: {
    name: 'ChecklistCard',
    props: ['title'],
    emits: ['toggle-task', 'toggle-pending', 'edit-checklist', 'submit-checklist', 'log-temperature'],
    template: `
      <div class="checklist-card-stub">
        <span>{{ title }}</span>
        <button class="emit-toggle" @click="$emit('toggle-task', { sectionIndex: 1, taskIndex: 2 })" />
        <button class="emit-pending" @click="$emit('toggle-pending', { sectionIndex: 3, taskIndex: 4 })" />
        <button class="emit-edit" @click="$emit('edit-checklist')" />
        <button class="emit-submit" @click="$emit('submit-checklist', { checklistId: 'local-id' })" />
        <button class="emit-temperature" @click="$emit('log-temperature', { checklistId: 'a', taskId: 'b', valueC: 2.5 })" />
      </div>
    `,
  },
}))

function makeProps(overrides = {}) {
  return {
    title: 'Checklists',
    dateLabel: 'Tuesday 07 April 2026',
    cards: [],
    reminderSummary: {
      totalCount: 0,
      temperatureCount: 0,
      checklistTitles: [],
    },
    ...overrides,
  }
}

describe('ChecklistDashboard', () => {
  it('renders the loading state', () => {
    const wrapper = mount(ChecklistDashboard, {
      props: makeProps({ isLoading: true }),
    })

    expect(wrapper.text()).toContain('Loading checklists')
    expect(wrapper.text()).toContain('fetching the latest checklist state')
  })

  it('renders the error state', () => {
    const wrapper = mount(ChecklistDashboard, {
      props: makeProps({ loadError: 'Request failed' }),
    })

    expect(wrapper.text()).toContain('Could not load the workbench')
    expect(wrapper.text()).toContain('Request failed')
  })

  it('renders the empty state when no cards are available', () => {
    const wrapper = mount(ChecklistDashboard, {
      props: makeProps(),
    })

    expect(wrapper.text()).toContain('No checklists on the workbench')
  })

  it('prefers the loading state over error and empty states', () => {
    const wrapper = mount(ChecklistDashboard, {
      props: makeProps({
        isLoading: true,
        loadError: 'Request failed',
        cards: [{ id: 'visible-1', title: 'Opening' }],
      }),
    })

    expect(wrapper.text()).toContain('Loading checklists')
    expect(wrapper.text()).not.toContain('Could not load the workbench')
    expect(wrapper.text()).not.toContain('No checklists on the workbench')
  })

  it('re-emits checklist card actions using the source index from filtered cards', async () => {
    const wrapper = mount(ChecklistDashboard, {
      props: makeProps({
        cards: [
          { id: 'visible-1', title: 'Opening', __sourceIndex: 4 },
          { id: 'visible-2', title: 'Closing', __sourceIndex: 7 },
        ],
      }),
    })

    const cards = wrapper.findAll('.checklist-card-stub')
    await cards[1].find('.emit-toggle').trigger('click')
    await cards[1].find('.emit-pending').trigger('click')
    await cards[1].find('.emit-edit').trigger('click')
    await cards[1].find('.emit-submit').trigger('click')
    await cards[1].find('.emit-temperature').trigger('click')

    expect(wrapper.emitted('toggle-task')).toEqual([
      [{ cardIndex: 7, sectionIndex: 1, taskIndex: 2 }],
    ])
    expect(wrapper.emitted('toggle-pending')).toEqual([
      [{ cardIndex: 7, sectionIndex: 3, taskIndex: 4 }],
    ])
    expect(wrapper.emitted('edit-checklist')).toEqual([[{ cardIndex: 7 }]])
    expect(wrapper.emitted('submit-checklist')).toEqual([
      [{ cardIndex: 7, checklistId: 'local-id' }],
    ])
    expect(wrapper.emitted('log-temperature')).toEqual([
      [{ checklistId: 'a', taskId: 'b', valueC: 2.5 }],
    ])
  })

  it('falls back to the rendered card index when no source index is present', async () => {
    const wrapper = mount(ChecklistDashboard, {
      props: makeProps({
        cards: [{ id: 'visible-1', title: 'Opening' }],
      }),
    })

    const card = wrapper.find('.checklist-card-stub')
    await card.find('.emit-toggle').trigger('click')

    expect(wrapper.emitted('toggle-task')).toEqual([
      [{ cardIndex: 0, sectionIndex: 1, taskIndex: 2 }],
    ])
  })

  it('renders the reminder banner when tasks are close to expiring', () => {
    const wrapper = mount(ChecklistDashboard, {
      props: makeProps({
        reminderSummary: {
          totalCount: 3,
          temperatureCount: 1,
          checklistTitles: ['Opening checks', 'Cold storage'],
        },
      }),
    })

    expect(wrapper.text()).toContain('3 tasks expire within the next hour')
    expect(wrapper.text()).toContain('1 temperature log is still missing')
    expect(wrapper.text()).toContain('Opening checks, Cold storage')
  })
})
