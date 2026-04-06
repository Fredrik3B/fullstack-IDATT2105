<template>
  <section class="temp-report" aria-label="Temperature report">
    <div class="report-top">
      <div>
        <h3 class="report-title">Temperature log</h3>
        <p class="report-copy">
          Latest recorded readings for temperature-controlled tasks on the workbench.
        </p>
      </div>
      <div class="report-badge" :class="{ danger: deviationCount > 0 }">
        {{ deviationCount > 0 ? `${deviationCount} deviations` : 'No deviations' }}
      </div>
    </div>

    <div v-if="rows.length === 0" class="empty-state">
      No temperature-controlled tasks are active in this period.
    </div>

    <ul v-else class="report-list">
      <li
        v-for="row in rows"
        :key="row.key"
        class="report-row"
        :class="{ danger: row.isDeviation }"
      >
        <div class="row-main">
          <div class="row-title">{{ row.label }}</div>
          <div class="row-sub">Target: {{ row.target }}</div>
        </div>
        <div class="row-meta">
          <span v-if="row.valueC !== null" class="row-value">{{ row.valueC }} C</span>
          <span v-else class="row-missing">No entry</span>
        </div>
      </li>
    </ul>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { formatTemperatureTarget, isTemperatureTask } from '../../composables/ic-checklists/temperature'

const props = defineProps({
  cards: {
    type: Array,
    default: () => [],
  },
})

const rows = computed(() => {
  const safeCards = Array.isArray(props.cards) ? props.cards : []
  const result = []

  safeCards.forEach((card) => {
    const sections = Array.isArray(card?.sections) ? card.sections : []
    sections.forEach((section) => {
      const items = Array.isArray(section?.items) ? section.items : []
      items.forEach((task) => {
        if (!isTemperatureTask(task)) return
        const latest = task?.latestMeasurement ?? null
        const valueC =
          latest && Number.isFinite(Number(latest.valueC)) ? Number(latest.valueC) : null
        const isDev = Boolean(latest?.deviation)

        result.push({
          key: `${card?.id ?? 'card'}:${task.id ?? task.label}`,
          label: String(task.label ?? 'Temperature'),
          target: formatTemperatureTarget(task) || '-',
          valueC,
          isDeviation: isDev,
        })
      })
    })
  })

  return result
})

const deviationCount = computed(() => rows.value.filter((row) => row.isDeviation).length)
</script>

<style scoped>
.temp-report {
  padding: var(--space-5);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  background: var(--color-bg-primary);
  box-shadow: var(--shadow-sm);
}

.report-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.report-title {
  margin: 0;
  font-size: var(--font-size-lg);
  color: var(--color-text-primary);
}

.report-copy {
  margin: var(--space-1) 0 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.report-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 28px;
  padding: 0 12px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: var(--font-weight-bold);
  background: var(--color-success-bg);
  color: var(--color-success-text);
}

.report-badge.danger {
  background: var(--color-warning-bg);
  color: var(--color-warning-text);
}

.empty-state {
  padding: var(--space-5) 0 var(--space-2);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.report-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: var(--space-2);
}

.report-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-subtle);
}

.report-row.danger {
  border-color: rgba(220, 164, 75, 0.55);
  background: rgba(251, 241, 222, 0.75);
}

.row-title {
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.row-sub {
  margin-top: 2px;
  font-size: 12px;
  font-weight: var(--font-weight-bold);
  color: var(--color-text-muted);
}

.row-meta {
  display: inline-flex;
  align-items: center;
  justify-content: flex-end;
  min-width: 92px;
}

.row-value {
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.row-missing {
  font-weight: var(--font-weight-bold);
  color: var(--color-text-muted);
}
</style>
