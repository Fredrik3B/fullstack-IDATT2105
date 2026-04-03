<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { useToast } from '@/composables/useToast'
import ChecklistDashboard from '../../../features/ic-checklists/ChecklistDashboard.vue'
import CreateChecklistModal from '../../../features/ic-checklists/CreateChecklistModal.vue'
import ChecklistLibraryModal from '../../../features/ic-checklists/ChecklistLibraryModal.vue'
import ManageTaskTemplatesModal from '../../../features/ic-checklists/ManageTaskTemplatesModal.vue'
import { useChecklistDashboard } from '../../../features/ic-checklists/useChecklistDashboard'
import { createChecklist, deleteChecklist, fetchChecklists, setChecklistWorkbenchState, updateChecklist } from '../../../api/checklists'
import { periodEnumToLabel } from '../../../features/ic-checklists/recurrence'

// Backend-only mode: keep empty so missing backend wiring is visible.
const initialCards = []

const {
  activePeriod,
  cards,
  displayCards,
  togglePending,
  toggleTask,
  submitCard,
  logTemperatureMeasurement,
  temperatureLatestByTaskId,
  now
} = useChecklistDashboard({
  initialCards,
  defaultActivePeriod: 'Daily',
  module: 'IC_FOOD'
})

onMounted(async () => {
  try {
    const data = await fetchChecklists({ module: 'IC_FOOD' })
    if (cards.value.length === 0) cards.value = Array.isArray(data) ? data : []
  } catch (err) {
    console.error('Failed to fetch IC-Food checklists', err)
  }
})

const isCreateOpen = ref(false)
const isLibraryOpen = ref(false)
const isTaskPoolOpen = ref(false)
const highlightedChecklistId = ref(null)
const toast = useToast()

const workbenchCards = computed(() => displayCards.value.filter((card) => card?.displayedOnWorkbench !== false))
const loadedChecklistIds = computed(() => workbenchCards.value.map((card) => card.id))
function handleCreate() {
  isCreateOpen.value = true
}
function handleOpenLibrary() {
  isLibraryOpen.value = true
}
function handleManageTasks() {
  isTaskPoolOpen.value = true
}
async function handleCreatedChecklist(newCard) {
  try {
    const created = await createChecklist({
      module: 'IC_FOOD',
      period: newCard?.period,
      title: newCard?.title,
      subtitle: newCard?.subtitle,
      recurring: newCard?.recurring,
      displayedOnWorkbench: newCard?.displayedOnWorkbench,
      taskTemplateIds: newCard?.taskTemplateIds
    })
    if (created) {
      cards.value.push(created)
      isCreateOpen.value = false
    }
  } catch (err) {
    console.error('Failed to create checklist', err)
    toast.warning(err?.response?.data?.detail ?? err?.response?.data?.message ?? 'Could not create checklist.')
  }
}

const isEditOpen = ref(false)
const editingCardIndex = ref(null)
const editingCard = computed(() =>
  Number.isInteger(editingCardIndex.value) ? cards.value[editingCardIndex.value] : null
)

async function handleUpdatedChecklist(updatedCard) {
  if (!Number.isInteger(editingCardIndex.value)) return

  try {
    const saved = await updateChecklist({
      checklistId: updatedCard?.id,
      period: updatedCard?.period,
      title: updatedCard?.title,
      subtitle: updatedCard?.subtitle,
      recurring: updatedCard?.recurring,
      displayedOnWorkbench: updatedCard?.displayedOnWorkbench,
      taskTemplateIds: updatedCard?.taskTemplateIds
    })
    if (saved) {
      cards.value.splice(editingCardIndex.value, 1, saved)
      isEditOpen.value = false
      editingCardIndex.value = null
    }
  } catch (err) {
    console.error('Failed to update checklist', err)
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
    const index = cards.value.findIndex((entry) => String(entry?.id) === String(checklistId))
    if (index >= 0) cards.value.splice(index, 1)
    isEditOpen.value = false
    editingCardIndex.value = null
    toast.success('Checklist deleted.')
  } catch (err) {
    console.error('Failed to delete checklist', err)
    toast.warning(err?.response?.data?.detail ?? err?.response?.data?.message ?? 'Could not delete checklist.')
  }
}

const dateLabel = computed(() => {
  // Frontend-only label. Backend should provide authoritative "business date" if needed.
  return new Intl.DateTimeFormat('en-US', {
    weekday: 'long',
    day: '2-digit',
    month: 'long',
    year: 'numeric'
  }).format(now.value)
})

function editChecklist({ cardIndex }) {
  editingCardIndex.value = cardIndex
  isEditOpen.value = true
}

async function openChecklistOnWorkbench(card) {
  if (!card?.id) return
  try {
    const saved = await setChecklistWorkbenchState({ checklistId: card.id, displayedOnWorkbench: true })
    const index = cards.value.findIndex((entry) => String(entry?.id) === String(card.id))
    if (index >= 0 && saved) {
      cards.value.splice(index, 1, saved)
    }
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
  }
}
</script>

<template>
  <ChecklistDashboard
    module-label="IC-Food"
    title="Checklists"
    :date-label="dateLabel"
    v-model:activePeriod="activePeriod"
    :cards="workbenchCards"
    :highlighted-checklist-id="highlightedChecklistId"
    :now="now"
    :temperature-latest-by-task-id="temperatureLatestByTaskId"
    @open-library="handleOpenLibrary"
    @manage-tasks="handleManageTasks"
    @create="handleCreate"
    @toggle-task="toggleTask"
    @toggle-pending="togglePending"
    @submit-checklist="submitCard"
    @log-temperature="logTemperatureMeasurement"
    @edit-checklist="editChecklist"
  />

  <CreateChecklistModal
    v-model:open="isCreateOpen"
    module="IC_FOOD"
    module-label="IC-Food"
    @manage-tasks="handleManageTasks"
    @created="handleCreatedChecklist"
  />

  <CreateChecklistModal
    v-model:open="isEditOpen"
    mode="edit"
    :initial-card="editingCard"
    module="IC_FOOD"
    module-label="IC-Food"
    @manage-tasks="handleManageTasks"
    @close="editingCardIndex = null"
    @delete="handleDeleteChecklist"
    @updated="handleUpdatedChecklist"
  />

  <ChecklistLibraryModal
    v-model:open="isLibraryOpen"
    module-label="IC-Food"
    :cards="cards"
    :loaded-checklist-ids="loadedChecklistIds"
    @open-checklist="openChecklistOnWorkbench"
  />

  <ManageTaskTemplatesModal
    v-model:open="isTaskPoolOpen"
    module="IC_FOOD"
    module-label="IC-Food"
  />
</template>
