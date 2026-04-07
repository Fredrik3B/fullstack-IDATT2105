<script setup>
defineProps({
  label: {
    type: String,
    required: true,
  },
  value: {
    type: [String, Number],
    required: true,
  },
  hint: {
    type: String,
    default: '',
  },
  valueVariant: {
    type: String,
    default: '',
    validator: (v) => ['', 'danger', 'warning'].includes(v),
  },
  interactive: {
    type: Boolean,
    default: false,
  },
})

defineEmits(['click'])
</script>

<template>
  <article
    class="stat-card"
    :class="{ 'stat-card--interactive': interactive }"
    :role="interactive ? 'button' : undefined"
    :tabindex="interactive ? 0 : undefined"
    @click="interactive && $emit('click')"
    @keydown.enter="interactive && $emit('click')"
    @keydown.space.prevent="interactive && $emit('click')"
  >
    <span class="stat-label">{{ label }}</span>
    <span class="stat-value" :class="valueVariant ? `stat-value--${valueVariant}` : ''">{{ value }}</span>
    <span class="stat-hint">{{ hint }}</span>
  </article>
</template>

<style scoped>
.stat-card {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  box-shadow: var(--shadow-sm);
  position: relative;
  overflow: hidden;
}

.stat-card::after {
  content: '';
  position: absolute;
  inset: auto -18px -18px auto;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(212, 232, 53, 0.08);
}

.stat-card--interactive {
  cursor: pointer;
  transition: transform var(--transition-normal), box-shadow var(--transition-normal), border-color var(--transition-normal);
}

.stat-card--interactive:hover,
.stat-card--interactive:focus-visible {
  transform: translateY(-2px);
  border-color: var(--color-dark-tertiary);
  box-shadow: var(--shadow-md);
  outline: none;
}

.stat-label {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.stat-value {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.stat-value--danger {
  color: var(--color-danger);
}

.stat-value--warning {
  color: var(--color-warning-text);
}

.stat-hint {
  font-size: var(--font-size-xs);
  color: var(--color-text-hint);
}
</style>
