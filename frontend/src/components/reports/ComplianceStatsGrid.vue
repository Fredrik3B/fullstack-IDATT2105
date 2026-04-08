<template>
  <div class="stats-grid">
    <div class="stat-block">
      <h3 class="stat-block-title">
        <span class="module-dot module-dot--food"></span>
        IK-Mat (Food)
      </h3>
      <div class="stat-row">
        <div class="stat-item">
          <span class="stat-number">{{ foodStats.completedTasks }}/{{ foodStats.totalTasks }}</span>
          <span class="stat-desc">Tasks completed</span>
        </div>
        <div class="stat-item">
          <span class="stat-number" :class="rateClass(foodStats.completionRate)">
            {{ formatRate(foodStats.completionRate) }}%
          </span>
          <span class="stat-desc">Completion rate</span>
        </div>
        <div class="stat-item">
          <span class="stat-number" :class="{ 'stat-danger': foodStats.deviatedTasks > 0 }">
            {{ foodStats.deviatedTasks }}
          </span>
          <span class="stat-desc">Deviations</span>
        </div>
        <div v-if="foodStats.temperatureReadings != null" class="stat-item">
          <span class="stat-number" :class="{ 'stat-danger': foodStats.outOfRangeReadings > 0 }">
            {{ foodStats.outOfRangeReadings }}/{{ foodStats.temperatureReadings }}
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
          <span class="stat-number">{{ alcoholStats.completedTasks }}/{{ alcoholStats.totalTasks }}</span>
          <span class="stat-desc">Tasks completed</span>
        </div>
        <div class="stat-item">
          <span class="stat-number" :class="rateClass(alcoholStats.completionRate)">
            {{ formatRate(alcoholStats.completionRate) }}%
          </span>
          <span class="stat-desc">Completion rate</span>
        </div>
        <div class="stat-item">
          <span class="stat-number" :class="{ 'stat-danger': alcoholStats.deviatedTasks > 0 }">
            {{ alcoholStats.deviatedTasks }}
          </span>
          <span class="stat-desc">Deviations</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { formatRate, rateClass } from './reportHelpers.js'

defineProps({
  foodStats: { type: Object, required: true },
  alcoholStats: { type: Object, required: true },
})
</script>

<style scoped>
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
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  flex-shrink: 0;
}

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

@media (max-width: 900px) {
  .stats-grid { grid-template-columns: 1fr; }
}
</style>
