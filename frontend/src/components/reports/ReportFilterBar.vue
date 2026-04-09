<template>
  <div class="filter-bar">
    <div class="filter-group">
      <label class="filter-label">Report type</label>
      <select class="filter-select" :value="reportType" @change="$emit('update:reportType', $event.target.value)">
        <option value="inspection">Full inspection report</option>
        <option value="summary">Internal summary</option>
      </select>
    </div>
    <div class="filter-group">
      <label class="filter-label">From</label>
      <input class="filter-input" type="date" :value="fromDate" @input="$emit('update:fromDate', $event.target.value)" />
    </div>
    <div class="filter-group">
      <label class="filter-label">To</label>
      <input class="filter-input" type="date" :value="toDate" @input="$emit('update:toDate', $event.target.value)" />
    </div>
    <button class="btn-generate" @click="$emit('generate')" :disabled="loading">
      {{ loading ? 'Generating...' : 'Generate report' }}
    </button>
    <button v-if="hasReport" class="btn-export" @click="$emit('export')">Export PDF</button>
    <button class="btn-deviation" type="button" @click="$emit('deviation')">
      Report deviation
    </button>
  </div>
</template>

<script setup>
/**
 * ReportFilterBar
 *
 * Toolbar at the top of the Reports view. Provides controls for selecting the
 * report type, date range, and triggering generation or PDF export. Uses v-model
 * style emits (`update:reportType`, `update:fromDate`, `update:toDate`) so the
 * parent view can bind the values with v-model.
 *
 * @prop {string}  [reportType] - Currently selected report type: 'inspection' | 'summary'.
 * @prop {string}  [fromDate]   - Start date value (ISO date string).
 * @prop {string}  [toDate]     - End date value (ISO date string).
 * @prop {boolean} [loading]    - Disables the Generate button while a report is loading.
 * @prop {boolean} [hasReport]  - Shows the Export PDF button when a report is available.
 *
 * @emits update:reportType - New report type selected.
 * @emits update:fromDate   - New from-date entered.
 * @emits update:toDate     - New to-date entered.
 * @emits generate          - User clicked "Generate report".
 * @emits export            - User clicked "Export PDF".
 * @emits deviation         - User clicked "Report deviation".
 */
defineProps({
  reportType: { type: String, default: 'inspection' },
  fromDate: { type: String, default: '' },
  toDate: { type: String, default: '' },
  loading: { type: Boolean, default: false },
  hasReport: { type: Boolean, default: false },
})

defineEmits(['update:reportType', 'update:fromDate', 'update:toDate', 'generate', 'export', 'deviation'])
</script>

<style scoped>
.filter-bar {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-5) var(--space-6);
  display: flex;
  align-items: flex-end;
  gap: var(--space-4);
  flex-wrap: wrap;
  box-shadow: var(--shadow-sm);
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.filter-label {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-muted);
}

.filter-select,
.filter-input {
  height: 36px;
  padding: 0 var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
  background: var(--color-bg-primary);
  font-family: inherit;
  outline: none;
}

.filter-select:focus,
.filter-input:focus { border-color: var(--color-dark-secondary); }

.btn-generate {
  margin-left: auto;
  height: 36px;
  padding: 0 var(--space-5);
  background: var(--color-accent);
  color: var(--color-dark-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-family: inherit;
  white-space: nowrap;
}

.btn-generate:hover { opacity: 0.85; }
.btn-generate:disabled { opacity: 0.5; cursor: not-allowed; }

.btn-export {
  height: 36px;
  padding: 0 var(--space-5);
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  cursor: pointer;
  font-family: inherit;
}

.btn-export:hover { border-color: var(--color-dark-primary); }

.btn-deviation {
  height: 36px;
  padding: 0 var(--space-5);
  background: var(--color-bg-primary);
  color: var(--color-danger, #dc2626);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  border: 1px solid var(--color-danger, #dc2626);
  border-radius: var(--radius-md);
  cursor: pointer;
  font-family: inherit;
  white-space: nowrap;
}

.btn-deviation:hover {
  background: var(--color-danger, #dc2626);
  color: white;
}

@media (max-width: 900px) {
  .filter-bar { flex-direction: column; align-items: stretch; }
  .btn-generate { margin-left: 0; }
}
</style>
