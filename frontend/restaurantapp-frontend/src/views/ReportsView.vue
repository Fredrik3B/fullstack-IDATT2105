<template>
  <div class="page-root">
    <section class="page-banner">
      <!-- ... same banner ... -->
    </section>

    <main class="page-main">
      <div class="page-content">
        <div class="filter-bar">
          <div class="filter-group">
            <label class="filter-label">Report type</label>
            <select class="filter-select" v-model="reportType">
              <option value="inspection">Full inspection report</option>
              <option value="summary">Internal summary</option>
            </select>
          </div>
          <div class="filter-group">
            <label class="filter-label">From</label>
            <input class="filter-input" type="date" v-model="fromDate" />
          </div>
          <div class="filter-group">
            <label class="filter-label">To</label>
            <input class="filter-input" type="date" v-model="toDate" />
          </div>
          <button class="btn-generate" @click="loadReport" :disabled="loading">
            {{ loading ? 'Generating...' : 'Generate report' }}
          </button>
          <button v-if="report" class="btn-export" @click="printReport">Export PDF</button>
          <button class="btn-deviation" type="button" @click="showDeviationForm = true">
            Report deviation
          </button>
        </div>

        <div v-if="loading" class="empty-state">
          <p class="empty-title">Generating report...</p>
        </div>

        <div v-else-if="error" class="empty-state">
          <p class="empty-title">Failed to load report</p>
          <p class="empty-sub">{{ error }}</p>
        </div>

        <div v-else-if="!report" class="empty-state">
          <div class="empty-icon">&#128196;</div>
          <p class="empty-title">No report generated</p>
          <p class="empty-sub">Select a date range and click generate.</p>
        </div>

        <InspectionReport v-else-if="reportType === 'inspection'" :report="report" />
        <SummaryReport v-else :report="report" />

        <div v-if="showDeviationForm" class="modal-overlay" @click.self="showDeviationForm = false">
          <DeviationReportForm
            @cancel="showDeviationForm = false"
            @submitted="onDeviationSubmitted"
          />
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { fetchInspectionReport, fetchSummaryReport } from '../api/reports'
import InspectionReport from '@/components/reports/InspectionReport.vue'
import SummaryReport from '@/components/reports/SummaryReport.vue'
import DeviationReportForm from '@/components/reports/DeviationReportForm.vue'

const route = useRoute()

const showDeviationForm = ref(false)

function onDeviationSubmitted() {
  showDeviationForm.value = false
  if (report.value) loadReport()
}

const reportType = ref('inspection')
const report = ref(null)
const loading = ref(false)
const error = ref('')

const today = new Date()
const monthAgo = new Date(today)
monthAgo.setMonth(monthAgo.getMonth() - 1)
const fromDate = ref(monthAgo.toISOString().slice(0, 10))
const toDate = ref(today.toISOString().slice(0, 10))

function applyPresetRange(preset) {
  const now = new Date()

  if (preset === 'today') {
    fromDate.value = now.toISOString().slice(0, 10)
    toDate.value = now.toISOString().slice(0, 10)
    return
  }

  if (preset === 'week') {
    const weekAgo = new Date(now)
    weekAgo.setDate(weekAgo.getDate() - 7)
    fromDate.value = weekAgo.toISOString().slice(0, 10)
    toDate.value = now.toISOString().slice(0, 10)
    return
  }

  if (preset === 'month') {
    const monthBack = new Date(now)
    monthBack.setMonth(monthBack.getMonth() - 1)
    fromDate.value = monthBack.toISOString().slice(0, 10)
    toDate.value = now.toISOString().slice(0, 10)
  }
}

async function applyRouteQueryIntent(query) {
  const action = String(query?.action ?? '').toLowerCase()
  const preset = String(query?.preset ?? '').toLowerCase()
  const requestedType = String(query?.reportType ?? '').toLowerCase()
  const autoLoad = String(query?.autoload ?? '') === '1'

  if (requestedType === 'inspection' || requestedType === 'summary') {
    reportType.value = requestedType
  }

  if (preset) {
    applyPresetRange(preset)
  }

  if (action === 'deviation') {
    showDeviationForm.value = true
  }

  if (autoLoad) {
    await loadReport()
  }
}

async function loadReport() {
  loading.value = true
  error.value = ''
  report.value = null
  try {
    const params = {
      from: fromDate.value + 'T00:00:00',
      to: toDate.value + 'T23:59:59'
    }
    report.value = reportType.value === 'inspection'
      ? await fetchInspectionReport(params)
      : await fetchSummaryReport(params)
  } catch (e) {
    error.value = e.response?.data?.detail || e.message || 'Unknown error'
  } finally {
    loading.value = false
  }
}

function printReport() {
  window.print()
}

onMounted(async () => {
  await applyRouteQueryIntent(route.query)
})

watch(
  () => route.query,
  async (query) => {
    await applyRouteQueryIntent(query)
  }
)
</script>

<style scoped>
.page-root {
  min-height: 100vh;
  background: var(--color-bg-secondary);
}

.page-banner {
  background: var(--color-dark-primary);
  border-bottom: 1px solid var(--color-dark-secondary);
  padding: var(--space-10) var(--space-6);
}
.page-banner-inner {
  max-width: 960px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}
.page-tag {
  display: inline-block;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--color-accent);
  border: 1px solid var(--color-accent);
  border-radius: var(--radius-full);
  padding: 3px var(--space-3);
  align-self: flex-start;
}
.page-heading {
  margin: 0;
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: #ffffff;
  line-height: var(--line-height-tight);
}
.page-accent { color: var(--color-accent); }
.page-sub {
  margin: 0;
  font-size: var(--font-size-md);
  color: var(--color-dark-border);
}

.page-main { padding: var(--space-10) var(--space-6); }
.page-content {
  max-width: 960px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
}

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

.empty-state {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-16) var(--space-6);
  text-align: center;
  box-shadow: var(--shadow-sm);
}
.empty-icon { font-size: 40px; margin-bottom: var(--space-4); }
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
  max-width: 400px;
  margin-inline: auto;
}
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

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: var(--space-6);
}
.modal-overlay > * {
  max-width: 720px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
}

@media print {
  .page-banner, .filter-bar { display: none; }
  .page-root { background: white; }
  .page-main { padding: 0; }
}

@media (max-width: 900px) {
  .filter-bar { flex-direction: column; align-items: stretch; }
  .btn-generate { margin-left: 0; }
}
</style>
