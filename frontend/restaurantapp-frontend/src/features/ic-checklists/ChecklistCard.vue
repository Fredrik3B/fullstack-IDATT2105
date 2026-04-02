<template>
  <article class="checklist-card" :class="{ featured: featured }">
    <header class="card-header">
      <div>
        <div class="title-row">
          <h2>{{ title }}</h2>
          <span v-if="moduleChip" class="module-chip">{{ moduleChip }}</span>
        </div>
        <p>{{ subtitle }}</p>
      </div>

      <div class="header-status">
        <button type="button" class="edit-button" @click="handleEditChecklist">Edit checklist</button>
        <span class="status-pill" :class="statusTone">{{ statusLabel }}</span>
        <div v-if="progress !== null" class="progress-track">
          <span class="progress-fill" :style="{ width: `${progress}%` }"></span>
        </div>
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
            @click="handleToggle(sectionIndex, taskIndex)"
          ></button>
          <div class="task-label">
            <div class="task-label__main">{{ task.label }}</div>
            <div v-if="isTemperatureTask(task) && formatTemperatureTarget(task)" class="task-label__sub">
              Target: {{ formatTemperatureTarget(task) }}
            </div>
          </div>
          <div class="task-actions">
            <template v-if="isTemperatureTask(task)">
              <span v-if="getLatestMeasurement(task)" class="task-meta">
                Last: {{ getLatestMeasurement(task)?.valueC }} C
              </span>
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
              :aria-label="task.state === 'pending' ? 'Remove pending flag' : 'Mark task as pending'"
              @click="handleFlag(sectionIndex, taskIndex)"
            >
              Flag
            </button>
          </div>
        </li>
      </ul>
    </div>

    <footer class="submit-bar" :class="{ overdue: periodExpired }">
      <div class="submit-copy">
        <div class="submit-title">{{ periodExpired ? 'This checklist period has ended' : 'Submit when this checklist is ready' }}</div>
        <p class="submit-text">{{ submitWarning }}</p>
      </div>
      <button type="button" class="submit-button" @click="handleSubmitChecklist">
        {{ periodExpired ? 'Submit and load next period' : 'Submit checklist' }}
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
          <button type="button" class="confirm-secondary" @click="closeConfirmDialog">Cancel</button>
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
import { getPeriodEnd, isPeriodExpired, normalizePeriodEnum } from './recurrence'
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
  onConfirm: null
})

const props = defineProps({
  id: {
    type: String,
    default: ''
  },
  title: {
    type: String,
    required: true
  },
  period: {
    type: String,
    default: 'daily'
  },
  activePeriodKey: {
    type: String,
    default: ''
  },
  subtitle: {
    type: String,
    required: true
  },
  statusLabel: {
    type: String,
    required: true
  },
  statusTone: {
    type: String,
    default: 'success'
  },
  progress: {
    type: Number,
    default: null
  },
  sections: {
    type: Array,
    required: true
  },
  featured: {
    type: Boolean,
    default: false
  },
  moduleChip: {
    type: String,
    default: 'IC-Food'
  },
  temperatureLatestByTaskId: {
    type: Object,
    default: null
  },
  now: {
    type: [Date, String, Number],
    default: null
  }
})

const emit = defineEmits(['toggle-task', 'toggle-pending', 'edit-checklist', 'submit-checklist', 'log-temperature'])

const activeDate = computed(() => {
  if (props.now instanceof Date) return props.now
  if (typeof props.now === 'string' || typeof props.now === 'number') {
    const parsed = new Date(props.now)
    return Number.isNaN(parsed.getTime()) ? new Date() : parsed
  }
  return new Date()
})

const periodEnd = computed(() => getPeriodEnd(props.period, props.activePeriodKey))
const periodExpired = computed(() => isPeriodExpired(props.period, props.activePeriodKey, activeDate.value))
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
    minute: '2-digit'
  }).format(periodEnd.value)
})
const submitWarning = computed(() => {
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
  openConfirmDialog({
    kicker: periodExpired.value ? 'Period Ended' : 'Submit Checklist',
    title: periodExpired.value ? 'Start the next checklist period?' : 'Submit this checklist now?',
    message: periodExpired.value
      ? `The current ${periodLabel.value} period for "${props.title}" has ended.`
      : `This will close the current ${periodLabel.value} run for "${props.title}".`,
    detail: 'A fresh set of task ids will be created for the next run, and the current entries will stay locked to this finished period.',
    confirmLabel: periodExpired.value ? 'Submit and continue' : 'Submit checklist',
    tone: 'warning',
    onConfirm: () => {
      closeConfirmDialog()
      emit('submit-checklist', { checklistId: props.id })
    }
  })
}

function getLatestMeasurement(task) {
  const id = task?.id
  if (!id) return null

  const container = props.temperatureLatestByTaskId
  if (!container) return null
  if (typeof container.get === 'function') return container.get(id) ?? null
  return container[id] ?? null
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
    detail: 'Temperature entries are stored for reporting later, including monthly graphs and audit history.',
    confirmLabel: 'Save reading',
    tone: 'default',
    onConfirm: () => {
      closeConfirmDialog()
      emit('log-temperature', { checklistId, taskId: id, valueC })
    }
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
  border: 1px solid rgba(210, 213, 230, 0.95);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 18px 42px rgba(26, 26, 46, 0.08);
}

.checklist-card.featured {
  border-width: 2px;
  border-color: rgba(45, 43, 85, 0.55);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-4);
  padding: 22px 22px 18px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

h2 {
  margin: 0;
  font-size: 28px;
  line-height: 1.05;
  color: var(--color-text-primary);
}

p {
  margin: 6px 0 0;
  color: var(--color-text-muted);
  font-size: var(--font-size-md);
}

.module-chip,
.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 28px;
  padding: 0 12px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
}

.module-chip {
  background: var(--color-dark-secondary);
  color: var(--color-accent);
}

.header-status {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.edit-button {
  border: 1px solid rgba(45, 43, 85, 0.35);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.92);
  color: var(--color-dark-secondary);
  padding: 10px 14px;
  font: inherit;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  cursor: pointer;
  box-shadow: var(--shadow-sm);
  transition: transform 120ms ease, box-shadow 120ms ease, border-color 120ms ease, background 120ms ease, color 120ms ease;
}

.edit-button:hover {
  transform: translateY(-1px);
  border-color: rgba(45, 43, 85, 0.55);
  box-shadow: var(--shadow-md);
  background: var(--color-dark-secondary);
  color: var(--color-accent);
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
  background: #f4f2fb;
  color: #b6b2cf;
}

.progress-track {
  width: 72px;
  height: 6px;
  border-radius: var(--radius-full);
  background: #eceaf6;
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
  padding: 12px 22px 10px;
  background: rgba(242, 243, 250, 0.82);
  color: #a3a0bf;
  font-size: var(--font-size-sm);
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
  grid-template-columns: 18px minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
  min-height: 64px;
  padding: 0 22px;
  border-top: 1px solid var(--color-border-subtle);
  position: relative;
}

.task-label__main {
  color: var(--color-text-primary);
  font-weight: var(--font-weight-medium);
}

.task-label__sub {
  margin-top: 2px;
  font-size: 12px;
  font-weight: var(--font-weight-bold);
  color: var(--color-text-muted);
}

.task-row::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: transparent;
  border-radius: 0 6px 6px 0;
}

.task-row:first-child {
  border-top: 0;
}

.task-row.highlighted {
  background: rgba(255, 244, 208, 0.72);
}

.task-marker {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid #d7d6e7;
  background: #fff;
  padding: 0;
  position: relative;
  cursor: pointer;
  transition: transform 120ms ease, box-shadow 120ms ease, border-color 120ms ease, background 120ms ease;
}

.task-marker:hover {
  transform: scale(1.08);
  box-shadow: 0 0 0 5px rgba(152, 197, 74, 0.14);
}

.task-row.completed .task-marker {
  border-color: var(--color-success-border);
  background: var(--color-success);
  box-shadow: inset 0 0 0 3px rgba(255, 255, 255, 0.92);
}

.task-row.pending .task-marker {
  border-color: var(--color-warning);
  background: var(--color-warning-bg);
  box-shadow: 0 0 0 5px rgba(232, 192, 48, 0.12);
}

.task-row.todo .task-marker {
  border-color: #d7d6e7;
}

.task-row.completed {
  background: rgba(240, 247, 204, 0.55);
}

.task-row.completed .task-label {
  color: rgba(26, 26, 46, 0.78);
}

.task-row.completed .task-meta {
  color: rgba(76, 74, 114, 0.58);
}

.task-row.pending {
  background: rgba(255, 244, 208, 0.82);
}

.task-row.pending .task-label {
  color: rgba(26, 26, 46, 0.92);
  font-weight: var(--font-weight-medium);
}

.task-row.todo::before {
  background: transparent;
}

.task-row.pending::before,
.task-row.highlighted::before {
  background: var(--color-warning);
}

.task-row.completed::before {
  background: var(--color-success-border);
}

.task-label {
  font-size: 15px;
  color: var(--color-text-primary);
}

.task-actions {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.task-meta {
  color: var(--color-text-hint);
  font-size: var(--font-size-sm);
  white-space: nowrap;
}

.flag-button {
  border: 1px solid rgba(45, 43, 85, 0.14);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.96);
  color: var(--color-text-primary);
  padding: 8px 12px;
  font: inherit;
  font-size: 12px;
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.02em;
  cursor: pointer;
  transition: transform 120ms ease, box-shadow 120ms ease, background 120ms ease, border-color 120ms ease;
}

.flag-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 18px rgba(45, 43, 85, 0.14);
}

.flag-button.active {
  border-color: rgba(232, 192, 48, 0.6);
  background: linear-gradient(180deg, rgba(255, 244, 208, 0.98) 0%, rgba(232, 192, 48, 0.95) 100%);
  color: rgba(36, 28, 0, 0.92);
}

.task-row.pending .task-meta {
  color: var(--color-warning-text);
  font-weight: var(--font-weight-bold);
}

.task-row.completed .task-actions .flag-button {
  opacity: 0.65;
}

@media (max-width: 720px) {
  .card-header,
  .header-status {
    flex-direction: column;
    align-items: flex-start;
  }

  .task-row {
    grid-template-columns: 18px minmax(0, 1fr);
    padding-top: 14px;
    padding-bottom: 14px;
  }

  .task-actions {
    grid-column: 2;
    flex-wrap: wrap;
  }
}

.temp-input {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.temp-field {
  width: 74px;
  border-radius: 999px;
  border: 1px solid rgba(45, 43, 85, 0.18);
  padding: 8px 10px;
  background: rgba(255, 255, 255, 0.95);
  font: inherit;
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
}

.temp-save {
  border: 1px solid rgba(45, 43, 85, 0.18);
  border-radius: 999px;
  padding: 8px 12px;
  background: var(--color-dark-secondary);
  color: var(--color-accent);
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
  gap: 16px;
  padding: 18px 22px 22px;
  border-top: 1px solid var(--color-border-subtle);
  background: linear-gradient(180deg, rgba(249, 249, 253, 0.98) 0%, rgba(242, 244, 250, 0.98) 100%);
}

.submit-bar.overdue {
  background: linear-gradient(180deg, rgba(255, 244, 208, 0.92) 0%, rgba(255, 237, 187, 0.98) 100%);
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

.submit-button {
  flex-shrink: 0;
  border: 0;
  border-radius: 999px;
  min-height: 42px;
  padding: 0 16px;
  background: var(--color-dark-secondary);
  color: var(--color-accent);
  font: inherit;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  cursor: pointer;
  box-shadow: var(--shadow-sm);
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
  border-radius: 22px;
  border: 1px solid rgba(214, 219, 235, 0.95);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(247, 248, 252, 0.98) 100%);
  box-shadow: 0 28px 60px rgba(10, 14, 24, 0.28);
  padding: 22px;
}

.confirm-dialog.warning {
  border-color: rgba(232, 192, 48, 0.42);
  background: linear-gradient(180deg, rgba(255, 250, 233, 0.98) 0%, rgba(255, 244, 208, 0.98) 100%);
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
  padding: 12px 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.76);
  border: 1px solid rgba(215, 220, 235, 0.9);
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
  border-radius: 999px;
  min-height: 40px;
  padding: 0 14px;
  font: inherit;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  cursor: pointer;
}

.confirm-secondary {
  background: rgba(45, 43, 85, 0.08);
  color: var(--color-text-primary);
}

.confirm-primary {
  background: var(--color-dark-secondary);
  color: var(--color-accent);
}

@media (max-width: 720px) {
  .submit-bar {
    flex-direction: column;
    align-items: flex-start;
  }

  .submit-button {
    width: 100%;
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
