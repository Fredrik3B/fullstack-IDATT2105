import { computed, ref } from 'vue'
import { normalizePeriodEnum } from './recurrence'
import { recalcCardProgress } from './recalcCardProgress'
import { useNowTick } from './useNowTick'
import { createTemperatureMeasurement } from '../../api/temperatureMeasurements'
import { setTaskCompletion, setTaskFlag, submitChecklist } from '../../api/checklists'
import { useToast } from '@/composables/useToast'

/**
 * Normalize id-like fields from backend cards into string ids for UI consistency.
 *
 * @param {any} card
 * @returns {any}
 */
function normalizeChecklistCardIds(card) {
  if (!card || typeof card !== 'object') return card

  return {
    ...card,
    id: card.id != null ? String(card.id) : card.id,
    sections: Array.isArray(card.sections)
      ? card.sections.map((section) => ({
          ...section,
          items: Array.isArray(section.items)
            ? section.items.map((item) => ({
                ...item,
                id: item?.id != null ? String(item.id) : item?.id,
                templateId: item?.templateId != null ? String(item.templateId) : item?.templateId,
                latestMeasurement: item?.latestMeasurement
                  ? {
                      ...item.latestMeasurement,
                      id:
                        item.latestMeasurement.id != null
                          ? String(item.latestMeasurement.id)
                          : item.latestMeasurement.id,
                      checklistId:
                        item.latestMeasurement.checklistId != null
                          ? String(item.latestMeasurement.checklistId)
                          : item.latestMeasurement.checklistId,
                      taskId:
                        item.latestMeasurement.taskId != null
                          ? String(item.latestMeasurement.taskId)
                          : item.latestMeasurement.taskId,
                    }
                  : item?.latestMeasurement,
              }))
            : section.items,
        }))
      : card.sections,
  }
}

/**
 * Main dashboard state/composable for checklist interactions in one module view.
 *
 * @param {{initialCards?: Array<any>, defaultActivePeriod?: string, module?: string|null}} [options]
 * @returns {{
 *   activePeriod: import('vue').Ref<string>,
 *   cards: import('vue').Ref<Array<any>>,
 *   displayCards: import('vue').ComputedRef<Array<any>>,
 *   togglePending: (payload: {cardIndex: number, sectionIndex: number, taskIndex: number}) => Promise<void>,
 *   toggleTask: (payload: {cardIndex: number, sectionIndex: number, taskIndex: number}) => Promise<void>,
 *   submitCard: (payload: {cardIndex: number}) => Promise<any|null>,
 *   logTemperatureMeasurement: (payload: {checklistId: string|number, taskId: string|number, valueC: number}) => Promise<any|null>,
 *   now: import('vue').Ref<Date>
 * }}
 */
export function useChecklistDashboard({
  initialCards,
  defaultActivePeriod = 'Daily',
  module = null,
} = {}) {
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

  /**
   * Apply backend task mutation response fields back onto local task object.
   *
   * @param {any} task
   * @param {any} response
   * @returns {void}
   */
  function syncTaskWithResponse(task, response) {
    if (!task || !response || typeof response !== 'object') return
    task.state = response.state ?? task.state
    task.highlighted = Boolean(response.highlighted)
    task.completedForPeriodKey = response.completedForPeriodKey ?? null
    task.completedAt = response.completedAt ?? null
    task.pendingForPeriodKey = response.pendingForPeriodKey ?? null
    task.latestMeasurement = response.latestMeasurement ?? task.latestMeasurement ?? null
  }

  /**
   * Locate checklist card and task references by ids.
   *
   * @param {string|number} checklistId
   * @param {string|number} taskId
   * @returns {{card: any|null, task: any|null}}
   */
  function findTaskById(checklistId, taskId) {
    const card = cards.value.find((entry) => String(entry?.id) === String(checklistId))
    if (!card) return { card: null, task: null }

    for (const section of Array.isArray(card.sections) ? card.sections : []) {
      for (const task of Array.isArray(section.items) ? section.items : []) {
        if (String(task?.id) === String(taskId)) {
          return { card, task }
        }
      }
    }

    return { card, task: null }
  }

  /**
   * Toggle completion state for one task and persist through API.
   *
   * @param {{cardIndex: number, sectionIndex: number, taskIndex: number}} payload
   * @returns {Promise<void>}
   */
  async function toggleTask({ cardIndex, sectionIndex, taskIndex }) {
    const card = cards.value[cardIndex]
    if (!card) return

    const section = Array.isArray(card.sections) ? card.sections[sectionIndex] : null
    const task = section && Array.isArray(section.items) ? section.items[taskIndex] : null
    if (!task) return
    if (task.isSaving) return

    const currentPeriodKey = getCardPeriodKey(card)
    if (!currentPeriodKey) return
    const checklistId = card.id
    const taskId = task.id

    const previous = { ...task }
    task.isSaving = true
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
        completedAt: task.state === 'completed' ? task.completedAt : null,
      })
      syncTaskWithResponse(task, resp)
      recalcCardProgress(card)
    } catch (err) {
      Object.assign(task, previous)
      recalcCardProgress(card)
      console.error('Failed to persist task completion', err)
    } finally {
      task.isSaving = false
    }
  }

  /**
   * Toggle pending/flagged state for one task and persist through API.
   *
   * @param {{cardIndex: number, sectionIndex: number, taskIndex: number}} payload
   * @returns {Promise<void>}
   */
  async function togglePending({ cardIndex, sectionIndex, taskIndex }) {
    const card = cards.value[cardIndex]
    if (!card) return

    const section = Array.isArray(card.sections) ? card.sections[sectionIndex] : null
    const task = section && Array.isArray(section.items) ? section.items[taskIndex] : null
    if (!task) return
    if (task.isSaving) return

    const currentPeriodKey = getCardPeriodKey(card)
    if (!currentPeriodKey) return
    const checklistId = card.id
    const taskId = task.id

    const previous = { ...task }
    task.isSaving = true
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
      })
      syncTaskWithResponse(task, resp)
      recalcCardProgress(card)
    } catch (err) {
      Object.assign(task, previous)
      recalcCardProgress(card)
      console.error('Failed to persist task flag', err)
    } finally {
      task.isSaving = false
    }
  }

  /**
   * Persist one temperature reading for a task.
   *
   * @param {{checklistId: string|number, taskId: string|number, valueC: number}} payload
   * @returns {Promise<any|null>}
   */
  async function logTemperatureMeasurement({ checklistId, taskId, valueC }) {
    if (!checklistId || !taskId) return null

    const { card, task } = findTaskById(checklistId, taskId)
    if (task?.isTemperatureSaving) return null
    const periodKey = card ? getCardPeriodKey(card) : null

    if (task) {
      task.isTemperatureSaving = true
    }

    try {
      const created = await createTemperatureMeasurement({
        module,
        checklistId,
        taskId,
        valueC,
        periodKey,
      })
      if (task) {
        task.latestMeasurement = created
          ? {
              id: created.id,
              valueC: created.valueC,
              measuredAt: created.measuredAt,
              periodKey: created.periodKey,
              deviation: Boolean(created.deviation),
            }
          : task.latestMeasurement
      }
      toast.success('Temperature reading saved.')
      return created
    } catch (err) {
      console.error('Failed to persist temperature measurement', err)
      toast.error(
        err?.response?.data?.detail ??
          err?.response?.data?.message ??
          'Could not save temperature reading.',
      )
      return null
    } finally {
      if (task) {
        task.isTemperatureSaving = false
      }
    }
  }

  /**
   * Submit a checklist and replace card with refreshed period card from backend.
   *
   * @param {{cardIndex: number}} payload
   * @returns {Promise<any|null>}
   * @throws {Error} Re-throws submit failures after showing UI toast.
   */
  async function submitCard({ cardIndex }) {
    const card = cards.value[cardIndex]
    if (!card?.id) return null
    if (card.isSubmitting) return null

    card.isSubmitting = true

    try {
      const refreshed = await submitChecklist({ checklistId: card.id })
      if (refreshed) {
        cards.value.splice(cardIndex, 1, normalizeChecklistCardIds(refreshed))
        toast.success(`Started a fresh ${card.title} checklist period.`)
      }
      return refreshed
    } catch (err) {
      console.error('Failed to submit checklist', err)
      toast.error(
        err?.response?.data?.detail ??
          err?.response?.data?.message ??
          'Could not submit checklist.',
      )
      throw err
    } finally {
      card.isSubmitting = false
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
    now,
  }
}
