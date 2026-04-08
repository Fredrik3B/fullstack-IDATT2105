import { beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick, ref } from 'vue'
import { useChecklistDashboard } from '@/composables/ic-checklists/useChecklistDashboard'

const toast = {
  success: vi.fn(),
  error: vi.fn(),
  warning: vi.fn(),
}

vi.mock('@/composables/useToast', () => ({
  useToast: () => toast,
}))

const nowRef = ref(new Date('2026-04-07T09:30:00'))

vi.mock('@/composables/ic-checklists/useNowTick', () => ({
  useNowTick: () => nowRef,
}))

vi.mock('@/api/checklists', () => ({
  setTaskCompletion: vi.fn(),
  setTaskFlag: vi.fn(),
  submitChecklist: vi.fn(),
}))

vi.mock('@/api/temperatureMeasurements', () => ({
  createTemperatureMeasurement: vi.fn(),
}))

import {
  setTaskCompletion,
  setTaskFlag,
  submitChecklist,
} from '@/api/checklists'
import { createTemperatureMeasurement } from '@/api/temperatureMeasurements'

function makeCard(overrides = {}) {
  return {
    id: 'card-1',
    title: 'Opening checks',
    period: 'daily',
    activePeriodKey: '2026-04-07',
    statusLabel: '0/1 completed',
    statusTone: 'muted',
    progress: 0,
    sections: [
      {
        title: 'Kitchen',
        items: [
          {
            id: 'task-1',
            label: 'Sanitize counters',
            state: 'todo',
            highlighted: false,
            isSaving: false,
          },
        ],
      },
    ],
    ...overrides,
  }
}

describe('useChecklistDashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    nowRef.value = new Date('2026-04-07T09:30:00')
  })

  it('filters display cards by the selected period and preserves source indexes', async () => {
    const { activePeriod, displayCards } = useChecklistDashboard({
      initialCards: [
        makeCard({ id: 'daily-card', period: 'daily' }),
        makeCard({ id: 'weekly-card', period: 'weekly' }),
      ],
    })

    expect(displayCards.value.map((card) => card.id)).toEqual(['daily-card'])
    expect(displayCards.value[0].__sourceIndex).toBe(0)

    activePeriod.value = 'Weekly'
    await nextTick()

    expect(displayCards.value.map((card) => card.id)).toEqual(['weekly-card'])
    expect(displayCards.value[0].__sourceIndex).toBe(1)
  })

  it('optimistically completes a task and syncs the saved response', async () => {
    setTaskCompletion.mockResolvedValue({
      state: 'completed',
      highlighted: false,
      completedForPeriodKey: '2026-04-07',
      completedAt: '2026-04-07T09:30:00.000Z',
    })

    const { cards, toggleTask } = useChecklistDashboard({
      initialCards: [makeCard()],
    })

    await toggleTask({ cardIndex: 0, sectionIndex: 0, taskIndex: 0 })

    const task = cards.value[0].sections[0].items[0]
    expect(setTaskCompletion).toHaveBeenCalledWith({
      checklistId: 'card-1',
      taskId: 'task-1',
      state: 'completed',
      periodKey: '2026-04-07',
      completedAt: expect.any(String),
    })
    expect(task.state).toBe('completed')
    expect(task.isSaving).toBe(false)
    expect(cards.value[0].progress).toBe(100)
    expect(cards.value[0].statusLabel).toBe('Completed')
    expect(cards.value[0].statusTone).toBe('success')
  })

  it('does not complete a temperature task before a reading is saved for the active period', async () => {
    const { cards, toggleTask } = useChecklistDashboard({
      initialCards: [
        makeCard({
          sections: [
            {
              title: 'Cooling',
              items: [
                {
                  id: 'temp-1',
                  label: 'Main fridge',
                  type: 'temperature',
                  targetMax: 4,
                  state: 'todo',
                  highlighted: false,
                  latestMeasurement: null,
                },
              ],
            },
          ],
        }),
      ],
    })

    await toggleTask({ cardIndex: 0, sectionIndex: 0, taskIndex: 0 })

    expect(setTaskCompletion).not.toHaveBeenCalled()
    expect(cards.value[0].sections[0].items[0].state).toBe('todo')
    expect(toast.warning).toHaveBeenCalledWith(
      'Save a temperature reading before marking this task as complete.',
    )
  })

  it('allows completing a temperature task after a reading is saved for the active period', async () => {
    setTaskCompletion.mockResolvedValue({
      state: 'completed',
      highlighted: false,
      completedForPeriodKey: '2026-04-07',
      completedAt: '2026-04-07T09:30:00.000Z',
    })

    const { cards, toggleTask } = useChecklistDashboard({
      initialCards: [
        makeCard({
          sections: [
            {
              title: 'Cooling',
              items: [
                {
                  id: 'temp-1',
                  label: 'Main fridge',
                  type: 'temperature',
                  targetMax: 4,
                  state: 'todo',
                  highlighted: false,
                  latestMeasurement: {
                    id: 'measurement-1',
                    valueC: 3.2,
                    measuredAt: '2026-04-07T09:00:00Z',
                    periodKey: '2026-04-07',
                  },
                },
              ],
            },
          ],
        }),
      ],
    })

    await toggleTask({ cardIndex: 0, sectionIndex: 0, taskIndex: 0 })

    expect(setTaskCompletion).toHaveBeenCalledWith({
      checklistId: 'card-1',
      taskId: 'temp-1',
      state: 'completed',
      periodKey: '2026-04-07',
      completedAt: expect.any(String),
    })
    expect(cards.value[0].sections[0].items[0].state).toBe('completed')
  })

  it('reverts a task toggle when persistence fails', async () => {
    const errorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    setTaskCompletion.mockRejectedValue(new Error('save failed'))

    const { cards, toggleTask } = useChecklistDashboard({
      initialCards: [makeCard()],
    })

    await toggleTask({ cardIndex: 0, sectionIndex: 0, taskIndex: 0 })

    const task = cards.value[0].sections[0].items[0]
    expect(task.state).toBe('todo')
    expect(task.isSaving).toBe(false)
    expect(cards.value[0].progress).toBe(0)
    expect(cards.value[0].statusTone).toBe('muted')
    expect(errorSpy).toHaveBeenCalled()
    errorSpy.mockRestore()
  })

  it('does not try to toggle a task when the checklist has no active period key', async () => {
    const { cards, toggleTask } = useChecklistDashboard({
      initialCards: [makeCard({ activePeriodKey: '' })],
    })

    await toggleTask({ cardIndex: 0, sectionIndex: 0, taskIndex: 0 })

    expect(setTaskCompletion).not.toHaveBeenCalled()
    expect(cards.value[0].sections[0].items[0].state).toBe('todo')
  })

  it('flags a task as pending for the active checklist period', async () => {
    setTaskFlag.mockResolvedValue({
      state: 'pending',
      highlighted: true,
      pendingForPeriodKey: '2026-04-07',
    })

    const { cards, togglePending } = useChecklistDashboard({
      initialCards: [makeCard()],
    })

    await togglePending({ cardIndex: 0, sectionIndex: 0, taskIndex: 0 })

    const task = cards.value[0].sections[0].items[0]
    expect(setTaskFlag).toHaveBeenCalledWith({
      checklistId: 'card-1',
      taskId: 'task-1',
      state: 'pending',
      periodKey: '2026-04-07',
    })
    expect(task.state).toBe('pending')
    expect(task.highlighted).toBe(true)
    expect(task.pendingForPeriodKey).toBe('2026-04-07')
    expect(task.isSaving).toBe(false)
  })

  it('logs a temperature reading, updates the latest measurement, and shows success feedback', async () => {
    createTemperatureMeasurement.mockResolvedValue({
      id: 88,
      valueC: 6.4,
      measuredAt: '2026-04-07T09:45:00Z',
      periodKey: '2026-04-07',
      deviation: true,
    })

    const { cards, logTemperatureMeasurement } = useChecklistDashboard({
      module: 'IC_FOOD',
      initialCards: [
        makeCard({
          sections: [
            {
              title: 'Cooling',
              items: [
                {
                  id: 'temp-1',
                  label: 'Main fridge',
                  type: 'temperature',
                  targetMax: 4,
                },
              ],
            },
          ],
        }),
      ],
    })

    const created = await logTemperatureMeasurement({
      checklistId: 'card-1',
      taskId: 'temp-1',
      valueC: 6.4,
    })

    expect(createTemperatureMeasurement).toHaveBeenCalledWith({
      module: 'IC_FOOD',
      checklistId: 'card-1',
      taskId: 'temp-1',
      valueC: 6.4,
      periodKey: '2026-04-07',
    })
    expect(created).toMatchObject({ id: 88, valueC: 6.4, deviation: true })
    expect(cards.value[0].sections[0].items[0].latestMeasurement).toEqual({
      id: 88,
      valueC: 6.4,
      measuredAt: '2026-04-07T09:45:00Z',
      periodKey: '2026-04-07',
      deviation: true,
    })
    expect(toast.success).toHaveBeenCalledWith('Temperature reading saved.')
  })

  it('shows an error toast and preserves the previous reading when temperature logging fails', async () => {
    const errorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    createTemperatureMeasurement.mockRejectedValue({
      response: {
        data: {
          detail: 'Reading outside allowed range.',
        },
      },
    })

    const { cards, logTemperatureMeasurement } = useChecklistDashboard({
      module: 'IC_FOOD',
      initialCards: [
        makeCard({
          sections: [
            {
              title: 'Cooling',
              items: [
                {
                  id: 'temp-1',
                  label: 'Main fridge',
                  type: 'temperature',
                  latestMeasurement: {
                    id: 'existing',
                    valueC: 3.2,
                    measuredAt: '2026-04-07T08:00:00Z',
                    periodKey: '2026-04-07',
                    deviation: false,
                  },
                },
              ],
            },
          ],
        }),
      ],
    })

    const result = await logTemperatureMeasurement({
      checklistId: 'card-1',
      taskId: 'temp-1',
      valueC: 6.4,
    })

    expect(result).toBeNull()
    expect(cards.value[0].sections[0].items[0].latestMeasurement).toMatchObject({
      id: 'existing',
      valueC: 3.2,
    })
    expect(cards.value[0].sections[0].items[0].isTemperatureSaving).toBe(false)
    expect(toast.error).toHaveBeenCalledWith('Reading outside allowed range.')
    expect(errorSpy).toHaveBeenCalled()
    errorSpy.mockRestore()
  })

  it('submits a checklist, replaces the card with normalized ids, and shows success feedback', async () => {
    submitChecklist.mockResolvedValue({
      id: 42,
      title: 'Opening checks',
      period: 'daily',
      activePeriodKey: '2026-04-08',
      sections: [
        {
          title: 'Kitchen',
          items: [
            {
              id: 77,
              label: 'Sanitize counters',
              state: 'todo',
              latestMeasurement: {
                id: 91,
                checklistId: 42,
                taskId: 77,
                valueC: 3.2,
              },
            },
          ],
        },
      ],
    })

    const { cards, submitCard } = useChecklistDashboard({
      initialCards: [makeCard()],
    })

    const saved = await submitCard({ cardIndex: 0 })

    expect(submitChecklist).toHaveBeenCalledWith({ checklistId: 'card-1' })
    expect(saved.id).toBe(42)
    expect(cards.value[0].id).toBe('42')
    expect(cards.value[0].sections[0].items[0].id).toBe('77')
    expect(cards.value[0].sections[0].items[0].latestMeasurement).toMatchObject({
      id: '91',
      checklistId: '42',
      taskId: '77',
    })
    expect(toast.success).toHaveBeenCalledWith('Started a fresh Opening checks checklist period.')
  })

  it('shows an error toast and rethrows when checklist submission fails', async () => {
    const errorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    submitChecklist.mockRejectedValue({
      response: {
        data: {
          message: 'Submission failed.',
        },
      },
    })

    const { cards, submitCard } = useChecklistDashboard({
      initialCards: [makeCard()],
    })

    await expect(submitCard({ cardIndex: 0 })).rejects.toMatchObject({
      response: { data: { message: 'Submission failed.' } },
    })

    expect(cards.value[0].isSubmitting).toBe(false)
    expect(toast.error).toHaveBeenCalledWith('Submission failed.')
    expect(errorSpy).toHaveBeenCalled()
    errorSpy.mockRestore()
  })

  it('builds reminder summaries and auto-flags incomplete tasks within the next hour', async () => {
    nowRef.value = new Date('2026-04-07T23:10:00')
    setTaskFlag.mockResolvedValue({
      state: 'pending',
      highlighted: true,
      pendingForPeriodKey: '2026-04-07',
    })

    const { cards, reminderSummary } = useChecklistDashboard({
      initialCards: [
        makeCard({
          title: 'Closing checks',
          sections: [
            {
              title: 'Kitchen',
              items: [
                {
                  id: 'task-1',
                  label: 'Sanitize counters',
                  state: 'todo',
                  highlighted: false,
                },
              ],
            },
          ],
        }),
      ],
    })

    await Promise.resolve()
    await nextTick()

    expect(reminderSummary.value.totalCount).toBe(1)
    expect(setTaskFlag).toHaveBeenCalledWith({
      checklistId: 'card-1',
      taskId: 'task-1',
      state: 'pending',
      periodKey: '2026-04-07',
    })
    expect(cards.value[0].sections[0].items[0].state).toBe('pending')
    expect(toast.warning).toHaveBeenCalledWith(
      '1 task expires within the next hour and still needs attention.',
    )
  })

  it('counts missing temperature logs in the reminder summary and warning toast', async () => {
    nowRef.value = new Date('2026-04-07T23:20:00')
    setTaskFlag.mockResolvedValue({
      state: 'pending',
      highlighted: true,
      pendingForPeriodKey: '2026-04-07',
    })

    const { reminderSummary } = useChecklistDashboard({
      module: 'IC_FOOD',
      initialCards: [
        makeCard({
          title: 'Cold storage',
          sections: [
            {
              title: 'Cooling',
              items: [
                {
                  id: 'temp-1',
                  label: 'Main fridge',
                  type: 'temperature',
                  targetMax: 4,
                  state: 'todo',
                  highlighted: false,
                  latestMeasurement: null,
                },
              ],
            },
          ],
        }),
      ],
    })

    await Promise.resolve()
    await nextTick()

    expect(reminderSummary.value.totalCount).toBe(1)
    expect(reminderSummary.value.temperatureCount).toBe(1)
    expect(toast.warning).toHaveBeenCalledWith(
      '1 task expires within the next hour and still needs attention. 1 temperature log is still missing.',
    )
  })

  it('does not create reminders for tasks outside the one-hour window', () => {
    nowRef.value = new Date('2026-04-07T20:30:00')

    const { reminderSummary } = useChecklistDashboard({
      initialCards: [makeCard()],
    })

    expect(reminderSummary.value.totalCount).toBe(0)
    expect(setTaskFlag).not.toHaveBeenCalled()
    expect(toast.warning).not.toHaveBeenCalled()
  })
})
