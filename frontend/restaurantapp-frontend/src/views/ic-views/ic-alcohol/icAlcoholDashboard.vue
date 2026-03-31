<script setup>
import { computed, onMounted, ref } from 'vue'
import ChecklistDashboard from '../../../features/ic-checklists/ChecklistDashboard.vue'
import CreateChecklistModal from '../../../features/ic-checklists/CreateChecklistModal.vue'
import { useChecklistDashboard } from '../../../features/ic-checklists/useChecklistDashboard'
import { createChecklist, fetchChecklists, updateChecklist } from '../../../api/checklists'

// Fallback cards used if backend fetch fails.
const initialCards = [
  {
    id: 'ic-alcohol-storage-temperatures',
    period: 'daily',
    title: 'Storage temperatures',
    subtitle: 'IC-Alcohol - daily',
    statusLabel: '0/2 completed',
    statusTone: 'muted',
    progress: null,
    sections: [
      {
        title: 'Temperature control',
        items: [
          {
            id: 'ic-alcohol-storage-temp-1',
            type: 'temperature',
            unit: 'C',
            targetMin: 2,
            targetMax: 4,
            label: 'Cold room',
            meta: '',
            state: 'todo'
          },
          {
            id: 'ic-alcohol-storage-temp-2',
            type: 'temperature',
            unit: 'C',
            targetMax: -18,
            label: 'Freezer',
            meta: '',
            state: 'todo'
          }
        ]
      }
    ]
  },
  {
    id: 'ic-alcohol-serving-routine',
    period: 'daily',
    title: 'Serving routine',
    subtitle: 'IC-Alcohol - daily',
    statusLabel: '2/5 completed',
    statusTone: 'warning',
    progress: 40,
    featured: true,
    moduleChip: 'IC-Alcohol',
    sections: [
      {
        title: 'Training and certification',
        items: [
          {
            id: 'ic-alcohol-serving-routine-train-1',
            label: 'Confirm internal training for evening shift',
            meta: '08:00',
            state: 'completed'
          }
        ]
      },
      {
        title: 'Daily logging',
        items: [
          {
            id: 'ic-alcohol-serving-routine-log-1',
            label: 'Log the number of refused guests',
            meta: 'Waiting',
            state: 'pending',
            highlighted: true
          },
          {
            id: 'ic-alcohol-serving-routine-log-2',
            label: 'Confirm responsible serving reviewed',
            meta: 'Missing',
            state: 'todo'
          }
        ]
      },
      {
        title: 'Documentation',
        items: [
          {
            id: 'ic-alcohol-serving-routine-doc-1',
            label: 'Check validity of serving certificate',
            meta: '18:00',
            state: 'todo'
          },
          {
            id: 'ic-alcohol-serving-routine-doc-2',
            label: 'Record any deviations in the shift log',
            meta: 'After closing',
            state: 'todo'
          }
        ]
      }
    ]
  },
  {
    id: 'ic-alcohol-weekend-report',
    period: 'weekly',
    title: 'Weekend report',
    subtitle: 'Weekly - Friday',
    statusLabel: 'Planned',
    statusTone: 'muted',
    progress: null,
    sections: [
      {
        title: 'Control points',
        items: [
          { id: 'ic-alcohol-weekend-report-1', label: 'Confirm staffing plan for the weekend', meta: 'Fri', state: 'todo' },
          {
            id: 'ic-alcohol-weekend-report-2',
            label: 'Review stock of non-alcoholic alternatives',
            meta: 'Fri',
            state: 'todo'
          }
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
  module: 'IC_ALCOHOL'
})

onMounted(async () => {
  try {
    const data = await fetchChecklists({ module: 'IC_ALCOHOL' })
    if (Array.isArray(data) && data.length > 0) cards.value = data
  } catch (err) {
    // eslint-disable-next-line no-console
    console.error('Failed to fetch IC-Alcohol checklists; using fallback initialCards', err)
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
