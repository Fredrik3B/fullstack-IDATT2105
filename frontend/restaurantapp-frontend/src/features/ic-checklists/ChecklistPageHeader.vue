<template>
  <section class="page-header">
    <div class="page-header__main">
      <div class="eyebrow">{{ moduleLabel }}</div>
      <h1>{{ title }}</h1>
      <p class="page-date">{{ dateLabel }}</p>
      <p v-if="moduleDescription" class="page-description">{{ moduleDescription }}</p>
    </div>

    <div class="actions">
      <div class="insight-card">
        <span class="insight-label">Workbench</span>
        <span class="insight-value">{{ activePeriod }}</span>
        <span class="insight-text">{{
          summaryHint || 'Filter the workbench and manage reusable checklists.'
        }}</span>
      </div>

      <div class="period-switcher" aria-label="Checklist period filter">
        <button
          v-for="option in periods"
          :key="option"
          type="button"
          class="period-button"
          :class="{ active: option === activePeriod }"
          @click="emit('update:activePeriod', option)"
        >
          {{ option }}
        </button>
      </div>

      <div class="primary-actions">
        <button type="button" class="create-button" @click="emit('create')">New checklist</button>
        <button type="button" class="ghost-button" @click="emit('open-library')">
          Open library
        </button>
        <button type="button" class="ghost-button" @click="emit('refresh')">Refresh</button>
      </div>

      <div class="secondary-actions">
        <span class="secondary-actions__label">Admin</span>
        <button type="button" class="secondary-button" @click="emit('manage-tasks')">
          {{ manageLabel }}
        </button>
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  moduleLabel: {
    type: String,
    default: '',
  },
  title: {
    type: String,
    required: true,
  },
  dateLabel: {
    type: String,
    required: true,
  },
  moduleDescription: {
    type: String,
    default: '',
  },
  summaryHint: {
    type: String,
    default: '',
  },
  periods: {
    type: Array,
    default: () => ['Daily', 'Weekly', 'Monthly'],
  },
  activePeriod: {
    type: String,
    default: 'Daily',
  },
  manageLabel: {
    type: String,
    default: 'Task pool',
  },
})

const emit = defineEmits([
  'update:activePeriod',
  'create',
  'open-library',
  'manage-tasks',
  'refresh',
])
</script>

<style scoped>
.page-header {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(340px, 0.9fr);
  gap: var(--space-6);
  padding: var(--space-8);
  border-radius: var(--radius-lg);
  background: linear-gradient(180deg, var(--color-dark-primary) 0%, #232248 100%);
  box-shadow: var(--shadow-md);
}

.page-header__main {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: var(--space-2);
}

.eyebrow {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 var(--space-3);
  border-radius: var(--radius-full);
  border: 1px solid rgba(212, 232, 53, 0.32);
  background: rgba(212, 232, 53, 0.08);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--color-accent);
}

h1 {
  margin: 0;
  font-size: clamp(32px, 4vw, 42px);
  line-height: 1.1;
  color: #ffffff;
}

.page-date,
.page-description {
  margin: 0;
  font-size: var(--font-size-md);
  line-height: var(--line-height-normal);
}

.page-date {
  color: var(--color-dark-border);
}

.page-description {
  max-width: 60ch;
  color: rgba(255, 255, 255, 0.82);
}

.actions {
  display: grid;
  gap: var(--space-4);
  align-content: start;
}

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
  font-size: 22px;
  font-weight: var(--font-weight-bold);
  color: #ffffff;
}

.insight-text {
  font-size: var(--font-size-sm);
  line-height: var(--line-height-normal);
  color: rgba(255, 255, 255, 0.76);
}

.period-switcher {
  display: inline-flex;
  width: fit-content;
  padding: 3px;
  border: 1px solid rgba(200, 200, 216, 0.18);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.08);
}

.period-button,
.ghost-button,
.create-button,
.secondary-button {
  border: 0;
  font-family: inherit;
}

.period-button {
  min-height: 36px;
  padding: 0 var(--space-4);
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-dark-border);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
}

.period-button.active {
  background: #ffffff;
  color: var(--color-dark-primary);
  box-shadow: var(--shadow-sm);
}

.primary-actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
}

.ghost-button,
.create-button,
.secondary-button {
  min-height: 40px;
  padding: 0 var(--space-4);
  border-radius: var(--radius-md);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  cursor: pointer;
}

.ghost-button {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(200, 200, 216, 0.18);
  color: #ffffff;
}

.create-button {
  background: var(--color-accent);
  color: var(--color-dark-primary);
}

.secondary-actions {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.secondary-actions__label {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-dark-border);
}

.secondary-button {
  background: transparent;
  border: 1px dashed rgba(200, 200, 216, 0.28);
  color: #ffffff;
}

@media (max-width: 900px) {
  .page-header {
    grid-template-columns: 1fr;
    padding: var(--space-6);
  }

  .primary-actions,
  .secondary-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .period-switcher {
    max-width: 100%;
    overflow-x: auto;
  }
}
</style>
