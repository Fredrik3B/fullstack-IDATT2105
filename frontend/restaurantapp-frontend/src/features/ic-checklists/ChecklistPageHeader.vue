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

      <div class="actions-row">
        <div class="period-switcher" aria-label="Checklist period filter">
          <button
            v-for="option in periods"
            :key="option"
            type="button"
            class="period-button"
            :class="{ active: option === activePeriod }"
            @click="handlePeriodClick(option)"
          >
            {{ option }}
          </button>
        </div>

        <button type="button" class="ghost-button" @click="emit('refresh')">Refresh</button>
        <button type="button" class="ghost-button" @click="emit('manage-tasks')">
          {{ manageLabel }}
        </button>

        <div class="menu-shell" ref="menuShell">
          <button type="button" class="create-button" @click="menuOpen = !menuOpen">
            New {{ createLabel.toLowerCase() }}
            <span class="menu-caret" :class="{ open: menuOpen }">&#9662;</span>
          </button>

          <div v-if="menuOpen" class="menu-panel">
            <button type="button" class="menu-item" @click="handleMenuAction('create')">
              <span class="menu-item__title">Create checklist</span>
              <span class="menu-item__hint">Build a new checklist from the shared task pool.</span>
            </button>
            <button type="button" class="menu-item" @click="handleMenuAction('library')">
              <span class="menu-item__title">Open checklist library</span>
              <span class="menu-item__hint"
                >Bring an existing checklist back onto the workbench.</span
              >
            </button>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'

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
  createLabel: {
    type: String,
    default: 'Checklists',
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
const menuOpen = ref(false)
const menuShell = ref(null)

function handlePeriodClick(option) {
  emit('update:activePeriod', option)
}

function handleMenuAction(action) {
  menuOpen.value = false
  if (action === 'create') {
    emit('create')
    return
  }
  emit('open-library')
}

function handleClickOutside(event) {
  if (menuShell.value && !menuShell.value.contains(event.target)) {
    menuOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('mousedown', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('mousedown', handleClickOutside)
})
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

.actions-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-3);
}

.menu-shell {
  position: relative;
}

.period-switcher {
  display: inline-flex;
  padding: 3px;
  border: 1px solid rgba(200, 200, 216, 0.18);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.08);
}

.period-button,
.ghost-button,
.create-button,
.menu-item {
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

.ghost-button,
.create-button {
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
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  background: var(--color-accent);
  color: var(--color-dark-primary);
}

.menu-caret {
  transition: transform 140ms ease;
}

.menu-caret.open {
  transform: rotate(180deg);
}

.menu-panel {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  z-index: 20;
  width: min(320px, 92vw);
  padding: var(--space-2);
  border-radius: var(--radius-lg);
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-lg);
}

.menu-item {
  width: 100%;
  display: block;
  text-align: left;
  padding: var(--space-4);
  border-radius: var(--radius-md);
  background: transparent;
  cursor: pointer;
}

.menu-item:hover {
  background: var(--color-bg-secondary);
}

.menu-item + .menu-item {
  margin-top: 2px;
}

.menu-item__title {
  display: block;
  color: var(--color-text-primary);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
}

.menu-item__hint {
  display: block;
  margin-top: 4px;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

@media (max-width: 900px) {
  .page-header {
    grid-template-columns: 1fr;
    padding: var(--space-6);
  }

  .actions {
    width: 100%;
  }

  .actions-row {
    flex-direction: column;
    align-items: stretch;
  }

  .period-switcher {
    width: fit-content;
    max-width: 100%;
    overflow-x: auto;
  }

  .menu-panel {
    left: 0;
    right: auto;
    width: 100%;
  }
}
</style>
