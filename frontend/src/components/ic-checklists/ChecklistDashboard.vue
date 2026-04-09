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
      :can-manage-checklists="canManageChecklists"
      :can-manage-task-pool="canManageTaskPool"
      :is-refreshing="isRefreshing"
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

    <section
      v-if="reminderSummary.totalCount > 0 && !isLoading && !loadError"
      class="reminder-banner"
      role="alert"
    >
      <div class="reminder-banner__title">
        {{ reminderSummary.totalCount }} task{{ reminderSummary.totalCount === 1 ? '' : 's' }} expire within the next hour.
      </div>
      <p class="reminder-banner__copy">
        {{
          reminderSummary.temperatureCount > 0
            ? `${reminderSummary.temperatureCount} temperature log${reminderSummary.temperatureCount === 1 ? ' is' : 's are'} still missing.`
            : 'Incomplete tasks are being flagged for follow-up.'
        }}
      </p>
      <p
        v-if="reminderSummary.checklistTitles?.length"
        class="reminder-banner__meta"
      >
        {{ reminderSummary.checklistTitles.join(', ') }}
      </p>
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
        :can-manage-checklists="canManageChecklists"
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

    <slot name="below-content" />
  </section>
</template>

<script setup>
/**
 * ChecklistDashboard
 *
 * Module-level workbench shell shared by IC-Food and IC-Alcohol views.
 * Composes the page header, the summary + temperature overview grid, an urgent
 * reminder banner, loading/error/empty states, and the grid of ChecklistCards.
 *
 * All data and interaction logic lives in the parent view; this component only
 * arranges the layout and forwards events upward.
 *
 * @prop {string}   moduleLabel         - Module name shown in the header eyebrow.
 * @prop {string}   title               - Page heading (e.g. "IC-Food Workbench").
 * @prop {string}   dateLabel           - Formatted current date shown below the heading.
 * @prop {string}   [moduleDescription] - Optional description paragraph in the header.
 * @prop {string}   [summaryHint]       - Hint text in the workbench insight card.
 * @prop {string[]} [periods]           - Period filter options (default: Daily/Weekly/Monthly).
 * @prop {string}   [activePeriod]      - Currently selected period filter.
 * @prop {string}   [manageLabel]       - Label for the task pool manage button.
 * @prop {boolean}  [canManageChecklists]  - Shows checklist management actions.
 * @prop {boolean}  [canManageTaskPool]    - Shows the task pool management button.
 * @prop {Array}    cards               - Array of checklist card data objects.
 * @prop {Date|string|number|null} [now] - Reference time forwarded to each ChecklistCard.
 * @prop {string|number|null} [highlightedChecklistId] - ID of the card to highlight.
 * @prop {Object}   [reminderSummary]   - `{ totalCount, temperatureCount, checklistTitles }`.
 * @prop {boolean}  [isLoading]         - Shows a loading state card.
 * @prop {boolean}  [isRefreshing]      - Passed to the header refresh button.
 * @prop {string}   [loadError]         - Shows an error state card.
 *
 * @emits toggle-task        - `{ cardIndex, sectionIndex, taskIndex }`
 * @emits toggle-pending     - `{ cardIndex, sectionIndex, taskIndex }`
 * @emits edit-checklist     - `{ cardIndex }`
 * @emits submit-checklist   - `{ cardIndex, checklistId }`
 * @emits log-temperature    - `{ checklistId, taskId, valueC }`
 * @emits update:activePeriod - New period string selected by the user.
 * @emits create             - User clicked "New checklist".
 * @emits open-library       - User clicked "Open library".
 * @emits manage-tasks       - User clicked the task pool manage button.
 * @emits refresh            - User clicked the refresh button.
 */
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
  canManageChecklists: {
    type: Boolean,
    default: false,
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
  reminderSummary: {
    type: Object,
    default: () => ({
      totalCount: 0,
      temperatureCount: 0,
      checklistTitles: [],
    }),
  },
  isLoading: {
    type: Boolean,
    default: false,
  },
  isRefreshing: {
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

.reminder-banner {
  padding: var(--space-4) var(--space-5);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-warning-border);
  background: linear-gradient(135deg, rgba(255, 244, 208, 0.92), rgba(255, 250, 237, 0.98));
  box-shadow: var(--shadow-sm);
}

.reminder-banner__title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  color: var(--color-warning-text);
}

.reminder-banner__copy,
.reminder-banner__meta {
  margin: var(--space-2) 0 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  line-height: var(--line-height-normal);
}

.reminder-banner__meta {
  color: var(--color-text-muted);
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
