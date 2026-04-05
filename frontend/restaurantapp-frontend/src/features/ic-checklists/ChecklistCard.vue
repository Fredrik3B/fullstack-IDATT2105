<template>
  <article
    :id="`checklist-card-${id}`"
    class="checklist-card"
    :class="{ featured, 'on-workbench': highlightedWorkbench }"
  >
    <header class="card-header">
      <div class="card-header__main">
        <div class="title-row">
          <h2>{{ title }}</h2>
          <span v-if="moduleChip" class="module-chip">{{ moduleChip }}</span>
        </div>
        <p>{{ subtitle }}</p>
        <div class="header-meta">
          <span class="status-pill" :class="statusTone">{{ statusLabel }}</span>
          <span v-if="progress !== null" class="header-progress">{{ progress }}% complete</span>
        </div>
      </div>

      <div class="header-status">
        <div v-if="progress !== null" class="progress-track" aria-label="Checklist progress">
          <span class="progress-fill" :style="{ width: `${progress}%` }"></span>
        </div>
        <button type="button" class="edit-button" @click="handleEditChecklist">Edit</button>
      </div>
    </header>

    <div v-for="(section, sectionIndex) in sections" :key="section.title" class="section">
      <div class="section-label">{{ section.title }}</div>

      <ul class="task-list">
        <li
          v-for="(task, taskIndex) in section.items"
          :key="task.id ?? task.label"
          class="task-row"
          :class="[task.state, { highlighted: task.highlighted }]"
        >
          <button
            type="button"
            class="task-marker"
            :aria-pressed="task.state === 'completed'"
            :aria-label="task.state === 'completed' ? 'Mark as incomplete' : 'Mark as complete'"
            :disabled="task.isSaving"
            @click="handleToggle(sectionIndex, taskIndex)"
          >
            <span v-if="task.state === 'completed'" class="task-marker__icon">&#10003;</span>
            <span v-else-if="task.state === 'pending'" class="task-marker__icon">!</span>
          </button>

          <div class="task-label">
            <div class="task-label__main">{{ task.label }}</div>
            <div
              v-if="isTemperatureTask(task) && formatTemperatureTarget(task)"
              class="task-label__sub"
            >
              Target: {{ formatTemperatureTarget(task) }}
            </div>
          </div>

          <div class="task-actions">
            <template v-if="isTemperatureTask(task)">
              <span v-if="getLatestMeasurement(task)" class="task-meta"
                >Last: {{ getLatestMeasurement(task)?.valueC }} C</span
              >
              <div class="temp-input">
                <input
                  v-model.trim="temperatureDraftByTaskId[task.id]"
                  class="temp-field"
                  inputmode="decimal"
                  type="number"
                  step="0.1"
                  placeholder="C"
                  :aria-label="`Log temperature for ${task.label}`"
                />
                <button
                  type="button"
                  class="temp-save"
                  :disabled="!canSaveTemperature(task)"
                  @click="handleSaveTemperature(task)"
                >
                  Save
                </button>
              </div>
            </template>

            <span v-else-if="task.meta" class="task-meta">{{ task.meta }}</span>

            <button
              type="button"
              class="flag-button"
              :class="{ active: task.state === 'pending' }"
              :aria-pressed="task.state === 'pending'"
              :aria-label="
                task.state === 'pending' ? 'Remove pending flag' : 'Mark task as pending'
              "
              :disabled="task.isSaving"
              @click="handleFlag(sectionIndex, taskIndex)"
            >
              {{ task.isSaving ? 'Saving...' : task.state === 'pending' ? 'Flagged' : 'Flag' }}
            </button>
          </div>
        </li>
      </ul>
    </div>

    <footer class="submit-bar" :class="{ overdue: periodExpired, blocked: !canSubmitCurrentPeriod }">
      <div class="submit-copy">
        <div class="submit-title">
          {{
            !canSubmitCurrentPeriod
              ? 'Submission locked until the next real period starts'
              : periodExpired
              ? 'This checklist period has ended'
              : 'Submit when this checklist is ready'
          }}
        </div>
        <div v-if="!canSubmitCurrentPeriod" class="submit-state-chip">
          Waiting for {{ activePeriodKey }}
        </div>
        <p class="submit-text">{{ submitWarning }}</p>
      </div>
      <button
        type="button"
        class="submit-button"
        :disabled="!canSubmitCurrentPeriod"
        @click="handleSubmitChecklist"
      >
        {{
          !canSubmitCurrentPeriod
            ? 'Waiting for next period'
            : periodExpired
            ? 'Submit and load next period'
            : 'Submit checklist'
        }}
      </button>
    </footer>

    <div
      v-if="confirmDialog.open"
      class="confirm-overlay"
      role="dialog"
      aria-modal="true"
      :aria-label="confirmDialog.title"
    >
      <div class="confirm-dialog" :class="{ warning: confirmDialog.tone === 'warning' }">
        <div class="confirm-kicker">{{ confirmDialog.kicker }}</div>
        <h3>{{ confirmDialog.title }}</h3>
        <p>{{ confirmDialog.message }}</p>
        <div v-if="confirmDialog.detail" class="confirm-detail">{{ confirmDialog.detail }}</div>
        <div class="confirm-actions">
          <button type="button" class="confirm-secondary" @click="closeConfirmDialog">
            Cancel
          </button>
          <button type="button" class="confirm-primary" @click="confirmDialog.onConfirm?.()">
            {{ confirmDialog.confirmLabel }}
          </button>
        </div>
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed, reactive } from 'vue'
import { getPeriodEnd, getPeriodKey, isPeriodExpired, normalizePeriodEnum } from './recurrence'
import { formatTemperatureTarget, isTemperatureTask } from './temperature'

const temperatureDraftByTaskId = reactive({})
const confirmDialog = reactive({
  open: false,
  kicker: '',
  title: '',
  message: '',
  detail: '',
  confirmLabel: 'Confirm',
  tone: 'default',
  onConfirm: null,
})

const props = defineProps({
  id: {
    type: String,
    default: '',
  },
  title: {
    type: String,
    required: true,
  },
  period: {
    type: String,
    default: 'daily',
  },
  activePeriodKey: {
    type: String,
    default: '',
  },
  subtitle: {
    type: String,
    required: true,
  },
  statusLabel: {
    type: String,
    required: true,
  },
  statusTone: {
    type: String,
    default: 'success',
  },
  progress: {
    type: Number,
    default: null,
  },
  sections: {
    type: Array,
    required: true,
  },
  featured: {
    type: Boolean,
    default: false,
  },
  moduleChip: {
    type: String,
    default: 'IC-Food',
  },
  highlightedWorkbench: {
    type: Boolean,
    default: false,
  },
  now: {
    type: [Date, String, Number],
    default: null,
  },
})

const emit = defineEmits([
  'toggle-task',
  'toggle-pending',
  'edit-checklist',
  'submit-checklist',
  'log-temperature',
])

const activeDate = computed(() => {
  if (props.now instanceof Date) return props.now
  if (typeof props.now === 'string' || typeof props.now === 'number') {
    const parsed = new Date(props.now)
    return Number.isNaN(parsed.getTime()) ? new Date() : parsed
  }
  return new Date()
})

const periodEnd = computed(() => getPeriodEnd(props.period, props.activePeriodKey))
const periodExpired = computed(() =>
  isPeriodExpired(props.period, props.activePeriodKey, activeDate.value),
)
const currentPeriodKey = computed(() => getPeriodKey(props.period, activeDate.value))
const canSubmitCurrentPeriod = computed(() => {
  const activeKey = String(props.activePeriodKey ?? '').trim()
  const currentKey = String(currentPeriodKey.value ?? '').trim()
  return Boolean(activeKey) && activeKey === currentKey
})
const periodLabel = computed(() => {
  const normalized = normalizePeriodEnum(props.period)
  if (normalized === 'weekly') return 'weekly'
  if (normalized === 'monthly') return 'monthly'
  return 'daily'
})
const formattedPeriodEnd = computed(() => {
  if (!periodEnd.value) return ''
  return new Intl.DateTimeFormat('en-US', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  }).format(periodEnd.value)
})
const submitWarning = computed(() => {
  if (!canSubmitCurrentPeriod.value) {
    return `This checklist was already submitted once, so the system has prepared the next ${periodLabel.value} run with period key ${props.activePeriodKey}. To prevent skipping ahead, submit stays locked until that real ${periodLabel.value} period actually begins.`
  }
  if (periodExpired.value) {
    return `The current ${periodLabel.value} run ended${formattedPeriodEnd.value ? ` at ${formattedPeriodEnd.value}` : ''}. Submit it to lock the entries and load a fresh set of tasks with new ids.`
  }
  return `Submitting locks the current ${periodLabel.value} run and immediately starts a fresh one with new task ids.`
})

function handleToggle(sectionIndex, taskIndex) {
  emit('toggle-task', { sectionIndex, taskIndex })
}

function handleFlag(sectionIndex, taskIndex) {
  emit('toggle-pending', { sectionIndex, taskIndex })
}

function handleEditChecklist() {
  emit('edit-checklist')
}

function handleSubmitChecklist() {
  if (!canSubmitCurrentPeriod.value) return
  openConfirmDialog({
    kicker: periodExpired.value ? 'Period Ended' : 'Submit Checklist',
    title: periodExpired.value ? 'Start the next checklist period?' : 'Submit this checklist now?',
    message: periodExpired.value
      ? `The current ${periodLabel.value} period for "${props.title}" has ended.`
      : `This will close the current ${periodLabel.value} run for "${props.title}".`,
    detail:
      'A fresh set of task ids will be created for the next run, and the current entries will stay locked to this finished period.',
    confirmLabel: periodExpired.value ? 'Submit and continue' : 'Submit checklist',
    tone: 'warning',
    onConfirm: () => {
      closeConfirmDialog()
      emit('submit-checklist', { checklistId: props.id })
    },
  })
}

function getLatestMeasurement(task) {
  return task?.latestMeasurement ?? null
}

function canSaveTemperature(task) {
  const id = task?.id
  if (!id) return false
  const value = Number(temperatureDraftByTaskId[id])
  return Number.isFinite(value)
}

function handleSaveTemperature(task) {
  if (!isTemperatureTask(task)) return
  const id = task?.id
  if (!id) return
  const checklistId = props.id
  const valueC = Number(temperatureDraftByTaskId[id])
  if (!Number.isFinite(valueC)) return

  openConfirmDialog({
    kicker: 'Confirm Reading',
    title: `Save ${valueC} C for ${task.label}?`,
    message: 'Double-check the reading before saving it to the checklist log.',
    detail:
      'Temperature entries are stored for reporting later, including monthly graphs and audit history.',
    confirmLabel: 'Save reading',
    tone: 'default',
    onConfirm: () => {
      closeConfirmDialog()
      emit('log-temperature', { checklistId, taskId: id, valueC })
    },
  })
}

function openConfirmDialog(config) {
  confirmDialog.open = true
  confirmDialog.kicker = config?.kicker ?? ''
  confirmDialog.title = config?.title ?? 'Confirm action'
  confirmDialog.message = config?.message ?? ''
  confirmDialog.detail = config?.detail ?? ''
  confirmDialog.confirmLabel = config?.confirmLabel ?? 'Confirm'
  confirmDialog.tone = config?.tone ?? 'default'
  confirmDialog.onConfirm = typeof config?.onConfirm === 'function' ? config.onConfirm : null
}

function closeConfirmDialog() {
  confirmDialog.open = false
  confirmDialog.kicker = ''
  confirmDialog.title = ''
  confirmDialog.message = ''
  confirmDialog.detail = ''
  confirmDialog.confirmLabel = 'Confirm'
  confirmDialog.tone = 'default'
  confirmDialog.onConfirm = null
}
</script>

<style scoped>
.checklist-card {
  position: relative;
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-bg-primary);
  box-shadow: var(--shadow-sm);
}

.checklist-card.featured {
  border-color: var(--color-dark-tertiary);
}

.checklist-card.on-workbench {
  border-color: var(--color-success-border);
  box-shadow:
    0 0 0 3px rgba(212, 232, 53, 0.16),
    var(--shadow-md);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-4);
  padding: var(--space-5);
  border-bottom: 1px solid var(--color-border-subtle);
}

.card-header__main {
  min-width: 0;
  display: grid;
  gap: var(--space-2);
}

.title-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

h2 {
  margin: 0;
  font-size: 24px;
  line-height: 1.2;
  color: var(--color-text-primary);
}

p {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-md);
  line-height: var(--line-height-normal);
}

.header-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
}

.module-chip,
.status-pill,
.header-progress {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 24px;
  padding: 0 var(--space-3);
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
}

.module-chip {
  background: var(--color-bg-subtle);
  color: var(--color-dark-secondary);
}

.header-status {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: var(--space-3);
  min-width: 160px;
}

.edit-button {
  min-height: 36px;
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  padding: 0 var(--space-4);
  font: inherit;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  cursor: pointer;
  transition:
    border-color 120ms ease,
    background 120ms ease;
}

.edit-button:hover {
  border-color: var(--color-dark-secondary);
  background: var(--color-bg-secondary);
}

.status-pill.success {
  background: var(--color-success-bg);
  color: var(--color-success-text);
}

.status-pill.warning {
  background: var(--color-warning-bg);
  color: var(--color-warning-text);
}

.status-pill.muted {
  background: var(--color-bg-secondary);
  color: var(--color-text-muted);
}

.header-progress {
  background: var(--color-bg-secondary);
  color: var(--color-text-secondary);
}

.progress-track {
  width: 120px;
  height: 8px;
  border-radius: var(--radius-full);
  background: var(--color-bg-tertiary);
  overflow: hidden;
}

.progress-fill {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: var(--color-accent);
}

.section + .section {
  border-top: 1px solid var(--color-border-subtle);
}

.section-label {
  padding: var(--space-3) var(--space-5);
  background: var(--color-bg-secondary);
  color: var(--color-text-muted);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.task-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.task-row {
  display: grid;
  grid-template-columns: 24px minmax(0, 1fr) auto;
  align-items: center;
  gap: var(--space-3);
  min-height: 68px;
  padding: 0 var(--space-5);
  border-top: 1px solid var(--color-border-subtle);
  position: relative;
}

.task-row:first-child {
  border-top: 0;
}

.task-row::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: transparent;
}

.task-row.highlighted {
  background: rgba(255, 244, 208, 0.72);
}

.task-marker {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 2px solid var(--color-border-strong);
  background: #fff;
  padding: 0;
  cursor: pointer;
  transition:
    transform 120ms ease,
    box-shadow 120ms ease,
    border-color 120ms ease,
    background 120ms ease;
}

.task-marker:disabled,
.flag-button:disabled {
  cursor: wait;
  opacity: 0.7;
}

.task-marker:hover {
  transform: scale(1.04);
  box-shadow: 0 0 0 5px rgba(152, 197, 74, 0.14);
}

.task-marker__icon {
  font-size: 12px;
  font-weight: var(--font-weight-bold);
  line-height: 1;
  color: #ffffff;
}

.task-row.completed .task-marker {
  border-color: var(--color-success-border);
  background: var(--color-success);
}

.task-row.pending .task-marker {
  border-color: var(--color-warning);
  background: var(--color-warning);
  box-shadow: 0 0 0 4px rgba(232, 192, 48, 0.12);
}

.task-row.completed {
  background: rgba(240, 247, 204, 0.38);
}

.task-row.pending {
  background: rgba(255, 244, 208, 0.82);
}

.task-row.pending::before,
.task-row.highlighted::before {
  background: var(--color-warning);
}

.task-row.completed::before {
  background: var(--color-success-border);
}

.task-label {
  font-size: var(--font-size-md);
  color: var(--color-text-primary);
}

.task-label__main {
  color: var(--color-text-primary);
  font-weight: var(--font-weight-medium);
  line-height: 1.4;
}

.task-label__sub {
  margin-top: 2px;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-muted);
}

.task-actions {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: var(--space-2);
}

.task-meta {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
  white-space: nowrap;
}

.flag-button {
  min-height: 32px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  padding: 0 var(--space-3);
  font: inherit;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  cursor: pointer;
  transition:
    background 120ms ease,
    border-color 120ms ease;
}

.flag-button:hover {
  background: var(--color-bg-secondary);
}

.flag-button.active {
  border-color: var(--color-warning-border);
  background: var(--color-warning-bg);
  color: var(--color-warning-text);
}

.temp-input {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
}

.temp-field {
  width: 84px;
  min-height: 34px;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border-strong);
  padding: 0 var(--space-3);
  background: var(--color-bg-primary);
  font: inherit;
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
}

.temp-save {
  min-height: 34px;
  border: 1px solid var(--color-dark-secondary);
  border-radius: var(--radius-md);
  padding: 0 var(--space-3);
  background: var(--color-dark-secondary);
  color: #ffffff;
  font: inherit;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  cursor: pointer;
}

.temp-save:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.submit-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5);
  border-top: 1px solid var(--color-border-subtle);
  background: var(--color-bg-secondary);
}

.submit-bar.overdue {
  background: var(--color-warning-bg);
}

.submit-bar.blocked {
  background:
    linear-gradient(135deg, rgba(218, 228, 246, 0.92), rgba(242, 245, 250, 0.98));
  border-top-color: rgba(68, 92, 133, 0.22);
}

.submit-copy {
  min-width: 0;
}

.submit-title {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.submit-text {
  margin: 6px 0 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.submit-state-chip {
  display: inline-flex;
  align-items: center;
  margin-top: 10px;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(68, 92, 133, 0.12);
  color: #31496d;
  font-size: 12px;
  font-weight: var(--font-weight-bold);
}

.submit-button {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 0;
  border-radius: var(--radius-md);
  min-height: 40px;
  padding: 0 var(--space-4);
  background: var(--color-dark-secondary);
  color: #ffffff;
  font: inherit;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  text-align: center;
  white-space: normal;
  cursor: pointer;
  transition:
    background-color 140ms ease,
    color 140ms ease,
    opacity 140ms ease;
}

.submit-button:disabled {
  background: #c7d1e0;
  color: #53657f;
  cursor: not-allowed;
}

.confirm-overlay {
  position: absolute;
  inset: 0;
  z-index: 4;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(16, 18, 33, 0.48);
  backdrop-filter: blur(6px);
}

.confirm-dialog {
  width: min(420px, 100%);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border);
  background: var(--color-bg-primary);
  box-shadow: 0 28px 60px rgba(10, 14, 24, 0.28);
  padding: var(--space-5);
}

.confirm-dialog.warning {
  border-color: var(--color-warning-border);
  background: #fffdf5;
}

.confirm-kicker {
  margin-bottom: 8px;
  font-size: 11px;
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.confirm-dialog h3 {
  margin: 0;
  font-size: 24px;
  line-height: 1.1;
  color: var(--color-text-primary);
}

.confirm-dialog p {
  margin: 10px 0 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
}

.confirm-detail {
  margin-top: 12px;
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  font-size: 13px;
  color: var(--color-text-muted);
}

.confirm-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
}

.confirm-secondary,
.confirm-primary {
  border: 0;
  border-radius: var(--radius-md);
  min-height: 40px;
  padding: 0 var(--space-4);
  font: inherit;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  cursor: pointer;
}

.confirm-secondary {
  background: var(--color-bg-secondary);
  color: var(--color-text-primary);
}

.confirm-primary {
  background: var(--color-dark-secondary);
  color: #ffffff;
}

@media (max-width: 720px) {
  .card-header,
  .header-status,
  .submit-bar {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-status {
    min-width: 0;
    width: 100%;
  }

  .progress-track,
  .submit-button {
    width: 100%;
  }

  .submit-button {
    min-height: 44px;
  }

  .task-row {
    grid-template-columns: 24px minmax(0, 1fr);
    padding-top: 14px;
    padding-bottom: 14px;
  }

  .task-actions {
    grid-column: 2;
    justify-content: flex-start;
  }

  .confirm-actions {
    flex-direction: column-reverse;
  }

  .confirm-secondary,
  .confirm-primary {
    width: 100%;
  }
}
</style>
