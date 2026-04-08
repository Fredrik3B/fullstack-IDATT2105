import { computed, ref, watch } from 'vue'
import { getPeriodEnd, normalizePeriodEnum } from './recurrence'
import { recalcCardProgress } from './recalcCardProgress'
import { useNowTick } from './useNowTick'
import { createTemperatureMeasurement } from '../../api/temperatureMeasurements'
import { setTaskCompletion, setTaskFlag, submitChecklist } from '../../api/checklists'
import { useToast } from '@/composables/useToast'
import { hasTemperatureMeasurementForPeriod, isTemperatureTask } from './temperature'

const REMINDER_WINDOW_MS = 60 * 60 * 1000

function getTaskReminderKey(checklistId, taskId, periodKey) {
  return [String(checklistId ?? ''), String(taskId ?? ''), String(periodKey ?? '')].join(':')
}

function buildReminderSummary(sourceCards, nowDate) {
  const reminders = []

  sourceCards.forEach((card, cardIndex) => {
    if (!card || card.displayedOnWorkbench === false) return

    const periodKey = String(card.activePeriodKey ?? '').trim()
    if (!periodKey) return

    const periodEnd = getPeriodEnd(card.period, periodKey)
    if (!periodEnd) return

    const msUntilExpiry = periodEnd.getTime() - nowDate.getTime()
    if (msUntilExpiry < 0 || msUntilExpiry > REMINDER_WINDOW_MS) return

    ;(Array.isArray(card.sections) ? card.sections : []).forEach((section, sectionIndex) => {
      ;(Array.isArray(section.items) ? section.items : []).forEach((task, taskIndex) => {
        if (!task || task.state === 'completed') return

        const isTempTask = isTemperatureTask(task)
        const missingTemperatureLog =
          isTempTask && !hasTemperatureMeasurementForPeriod(task, periodKey)

        reminders.push({
          checklistId: card.id,
          checklistTitle: card.title,
          taskId: task.id,
          taskLabel: task.label,
          periodKey,
          cardIndex,
          sectionIndex,
          taskIndex,
          isTemperatureTask: isTempTask,
          missingTemperatureLog,
          alreadyFlagged: task.state === 'pending',
          minutesUntilExpiry: Math.max(0, Math.ceil(msUntilExpiry / 60000)),
        })
      })
    })
  })

  const checklistTitles = Array.from(new Set(reminders.map((entry) => entry.checklistTitle).filter(Boolean)))

  return {
    totalCount: reminders.length,
    temperatureCount: reminders.filter((entry) => entry.missingTemperatureLog).length,
    checklistCount: checklistTitles.length,
    checklistTitles,
    reminders,
  }
}

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
  const autoFlagInFlight = new Set()
  const autoFlagHandled = new Set()
  const announcedReminderKeys = ref(new Set())

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

  const reminderSummary = computed(() => buildReminderSummary(displayCards.value, now.value))

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

    if (
      task.state !== 'completed' &&
      isTemperatureTask(task) &&
      !hasTemperatureMeasurementForPeriod(task, currentPeriodKey)
    ) {
      toast.warning('Save a temperature reading before marking this task as complete.')
      return
    }

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

  async function autoFlagTask(reminder) {
    const key = getTaskReminderKey(reminder.checklistId, reminder.taskId, reminder.periodKey)
    if (!reminder?.checklistId || !reminder?.taskId || !reminder?.periodKey) return
    if (reminder.alreadyFlagged || autoFlagHandled.has(key) || autoFlagInFlight.has(key)) {
      if (reminder.alreadyFlagged) autoFlagHandled.add(key)
      return
    }

    const card = cards.value[reminder.cardIndex]
    const section = Array.isArray(card?.sections) ? card.sections[reminder.sectionIndex] : null
    const task = section && Array.isArray(section.items) ? section.items[reminder.taskIndex] : null
    if (!card || !task || task.isSaving || task.state === 'completed') return

    const previous = { ...task }
    autoFlagInFlight.add(key)
    task.isSaving = true
    task.state = 'pending'
    task.highlighted = true
    task.pendingForPeriodKey = reminder.periodKey
    recalcCardProgress(card)

    try {
      const resp = await setTaskFlag({
        checklistId: reminder.checklistId,
        taskId: reminder.taskId,
        state: 'pending',
        periodKey: reminder.periodKey,
      })
      syncTaskWithResponse(task, resp)
      recalcCardProgress(card)
      autoFlagHandled.add(key)
    } catch (err) {
      Object.assign(task, previous)
      recalcCardProgress(card)
      console.error('Failed to persist automatic reminder flag', err)
    } finally {
      task.isSaving = false
      autoFlagInFlight.delete(key)
    }
  }

  watch(
    [reminderSummary, now],
    ([summary]) => {
      const nextAnnouncementKeys = new Set()

      summary.reminders.forEach((reminder) => {
        const key = getTaskReminderKey(reminder.checklistId, reminder.taskId, reminder.periodKey)
        nextAnnouncementKeys.add(key)
        void autoFlagTask(reminder)
      })

      const unseen = summary.reminders.filter((reminder) => {
        const key = getTaskReminderKey(reminder.checklistId, reminder.taskId, reminder.periodKey)
        return !announcedReminderKeys.value.has(key)
      })

      if (unseen.length > 0) {
        const tempCount = unseen.filter((entry) => entry.missingTemperatureLog).length
        const baseMessage =
          unseen.length === 1
            ? '1 task expires within the next hour and still needs attention.'
            : `${unseen.length} tasks expire within the next hour and still need attention.`
        const detail =
          tempCount > 0
            ? ` ${tempCount} temperature ${tempCount === 1 ? 'log is' : 'logs are'} still missing.`
            : ''
        toast.warning(`${baseMessage}${detail}`)
      }

      announcedReminderKeys.value = nextAnnouncementKeys
    },
    { immediate: true },
  )

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
    reminderSummary,
    togglePending,
    toggleTask,
    submitCard,
    logTemperatureMeasurement,
    now,
  }
}
