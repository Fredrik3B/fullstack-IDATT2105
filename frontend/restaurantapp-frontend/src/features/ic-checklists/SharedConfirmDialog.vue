<template>
  <div
    v-if="open"
    class="confirm-overlay"
    role="dialog"
    aria-modal="true"
    :aria-label="title || 'Confirm action'"
  >
    <div class="confirm-dialog" :class="[`tone-${tone}`]">
      <div v-if="kicker" class="confirm-kicker">{{ kicker }}</div>
      <h2>{{ title }}</h2>
      <p v-if="message">{{ message }}</p>
      <div v-if="detail" class="confirm-detail">{{ detail }}</div>
      <div class="confirm-actions">
        <button type="button" class="confirm-secondary" @click="handleCancel">
          {{ cancelLabel }}
        </button>
        <button
          type="button"
          class="confirm-primary"
          :class="[`tone-${tone}`]"
          @click="emit('confirm')"
        >
          {{ confirmLabel }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  open: { type: Boolean, default: false },
  kicker: { type: String, default: '' },
  title: { type: String, default: 'Confirm action' },
  message: { type: String, default: '' },
  detail: { type: String, default: '' },
  confirmLabel: { type: String, default: 'Confirm' },
  cancelLabel: { type: String, default: 'Cancel' },
  tone: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'warning', 'danger'].includes(value),
  },
})

const emit = defineEmits(['confirm', 'cancel', 'update:open'])

function handleCancel() {
  emit('update:open', false)
  emit('cancel')
}
</script>

<style scoped>
.confirm-overlay {
  position: fixed;
  inset: 0;
  z-index: 90;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-6);
  background: rgba(12, 16, 28, 0.52);
  backdrop-filter: blur(4px);
}

.confirm-dialog {
  width: min(460px, 100%);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border);
  background: var(--color-bg-primary);
  box-shadow: var(--shadow-lg);
  padding: var(--space-6);
  display: grid;
  gap: var(--space-4);
}

.confirm-dialog.tone-warning {
  background: #fffdf6;
  border-color: var(--color-warning-border);
}

.confirm-dialog.tone-danger {
  background: #fff8f8;
  border-color: var(--color-danger-border);
}

.confirm-kicker {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.confirm-dialog h2 {
  margin: 0;
  font-size: var(--font-size-2xl);
  line-height: 1.2;
  color: var(--color-text-primary);
}

.confirm-dialog p,
.confirm-detail {
  margin: 0;
  font-size: var(--font-size-md);
  line-height: var(--line-height-normal);
  color: var(--color-text-secondary);
}

.confirm-detail {
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  background: var(--color-bg-secondary);
}

.confirm-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
}

.confirm-secondary,
.confirm-primary {
  min-height: 40px;
  padding: 0 var(--space-4);
  border-radius: var(--radius-md);
  border: 1px solid transparent;
  font: inherit;
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  cursor: pointer;
}

.confirm-secondary {
  background: var(--color-bg-secondary);
  border-color: var(--color-border);
  color: var(--color-text-primary);
}

.confirm-primary {
  background: var(--color-dark-secondary);
  color: #ffffff;
}

.confirm-primary.tone-warning {
  background: #d6723b;
}

.confirm-primary.tone-danger {
  background: var(--color-danger);
}

@media (max-width: 720px) {
  .confirm-actions {
    flex-direction: column-reverse;
  }

  .confirm-secondary,
  .confirm-primary {
    width: 100%;
  }
}
</style>
