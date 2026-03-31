<script setup>
import { computed, onMounted, ref } from 'vue'
import ChecklistDashboard from '../../../features/ic-checklists/ChecklistDashboard.vue'
import CreateChecklistModal from '../../../features/ic-checklists/CreateChecklistModal.vue'
import { useChecklistDashboard } from '../../../features/ic-checklists/useChecklistDashboard'
import { createChecklist, fetchChecklists, updateChecklist } from '../../../api/checklists'

// Backend-only mode: keep empty so missing backend wiring is visible.
const initialCards = []

const {
  activePeriod,
  cards,
  displayCards,
  togglePending,
  toggleTask,
  logTemperatureMeasurement,
  temperatureLatestByTaskId,
  now
} = useChecklistDashboard({
  initialCards,
  defaultActivePeriod: 'Daily',
  module: 'IC_ALCOHOL'
})

onMounted(async () => {
  const data = await fetchChecklists({ module: 'IC_ALCOHOL' })
  if (cards.value.length === 0) cards.value = Array.isArray(data) ? data : []
})

const isCreateOpen = ref(false)
function handleCreate() {
  isCreateOpen.value = true
}
async function handleCreatedChecklist(newCard) {
  const optimisticIndex = cards.value.length
  cards.value.push(newCard)

  try {
    const created = await createChecklist({
      module: 'IC_ALCOHOL',
      period: newCard?.period,
      title: newCard?.title,
      subtitle: newCard?.subtitle,
      sections: newCard?.sections
    })
    if (created) {
      created.moduleChip = created.moduleChip ?? newCard?.moduleChip
      created.featured = created.featured ?? newCard?.featured
      cards.value.splice(optimisticIndex, 1, created)
    }
  } catch (err) {
    console.error('Failed to create checklist; keeping optimistic card', err)
  }
}

const isEditOpen = ref(false)
const editingCardIndex = ref(null)
const editingCard = computed(() =>
  Number.isInteger(editingCardIndex.value) ? cards.value[editingCardIndex.value] : null
)

async function handleUpdatedChecklist(updatedCard) {
  if (!Number.isInteger(editingCardIndex.value)) return
  const previous = cards.value[editingCardIndex.value]
  cards.value.splice(editingCardIndex.value, 1, updatedCard)

  try {
    const saved = await updateChecklist({
      checklistId: updatedCard?.id,
      period: updatedCard?.period,
      title: updatedCard?.title,
      subtitle: updatedCard?.subtitle,
      sections: updatedCard?.sections
    })
    if (saved) {
      saved.moduleChip = saved.moduleChip ?? updatedCard?.moduleChip
      saved.featured = saved.featured ?? updatedCard?.featured
      cards.value.splice(editingCardIndex.value, 1, saved)
    }
  } catch (err) {
    console.error('Failed to update checklist; reverting optimistic update', err)
    cards.value.splice(editingCardIndex.value, 1, previous)
  }
}

const dateLabel = computed(() => {
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
</script>

<template>
  <ChecklistDashboard
    module-label="IC-Alcohol"
    title="Checklists"
    :date-label="dateLabel"
    v-model:activePeriod="activePeriod"
    :cards="displayCards"
    :temperature-latest-by-task-id="temperatureLatestByTaskId"
    @create="handleCreate"
    @toggle-task="toggleTask"
    @toggle-pending="togglePending"
    @log-temperature="logTemperatureMeasurement"
    @edit-checklist="editChecklist"
  />

  <CreateChecklistModal
    v-model:open="isCreateOpen"
    module-label="IC-Alcohol"
    module-chip="IC-Alcohol"
    @created="handleCreatedChecklist"
  />

  <CreateChecklistModal
    v-model:open="isEditOpen"
    mode="edit"
    :initial-card="editingCard"
    module-label="IC-Alcohol"
    module-chip="IC-Alcohol"
    @close="editingCardIndex = null"
    @updated="handleUpdatedChecklist"
  />
</template>
