<script setup>
defineProps({
  label: {
    type: String,
    required: true,
  },
  variant: {
    type: String,
    default: 'food',
    validator: (v) => ['food', 'alcohol'].includes(v),
  },
  completedTasks: {
    type: Number,
    default: 0,
  },
  totalTasks: {
    type: Number,
    default: 0,
  },
  completionRate: {
    type: Number,
    default: 0,
  },
  checklists: {
    type: Array,
    default: () => [],
  },
  isLoading: {
    type: Boolean,
    default: false,
  },
  error: {
    type: String,
    default: '',
  },
})

defineEmits(['open'])

function formatPeriod(period) {
  if (!period) return 'Unknown'
  return String(period).charAt(0).toUpperCase() + String(period).slice(1).toLowerCase()
}

function getTaskCount(card) {
  const sections = Array.isArray(card?.sections) ? card.sections : []
  return sections.flatMap((section) => (Array.isArray(section?.items) ? section.items : [])).length
}
</script>

<template>
  <article class="checklist-module-card" :class="`checklist-module-card--${variant}`">
    <div class="checklist-header">
      <div class="checklist-header__left">
        <span class="checklist-dot" :class="`checklist-dot--${variant}`"></span>
        <h3 class="checklist-title">{{ label }}</h3>
      </div>
      <button class="module-open-btn" type="button" @click="$emit('open')">Open</button>
    </div>

    <div class="module-progress">
      <div class="module-progress__bar">
        <div
          class="module-progress__fill"
          :class="variant === 'alcohol' ? 'module-progress__fill--alcohol' : ''"
          :style="{ width: `${completionRate}%` }"
        ></div>
      </div>
      <p class="module-progress__text">{{ completedTasks }} / {{ totalTasks }} tasks completed</p>
    </div>

    <div class="checklist-body">
      <p v-if="isLoading" class="checklist-hint">Loading checklists...</p>
      <p v-else-if="error" class="checklist-hint">{{ error }}</p>
      <template v-else-if="checklists.length">
        <article
          v-for="card in checklists"
          :key="card.id ?? card.title"
          class="checklist-preview"
          role="button"
          tabindex="0"
          @click="$emit('open')"
          @keydown.enter="$emit('open')"
          @keydown.space.prevent="$emit('open')"
        >
          <div class="checklist-preview__top">
            <h4 class="checklist-preview__title">{{ card.title }}</h4>
            <span class="checklist-preview__period">{{ formatPeriod(card.period) }}</span>
          </div>
          <p v-if="card.subtitle" class="checklist-preview__subtitle">{{ card.subtitle }}</p>
          <p class="checklist-preview__meta">{{ getTaskCount(card) }} tasks</p>
        </article>
      </template>
      <p v-else class="checklist-hint">No daily {{ label }} checklists found.</p>
    </div>
  </article>
</template>

<style scoped>
.checklist-module-card {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  min-height: 300px;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  box-shadow: var(--shadow-sm);
  position: relative;
  overflow: hidden;
}

.checklist-module-card::before {
  content: '';
  position: absolute;
  inset: 0 0 auto 0;
  height: 4px;
  background: linear-gradient(90deg, var(--color-accent), transparent);
}

.checklist-module-card--alcohol::before {
  background: linear-gradient(90deg, var(--color-dark-tertiary), transparent);
}

.checklist-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}

.checklist-header__left {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.checklist-dot {
  width: 10px;
  height: 10px;
  border-radius: var(--radius-full);
  flex-shrink: 0;
}

.checklist-dot--food {
  background: var(--color-accent);
}

.checklist-dot--alcohol {
  background: var(--color-dark-tertiary);
}

.checklist-title {
  margin: 0;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.module-open-btn {
  min-height: 34px;
  padding: 0 var(--space-3);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border-strong);
  background: var(--color-bg-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  cursor: pointer;
}

.module-open-btn:hover {
  border-color: var(--color-dark-tertiary);
}

.module-progress {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.module-progress__bar {
  width: 100%;
  height: 8px;
  border-radius: var(--radius-full);
  background: var(--color-bg-tertiary);
  overflow: hidden;
}

.module-progress__fill {
  height: 100%;
  border-radius: var(--radius-full);
  background: var(--color-accent);
}

.module-progress__fill--alcohol {
  background: var(--color-dark-tertiary);
}

.module-progress__text {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.checklist-body {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.checklist-preview {
  padding: var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-bg-secondary);
  cursor: pointer;
  transition: transform 0.16s ease, box-shadow 0.16s ease, border-color 0.16s ease;
}

.checklist-preview:hover,
.checklist-preview:focus-visible {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--color-dark-tertiary);
  outline: none;
}

.checklist-preview__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-3);
}

.checklist-preview__title {
  margin: 0;
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.checklist-preview__period {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-muted);
  text-transform: uppercase;
}

.checklist-preview__subtitle,
.checklist-preview__meta {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.checklist-hint {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-hint);
}
</style>
