<template>
  <section class="summary-card" aria-label="Checklist summary">
    <div class="summary-top">
      <div>
        <h3 class="summary-title">Checklist summary</h3>
        <p class="summary-copy">
          Completion across all workbench checklists in the selected period.
        </p>
      </div>
      <div class="summary-total">{{ totals.total }} tasks</div>
    </div>

    <div class="summary-bar" role="img" :aria-label="summaryAriaLabel">
      <div
        class="summary-segment summary-segment--completed"
        :style="{ width: `${totals.total ? totals.completedPct : 0}%` }"
      ></div>
      <div
        class="summary-segment summary-segment--pending"
        :style="{ width: `${totals.total ? totals.pendingPct : 0}%` }"
      ></div>
      <div
        class="summary-segment summary-segment--todo"
        :style="{ width: `${totals.total ? totals.todoPct : 0}%` }"
      ></div>
    </div>

    <div class="summary-legend">
      <div class="legend-item">
        <span class="legend-dot legend-dot--completed"></span>
        <span class="legend-label">Completed</span>
        <span class="legend-value">{{ totals.completed }}</span>
      </div>
      <div class="legend-item">
        <span class="legend-dot legend-dot--pending"></span>
        <span class="legend-label">Flagged</span>
        <span class="legend-value">{{ totals.pending }}</span>
      </div>
      <div class="legend-item">
        <span class="legend-dot legend-dot--todo"></span>
        <span class="legend-label">Not started</span>
        <span class="legend-value">{{ totals.todo }}</span>
      </div>
    </div>
  </section>
</template>

<script setup>
/**
 * ChecklistSummaryCard
 *
 * Overview card shown at the top of IC module dashboards. Aggregates task states
 * across all workbench checklists for the selected period and displays a
 * segmented progress bar (completed / pending / not started) with a legend.
 * Segment widths and aria labels are derived from the `countTaskStates` composable.
 *
 * @prop {Array} [cards] - Array of checklist card objects from the workbench.
 */
import { computed } from 'vue'
import { countTaskStates, getSummaryAriaLabel } from '@/composables/ic-checklists/checklistSummary'

const props = defineProps({
  cards: {
    type: Array,
    default: () => [],
  },
})

const totals = computed(() => countTaskStates(props.cards))
const summaryAriaLabel = computed(() => getSummaryAriaLabel(totals.value))
</script>

<style scoped>
.summary-card {
  padding: var(--space-5);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  background: var(--color-bg-primary);
  box-shadow: var(--shadow-sm);
}

.summary-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.summary-title {
  margin: 0;
  font-size: var(--font-size-lg);
  color: var(--color-text-primary);
}

.summary-copy {
  margin: var(--space-1) 0 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.summary-total {
  min-width: 84px;
  text-align: right;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-secondary);
}

.summary-bar {
  display: flex;
  height: 10px;
  border-radius: var(--radius-full);
  overflow: hidden;
  background: var(--color-bg-tertiary);
  border: 1px solid var(--color-border-subtle);
}

.summary-segment {
  height: 100%;
  min-width: 0;
  transition: width var(--transition-normal);
}

.summary-segment--completed {
  background: var(--color-success);
}

.summary-segment--pending {
  background: var(--color-warning);
}

.summary-segment--todo {
  background: rgba(75, 74, 114, 0.22);
}

.summary-legend {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-3);
  margin-top: var(--space-4);
}

.legend-item {
  display: grid;
  grid-template-columns: 10px minmax(0, 1fr) auto;
  align-items: center;
  gap: 8px;
  padding: var(--space-3);
  border-radius: var(--radius-md);
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-subtle);
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.legend-dot--completed {
  background: var(--color-success);
}

.legend-dot--pending {
  background: var(--color-warning);
}

.legend-dot--todo {
  background: rgba(75, 74, 114, 0.35);
}

.legend-label {
  color: var(--color-text-secondary);
  font-size: 13px;
  font-weight: var(--font-weight-medium);
}

.legend-value {
  color: var(--color-text-primary);
  font-size: 13px;
  font-weight: var(--font-weight-bold);
}

@media (max-width: 980px) {
  .summary-legend {
    grid-template-columns: 1fr;
  }
}
</style>
