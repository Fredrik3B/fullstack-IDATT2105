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
              <span class="stat-number" :class="rateClass(report.foodStats.completionRate)">
                {{ formatRate(report.foodStats.completionRate) }}%
              </span>
              <span class="stat-desc">Completion rate</span>
            </div>
            <div class="stat-item">
              <span class="stat-number" :class="{ 'stat-danger': report.foodStats.deviatedTasks > 0 }">
                {{ report.foodStats.deviatedTasks }}
              </span>
              <span class="stat-desc">Deviations</span>
            </div>
            <div class="stat-item">
              <span class="stat-number" :class="{ 'stat-danger': report.foodStats.outOfRangeReadings > 0 }">
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
              <span class="stat-number" :class="rateClass(report.alcoholStats.completionRate)">
                {{ formatRate(report.alcoholStats.completionRate) }}%
              </span>
              <span class="stat-desc">Completion rate</span>
            </div>
            <div class="stat-item">
              <span class="stat-number" :class="{ 'stat-danger': report.alcoholStats.deviatedTasks > 0 }">
                {{ report.alcoholStats.deviatedTasks }}
              </span>
              <span class="stat-desc">Deviations</span>
            </div>
          </div>
        </div>
      </div>
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
defineProps({
  report: { type: Object, required: true }
})

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

function rateClass(rate) {
  if (rate >= 90) return 'stat-ok'
  if (rate > 0) return 'stat-warn'
  return ''
}
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
.module-dot { width: 8px; height: 8px; border-radius: var(--radius-full); flex-shrink: 0; }
.module-dot--food { background: var(--color-accent); }
.module-dot--alcohol { background: var(--color-dark-tertiary); }
.stat-row { display: flex; gap: var(--space-6); flex-wrap: wrap; }
.stat-item { display: flex; flex-direction: column; gap: 2px; }
.stat-number {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}
.stat-desc { font-size: var(--font-size-xs); color: var(--color-text-muted); }
.stat-ok { color: var(--color-success-text, #16a34a); }
.stat-warn { color: var(--color-warning, #d97706); }
.stat-danger { color: var(--color-danger, #dc2626); }

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

/* ── Responsive ── */
@media (max-width: 900px) {
  .stats-grid { grid-template-columns: 1fr; }
}

@media print {
  .report-sections { gap: var(--space-4); }
  .stat-block, .deviation-card, .period-card { box-shadow: none; border-color: #ddd; }
}
</style>
