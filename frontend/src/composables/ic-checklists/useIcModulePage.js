import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useToast } from '@/composables/useToast'
import { useAuthStore } from '@/stores/auth'
import {
  createChecklist,
  deleteChecklist,
  fetchChecklists,
  setChecklistWorkbenchState,
  updateChecklist,
} from '@/api/checklists'
import { normalizePeriodEnum, periodEnumToLabel } from './recurrence'
import { useChecklistDashboard } from './useChecklistDashboard'

const CHECKLIST_SCREEN_CACHE = new Map()

/**
 * Normalize card id fields and nested task ids to string values.
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
 * Read cached module entry.
 *
 * @param {string} module
 * @returns {any|null}
 */
function getCacheEntry(module) {
  return CHECKLIST_SCREEN_CACHE.get(module) ?? null
}

/**
 * Write cached module entry.
 *
 * @param {string} module
 * @param {any} entry
 * @returns {void}
 */
function setCacheEntry(module, entry) {
  CHECKLIST_SCREEN_CACHE.set(module, entry)
}

/**
 * Determine whether cached checklist payload can be reused for immediate render.
 *
 * @param {any} entry
 * @returns {boolean}
 */
function hasUsableCachedCards(entry) {
  return Boolean(
    entry &&
      Array.isArray(entry.cards) &&
      (entry.cards.length > 0 || entry.lastModified),
  )
}

/**
 * Choose the best active period label from current card set.
 *
 * @param {Array<any>} cards
 * @param {string|null|undefined} preferredLabel
 * @returns {string}
 */
function pickAvailablePeriod(cards, preferredLabel) {
  const safeCards = Array.isArray(cards) ? cards : []
  if (!safeCards.length) return preferredLabel ?? 'Daily'

  const available = new Set(
    safeCards
      .filter((card) => card?.displayedOnWorkbench !== false)
      .map((card) => normalizePeriodEnum(card?.period))
      .filter(Boolean),
  )

  if (!available.size) {
    safeCards.forEach((card) => {
      const normalized = normalizePeriodEnum(card?.period)
      if (normalized) available.add(normalized)
    })
  }

  const preferred = normalizePeriodEnum(preferredLabel)
  if (preferred && available.has(preferred)) {
    return periodEnumToLabel(preferred)
  }

  for (const option of ['daily', 'weekly', 'monthly']) {
    if (available.has(option)) {
      return periodEnumToLabel(option)
    }
  }

  return preferredLabel ?? 'Daily'
}

/**
 * Page-level composable for IC module checklist screens.
 *
 * Coordinates checklist loading, modal state, and workbench interactions.
 *
 * @param {{module: string, moduleLabel: string}} params
 * @returns {Record<string, any>}
 */
export function useIcModulePage({ module, moduleLabel }) {
  const auth = useAuthStore()
  const toast = useToast()
  const isCreateOpen = ref(false)
  const isEditOpen = ref(false)
  const isLibraryOpen = ref(false)
  const isTaskPoolOpen = ref(false)
  const isCreatingChecklist = ref(false)
  const isUpdatingChecklist = ref(false)
  const isLoading = ref(false)
  const isRefreshing = ref(false)
  const loadError = ref('')
  const editingCardIndex = ref(null)
  const highlightedChecklistId = ref(null)
  const openingChecklistId = ref(null)
  const deletingChecklistId = ref(null)
  const removingChecklistId = ref(null)
  const deleteDialog = ref({
    open: false,
    checklist: null,
  })

  const {
    activePeriod,
    cards,
    displayCards,
    reminderSummary,
    togglePending,
    toggleTask,
    submitCard,
    logTemperatureMeasurement,
    now,
  } = useChecklistDashboard({
    initialCards: [],
    defaultActivePeriod: 'Daily',
    module,
  })

  const workbenchCards = computed(() =>
    displayCards.value.filter((card) => card?.displayedOnWorkbench !== false),
  )
  const canManageChecklists = computed(() => auth.isAdminOrManager)
  const canManageTaskPool = computed(() => auth.isAdminOrManager)
  const loadedChecklistIds = computed(() => workbenchCards.value.map((card) => card.id))
  const editingCard = computed(() =>
    Number.isInteger(editingCardIndex.value) ? cards.value[editingCardIndex.value] : null,
  )
  const dateLabel = computed(() =>
    new Intl.DateTimeFormat('en-US', {
      weekday: 'long',
      day: '2-digit',
      month: 'long',
      year: 'numeric',
    }).format(now.value),
  )

  watch(activePeriod, (nextPeriod) => {
    const cached = getCacheEntry(module)
    if (!cached) return
    setCacheEntry(module, {
      ...cached,
      activePeriod: nextPeriod,
    })
  })

  function isChecklistMissing(err) {
    const message = String(
      err?.response?.data?.detail ?? err?.response?.data?.message ?? err?.message ?? '',
    )
    return message.includes('Checklist not found')
  }

  /**
   * Reload checklists for current module, with cache/304-aware behavior.
   *
   * @param {{background?: boolean, force?: boolean}} [options]
   * @returns {Promise<void>|Promise<any>}
   */
  async function reloadChecklists({ background = false, force = false } = {}) {
    const cached = getCacheEntry(module)
    if (cached?.promise && !force) return cached.promise

    if (force && cached?.promise) {
      try {
        await cached.promise
      } catch {
        // Ignore the previous request result and continue with a fresh fetch.
      }
    }

    const hasCachedState = hasUsableCachedCards(cached)
    const hasVisibleCards = Array.isArray(cards.value) && cards.value.length > 0
    if (background && (hasVisibleCards || hasCachedState)) {
      isRefreshing.value = true
    } else {
      isLoading.value = true
    }
    loadError.value = ''

    const request = fetchChecklists({
      module,
      ifModifiedSince: force ? null : cached?.lastModified,
    })
      .then((response) => {
        if (response.status === 304 && cached) {
          setCacheEntry(module, {
            ...cached,
            promise: null,
          })
          return
        }

        const nextCards = Array.isArray(response.data)
          ? response.data.map(normalizeChecklistCardIds)
          : []

        cards.value = nextCards
        activePeriod.value = pickAvailablePeriod(nextCards, activePeriod.value ?? cached?.activePeriod)
        setCacheEntry(module, {
          cards: nextCards,
          lastModified: response.lastModified,
          activePeriod: activePeriod.value,
          promise: null,
        })
      })
      .catch((err) => {
        console.error(`Failed to fetch ${moduleLabel} checklists`, err)
        loadError.value =
          err?.response?.data?.detail ??
          err?.response?.data?.message ??
          'Could not refresh checklists.'
        if (!hasVisibleCards && !hasCachedState) {
          toast.warning(loadError.value)
        }
      })
      .finally(() => {
        isLoading.value = false
        isRefreshing.value = false
        const latest = getCacheEntry(module)
        if (latest?.promise === request) {
          setCacheEntry(module, {
            ...latest,
            promise: null,
          })
        }
      })

    setCacheEntry(module, {
      cards: cached?.cards ?? [],
      lastModified: cached?.lastModified ?? null,
      activePeriod: cached?.activePeriod ?? activePeriod.value,
      promise: request,
    })

    return request
  }

  onMounted(async () => {
    const cached = getCacheEntry(module)
    if (hasUsableCachedCards(cached)) {
      cards.value = cached.cards
      activePeriod.value = pickAvailablePeriod(cached.cards, cached.activePeriod)
      await reloadChecklists({ background: true })
      return
    }
    await reloadChecklists()
  })

  function openCreateModal() {
    if (!canManageChecklists.value) return
    isCreateOpen.value = true
  }

  function openLibraryModal() {
    if (!canManageChecklists.value) return
    isLibraryOpen.value = true
  }

  function openTaskPoolModal() {
    if (!canManageTaskPool.value) return
    isTaskPoolOpen.value = true
  }

  function editChecklist({ cardIndex }) {
    if (!canManageChecklists.value) return
    editingCardIndex.value = cardIndex
    isEditOpen.value = true
  }

  function closeEditModal() {
    isEditOpen.value = false
    editingCardIndex.value = null
  }

  function requestDeleteChecklist(checklist) {
    deleteDialog.value = {
      open: true,
      checklist,
    }
  }

  function closeDeleteDialog() {
    deleteDialog.value = {
      open: false,
      checklist: null,
    }
  }

  /**
   * Persist newly created checklist, then refresh list.
   *
   * @param {any} newCard
   * @returns {Promise<void>}
   */
  async function handleCreatedChecklist(newCard) {
    if (isCreatingChecklist.value) return

    isCreatingChecklist.value = true
    try {
      const created = normalizeChecklistCardIds(
        await createChecklist({
        module,
        period: newCard?.period,
        title: newCard?.title,
        subtitle: newCard?.subtitle,
        recurring: newCard?.recurring,
        displayedOnWorkbench: newCard?.displayedOnWorkbench,
        taskTemplateIds: newCard?.taskTemplateIds,
        }),
      )

      if (created?.id != null) {
        cards.value = [
          created,
          ...cards.value.filter((card) => String(card?.id ?? '') !== String(created.id)),
        ]
        setCacheEntry(module, {
          cards: cards.value,
          lastModified: null,
          activePeriod: activePeriod.value,
          promise: null,
        })
      }

      isCreateOpen.value = false
    } catch (err) {
      console.error('Failed to create checklist', err)
      toast.warning(
        err?.response?.data?.detail ??
          err?.response?.data?.message ??
          'Could not create checklist.',
      )
    } finally {
      isCreatingChecklist.value = false
    }
  }

  /**
   * Persist checklist updates, then refresh list.
   *
   * @param {any} updatedCard
   * @returns {Promise<void>}
   */
  async function handleUpdatedChecklist(updatedCard) {
    if (!Number.isInteger(editingCardIndex.value)) return
    if (isUpdatingChecklist.value) return

    isUpdatingChecklist.value = true

    try {
      await updateChecklist({
        checklistId: updatedCard?.id,
        period: updatedCard?.period,
        title: updatedCard?.title,
        subtitle: updatedCard?.subtitle,
        recurring: updatedCard?.recurring,
        displayedOnWorkbench: updatedCard?.displayedOnWorkbench,
        taskTemplateIds: updatedCard?.taskTemplateIds,
      })
      await reloadChecklists({ force: true })
      isEditOpen.value = false
      editingCardIndex.value = null
    } catch (err) {
      console.error('Failed to update checklist', err)
      if (isChecklistMissing(err)) {
        await reloadChecklists({ force: true })
        isEditOpen.value = false
        editingCardIndex.value = null
      }
      toast.warning(
        err?.response?.data?.detail ??
          err?.response?.data?.message ??
          'Could not update checklist.',
      )
    } finally {
      isUpdatingChecklist.value = false
    }
  }

  /**
   * Delete a checklist and refresh screen state.
   *
   * @param {any} checklist
   * @returns {Promise<void>}
   */
  async function handleDeleteChecklist(checklist) {
    const checklistId = checklist?.id
    if (!checklistId) return
    if (String(deletingChecklistId.value ?? '') === String(checklistId)) return

    deletingChecklistId.value = checklistId

    try {
      await deleteChecklist({ checklistId })
      await reloadChecklists({ force: true })
      isEditOpen.value = false
      editingCardIndex.value = null
      closeDeleteDialog()
      toast.success('Checklist deleted.')
    } catch (err) {
      console.error('Failed to delete checklist', err)
      if (isChecklistMissing(err)) {
        await reloadChecklists()
        isEditOpen.value = false
        editingCardIndex.value = null
        closeDeleteDialog()
      }
      toast.warning(
        err?.response?.data?.detail ??
          err?.response?.data?.message ??
          'Could not delete checklist.',
      )
    } finally {
      deletingChecklistId.value = null
    }
  }

  /**
   * Move a library checklist onto workbench and highlight it on screen.
   *
   * @param {any} card
   * @returns {Promise<void>}
   */
  async function openChecklistOnWorkbench(card) {
    if (!card?.id) return
    if (String(openingChecklistId.value ?? '') === String(card.id)) return

    openingChecklistId.value = card.id

    try {
      const saved = await setChecklistWorkbenchState({
        checklistId: card.id,
        displayedOnWorkbench: true,
      })
      await reloadChecklists({ force: true })
      isLibraryOpen.value = false
      activePeriod.value = periodEnumToLabel((saved ?? card).period)
      highlightedChecklistId.value = card.id
      await nextTick()
      document
        .getElementById(`checklist-card-${card.id}`)
        ?.scrollIntoView({ behavior: 'smooth', block: 'start' })
      window.setTimeout(() => {
        if (String(highlightedChecklistId.value) === String(card.id)) {
          highlightedChecklistId.value = null
        }
      }, 2200)
    } catch (err) {
      console.error('Failed to load checklist onto workbench', err)
      if (isChecklistMissing(err)) {
        await reloadChecklists({ force: true })
        isLibraryOpen.value = false
      }
      toast.warning(
        err?.response?.data?.detail ??
          err?.response?.data?.message ??
          'Could not load checklist onto workbench.',
      )
    } finally {
      openingChecklistId.value = null
    }
  }

  /**
   * Remove a checklist from workbench view.
   *
   * @param {any} card
   * @returns {Promise<void>}
   */
  async function removeChecklistFromWorkbench(card) {
    if (!card?.id) return
    if (String(removingChecklistId.value ?? '') === String(card.id)) return

    removingChecklistId.value = card.id

    try {
      await setChecklistWorkbenchState({
        checklistId: card.id,
        displayedOnWorkbench: false,
      })
      await reloadChecklists({ force: true })
      isEditOpen.value = false
      editingCardIndex.value = null
      toast.success('Checklist removed from workbench.')
    } catch (err) {
      console.error('Failed to remove checklist from workbench', err)
      if (isChecklistMissing(err)) {
        await reloadChecklists({ force: true })
        isEditOpen.value = false
        editingCardIndex.value = null
      }
      toast.warning(
        err?.response?.data?.detail ??
          err?.response?.data?.message ??
          'Could not remove checklist from workbench.',
      )
    } finally {
      removingChecklistId.value = null
    }
  }

  return {
    activePeriod,
    cards,
    workbenchCards,
    reminderSummary,
    canManageChecklists,
    canManageTaskPool,
    loadedChecklistIds,
    highlightedChecklistId,
    now,
    dateLabel,
    isLoading,
    isRefreshing,
    loadError,
    isCreateOpen,
    isEditOpen,
    isLibraryOpen,
    isTaskPoolOpen,
    isCreatingChecklist,
    isUpdatingChecklist,
    openingChecklistId,
    deletingChecklistId,
    removingChecklistId,
    editingCard,
    deleteDialog,
    togglePending,
    toggleTask,
    submitCard,
    logTemperatureMeasurement,
    reloadChecklists,
    openCreateModal,
    openLibraryModal,
    openTaskPoolModal,
    editChecklist,
    closeEditModal,
    requestDeleteChecklist,
    closeDeleteDialog,
    handleCreatedChecklist,
    handleUpdatedChecklist,
    handleDeleteChecklist,
    openChecklistOnWorkbench,
    removeChecklistFromWorkbench,
  }
}
