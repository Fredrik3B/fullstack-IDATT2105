import { beforeEach, describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import ChecklistCard from '@/components/ic-checklists/ChecklistCard.vue'

function makeProps(overrides = {}) {
  return {
    id: 'card-1',
    title: 'Cold storage',
    subtitle: 'Daily temperature follow-up',
    statusLabel: '1/2 completed',
    statusTone: 'warning',
    period: 'daily',
    activePeriodKey: '2026-04-07',
    progress: 50,
    now: '2026-04-07T10:00:00Z',
    sections: [
      {
        title: 'Fridges',
        items: [
          {
            id: 'temp-1',
            label: 'Main fridge',
            type: 'temperature',
            targetMin: 0,
            targetMax: 4,
            latestMeasurement: { valueC: 3.1 },
            state: 'todo',
          },
          {
            id: 'task-2',
            label: 'Check seals',
            state: 'pending',
            highlighted: true,
          },
        ],
      },
    ],
    ...overrides,
  }
}

describe('ChecklistCard', () => {
  beforeEach(() => {
    document.body.innerHTML = ''
  })

  it('renders temperature target and latest reading for fridge-item tasks', () => {
    const wrapper = mount(ChecklistCard, {
      props: makeProps(),
    })

    expect(wrapper.text()).toContain('Target: 0-4 C')
    expect(wrapper.text()).toContain('Last: 3.1 C')
  })

  it('emits task toggle and pending toggle interactions with indexes', async () => {
    const wrapper = mount(ChecklistCard, {
      props: makeProps(),
    })

    const taskMarkers = wrapper.findAll('.task-marker')
    const flagButtons = wrapper.findAll('.flag-button')

    await taskMarkers[0].trigger('click')
    await flagButtons[1].trigger('click')

    expect(wrapper.emitted('toggle-task')).toEqual([[{ sectionIndex: 0, taskIndex: 0 }]])
    expect(wrapper.emitted('toggle-pending')).toEqual([[{ sectionIndex: 0, taskIndex: 1 }]])
  })

  it('disables completion for temperature tasks without a reading in the active period', () => {
    const wrapper = mount(ChecklistCard, {
      props: makeProps({
        sections: [
          {
            title: 'Fridges',
            items: [
              {
                id: 'temp-1',
                label: 'Main fridge',
                type: 'temperature',
                targetMin: 0,
                targetMax: 4,
                latestMeasurement: null,
                state: 'todo',
              },
            ],
          },
        ],
      }),
    })

    expect(wrapper.find('.task-marker').attributes('disabled')).toBeDefined()
    expect(wrapper.text()).toContain('Save a reading to complete')
  })

  it('allows completion for temperature tasks with a reading in the active period', async () => {
    const wrapper = mount(ChecklistCard, {
      props: makeProps({
        sections: [
          {
            title: 'Fridges',
            items: [
              {
                id: 'temp-1',
                label: 'Main fridge',
                type: 'temperature',
                targetMin: 0,
                targetMax: 4,
                latestMeasurement: {
                  valueC: 3.1,
                  periodKey: '2026-04-07',
                },
                state: 'todo',
              },
            ],
          },
        ],
      }),
    })

    await wrapper.find('.task-marker').trigger('click')

    expect(wrapper.emitted('toggle-task')).toEqual([[{ sectionIndex: 0, taskIndex: 0 }]])
  })

  it('confirms and emits temperature logging, then clears the draft input', async () => {
    const wrapper = mount(ChecklistCard, {
      props: makeProps(),
      attachTo: document.body,
    })

    await wrapper.find('.temp-field').setValue('5.5')
    await wrapper.find('.temp-save').trigger('click')

    expect(wrapper.text()).toContain('Save 5.5 C for Main fridge?')

    await wrapper.find('.confirm-primary').trigger('click')

    expect(wrapper.emitted('log-temperature')).toEqual([
      [{ checklistId: 'card-1', taskId: 'temp-1', valueC: 5.5 }],
    ])
    expect(wrapper.find('.temp-field').element.value).toBe('')
  })

  it('does not open the confirmation dialog for an invalid temperature value', async () => {
    const wrapper = mount(ChecklistCard, {
      props: makeProps(),
      attachTo: document.body,
    })

    expect(wrapper.find('.temp-save').attributes('disabled')).toBeDefined()
    await wrapper.find('.temp-save').trigger('click')

    expect(wrapper.find('.confirm-overlay').exists()).toBe(false)
    expect(wrapper.emitted('log-temperature')).toBeUndefined()
  })

  it('marks an expired prior period as overdue while still keeping submission locked', () => {
    const wrapper = mount(ChecklistCard, {
      props: makeProps({
        now: '2026-04-08T10:00:00Z',
      }),
    })

    expect(wrapper.find('.submit-bar').classes()).toContain('overdue')
    expect(wrapper.text()).toContain('Submission is locked until the next real period starts')
    expect(wrapper.text()).toContain('Waiting for next period')
  })

  it('locks submission when the active period is no longer current', () => {
    const wrapper = mount(ChecklistCard, {
      props: makeProps({
        activePeriodKey: '2026-04-06',
      }),
    })

    const button = wrapper.find('.submit-button')
    expect(button.attributes('disabled')).toBeDefined()
    expect(wrapper.text()).toContain('Submission is locked until the next real period starts')
    expect(wrapper.text()).toContain('Waiting for 2026-04-06')
  })

  it('emits edit-checklist when management is enabled', async () => {
    const wrapper = mount(ChecklistCard, {
      props: makeProps({
        canManageChecklists: true,
      }),
    })

    await wrapper.find('.edit-button').trigger('click')

    expect(wrapper.emitted('edit-checklist')).toEqual([[]])
  })

  it('confirms checklist submission and emits the checklist id for the current period', async () => {
    const wrapper = mount(ChecklistCard, {
      props: makeProps(),
      attachTo: document.body,
    })

    await wrapper.find('.submit-button').trigger('click')

    expect(wrapper.text()).toContain('Submit this checklist now?')
    await wrapper.find('.confirm-primary').trigger('click')

    expect(wrapper.emitted('submit-checklist')).toEqual([[{ checklistId: 'card-1' }]])
  })

  it('does not open submit confirmation while the card is already submitting', async () => {
    const wrapper = mount(ChecklistCard, {
      props: makeProps({
        isSubmitting: true,
      }),
      attachTo: document.body,
    })

    expect(wrapper.find('.submit-button').attributes('disabled')).toBeDefined()
    await wrapper.find('.submit-button').trigger('click')

    expect(wrapper.find('.confirm-overlay').exists()).toBe(false)
    expect(wrapper.emitted('submit-checklist')).toBeUndefined()
  })
})
