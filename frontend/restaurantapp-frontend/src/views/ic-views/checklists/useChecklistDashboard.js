import { computed, ref, watchEffect } from 'vue'
import { deriveDisplayCards, getCardPeriodEnum, getPeriodKey, seedCompletionMetaForCurrentPeriod } from './recurrence'
import { recalcCardProgress } from './recalcCardProgress'
import { useNowTick } from './useNowTick'

export function useChecklistDashboard({ initialCards, defaultActivePeriod = 'Daily' }) {
  const cards = ref(Array.isArray(initialCards) ? initialCards : [])
  const activePeriod = ref(defaultActivePeriod)
  const now = useNowTick(60_000)

  const seeded = ref(false)
  watchEffect(() => {
    if (seeded.value) return
    seedCompletionMetaForCurrentPeriod(cards.value, now.value)
    seeded.value = true
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

  function toggleTask({ cardIndex, sectionIndex, taskIndex }) {
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

    // Backend TODO:
    // Persist per-user completion *per period* (no cron needed; client/server can compute reset by comparing periodKey).
    // Suggested endpoint:
    //   PUT /api/checklists/{checklistId}/tasks/{taskId}/completion
    // Body:
    //   { state: 'completed'|'todo', periodKey: string, completedAt?: string (ISO) }
    // Notes:
    // - Use `checklistId = card.id` and `taskId = task.id` (frontend currently seeds these ids locally).
    // - When `checklistId`/`taskId` are missing, backend integration should be blocked until ids exist.
    void checklistId
    void taskId
    if (task.state === 'completed') {
      task.state = 'todo'
      task.completedForPeriodKey = null
    } else {
      task.state = 'completed'
      task.highlighted = false
      task.pendingForPeriodKey = null
      task.completedAt = new Date().toISOString()
      task.completedForPeriodKey = currentPeriodKey
    }

    recalcCardProgress(card)
  }

  function togglePending({ cardIndex, sectionIndex, taskIndex }) {
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

    // Backend TODO:
    // Optional to persist; but if you want flags to sync across devices:
    //   PUT /api/checklists/{checklistId}/tasks/{taskId}/flag
    // Body:
    //   { state: 'pending'|'todo', periodKey: string, flaggedAt?: string (ISO) }
    void checklistId
    void taskId
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
  }

  return {
    activePeriod,
    cards,
    displayCards,
    togglePending,
    toggleTask,
    now
  }
}
