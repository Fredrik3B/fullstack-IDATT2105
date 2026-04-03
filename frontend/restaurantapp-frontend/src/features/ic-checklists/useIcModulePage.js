import { computed, nextTick, onMounted, ref } from 'vue'
import { useToast } from '@/composables/useToast'
import { createChecklist, deleteChecklist, fetchChecklists, setChecklistWorkbenchState, updateChecklist } from '../../api/checklists'
import { periodEnumToLabel } from './recurrence'
import { useChecklistDashboard } from './useChecklistDashboard'

export function useIcModulePage({ module, moduleLabel }) {
  const toast = useToast()
  const isCreateOpen = ref(false)
  const isEditOpen = ref(false)
  const isLibraryOpen = ref(false)
  const isTaskPoolOpen = ref(false)
  const isCreatingChecklist = ref(false)
  const editingCardIndex = ref(null)
  const highlightedChecklistId = ref(null)

  const {
    activePeriod,
    cards,
    displayCards,
    togglePending,
    toggleTask,
    submitCard,
    logTemperatureMeasurement,
    now
  } = useChecklistDashboard({
    initialCards: [],
    defaultActivePeriod: 'Daily',
    module
  })

  const workbenchCards = computed(() => displayCards.value.filter((card) => card?.displayedOnWorkbench !== false))
  const loadedChecklistIds = computed(() => workbenchCards.value.map((card) => card.id))
  const editingCard = computed(() =>
    Number.isInteger(editingCardIndex.value) ? cards.value[editingCardIndex.value] : null
  )
  const dateLabel = computed(() =>
    new Intl.DateTimeFormat('en-US', {
      weekday: 'long',
      day: '2-digit',
      month: 'long',
      year: 'numeric'
    }).format(now.value)
  )

  function isChecklistMissing(err) {
    const message = String(err?.response?.data?.detail ?? err?.response?.data?.message ?? err?.message ?? '')
    return message.includes('Checklist not found')
  }

  async function reloadChecklists() {
    try {
      const data = await fetchChecklists({ module })
      cards.value = Array.isArray(data) ? data : []
    } catch (err) {
      console.error(`Failed to fetch ${moduleLabel} checklists`, err)
      toast.warning(err?.response?.data?.detail ?? err?.response?.data?.message ?? 'Could not refresh checklists.')
    }
  }

  onMounted(async () => {
    await reloadChecklists()
  })

  function openCreateModal() {
    isCreateOpen.value = true
  }

  function openLibraryModal() {
    isLibraryOpen.value = true
  }

  function openTaskPoolModal() {
    isTaskPoolOpen.value = true
  }

  function editChecklist({ cardIndex }) {
    editingCardIndex.value = cardIndex
    isEditOpen.value = true
  }

  function closeEditModal() {
    isEditOpen.value = false
    editingCardIndex.value = null
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
        taskTemplateIds: newCard?.taskTemplateIds
      })
      await reloadChecklists()
      isCreateOpen.value = false
    } catch (err) {
      console.error('Failed to create checklist', err)
      toast.warning(err?.response?.data?.detail ?? err?.response?.data?.message ?? 'Could not create checklist.')
    } finally {
      isCreatingChecklist.value = false
    }
  }

  async function handleUpdatedChecklist(updatedCard) {
    if (!Number.isInteger(editingCardIndex.value)) return

    try {
      await updateChecklist({
        checklistId: updatedCard?.id,
        period: updatedCard?.period,
        title: updatedCard?.title,
        subtitle: updatedCard?.subtitle,
        recurring: updatedCard?.recurring,
        displayedOnWorkbench: updatedCard?.displayedOnWorkbench,
        taskTemplateIds: updatedCard?.taskTemplateIds
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
      toast.warning(err?.response?.data?.detail ?? err?.response?.data?.message ?? 'Could not update checklist.')
    }
  }

  async function handleDeleteChecklist(checklist) {
    const checklistId = checklist?.id
    if (!checklistId) return

    const confirmed = window.confirm(`Delete "${checklist?.title || 'this checklist'}"? This cannot be undone.`)
    if (!confirmed) return

    try {
      await deleteChecklist({ checklistId })
      await reloadChecklists()
      isEditOpen.value = false
      editingCardIndex.value = null
      toast.success('Checklist deleted.')
    } catch (err) {
      console.error('Failed to delete checklist', err)
      if (isChecklistMissing(err)) {
        await reloadChecklists()
        isEditOpen.value = false
        editingCardIndex.value = null
      }
      toast.warning(err?.response?.data?.detail ?? err?.response?.data?.message ?? 'Could not delete checklist.')
    }
  }

  async function openChecklistOnWorkbench(card) {
    if (!card?.id) return

    try {
      const saved = await setChecklistWorkbenchState({ checklistId: card.id, displayedOnWorkbench: true })
      await reloadChecklists()
      isLibraryOpen.value = false
      activePeriod.value = periodEnumToLabel((saved ?? card).period)
      highlightedChecklistId.value = card.id
      await nextTick()
      document.getElementById(`checklist-card-${card.id}`)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
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
      toast.warning(err?.response?.data?.detail ?? err?.response?.data?.message ?? 'Could not load checklist onto workbench.')
    }
  }

  return {
    activePeriod,
    cards,
    workbenchCards,
    loadedChecklistIds,
    highlightedChecklistId,
    now,
    dateLabel,
    isCreateOpen,
    isEditOpen,
    isLibraryOpen,
    isTaskPoolOpen,
    editingCard,
    togglePending,
    toggleTask,
    submitCard,
    logTemperatureMeasurement,
    openCreateModal,
    openLibraryModal,
    openTaskPoolModal,
    editChecklist,
    closeEditModal,
    handleCreatedChecklist,
    handleUpdatedChecklist,
    handleDeleteChecklist,
    openChecklistOnWorkbench
  }
}
