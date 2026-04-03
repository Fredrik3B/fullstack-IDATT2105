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
          @click="handlePeriodClick(option)"
        >
          {{ option }}
        </button>
      </div>

      <button type="button" class="manage-button" @click="emit('manage-tasks')">{{ manageLabel }}</button>
      <div class="menu-shell">
        <button type="button" class="create-button" @click="menuOpen = !menuOpen">
          {{ createLabel }}
          <span class="menu-caret" :class="{ open: menuOpen }">▾</span>
        </button>
        <div v-if="menuOpen" class="menu-panel">
          <button type="button" class="menu-item" @click="handleMenuAction('create')">
            <span class="menu-item__title">New checklist</span>
            <span class="menu-item__hint">Start a fresh checklist template.</span>
          </button>
          <button type="button" class="menu-item" @click="handleMenuAction('library')">
            <span class="menu-item__title">Checklist library</span>
            <span class="menu-item__hint">Browse saved checklists and open one on the workbench.</span>
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'

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
    default: () => ['Daily', 'Weekly', 'Monthly']
  },
  activePeriod: {
    type: String,
    default: 'Daily'
  },
  createLabel: {
    type: String,
    default: 'Checklists'
  },
  manageLabel: {
    type: String,
    default: 'Task pool'
  }
})


const emit = defineEmits(['update:activePeriod', 'create', 'open-library', 'manage-tasks'])
const menuOpen = ref(false)


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

.menu-shell {
  position: relative;
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
.manage-button,
.create-button,
.menu-item {
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

.manage-button {
  padding: 14px 20px;
  border-radius: 14px;
  background: #ffffff;
  color: var(--color-text-primary);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  box-shadow: var(--shadow-md);
  cursor: pointer;
}

.create-button {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  border-radius: 14px;
  background: var(--color-dark-secondary);
  color: var(--color-accent);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  box-shadow: var(--shadow-md);
  cursor: pointer;
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
  padding: 10px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.98);
  border: 1px solid rgba(214, 219, 235, 0.95);
  box-shadow: 0 22px 52px rgba(18, 22, 34, 0.16);
}

.menu-item {
  width: 100%;
  display: block;
  text-align: left;
  padding: 14px 14px;
  border-radius: 14px;
  background: transparent;
  cursor: pointer;
}

.menu-item:hover {
  background: rgba(245, 246, 251, 0.95);
}

.menu-item + .menu-item {
  margin-top: 4px;
}

.menu-item__title {
  display: block;
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
}

.menu-item__hint {
  display: block;
  margin-top: 4px;
  color: var(--color-text-muted);
  font-size: 13px;
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

  .menu-panel {
    left: 0;
    right: auto;
    width: 100%;
  }
}
</style>
