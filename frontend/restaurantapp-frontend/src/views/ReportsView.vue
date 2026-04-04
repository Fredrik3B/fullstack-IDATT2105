<template>
  <div class="page-root">
    <section class="page-banner">
      <div class="page-banner-inner">
        <span class="page-tag">Reports</span>
        <h1 class="page-heading">Audit and <span class="page-accent">inspection reports</span></h1>
        <p class="page-sub">Compliance reports for internal reviews and Mattilsynet inspections</p>
      </div>
    </section>

    <main class="page-main">
      <div class="page-content">

        <!-- Filter bar -->
        <div class="filter-bar">
          <div class="filter-group">
            <label class="filter-label" for="filter-from">From</label>
            <input class="filter-input" id="filter-from" type="date" v-model="fromDate" />
          </div>
          <div class="filter-group">
            <label class="filter-label" for="filter-to">To</label>
            <input class="filter-input" id="filter-to" type="date" v-model="toDate" />
          </div>
          <button class="btn-generate" type="button" @click="loadReport" :disabled="loading">
            {{ loading ? 'Generating...' : 'Generate report' }}
          </button>
          <button v-if="report" class="btn-export" type="button" @click="printReport">
            Export PDF
          </button>
        </div>

        <!-- Loading -->
        <div v-if="loading" class="empty-state">
          <p class="empty-title">Generating report...</p>
        </div>

        <!-- Error -->
        <div v-else-if="error" class="empty-state">
          <p class="empty-title">Failed to load report</p>
          <p class="empty-sub">{{ error }}</p>
        </div>

        <!-- Empty -->
        <div v-else-if="!report" class="empty-state">
          <div class="empty-icon">&#128196;</div>
          <p class="empty-title">No report generated</p>
          <p class="empty-sub">Select a date range and click generate to create an inspection report.</p>
        </div>

        <!-- Report content -->
        <template v-else>

          <!-- Report header -->
          <div class="report-header-card">
            <div class="report-header-top">
              <div>
                <h2 class="report-org-name">{{ report.organization.name }}</h2>
                <p class="report-period">
                  {{ formatDate(report.period.from) }} – {{ formatDate(report.period.to) }}
                </p>
              </div>
              <span class="report-generated">Generated {{ formatDateTime(report.generatedAt) }}</span>
            </div>
            <div class="report-org-details">
              <div class="org-detail">
                <span class="org-detail-label">Admins</span>
                <span class="org-detail-value">{{ report.organization.adminNames.join(', ') || 'None' }}</span>
              </div>
              <div class="org-detail">
                <span class="org-detail-label">Managers</span>
                <span class="org-detail-value">{{ report.organization.managerNames.join(', ') || 'None' }}</span>
              </div>
              <div class="org-detail">
                <span class="org-detail-label">Total staff</span>
                <span class="org-detail-value">{{ report.organization.totalStaff }}</span>
              </div>
            </div>
          </div>

          <!-- Stats overview -->
          <section>
            <h2 class="section-heading">Compliance overview</h2>
            <div class="stats-grid">
              <div class="stat-block">
                <h3 class="stat-block-title">
                  <span class="module-dot module-dot--food"></span>
                  IK-Mat (Food)
                </h3>
                <div class="stat-row">
                  <div class="stat-item">
                    <span class="stat-number">{{ report.foodStats.completedTasks }}/{{ report.foodStats.totalTasks }}</span>
                    <span class="stat-desc">Tasks completed</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-number" :class="report.foodStats.completionRate >= 90 ? 'stat-ok' : 'stat-warn'">
                      {{ formatRate(report.foodStats.completionRate) }}%
                    </span>
                    <span class="stat-desc">Completion rate</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-number" :class="report.foodStats.deviatedTasks > 0 ? 'stat-danger' : ''">
                      {{ report.foodStats.deviatedTasks }}
                    </span>
                    <span class="stat-desc">Deviations</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-number" :class="report.foodStats.outOfRangeReadings > 0 ? 'stat-danger' : ''">
                      {{ report.foodStats.outOfRangeReadings }}/{{ report.foodStats.temperatureReadings }}
                    </span>
                    <span class="stat-desc">Temp. out of range</span>
                  </div>
                </div>
              </div>

              <div class="stat-block">
                <h3 class="stat-block-title">
                  <span class="module-dot module-dot--alcohol"></span>
                  IK-Alkohol (Alcohol)
                </h3>
                <div class="stat-row">
                  <div class="stat-item">
                    <span class="stat-number">{{ report.alcoholStats.completedTasks }}/{{ report.alcoholStats.totalTasks }}</span>
                    <span class="stat-desc">Tasks completed</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-number" :class="report.alcoholStats.completionRate >= 90 ? 'stat-ok' : 'stat-warn'">
                      {{ formatRate(report.alcoholStats.completionRate) }}%
                    </span>
                    <span class="stat-desc">Completion rate</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-number" :class="report.alcoholStats.deviatedTasks > 0 ? 'stat-danger' : ''">
                      {{ report.alcoholStats.deviatedTasks }}
                    </span>
                    <span class="stat-desc">Deviations</span>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <!-- Checklists -->
          <section>
            <h2 class="section-heading">Checklists ({{ report.checklists.activeChecklists }} active of {{ report.checklists.totalChecklists }})</h2>
            <div class="checklist-table">
              <div class="table-head">
                <span>Name</span>
                <span>Area</span>
                <span>Frequency</span>
                <span>Completed</span>
                <span>Rate</span>
                <span>Deviations</span>
              </div>
              <div v-for="cl in report.checklists.checklists" :key="cl.name" class="table-row">
                <span class="cell-name">{{ cl.name }}</span>
                <span>
                  <span class="module-badge" :class="cl.complianceArea === 'IK_MAT' ? 'module-badge--food' : 'module-badge--alcohol'">
                    {{ cl.complianceArea === 'IK_MAT' ? 'Food' : 'Alcohol' }}
                  </span>
                </span>
                <span class="cell-muted">{{ cl.frequency }}</span>
                <span>{{ cl.completedTasks }}/{{ cl.totalTasks }}</span>
                <span :class="cl.completionRate >= 90 ? 'stat-ok' : 'stat-warn'">{{ formatRate(cl.completionRate) }}%</span>
                <span :class="cl.deviatedTasks > 0 ? 'stat-danger' : ''">{{ cl.deviatedTasks }}</span>
              </div>
              <div v-if="!report.checklists.checklists.length" class="table-empty">
                No checklists in this period
              </div>
            </div>
          </section>

          <!-- Temperature log -->
          <section>
            <h2 class="section-heading">Temperature log ({{ report.temperatureLog.length }} readings)</h2>
            <div class="checklist-table">
              <div class="table-head table-head--temp">
                <span>Time</span>
                <span>Task</span>
                <span>Value</span>
                <span>Range</span>
                <span>Status</span>
                <span>Recorded by</span>
              </div>
              <div v-for="(point, i) in report.temperatureLog" :key="i"
                   class="table-row table-row--temp"
                   :class="{ 'table-row--danger': !point.withinRange }">
                <span class="cell-muted">{{ formatDateTime(point.measuredAt) }}</span>
                <span class="cell-name">{{ point.taskName }}</span>
                <span class="cell-temp" :class="point.withinRange ? '' : 'stat-danger'">
                  {{ point.valueC }}°C
                </span>
                <span class="cell-muted">{{ point.targetMin }}°C – {{ point.targetMax }}°C</span>
                <span>
                  <span class="status-pill" :class="point.withinRange ? 'status-pill--ok' : 'status-pill--danger'">
                    {{ point.withinRange ? 'OK' : 'Out of range' }}
                  </span>
                </span>
                <span class="cell-muted">{{ point.recordedBy }}</span>
              </div>
              <div v-if="!report.temperatureLog.length" class="table-empty">
                No temperature readings in this period
              </div>
            </div>
          </section>

          <!-- Deviations -->
          <section>
            <h2 class="section-heading">Deviations ({{ report.deviations.length }})</h2>
            <div v-if="report.deviations.length" class="deviation-list">
              <div v-for="(dev, i) in report.deviations" :key="i" class="deviation-card">
                <div class="deviation-header">
                  <span class="deviation-task">{{ dev.taskName }}</span>
                  <span class="cell-muted">{{ dev.checklistName }}</span>
                </div>
                <div class="deviation-details">
                  <span class="cell-muted" v-if="dev.endedAt">{{ formatDateTime(dev.endedAt) }}</span>
                  <span class="cell-muted" v-if="dev.periodKey">Period: {{ dev.periodKey }}</span>
                </div>
                <p v-if="dev.meta" class="deviation-meta">{{ dev.meta }}</p>
              </div>
            </div>
            <div v-else class="empty-state empty-state--small">
              <p class="empty-title">No deviations</p>
              <p class="empty-sub">No deviated tasks found in this period.</p>
            </div>
          </section>

        </template>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { fetchInspectionReport } from '../api/reports'

const report = ref(null)
const loading = ref(false)
const error = ref('')

const today = new Date()
const monthAgo = new Date(today)
monthAgo.setMonth(monthAgo.getMonth() - 1)

const fromDate = ref(monthAgo.toISOString().slice(0, 10))
const toDate = ref(today.toISOString().slice(0, 10))

async function loadReport() {
  loading.value = true
  error.value = ''
  report.value = null
  try {
    report.value = await fetchInspectionReport({
      from: fromDate.value + 'T00:00:00',
      to: toDate.value + 'T23:59:59'
    })
  } catch (e) {
    error.value = e.response?.data?.detail || e.message || 'Unknown error'
  } finally {
    loading.value = false
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('en-GB', { day: 'numeric', month: 'long', year: 'numeric' })
}

function formatDateTime(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('en-GB', {
    day: 'numeric', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

function formatRate(rate) {
  if (rate == null || isNaN(rate)) return '0'
  return Number(rate).toFixed(1)
}

function printReport() {
  window.print()
}
</script>

<style scoped>
.page-root {
  min-height: 100vh;
  background: var(--color-bg-secondary);
}

/* ── Banner ── */
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

/* ── Main ── */
.page-main { padding: var(--space-10) var(--space-6); }
.page-content {
  max-width: 960px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
}

/* ── Filter bar ── */
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

/* ── Report header ── */
.report-header-card {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
}
.report-header-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-5);
}
.report-org-name {
  margin: 0;
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}
.report-period {
  margin: var(--space-1) 0 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}
.report-generated {
  font-size: var(--font-size-xs);
  color: var(--color-text-hint);
  white-space: nowrap;
}
.report-org-details {
  display: flex;
  gap: var(--space-8);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
}
.org-detail { display: flex; flex-direction: column; gap: 2px; }
.org-detail-label {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-muted);
}
.org-detail-value {
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

/* ── Stats ── */
.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
}
.stat-block {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-5) var(--space-6);
  box-shadow: var(--shadow-sm);
}
.stat-block-title {
  margin: 0 0 var(--space-4);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  display: flex;
  align-items: center;
  gap: var(--space-2);
}
.module-dot {
  width: 8px; height: 8px;
  border-radius: var(--radius-full);
  flex-shrink: 0;
}
.module-dot--food { background: var(--color-accent); }
.module-dot--alcohol { background: var(--color-dark-tertiary); }
.stat-row {
  display: flex;
  gap: var(--space-6);
  flex-wrap: wrap;
}
.stat-item { display: flex; flex-direction: column; gap: 2px; }
.stat-number {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}
.stat-desc {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}
.stat-ok { color: var(--color-success-text, #16a34a); }
.stat-warn { color: var(--color-warning, #d97706); }
.stat-danger { color: var(--color-danger, #dc2626); }

/* ── Tables ── */
.checklist-table {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}
.table-head {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr 1fr;
  padding: var(--space-3) var(--space-6);
  background: var(--color-bg-tertiary);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  border-bottom: 1px solid var(--color-border);
}
.table-head--temp {
  grid-template-columns: 1.2fr 1.5fr 0.7fr 1fr 1fr 1.2fr;
}
.table-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr 1fr;
  padding: var(--space-3) var(--space-6);
  border-bottom: 1px solid var(--color-border-subtle, var(--color-border));
  align-items: center;
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
}
.table-row--temp {
  grid-template-columns: 1.2fr 1.5fr 0.7fr 1fr 1fr 1.2fr;
}
.table-row:last-child { border-bottom: none; }
.table-row--danger {
  background: color-mix(in srgb, var(--color-danger, #dc2626) 6%, transparent);
}
.table-empty {
  padding: var(--space-8);
  text-align: center;
  font-size: var(--font-size-sm);
  color: var(--color-text-hint);
}
.cell-name { font-weight: var(--font-weight-medium); }
.cell-muted { color: var(--color-text-muted); font-size: var(--font-size-xs); }
.cell-temp { font-weight: var(--font-weight-bold); }

/* ── Badges/pills ── */
.module-badge {
  display: inline-block;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  padding: 2px var(--space-2);
  border-radius: var(--radius-full);
}
.module-badge--food { background: var(--color-accent-light); color: var(--color-accent-text); }
.module-badge--alcohol { background: var(--color-bg-subtle, #f0f0f0); color: var(--color-dark-tertiary); }
.status-pill {
  display: inline-block;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  padding: 2px var(--space-2);
  border-radius: var(--radius-full);
}
.status-pill--ok { background: var(--color-success-bg, #dcfce7); color: var(--color-success-text, #16a34a); }
.status-pill--danger { background: color-mix(in srgb, var(--color-danger, #dc2626) 12%, transparent); color: var(--color-danger, #dc2626); }

/* ── Deviations ── */
.deviation-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}
.deviation-card {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-left: 3px solid var(--color-danger, #dc2626);
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
.deviation-task {
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}
.deviation-details {
  display: flex;
  gap: var(--space-4);
  margin-bottom: var(--space-2);
}
.deviation-meta {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

/* ── Empty state ── */
.empty-state {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-16) var(--space-6);
  text-align: center;
  box-shadow: var(--shadow-sm);
}
.empty-state--small { padding: var(--space-8) var(--space-6); }
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

/* ── Print ── */
@media print {
  .page-banner, .filter-bar, .btn-export { display: none; }
  .page-root { background: white; }
  .page-main { padding: 0; }
  .report-header-card, .stat-block, .checklist-table, .deviation-card {
    box-shadow: none;
    border-color: #ddd;
  }
}

/* ── Responsive ── */
@media (max-width: 900px) {
  .stats-grid { grid-template-columns: 1fr; }
  .filter-bar { flex-direction: column; align-items: stretch; }
  .btn-generate { margin-left: 0; }
  .report-header-top { flex-direction: column; gap: var(--space-2); }
  .report-org-details { flex-direction: column; gap: var(--space-3); }
  .table-head, .table-row { font-size: var(--font-size-xs); padding: var(--space-2) var(--space-3); }
}
</style>
