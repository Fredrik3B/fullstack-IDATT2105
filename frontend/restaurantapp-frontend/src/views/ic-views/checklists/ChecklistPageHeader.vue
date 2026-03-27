<template>
  <section class="page-header">
    <div>
      <div class="eyebrow">{{ moduleLabel }}</div>
      <h1>{{ title }}</h1>
      <p>{{ dateLabel }}</p>
    </div>

    <div class="actions">
      <div class="period-switcher">
        <button
          v-for="option in periods"
          :key="option"
          type="button"
          class="period-button"
          :class="{ active: option === activePeriod }"
        >
          {{ option }}
        </button>
      </div>

      <button type="button" class="create-button">{{ createLabel }}</button>
    </div>
  </section>
</template>

<script setup>
defineProps({
  moduleLabel: {
    type: String,
    default: ''
  },
  title: {
    type: String,
    required: true
  },
  dateLabel: {
    type: String,
    required: true
  },
  periods: {
    type: Array,
    default: () => ['Daglig', 'Ukentlig', 'Månedlig']
  },
  activePeriod: {
    type: String,
    default: 'Daglig'
  },
  createLabel: {
    type: String,
    default: '+ Ny sjekkliste'
  }
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-6);
  margin-bottom: var(--space-6);
}

.eyebrow {
  margin-bottom: var(--space-2);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

h1 {
  margin: 0;
  font-size: 36px;
  line-height: 1.05;
  color: var(--color-text-primary);
}

p {
  margin: var(--space-2) 0 0;
  color: var(--color-text-muted);
  font-size: var(--font-size-md);
}

.actions {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.period-switcher {
  display: inline-flex;
  padding: 4px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.78);
  box-shadow: var(--shadow-sm);
}

.period-button,
.create-button {
  border: 0;
  font-family: inherit;
}

.period-button {
  padding: 10px 16px;
  border-radius: 10px;
  background: transparent;
  color: var(--color-text-muted);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
}

.period-button.active {
  background: #ffffff;
  color: var(--color-text-primary);
  box-shadow: var(--shadow-sm);
}

.create-button {
  padding: 14px 20px;
  border-radius: 14px;
  background: var(--color-dark-secondary);
  color: var(--color-accent);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  box-shadow: var(--shadow-md);
  cursor: pointer;
}

@media (max-width: 900px) {
  .page-header {
    flex-direction: column;
  }

  .actions {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
  }

  .period-switcher {
    width: fit-content;
    max-width: 100%;
    overflow-x: auto;
  }
}
</style>
