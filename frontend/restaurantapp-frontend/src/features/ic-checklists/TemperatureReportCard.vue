<template>
  <footer class="temp-report" aria-label="Temperature report">
    <div class="report-top">
      <h3 class="report-title">Temperature log</h3>
      <div class="report-badge" :class="{ danger: deviationCount > 0 }">
        {{ deviationCount > 0 ? `${deviationCount} deviations` : 'No deviations' }}
      </div>
    </div>

    <ul class="report-list">
      <li v-for="row in rows" :key="row.key" class="report-row" :class="{ danger: row.isDeviation }">
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
  </footer>
</template>

<script setup>
import { computed } from 'vue'
import { formatTemperatureTarget, isTemperatureDeviation, isTemperatureTask } from './temperature'

const props = defineProps({
  cards: {
    type: Array,
    default: () => []
  },
  temperatureLatestByTaskId: {
    type: Object,
    default: null
  }
})

function latestForTaskId(taskId) {
  if (!taskId) return null
  const container = props.temperatureLatestByTaskId
  if (!container) return null
  if (typeof container.get === 'function') return container.get(taskId) ?? null
  return container[taskId] ?? null
}

const rows = computed(() => {
  const safeCards = Array.isArray(props.cards) ? props.cards : []
  const result = []

  safeCards.forEach((card) => {
    const sections = Array.isArray(card?.sections) ? card.sections : []
    sections.forEach((section) => {
      const items = Array.isArray(section?.items) ? section.items : []
      items.forEach((task) => {
        if (!isTemperatureTask(task)) return
        const latest = latestForTaskId(task.id)
        const valueC = latest && Number.isFinite(Number(latest.valueC)) ? Number(latest.valueC) : null
        const isDev = valueC !== null ? isTemperatureDeviation(task, valueC) : false

        result.push({
          key: `${card?.id ?? 'card'}:${task.id ?? task.label}`,
          label: String(task.label ?? 'Temperature'),
          target: formatTemperatureTarget(task) || '—',
          valueC,
          isDeviation: isDev
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
  margin-top: 18px;
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid rgba(210, 213, 230, 0.95);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: var(--shadow-sm);
}

.report-top {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.report-title {
  margin: 0;
  font-size: 14px;
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.08em;
  text-transform: uppercase;
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

.report-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 8px;
}

.report-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(242, 243, 250, 0.58);
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

