<script setup>
import { computed, onMounted, ref } from 'vue'
import ChecklistDashboard from '../../../features/ic-checklists/ChecklistDashboard.vue'
import CreateChecklistModal from '../../../features/ic-checklists/CreateChecklistModal.vue'
import { useChecklistDashboard } from '../../../features/ic-checklists/useChecklistDashboard'
import { createChecklist, fetchChecklists, updateChecklist } from '../../../api/checklists'

// Fallback cards used if backend fetch fails.
const initialCards = [
  {
    id: 'ic-food-before-opening',
    period: 'daily',
    title: 'Before opening',
    subtitle: 'Daily - opening',
    statusLabel: '4/5 completed',
    statusTone: 'success',
    progress: 80,
    sections: [
      {
        title: 'Temperature control',
        items: [
          {
            id: 'ic-food-before-opening-temp-1',
            type: 'temperature',
            unit: 'C',
            targetMin: 2,
            targetMax: 4,
            label: 'Cold room 1',
            meta: '',
            state: 'todo'
          },
          {
            id: 'ic-food-before-opening-temp-2',
            type: 'temperature',
            unit: 'C',
            targetMax: -18,
            label: 'Freezer',
            meta: '',
            state: 'todo'
          }
        ]
      },
      {
        title: 'Hygiene',
        items: [
          { id: 'ic-food-before-opening-hyg-1', label: 'Clean work surfaces', meta: '08:30', state: 'pending' },
          {
            id: 'ic-food-before-opening-hyg-2',
            label: 'Check hot-holding temperature',
            meta: 'Remaining',
            state: 'pending',
            highlighted: true
          },
          {
            id: 'ic-food-before-opening-hyg-3',
            label: 'Check labels and dates on goods',
            meta: '09:00',
            state: 'completed'
          }
        ]
      }
    ]
  },
  {
    id: 'ic-food-closing',
    period: 'daily',
    title: 'Evening closing',
    subtitle: 'Daily - closing',
    statusLabel: '0/6 completed',
    statusTone: 'muted',
    progress: null,
    sections: [
      {
        title: 'Cleaning',
        items: [
          { id: 'ic-food-closing-clean-1', label: 'Wash and disinfect all work surfaces', meta: '', state: 'todo' },
          { id: 'ic-food-closing-clean-2', label: 'Empty and wash trash bins', meta: '', state: 'todo' },
          { id: 'ic-food-closing-clean-3', label: 'Clean oven and deep fryers', meta: '', state: 'todo' }
        ]
      },
      {
        title: 'Stock check',
        items: [
          { id: 'ic-food-closing-stock-1', label: 'Check expiry dates on ingredients', meta: '', state: 'todo' },
          { id: 'ic-food-closing-stock-2', label: 'Evening measurement: cold room and freezer', meta: '', state: 'todo' },
          { id: 'ic-food-closing-stock-3', label: 'Lock up and activate the alarm', meta: '', state: 'todo' }
        ]
      }
    ]
  },
  {
    id: 'ic-food-weekly-maintenance',
    period: 'weekly',
    title: 'Weekly maintenance',
    subtitle: 'Weekly - Monday',
    statusLabel: 'Completed',
    statusTone: 'success',
    progress: null,
    sections: [
      {
        title: 'Equipment',
        items: [
          {
            id: 'ic-food-weekly-maintenance-eq-1',
            label: 'Deep clean the kitchen hood and filters',
            meta: 'Mon',
            state: 'completed'
          },
          {
            id: 'ic-food-weekly-maintenance-eq-2',
            label: 'Check and calibrate thermometers',
            meta: 'Mon',
            state: 'completed'
          }
        ]
      },
      {
        title: 'Documentation',
        items: [
          { id: 'ic-food-weekly-maintenance-doc-1', label: 'Review the training log', meta: 'Mon', state: 'completed' },
          { id: 'ic-food-weekly-maintenance-doc-2', label: 'Verify employee certificates', meta: 'Mon', state: 'completed' }
        ]
      }
    ]
  },
  {
    id: 'ic-alcohol-serving-routine',
    period: 'daily',
    title: 'Serving routine',
    subtitle: 'IC-Alcohol - daily',
    statusLabel: '2/4 completed',
    statusTone: 'warning',
    progress: 50,
    featured: true,
    moduleChip: 'IC-Alcohol',
    sections: [
      {
        title: 'Training and certification',
        items: [
          {
            id: 'ic-alcohol-serving-routine-train-1',
            label: 'Age verification training confirmed',
            meta: '08:00',
            state: 'completed'
          }
        ]
      },
      {
        title: 'Daily logging',
        items: [
          { id: 'ic-alcohol-serving-routine-log-1', label: 'Log the number of refused guests', meta: '', state: 'todo' },
          { id: 'ic-alcohol-serving-routine-log-2', label: 'Confirm responsible serving reviewed', meta: '', state: 'todo' }
        ]
      },
      {
        title: 'Documentation',
        items: [
          { id: 'ic-alcohol-serving-routine-doc-1', label: 'Check validity of serving certificate', meta: '', state: 'todo' }
        ]
      }
    ]
  }
]

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
  module: 'IC_FOOD'
})

onMounted(async () => {
  try {
    const data = await fetchChecklists({ module: 'IC_FOOD' })
    if (Array.isArray(data) && data.length > 0) cards.value = data
  } catch (err) {
    // eslint-disable-next-line no-console
    console.error('Failed to fetch IC-Food checklists; using fallback initialCards', err)
  }
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
      module: 'IC_FOOD',
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
    // eslint-disable-next-line no-console
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
    // eslint-disable-next-line no-console
    console.error('Failed to update checklist; reverting optimistic update', err)
    cards.value.splice(editingCardIndex.value, 1, previous)
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
</script>

<template>
  <ChecklistDashboard
    module-label="IC-Food"
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
    module-label="IC-Food"
    module-chip="IC-Food"
    @created="handleCreatedChecklist"
  />

  <CreateChecklistModal
    v-model:open="isEditOpen"
    mode="edit"
    :initial-card="editingCard"
    module-label="IC-Food"
    module-chip="IC-Food"
    @close="editingCardIndex = null"
    @updated="handleUpdatedChecklist"
  />
</template>
