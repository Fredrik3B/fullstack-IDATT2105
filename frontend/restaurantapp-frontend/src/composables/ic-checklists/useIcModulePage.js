import { computed, nextTick, onMounted, ref } from 'vue'
import { useToast } from '@/composables/useToast'
import { useAuthStore } from '@/stores/auth'
import {
  createChecklist,
  deleteChecklist,
  fetchChecklists,
  setChecklistWorkbenchState,
  updateChecklist,
} from '../../api/checklists'
import { periodEnumToLabel } from './recurrence'
import { useChecklistDashboard } from './useChecklistDashboard'

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

  function isChecklistMissing(err) {
    const message = String(
      err?.response?.data?.detail ?? err?.response?.data?.message ?? err?.message ?? '',
    )
    return message.includes('Checklist not found')
  }

  async function reloadChecklists() {
    if (isLoading.value) return
    isLoading.value = true
    loadError.value = ''

    try {
      const data = await fetchChecklists({ module })
      cards.value = Array.isArray(data) ? data.map(normalizeChecklistCardIds) : []
    } catch (err) {
      console.error(`Failed to fetch ${moduleLabel} checklists`, err)
      loadError.value =
        err?.response?.data?.detail ??
        err?.response?.data?.message ??
        'Could not refresh checklists.'
      toast.warning(loadError.value)
    } finally {
      isLoading.value = false
    }
  }

  onMounted(async () => {
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

  async function handleCreatedChecklist(newCard) {
    if (isCreatingChecklist.value) return

    isCreatingChecklist.value = true
    try {
      await createChecklist({
        module,
        period: newCard?.period,
        title: newCard?.title,
        subtitle: newCard?.subtitle,
        recurring: newCard?.recurring,
        displayedOnWorkbench: newCard?.displayedOnWorkbench,
        taskTemplateIds: newCard?.taskTemplateIds,
      })
      await reloadChecklists()
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
      await reloadChecklists()
      isEditOpen.value = false
      editingCardIndex.value = null
    } catch (err) {
      console.error('Failed to update checklist', err)
      if (isChecklistMissing(err)) {
        await reloadChecklists()
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

  async function handleDeleteChecklist(checklist) {
    const checklistId = checklist?.id
    if (!checklistId) return
    if (String(deletingChecklistId.value ?? '') === String(checklistId)) return

    deletingChecklistId.value = checklistId

    try {
      await deleteChecklist({ checklistId })
      await reloadChecklists()
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

  async function openChecklistOnWorkbench(card) {
    if (!card?.id) return
    if (String(openingChecklistId.value ?? '') === String(card.id)) return

    openingChecklistId.value = card.id

    try {
      const saved = await setChecklistWorkbenchState({
        checklistId: card.id,
        displayedOnWorkbench: true,
      })
      await reloadChecklists()
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
        await reloadChecklists()
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

  async function removeChecklistFromWorkbench(card) {
    if (!card?.id) return
    if (String(removingChecklistId.value ?? '') === String(card.id)) return

    removingChecklistId.value = card.id

    try {
      await setChecklistWorkbenchState({
        checklistId: card.id,
        displayedOnWorkbench: false,
      })
      await reloadChecklists()
      isEditOpen.value = false
      editingCardIndex.value = null
      toast.success('Checklist removed from workbench.')
    } catch (err) {
      console.error('Failed to remove checklist from workbench', err)
      if (isChecklistMissing(err)) {
        await reloadChecklists()
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
    canManageChecklists,
    canManageTaskPool,
    loadedChecklistIds,
    highlightedChecklistId,
    now,
    dateLabel,
    isLoading,
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
