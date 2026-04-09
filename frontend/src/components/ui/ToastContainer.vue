<template>
  <Teleport to="body">
    <div class="toast-container" aria-live="polite" aria-atomic="false">
      <TransitionGroup name="toast">
        <div
          v-for="toast in toastStore.toasts"
          :key="toast.id"
          class="toast"
          :class="`toast--${toast.type}`"
          role="alert"
        >
          <component :is="iconFor(toast.type)" class="toast-icon" />
          <span class="toast-message">{{ toast.message }}</span>
          <button class="toast-close" @click="toastStore.remove(toast.id)" aria-label="Lukk">
            <X />
          </button>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<script setup>
/**
 * ToastContainer
 *
 * Global toast notification host. Teleports itself to `<body>` so it floats above
 * all other content. Reads from the shared `toastStore` and renders each queued
 * notification with an appropriate icon, message, and dismiss button.
 * Notifications slide in from the right and slide out when removed.
 *
 * No props or emits – this component owns its own state via the Pinia toast store.
 */
import { CheckCircle, AlertCircle, AlertTriangle, Info, X } from 'lucide-vue-next'
import { useToastStore } from '@/stores/toast'

const toastStore = useToastStore()

/**
 * Maps a toast type string to its corresponding Lucide icon component.
 * @param {'success'|'error'|'warning'|'info'} type - The toast notification type.
 * @returns {Component} The Lucide Vue icon component for that type.
 */
function iconFor(type) {
  return { success: CheckCircle, error: AlertCircle, warning: AlertTriangle, info: Info }[type]
}
</script>

<style scoped>
.toast-container {
  position: fixed;
  bottom: var(--space-6);
  right: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  z-index: 9999;
  width: 320px;
}

.toast {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-lg);
  border: 1px solid;
  box-shadow: var(--shadow-lg);
  font-size: var(--font-size-sm);
  line-height: var(--line-height-normal);
}

.toast-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
  margin-top: 1px;
}

.toast-message {
  flex: 1;
  font-weight: var(--font-weight-medium);
}

.toast-close {
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
  display: flex;
  align-items: center;
  flex-shrink: 0;
  opacity: 0.5;
  transition: opacity var(--transition-fast);
}

.toast-close:hover { opacity: 1; }

.toast-close svg {
  width: 14px;
  height: 14px;
}

/* ── Types ── */
.toast--success {
  background: var(--color-success-bg);
  border-color: var(--color-success-border);
  color: var(--color-success-text);
}

.toast--error {
  background: var(--color-danger-bg);
  border-color: var(--color-danger-border);
  color: var(--color-danger-text);
}

.toast--warning {
  background: var(--color-warning-bg);
  border-color: var(--color-warning-border);
  color: var(--color-warning-text);
}

.toast--info {
  background: var(--color-dark-primary);
  border-color: var(--color-dark-tertiary);
  color: var(--color-dark-border);
}

/* ── Transition ── */
.toast-enter-active {
  transition: transform var(--transition-normal), opacity var(--transition-normal);
}

.toast-leave-active {
  transition: transform var(--transition-slow), opacity var(--transition-slow);
}

.toast-enter-from {
  transform: translateX(110%);
  opacity: 0;
}

.toast-leave-to {
  transform: translateX(110%);
  opacity: 0;
}

@media (max-width: 480px) {
  .toast-container {
    left: var(--space-4);
    right: var(--space-4);
    width: auto;
  }
}
</style>
