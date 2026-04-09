<template>
  <div class="report-sections">

    <!-- Org header -->
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
      <ComplianceStatsGrid :food-stats="report.foodStats" :alcohol-stats="report.alcoholStats" />
    </section>

    <!-- Checklists -->
    <section>
      <h2 class="section-heading">Checklist performance</h2>
      <div class="insight-grid">
        <div class="insight-card">
          <span class="insight-label">Checklist completions</span>
          <strong class="insight-value">{{ totalChecklistCompletions }}</strong>
          <span class="insight-copy">Submitted runs during this period</span>
        </div>
        <div class="insight-card">
          <span class="insight-label">Avg completion rate</span>
          <strong class="insight-value">{{ formatRate(averageChecklistCompletionRate) }}%</strong>
          <span class="insight-copy">Average checklist quality across runs</span>
        </div>
      </div>
      <div class="report-table">
        <div class="table-head">
          <span>Name</span>
          <span>Area</span>
          <span>Frequency</span>
          <span>Runs</span>
          <span>Expected</span>
          <span>Avg rate</span>
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
          <span>{{ cl.completionsInPeriod }}</span>
          <span>{{ cl.expectedRuns }}</span>
          <span :class="rateClass(cl.averageCompletionRate)">{{ formatRate(cl.averageCompletionRate) }}%</span>
          <span :class="{ 'stat-danger': cl.deviatedTasks > 0 }">{{ cl.deviatedTasks }}</span>
        </div>
        <div v-if="!report.checklists.checklists.length" class="table-empty">
          No checklists in this period
        </div>
      </div>
    </section>

    <section>
      <h2 class="section-heading">Deviations by checklist</h2>
      <div class="report-table">
        <div class="table-head table-head--insights">
          <span>Checklist</span>
          <span>Area</span>
          <span>Deviations</span>
          <span>Runs</span>
          <span>Avg rate</span>
        </div>
        <div
          v-for="cl in deviationChecklistRows"
          :key="`${cl.name}-${cl.complianceArea}`"
          class="table-row table-row--insights"
        >
          <span class="cell-name">{{ cl.name }}</span>
          <span>
            <span class="module-badge" :class="cl.complianceArea === 'IK_MAT' ? 'module-badge--food' : 'module-badge--alcohol'">
              {{ cl.complianceArea === 'IK_MAT' ? 'Food' : 'Alcohol' }}
            </span>
          </span>
          <span :class="{ 'stat-danger': cl.deviatedTasks > 0 }">{{ cl.deviatedTasks }}</span>
          <span>{{ cl.completionsInPeriod }}</span>
          <span :class="rateClass(cl.averageCompletionRate)">{{ formatRate(cl.averageCompletionRate) }}%</span>
        </div>
        <div v-if="!deviationChecklistRows.length" class="table-empty">
          No checklist deviations in this period
        </div>
      </div>
    </section>

    <!-- Temperature chart -->
    <section v-if="report.temperatureLog.length">
      <h2 class="section-heading">Temperature trends by zone</h2>
      <div class="temperature-chart-grid">
        <div
          v-for="zone in temperatureZones"
          :key="zone.key"
          class="temperature-zone-card"
        >
          <div class="temperature-zone-header">
            <h3 class="temperature-zone-title">{{ zone.label }}</h3>
            <p class="temperature-zone-meta">{{ zone.log.length }} readings</p>
          </div>
          <TemperatureChart :log="zone.log" />
        </div>
      </div>
    </section>

    <section>
      <h2 class="section-heading">Deviations by day</h2>
      <DeviationTrendChart v-if="report.deviationsByDay.length" :points="report.deviationsByDay" />
      <div v-else class="table-empty chart-empty">No deviation trend in this period</div>
    </section>

    <section>
      <h2 class="section-heading">Most missed tasks</h2>
      <div class="report-table">
        <div class="table-head table-head--missed">
          <span>Task</span>
          <span>Checklist</span>
          <span>Area</span>
          <span>Missed</span>
        </div>
        <div
          v-for="task in report.missedTasks"
          :key="`${task.checklistName}-${task.taskName}`"
          class="table-row table-row--missed"
        >
          <span class="cell-name">{{ task.taskName }}</span>
          <span class="cell-muted">{{ task.checklistName }}</span>
          <span>
            <span class="module-badge" :class="task.complianceArea === 'IK_MAT' ? 'module-badge--food' : 'module-badge--alcohol'">
              {{ task.complianceArea === 'IK_MAT' ? 'Food' : 'Alcohol' }}
            </span>
          </span>
          <span :class="{ 'stat-danger': task.missedCount > 0 }">{{ task.missedCount }}</span>
        </div>
        <div v-if="!report.missedTasks.length" class="table-empty">
          No missed tasks in this period
        </div>
      </div>
    </section>

  </div>
</template>

<script setup>
/**
 * InspectionReport
 *
 * Full inspection report layout used when `reportType === 'inspection'`. Renders:
 * - Organisation header (name, period, admins, managers, staff count)
 * - Compliance stats grid (food + alcohol)
 * - Checklist performance table with completions, expected runs, and deviation counts
 * - Deviation highlights table (checklists with deviations, sorted by count desc)
 * - Temperature trend charts per zone (grouped from the flat temperatureLog array)
 * - Deviation-by-day bar chart
 * - Most-missed tasks table
 *
 * @prop {Object} report - The full inspection report object returned by the API.
 *   Key sub-objects: `organization`, `period`, `foodStats`, `alcoholStats`,
 *   `checklists`, `temperatureLog`, `deviationsByDay`, `missedTasks`.
 */
import { computed } from 'vue'
import DeviationTrendChart from '../ui/DeviationTrendChart.vue'
import TemperatureChart from '../ui/TemperatureChart.vue'
import ComplianceStatsGrid from './ComplianceStatsGrid.vue'
import { formatDate, formatDateTime, formatRate, rateClass, formatTemperatureRange } from './reportHelpers.js'

const props = defineProps({
  report: { type: Object, required: true }
})

/**
 * Groups the flat temperature log into per-zone objects keyed by zoneId (preferred)
 * or a composite of task name and target range. Entries within each zone are sorted
 * chronologically; zones are sorted alphabetically by label.
 * @returns {{ key: string, label: string, log: Object[] }[]}
 */
const temperatureZones = computed(() => {
  const grouped = new Map()
  const log = Array.isArray(props.report?.temperatureLog) ? props.report.temperatureLog : []

  log.forEach((point) => {
    const rangeLabel = formatTemperatureRange(point)
    const baseLabel = point.zoneName || point.taskName
    const label = rangeLabel ? `${baseLabel} (${rangeLabel})` : baseLabel
    const key =
      point.zoneId != null
        ? `zone-${point.zoneId}`
        : `${baseLabel}::${point.targetMin ?? ''}::${point.targetMax ?? ''}`

    if (!grouped.has(key)) {
      grouped.set(key, { key, label, log: [] })
    }
    grouped.get(key).log.push(point)
  })

  return Array.from(grouped.values())
    .map((zone) => ({
      ...zone,
      log: [...zone.log].sort((a, b) => new Date(a.measuredAt) - new Date(b.measuredAt)),
    }))
    .sort((a, b) => a.label.localeCompare(b.label))
})

const checklistRows = computed(() =>
  Array.isArray(props.report?.checklists?.checklists) ? props.report.checklists.checklists : []
)

const totalChecklistCompletions = computed(() =>
  checklistRows.value.reduce((sum, cl) => sum + Number(cl.completionsInPeriod || 0), 0),
)

const averageChecklistCompletionRate = computed(() => {
  if (!checklistRows.value.length) return 0
  return checklistRows.value.reduce((sum, cl) => sum + Number(cl.averageCompletionRate || 0), 0) / checklistRows.value.length
})

const deviationChecklistRows = computed(() =>
  [...checklistRows.value]
    .filter((cl) => Number(cl.deviatedTasks || 0) > 0)
    .sort((a, b) =>
      Number(b.deviatedTasks || 0) - Number(a.deviatedTasks || 0) ||
      String(a.name).localeCompare(String(b.name)),
    )
    .slice(0, 8),
)
</script>

<style scoped>
/* ── Header card ── */
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

.report-sections {
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
}

/* ── Temperature ── */
.temperature-chart-grid { display: grid; gap: var(--space-4); }
.temperature-zone-card { display: grid; gap: var(--space-3); }

.temperature-zone-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: var(--space-3);
}

.temperature-zone-title {
  margin: 0;
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.temperature-zone-meta {
  margin: 0;
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}

/* ── Insight cards ── */
.insight-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

.insight-card {
  display: grid;
  gap: 6px;
  padding: var(--space-5);
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.insight-label {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.insight-value {
  font-size: 30px;
  line-height: 1;
  color: var(--color-text-primary);
}

.insight-copy {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}

/* ── State classes (used in tables) ── */
.stat-ok { color: var(--color-success-text, #16a34a); }
.stat-warn { color: var(--color-warning, #d97706); }
.stat-danger { color: var(--color-danger, #dc2626); }

/* ── Tables ── */
.report-table {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.table-head {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr 1fr 1fr;
  padding: var(--space-3) var(--space-6);
  background: var(--color-bg-tertiary);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  border-bottom: 1px solid var(--color-border);
}

.table-head--insights { grid-template-columns: 2fr 1fr 1fr 1fr 1fr; }
.table-head--missed { grid-template-columns: 2fr 1.5fr 1fr 0.8fr; }

.table-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr 1fr 1fr;
  padding: var(--space-3) var(--space-6);
  border-bottom: 1px solid var(--color-border-subtle, var(--color-border));
  align-items: center;
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
}

.table-row--insights { grid-template-columns: 2fr 1fr 1fr 1fr 1fr; }
.table-row--missed { grid-template-columns: 2fr 1.5fr 1fr 0.8fr; }
.table-row:last-child { border-bottom: none; }

.table-empty {
  padding: var(--space-8);
  text-align: center;
  font-size: var(--font-size-sm);
  color: var(--color-text-hint);
}

.chart-empty {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
}

.cell-name { font-weight: var(--font-weight-medium); }
.cell-muted { color: var(--color-text-muted); font-size: var(--font-size-xs); }

/* ── Badges ── */
.module-badge {
  display: inline-block;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  padding: 2px var(--space-2);
  border-radius: var(--radius-full);
}

.module-badge--food { background: var(--color-accent-light); color: var(--color-accent-text); }
.module-badge--alcohol { background: var(--color-bg-subtle, #f0f0f0); color: var(--color-dark-tertiary); }

/* ── Responsive ── */
@media (max-width: 900px) {
  .insight-grid { grid-template-columns: 1fr; }
  .report-header-top { flex-direction: column; gap: var(--space-2); }
  .report-org-details { flex-direction: column; gap: var(--space-3); }
  .table-head, .table-row { font-size: var(--font-size-xs); padding: var(--space-2) var(--space-3); }
}

@media print {
  .report-sections { gap: var(--space-4); }
  .report-header-card, .report-table { box-shadow: none; border-color: #ddd; }
}
</style>
