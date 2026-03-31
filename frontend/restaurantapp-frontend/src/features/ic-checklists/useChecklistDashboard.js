import { computed, ref, watchEffect } from 'vue'
import { deriveDisplayCards, getCardPeriodEnum, getPeriodKey, seedCompletionMetaForCurrentPeriod } from './recurrence'
import { recalcCardProgress } from './recalcCardProgress'
import { useNowTick } from './useNowTick'
import { useTemperatureLog } from './useTemperatureLog'
import { setTaskCompletion, setTaskFlag } from '../../api/checklists'

export function useChecklistDashboard({ initialCards, defaultActivePeriod = 'Daily', module = null } = {}) {
  const cards = ref(Array.isArray(initialCards) ? initialCards : [])
  const activePeriod = ref(defaultActivePeriod)
  const now = useNowTick(60_000)

  // Temperature logging is a separate stream of data from checklist task state.
  // - Task state: completed/todo/pending (pending is "critical/should do", not a deviation log).
  // - Temperature measurements: time series stored per module/checklist/task.
  const { measurements: temperatureMeasurements, latestByTaskId: temperatureLatestByTaskId, logTemperature } =
    useTemperatureLog({ module })

  watchEffect(() => {
    seedCompletionMetaForCurrentPeriod(cards.value, now.value)
  })

  const displayCards = computed(() => {
    const derived = deriveDisplayCards(cards.value, { activePeriodLabel: activePeriod.value, now: now.value })
    derived.forEach((card) => recalcCardProgress(card))
    return derived
  })

  function getCardPeriodKey(card, date = new Date()) {
    const periodEnum = getCardPeriodEnum(card) ?? 'daily'
    return getPeriodKey(periodEnum, date)
  }

  async function toggleTask({ cardIndex, sectionIndex, taskIndex }) {
    const card = cards.value[cardIndex]
    if (!card) return

    const section = Array.isArray(card.sections) ? card.sections[sectionIndex] : null
    const task = section && Array.isArray(section.items) ? section.items[taskIndex] : null
    if (!task) return

    const currentPeriodKey = getCardPeriodKey(card, new Date())
    const checklistId = card.id
    const taskId = task.id

    // If the task is completed for a *previous* period, treat it as reset before applying the toggle.
    if (
      task.state === 'completed' &&
      task.completedForPeriodKey &&
      task.completedForPeriodKey !== currentPeriodKey
    ) {
      task.state = 'todo'
      task.completedForPeriodKey = null
    }

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
      if (resp && typeof resp === 'object') {
        task.state = resp.state ?? task.state
        task.highlighted = Boolean(resp.highlighted)
        task.completedForPeriodKey = resp.completedForPeriodKey ?? null
        task.completedAt = resp.completedAt ?? null
        task.pendingForPeriodKey = resp.pendingForPeriodKey ?? null
      }
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

    const currentPeriodKey = getCardPeriodKey(card, new Date())
    const checklistId = card.id
    const taskId = task.id

    // If the pending flag belonged to a *previous* period, treat it as reset before applying the toggle.
    if (task.state === 'pending' && task.pendingForPeriodKey && task.pendingForPeriodKey !== currentPeriodKey) {
      task.state = 'todo'
      task.pendingForPeriodKey = null
      task.highlighted = false
    }

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
        periodKey: currentPeriodKey,
        flaggedAt: task.state === 'pending' ? new Date().toISOString() : null
      })
      if (resp && typeof resp === 'object') {
        task.state = resp.state ?? task.state
        task.highlighted = Boolean(resp.highlighted)
        task.completedForPeriodKey = resp.completedForPeriodKey ?? null
        task.completedAt = resp.completedAt ?? null
        task.pendingForPeriodKey = resp.pendingForPeriodKey ?? null
      }
    } catch (err) {
      Object.assign(task, previous)
      recalcCardProgress(card)
      // eslint-disable-next-line no-console
      console.error('Failed to persist task flag', err)
    }
  }

  async function logTemperatureMeasurement({ checklistId, taskId, valueC }) {
    if (!checklistId || !taskId) return null

    const card = cards.value.find((c) => c?.id === checklistId)
    const periodKey = card ? getCardPeriodKey(card, new Date()) : null

    // Frontend behavior:
    // - Log immediately so it can be "reported" in the UI for IC-Food/IC-Alcohol.
    // Backend behavior (later):
    // - Persist and return created measurement id/timestamp.
    return await logTemperature({ checklistId, taskId, valueC, periodKey })
  }

  return {
    activePeriod,
    cards,
    displayCards,
    togglePending,
    toggleTask,
    logTemperatureMeasurement,
    temperatureMeasurements,
    temperatureLatestByTaskId,
    now
  }
}
