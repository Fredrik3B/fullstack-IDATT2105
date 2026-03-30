<script setup>
import { computed, ref } from 'vue'
import ChecklistDashboard from '../checklists/ChecklistDashboard.vue'
import CreateChecklistModal from '../checklists/CreateChecklistModal.vue'
import { useChecklistDashboard } from '../checklists/useChecklistDashboard'

// Backend TODO:
// Replace `initialCards` with data from `fetchChecklists({ module: 'IC_ALCOHOL' })` (see `frontend/restaurantapp-frontend/src/api/checklists.js`).
const initialCards = [
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

const { activePeriod, cards, displayCards, togglePending, toggleTask, now } = useChecklistDashboard({
  initialCards,
  defaultActivePeriod: 'Daily'
})

const isCreateOpen = ref(false)
function handleCreate() {
  isCreateOpen.value = true
}
function handleCreatedChecklist(newCard) {
  // Backend TODO:
  // Replace with: await createChecklist({ module: 'IC_ALCOHOL', ...payload })
  // and push the returned checklist card (with backend ids).
  // See: `frontend/restaurantapp-frontend/src/api/checklists.js`
  cards.value.push(newCard)
}

const isEditOpen = ref(false)
const editingCardIndex = ref(null)
const editingCard = computed(() =>
  Number.isInteger(editingCardIndex.value) ? cards.value[editingCardIndex.value] : null
)

function handleUpdatedChecklist(updatedCard) {
  if (!Number.isInteger(editingCardIndex.value)) return
  cards.value.splice(editingCardIndex.value, 1, updatedCard)
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
    @create="handleCreate"
    @toggle-task="toggleTask"
    @toggle-pending="togglePending"
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
