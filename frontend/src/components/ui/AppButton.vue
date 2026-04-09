<script setup>
/**
 * AppButton
 *
 * Reusable styled button used throughout the application.
 * Renders a native `<button>` with a slot for content and supports three visual
 * variants. When `loading` is true the label is replaced with a CSS spinner and
 * the button is disabled automatically.
 *
 * @prop {string}  [variant]   - Visual style: 'primary' (default) | 'secondary' | 'danger'.
 * @prop {boolean} [loading]   - Shows a spinner and disables the button while true.
 * @prop {boolean} [disabled]  - Explicitly disables the button.
 * @prop {string}  [type]      - HTML button type attribute (default: 'button').
 * @prop {boolean} [fullWidth] - Stretches the button to 100% of its container.
 */
defineProps({
  variant: {
    type: String,
    default: 'primary',
    validator: (v) => ['primary', 'secondary', 'danger'].includes(v),
  },
  loading: {
    type: Boolean,
    default: false,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  type: {
    type: String,
    default: 'button',
  },
  fullWidth: {
    type: Boolean,
    default: false,
  },
})
</script>

<template>
  <button
    :type="type"
    :disabled="disabled || loading"
    :class="['btn', `btn--${variant}`, { 'btn--full': fullWidth }]"
  >
    <span v-if="loading" class="btn-spinner"></span>
    <slot v-else />
  </button>
</template>

<style scoped>
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  border: none;
  border-radius: var(--radius-md);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  font-family: var(--font-sans);
  cursor: pointer;
  transition: transform var(--transition-fast), filter var(--transition-fast), background var(--transition-fast);
  padding: 0 var(--space-5);
  height: 44px;
  white-space: nowrap;
}

.btn--full {
  width: 100%;
  height: 48px;
}

/* Primary */
.btn--primary {
  background: linear-gradient(135deg, var(--color-dark-primary), var(--color-dark-secondary));
  color: var(--color-accent);
}

.btn--primary:hover:not(:disabled) {
  filter: brightness(1.06);
  transform: translateY(-1px);
}

/* Secondary */
.btn--secondary {
  background: transparent;
  color: var(--color-dark-secondary);
  border: 1.5px solid var(--color-dark-secondary);
}

.btn--secondary:hover:not(:disabled) {
  background: var(--color-dark-secondary);
  color: var(--color-accent);
}

/* Danger */
.btn--danger {
  background: var(--color-danger);
  color: #fff;
}

.btn--danger:hover:not(:disabled) {
  filter: brightness(1.08);
}

/* Disabled / loading */
.btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
  transform: none;
  filter: none;
}

/* Spinner */
.btn-spinner {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid rgba(212, 232, 53, 0.3);
  border-top-color: var(--color-accent);
  animation: btn-spin 700ms linear infinite;
}

@keyframes btn-spin {
  to { transform: rotate(360deg); }
}
</style>
