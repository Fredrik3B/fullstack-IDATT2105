<template>
  <div class="report-sections">

    <!-- Period -->
    <div class="period-card">
      <span class="period-label">Report period</span>
      <span class="period-value">{{ formatDate(report.period.from) }} – {{ formatDate(report.period.to) }}</span>
    </div>

    <!-- Stats overview -->
    <section>
      <h2 class="section-heading">Compliance overview</h2>
      <ComplianceStatsGrid :food-stats="report.foodStats" :alcohol-stats="report.alcoholStats" />
    </section>

    <!-- Unresolved items -->
    <section v-if="report.unresolvedItems?.length">
      <h2 class="section-heading">Unresolved items ({{ report.unresolvedItems.length }})</h2>
      <div class="deviation-list">
        <div v-for="(item, i) in report.unresolvedItems" :key="i" class="deviation-card">
          <div class="deviation-header">
            <span class="deviation-task">{{ item.name }}</span>
          </div>
          <div class="deviation-details" v-if="item.notDoneBy">
            <span class="cell-muted">{{ formatDateTime(item.notDoneBy) }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- No unresolved -->
    <section v-else>
      <h2 class="section-heading">Unresolved items</h2>
      <div class="empty-state-small">
        <p class="empty-title">All clear</p>
        <p class="empty-sub">No unresolved items in this period.</p>
      </div>
    </section>

  </div>
</template>

<script setup>
/**
 * SummaryReport
 *
 * Condensed internal summary report layout used when `reportType === 'summary'`.
 * Renders the report period, a compliance stats grid, and a section listing any
 * unresolved checklist items from the period. Shows an "All clear" message when
 * there are no unresolved items.
 *
 * @prop {Object} report - The summary report object returned by the API.
 *   Key sub-objects: `period`, `foodStats`, `alcoholStats`, `unresolvedItems`.
 */
import ComplianceStatsGrid from './ComplianceStatsGrid.vue'
import { formatDate, formatDateTime } from './reportHelpers.js'

defineProps({
  report: { type: Object, required: true }
})
</script>

<style scoped>
.report-sections {
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
}

/* ── Period card ── */
.period-card {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-4) var(--space-6);
  box-shadow: var(--shadow-sm);
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.period-label {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-text-muted);
}

.period-value {
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
}

/* ── Section heading ── */
.section-heading {
  margin: 0 0 var(--space-4) 0;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

/* ── Deviations ── */
.deviation-list { display: flex; flex-direction: column; gap: var(--space-3); }

.deviation-card {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-left: 3px solid var(--color-warning, #d97706);
  border-radius: var(--radius-md);
  padding: var(--space-4) var(--space-5);
  box-shadow: var(--shadow-sm);
}

.deviation-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: var(--space-2);
}

.deviation-task { font-weight: var(--font-weight-bold); color: var(--color-text-primary); }
.deviation-details { display: flex; gap: var(--space-4); }
.cell-muted { color: var(--color-text-muted); font-size: var(--font-size-xs); }

/* ── Empty ── */
.empty-state-small {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-8) var(--space-6);
  text-align: center;
  box-shadow: var(--shadow-sm);
}

.empty-title {
  margin: 0 0 var(--space-2);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.empty-sub {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

@media print {
  .report-sections { gap: var(--space-4); }
  .deviation-card, .period-card { box-shadow: none; border-color: #ddd; }
}
</style>
