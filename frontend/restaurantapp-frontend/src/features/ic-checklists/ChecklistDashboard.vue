<template>
  <section class="dashboard-shell">
    <ChecklistPageHeader
      :module-label="moduleLabel"
      :title="title"
      :date-label="dateLabel"
      :module-description="moduleDescription"
      :summary-hint="summaryHint"
      :periods="periods"
      :active-period="activePeriod"
      :manage-label="manageLabel"
      :can-manage-task-pool="canManageTaskPool"
      :is-refreshing="isLoading"
      @update:activePeriod="emit('update:activePeriod', $event)"
      @open-library="emit('open-library')"
      @manage-tasks="emit('manage-tasks')"
      @create="emit('create')"
      @refresh="emit('refresh')"
    />

    <section class="overview-grid">
      <ChecklistSummaryCard :cards="cards" />
      <TemperatureReportCard :cards="cards" />
    </section>

    <div v-if="isLoading" class="state-card">
      <h2>Loading checklists</h2>
      <p>The workbench is fetching the latest checklist state for this module.</p>
    </div>

    <div v-else-if="loadError" class="state-card state-card--error">
      <h2>Could not load the workbench</h2>
      <p>{{ loadError }}</p>
    </div>

    <div v-else-if="cards.length === 0" class="state-card">
      <h2>No checklists on the workbench</h2>
      <p>Create a checklist or load one from the library to start working in this module.</p>
    </div>

    <div v-else class="cards-grid">
      <ChecklistCard
        v-for="(card, cardIndex) in cards"
        :key="card.id ?? card.title"
        v-bind="card"
        :now="now"
        :highlighted-workbench="String(highlightedChecklistId ?? '') === String(card.id ?? '')"
        @toggle-task="
          emit('toggle-task', { cardIndex: card.__sourceIndex ?? cardIndex, ...$event })
        "
        @toggle-pending="
          emit('toggle-pending', { cardIndex: card.__sourceIndex ?? cardIndex, ...$event })
        "
        @edit-checklist="emit('edit-checklist', { cardIndex: card.__sourceIndex ?? cardIndex })"
        @submit-checklist="
          emit('submit-checklist', { cardIndex: card.__sourceIndex ?? cardIndex, ...$event })
        "
        @log-temperature="emit('log-temperature', $event)"
      />
    </div>
  </section>
</template>

<script setup>
import ChecklistCard from './ChecklistCard.vue'
import ChecklistPageHeader from './ChecklistPageHeader.vue'
import ChecklistSummaryCard from './ChecklistSummaryCard.vue'
import TemperatureReportCard from './TemperatureReportCard.vue'

defineProps({
  moduleLabel: {
    type: String,
    default: '',
  },
  title: {
    type: String,
    required: true,
  },
  dateLabel: {
    type: String,
    required: true,
  },
  moduleDescription: {
    type: String,
    default: '',
  },
  summaryHint: {
    type: String,
    default: '',
  },
  periods: {
    type: Array,
    default: () => ['Daily', 'Weekly', 'Monthly'],
  },
  activePeriod: {
    type: String,
    default: 'Daily',
  },
  createLabel: {
    type: String,
    default: 'Checklists',
  },
  manageLabel: {
    type: String,
    default: 'Task pool',
  },
  canManageTaskPool: {
    type: Boolean,
    default: false,
  },
  cards: {
    type: Array,
    required: true,
  },
  now: {
    type: [Date, String, Number],
    default: null,
  },
  highlightedChecklistId: {
    type: [String, Number],
    default: null,
  },
  isLoading: {
    type: Boolean,
    default: false,
  },
  loadError: {
    type: String,
    default: '',
  },
})

const emit = defineEmits([
  'toggle-task',
  'toggle-pending',
  'edit-checklist',
  'submit-checklist',
  'log-temperature',
  'update:activePeriod',
  'create',
  'open-library',
  'manage-tasks',
  'refresh',
])
</script>

<style scoped>
.dashboard-shell {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--space-8) var(--space-6) 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.overview-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(0, 1fr);
  gap: var(--space-4);
}

.state-card {
  padding: var(--space-8);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  background: var(--color-bg-primary);
  box-shadow: var(--shadow-sm);
}

.state-card--error {
  border-color: var(--color-danger-border);
  background: linear-gradient(180deg, #ffffff 0%, #fff8f8 100%);
}

.state-card h2 {
  margin: 0 0 var(--space-2);
  font-size: var(--font-size-xl);
  color: var(--color-text-primary);
}

.state-card p {
  margin: 0;
  max-width: 56ch;
  font-size: var(--font-size-md);
  line-height: var(--line-height-normal);
  color: var(--color-text-secondary);
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}

@media (max-width: 980px) {
  .dashboard-shell {
    padding-inline: var(--space-4);
  }

  .overview-grid,
  .cards-grid {
    grid-template-columns: 1fr;
  }
}
</style>
