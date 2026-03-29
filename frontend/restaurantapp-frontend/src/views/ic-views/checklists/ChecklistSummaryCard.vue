<template>
  <footer class="summary-card" aria-label="Checklist summary">
    <div class="summary-top">
      <h3 class="summary-title">Summary</h3>
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
  </footer>
</template>

<script setup>
import { computed } from 'vue'
import { countTaskStates, getSummaryAriaLabel } from './checklistSummary'

const props = defineProps({
  cards: {
    type: Array,
    default: () => []
  }
})

const totals = computed(() => countTaskStates(props.cards))
const summaryAriaLabel = computed(() => getSummaryAriaLabel(totals.value))
</script>

<style scoped>
.summary-card {
  margin-top: 18px;
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid rgba(210, 213, 230, 0.95);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: var(--shadow-sm);
}

.summary-top {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.summary-title {
  margin: 0;
  font-size: 14px;
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.summary-total {
  color: var(--color-text-muted);
  font-size: 13px;
  font-weight: var(--font-weight-bold);
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
  gap: 10px;
  margin-top: 12px;
}

.legend-item {
  display: grid;
  grid-template-columns: 10px minmax(0, 1fr) auto;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 14px;
  background: rgba(242, 243, 250, 0.58);
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

