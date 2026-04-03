import { computed, ref } from 'vue'
import { normalizePeriodEnum } from './recurrence'
import { recalcCardProgress } from './recalcCardProgress'
import { useNowTick } from './useNowTick'
import { createTemperatureMeasurement } from '../../api/temperatureMeasurements'
import { setTaskCompletion, setTaskFlag, submitChecklist } from '../../api/checklists'
import { useToast } from '@/composables/useToast'

export function useChecklistDashboard({ initialCards, defaultActivePeriod = 'Daily', module = null } = {}) {
  const toast = useToast()
  const cards = ref(Array.isArray(initialCards) ? initialCards : [])
  const activePeriod = ref(defaultActivePeriod)
  const now = useNowTick(60_000)

  const displayCards = computed(() => {
    const safeCards = Array.isArray(cards.value) ? cards.value : []
    const activeEnum = normalizePeriodEnum(activePeriod.value)

    return safeCards
      .map((card, sourceIndex) => ({ ...card, __sourceIndex: sourceIndex }))
      .filter((card) => {
        if (!activeEnum) return true
        const cardEnum = normalizePeriodEnum(card?.period)
        return !cardEnum || cardEnum === activeEnum
      })
  })

  function getCardPeriodKey(card) {
    return String(card?.activePeriodKey ?? '').trim()
  }

  function syncTaskWithResponse(task, response) {
    if (!task || !response || typeof response !== 'object') return
    task.state = response.state ?? task.state
    task.highlighted = Boolean(response.highlighted)
    task.completedForPeriodKey = response.completedForPeriodKey ?? null
    task.completedAt = response.completedAt ?? null
    task.pendingForPeriodKey = response.pendingForPeriodKey ?? null
    task.latestMeasurement = response.latestMeasurement ?? task.latestMeasurement ?? null
  }

  async function toggleTask({ cardIndex, sectionIndex, taskIndex }) {
    const card = cards.value[cardIndex]
    if (!card) return

    const section = Array.isArray(card.sections) ? card.sections[sectionIndex] : null
    const task = section && Array.isArray(section.items) ? section.items[taskIndex] : null
    if (!task) return

    const currentPeriodKey = getCardPeriodKey(card)
    if (!currentPeriodKey) return
    const checklistId = card.id
    const taskId = task.id

    const previous = { ...task }
    if (task.state === 'completed') {
      task.state = 'todo'
      task.completedForPeriodKey = null
      task.completedAt = null
    } else {
      task.state = 'completed'
      task.highlighted = false
      task.pendingForPeriodKey = null
      task.completedAt = new Date().toISOString()
      task.completedForPeriodKey = currentPeriodKey
    }

    recalcCardProgress(card)

    if (!checklistId || !taskId) return
    try {
      const resp = await setTaskCompletion({
        checklistId,
        taskId,
        state: task.state === 'completed' ? 'completed' : 'todo',
        periodKey: currentPeriodKey,
        completedAt: task.state === 'completed' ? task.completedAt : null
      })
      syncTaskWithResponse(task, resp)
      recalcCardProgress(card)
    } catch (err) {
      Object.assign(task, previous)
      recalcCardProgress(card)
      // eslint-disable-next-line no-console
      console.error('Failed to persist task completion', err)
    }
  }

  async function togglePending({ cardIndex, sectionIndex, taskIndex }) {
    const card = cards.value[cardIndex]
    if (!card) return

    const section = Array.isArray(card.sections) ? card.sections[sectionIndex] : null
    const task = section && Array.isArray(section.items) ? section.items[taskIndex] : null
    if (!task) return

    const currentPeriodKey = getCardPeriodKey(card)
    if (!currentPeriodKey) return
    const checklistId = card.id
    const taskId = task.id

    const previous = { ...task }
    const isPending = task.state === 'pending'
    if (isPending) {
      task.state = 'todo'
      task.highlighted = false
      task.pendingForPeriodKey = null
    } else {
      task.state = 'pending'
      task.highlighted = true
      task.pendingForPeriodKey = currentPeriodKey
    }

    recalcCardProgress(card)

    if (!checklistId || !taskId) return
    try {
      const resp = await setTaskFlag({
        checklistId,
        taskId,
        state: task.state === 'pending' ? 'pending' : 'todo',
        periodKey: currentPeriodKey
      })
      syncTaskWithResponse(task, resp)
      recalcCardProgress(card)
    } catch (err) {
      Object.assign(task, previous)
      recalcCardProgress(card)
      // eslint-disable-next-line no-console
      console.error('Failed to persist task flag', err)
    }
  }

  async function logTemperatureMeasurement({ checklistId, taskId, valueC }) {
    if (!checklistId || !taskId) return null

    const card = cards.value.find((entry) => String(entry?.id) === String(checklistId))
    const periodKey = card ? getCardPeriodKey(card) : null

    try {
      const created = await createTemperatureMeasurement({ module, checklistId, taskId, valueC, periodKey })
      if (card) {
        for (const section of Array.isArray(card.sections) ? card.sections : []) {
          for (const task of Array.isArray(section.items) ? section.items : []) {
            if (String(task?.id) === String(taskId)) {
              task.latestMeasurement = created
                ? {
                    id: created.id,
                    valueC: created.valueC,
                    measuredAt: created.measuredAt,
                    periodKey: created.periodKey,
                    deviation: Boolean(created.deviation)
                  }
                : task.latestMeasurement
              break
            }
          }
        }
      }
      toast.success('Temperature reading saved.')
      return created
    } catch (err) {
      console.error('Failed to persist temperature measurement', err)
      toast.error(err?.response?.data?.detail ?? err?.response?.data?.message ?? 'Could not save temperature reading.')
      return null
    }
  }

  async function submitCard({ cardIndex }) {
    const card = cards.value[cardIndex]
    if (!card?.id) return null

    try {
      const refreshed = await submitChecklist({ checklistId: card.id })
      if (refreshed) {
        cards.value.splice(cardIndex, 1, refreshed)
        toast.success(`Started a fresh ${card.title} checklist period.`)
      }
      return refreshed
    } catch (err) {
      console.error('Failed to submit checklist', err)
      toast.error(err?.response?.data?.detail ?? err?.response?.data?.message ?? 'Could not submit checklist.')
      throw err
    }
  }

  return {
    activePeriod,
    cards,
    displayCards,
    togglePending,
    toggleTask,
    submitCard,
    logTemperatureMeasurement,
    now
  }
}
