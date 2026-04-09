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
        <button
          type="button"
          class="confirm-secondary"
          :disabled="isProcessing"
          @click="handleCancel"
        >
          {{ cancelLabel }}
        </button>
        <button
          type="button"
          class="confirm-primary"
          :class="[`tone-${tone}`, { processing: isProcessing }]"
          :disabled="isProcessing"
          @click="emit('confirm')"
        >
          {{ confirmLabel }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * SharedConfirmDialog
 *
 * Reusable confirmation dialog used throughout the checklist workbench whenever a
 * destructive or important action needs user acknowledgement (submit, temperature
 * log, delete). The tone prop adjusts the background and button colour to match
 * the severity of the action.
 *
 * @prop {boolean} [open]          - Controls dialog visibility (v-model compatible via `update:open`).
 * @prop {string}  [kicker]        - Small uppercase label above the title.
 * @prop {string}  [title]         - Dialog heading (default: 'Confirm action').
 * @prop {string}  [message]       - Primary message body.
 * @prop {string}  [detail]        - Secondary detail block shown in a bordered box.
 * @prop {string}  [confirmLabel]  - Text for the confirm button (default: 'Confirm').
 * @prop {string}  [cancelLabel]   - Text for the cancel button (default: 'Cancel').
 * @prop {boolean} [isProcessing]  - Disables both buttons while the action is in flight.
 * @prop {string}  [tone]          - Visual tone: 'default' | 'warning' | 'danger'.
 *
 * @emits confirm      - User clicked the confirm button.
 * @emits cancel       - User clicked the cancel button.
 * @emits update:open  - Emitted with `false` when the dialog closes.
 */
defineProps({
  open: { type: Boolean, default: false },
  kicker: { type: String, default: '' },
  title: { type: String, default: 'Confirm action' },
  message: { type: String, default: '' },
  detail: { type: String, default: '' },
  confirmLabel: { type: String, default: 'Confirm' },
  cancelLabel: { type: String, default: 'Cancel' },
  isProcessing: { type: Boolean, default: false },
  tone: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'warning', 'danger'].includes(value),
  },
})

const emit = defineEmits(['confirm', 'cancel', 'update:open'])

/** Emits `update:open` with false and `cancel` so the parent can close and clean up. */
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

.confirm-secondary:disabled,
.confirm-primary:disabled,
.confirm-primary.processing {
  opacity: 0.7;
  cursor: wait;
  filter: saturate(0.8);
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
