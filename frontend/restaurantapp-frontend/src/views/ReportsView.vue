<template>
  <div class="page-root">
    <main class="page-main">
      <div class="page-content">

        <PageHeader
          eyebrow="Reports"
          title="Reports &amp; Deviations"
          description="Generate inspection reports and log food safety deviations"
        >
          <template #actions>
            <div class="insight-card">
              <span class="insight-label">Date range</span>
              <span class="insight-value">{{ fromDate }} – {{ toDate }}</span>
              <span class="insight-text">Adjust the filters below to generate a report</span>
            </div>
          </template>
        </PageHeader>

        <ReportFilterBar
          v-model:reportType="reportType"
          v-model:fromDate="fromDate"
          v-model:toDate="toDate"
          :loading="loading"
          :has-report="!!report"
          @generate="loadReport"
          @export="printReport"
          @deviation="showDeviationForm = true"
        />

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
import { fetchInspectionReport, fetchSummaryReport } from '@/api/reports'
import PageHeader from '@/components/layout/PageHeader.vue'
import ReportFilterBar from '@/components/reports/ReportFilterBar.vue'
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
  const from = new Date(now)

  if (preset === 'today') { /* from stays as now */ }
  else if (preset === 'week') { from.setDate(from.getDate() - 7) }
  else if (preset === 'month') { from.setMonth(from.getMonth() - 1) }
  else return

  fromDate.value = from.toISOString().slice(0, 10)
  toDate.value = now.toISOString().slice(0, 10)
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
  background: transparent;
}

.page-main { padding: var(--space-8) var(--space-6); }

.page-content {
  max-width: var(--max-width);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

/* ── Insight card (inside PageHeader slot) ── */
.insight-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  padding: var(--space-5);
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(200, 200, 216, 0.18);
}

.insight-label {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-dark-border);
}

.insight-value {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: #ffffff;
}

.insight-text {
  font-size: var(--font-size-sm);
  line-height: var(--line-height-normal);
  color: rgba(255, 255, 255, 0.76);
}

/* ── Empty / error states ── */
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

/* ── Deviation modal ── */
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
  .page-main { padding: 0; }
  .page-root { background: white; }
}
</style>
