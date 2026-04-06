<template>
  <div v-if="open" class="overlay" role="dialog" aria-modal="true" :aria-label="modalAriaLabel">
    <div class="modal">
      <header class="modal-header">
        <div>
          <div class="eyebrow">{{ moduleLabel }}</div>
          <h2>{{ modalTitle }}</h2>
          <p class="subtitle">{{ modalSubtitle }}</p>
        </div>
        <button type="button" class="icon-button" aria-label="Close" @click="close">&times;</button>
      </header>

      <form class="modal-body" @submit.prevent="submit">
        <section class="section-card">
          <div class="section-heading">
            <h3>Task details</h3>
            <p>Use clear titles so the task is easy to reuse across checklists.</p>
          </div>

          <div class="grid">
            <label class="field full">
              <span class="label">Task title</span>
              <input
                v-model.trim="title"
                class="input"
                type="text"
                placeholder="e.g. Check fridge 1"
                required
              />
            </label>

            <label class="field full">
              <span class="label">Meta (optional)</span>
              <input
                v-model.trim="meta"
                class="input"
                type="text"
                maxlength="255"
                placeholder="e.g. Record in cleaning log"
              />
            </label>

            <label class="field">
              <span class="label">Section</span>
              <select v-model="sectionType" class="input" required>
                <option disabled value="">Choose section</option>
                <option v-for="option in SECTION_TYPE_OPTIONS" :key="option" :value="option">
                  {{ formatSectionType(option) }}
                </option>
              </select>
            </label>

            <template v-if="isTemperatureControl">
              <label class="field">
                <span class="label">Target min (optional)</span>
                <input v-model.number="targetMin" class="input" type="number" step="0.1" />
              </label>

              <label class="field">
                <span class="label">Target max (optional)</span>
                <input v-model.number="targetMax" class="input" type="number" step="0.1" />
              </label>
            </template>
          </div>
        </section>

        <p v-if="error" class="error" role="alert">{{ error }}</p>

        <footer class="modal-footer">
          <button type="button" class="secondary" @click="close">Cancel</button>
          <button type="submit" class="primary">{{ submitButtonLabel }}</button>
        </footer>
      </form>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { SECTION_TYPE_OPTIONS, formatSectionType } from './taskTemplateOptions'

const props = defineProps({
  open: { type: Boolean, default: false },
  mode: {
    type: String,
    default: 'create',
    validator: (value) => ['create', 'edit'].includes(value),
  },
  initialTask: { type: Object, default: null },
  module: { type: String, required: true },
  moduleLabel: { type: String, default: '' },
})

const emit = defineEmits(['update:open', 'created', 'updated', 'close'])

const title = ref('')
const meta = ref('')
const sectionType = ref('')
const targetMin = ref(null)
const targetMax = ref(null)
const error = ref('')
const isEditMode = computed(() => props.mode === 'edit')
const modalTitle = computed(() => (isEditMode.value ? 'Edit task' : 'Create task'))
const modalSubtitle = computed(() =>
  isEditMode.value
    ? 'Update the reusable task in the shared module task pool.'
    : 'Add a reusable task to the shared module task pool.',
)
const submitButtonLabel = computed(() => (isEditMode.value ? 'Save changes' : 'Save task'))
const modalAriaLabel = computed(() =>
  isEditMode.value ? 'Edit task template' : 'Create task template',
)
const isTemperatureControl = computed(() => sectionType.value === 'TEMPERATURE_CONTROL')

function reset() {
  title.value = ''
  meta.value = ''
  sectionType.value = ''
  targetMin.value = null
  targetMax.value = null
  error.value = ''
}

function initFromTask(task) {
  if (!task) {
    reset()
    return
  }

  title.value = String(task.title ?? '').trim()
  meta.value = String(task.meta ?? '').trim()
  sectionType.value = String(task.sectionType ?? '')
  targetMin.value = task.targetMin ?? null
  targetMax.value = task.targetMax ?? null
  error.value = ''
}

watch(
  () => props.open,
  (isOpen) => {
    if (!isOpen) return
    if (isEditMode.value) {
      initFromTask(props.initialTask)
      return
    }
    reset()
  },
)

watch(
  () => props.initialTask,
  (task) => {
    if (props.open && isEditMode.value) initFromTask(task)
  },
)

watch(isTemperatureControl, (enabled) => {
  if (!enabled) {
    targetMin.value = null
    targetMax.value = null
  }
})

function close() {
  emit('update:open', false)
  emit('close')
}

function submit() {
  error.value = ''
  if (!title.value.trim()) {
    error.value = 'Task title is required.'
    return
  }
  if (!sectionType.value) {
    error.value = 'Section type is required.'
    return
  }
  if (
    Number.isFinite(targetMin.value) &&
    Number.isFinite(targetMax.value) &&
    Number(targetMin.value) > Number(targetMax.value)
  ) {
    error.value = 'Target min cannot be greater than target max.'
    return
  }

  const payload = {
    id: props.initialTask?.id ?? null,
    module: props.module,
    title: title.value.trim(),
    meta: meta.value.trim(),
    sectionType: sectionType.value,
    targetMin:
      isTemperatureControl.value && Number.isFinite(Number(targetMin.value))
        ? Number(targetMin.value)
        : null,
    targetMax:
      isTemperatureControl.value && Number.isFinite(Number(targetMax.value))
        ? Number(targetMax.value)
        : null,
  }

  if (isEditMode.value) {
    emit('updated', payload)
  } else {
    emit('created', payload)
  }
  close()
}
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  z-index: 80;
  background: rgba(12, 12, 24, 0.55);
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 64px 16px 32px;
  overflow: auto;
}

.modal {
  width: min(640px, 100%);
  max-height: calc(100vh - 48px);
  display: flex;
  flex-direction: column;
  border-radius: var(--radius-xl);
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  box-shadow: 0 30px 90px rgba(0, 0, 0, 0.25);
  overflow: hidden;
}

.modal-header,
.modal-body {
  padding: 20px 24px;
}

.modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  border-bottom: 1px solid var(--color-border-subtle);
}

.eyebrow {
  margin-bottom: 4px;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

h2 {
  margin: 0;
  font-size: 26px;
  line-height: 1.1;
  color: var(--color-text-primary);
}

.subtitle {
  margin: 6px 0 0;
  color: var(--color-text-muted);
}

.icon-button {
  width: 36px;
  height: 36px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-bg-primary);
  font-size: 24px;
  line-height: 1;
  cursor: pointer;
}

.modal-body {
  overflow: auto;
  display: grid;
  gap: var(--space-4);
}

.section-card {
  display: grid;
  gap: var(--space-4);
  padding: var(--space-5);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-bg-secondary);
}

.section-heading h3 {
  margin: 0;
  font-size: var(--font-size-lg);
  color: var(--color-text-primary);
}

.section-heading p {
  margin: 6px 0 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.field {
  display: grid;
  gap: 8px;
}

.full {
  grid-column: 1 / -1;
}

.label {
  font-size: 13px;
  font-weight: 700;
  color: var(--color-text-secondary);
}

.input {
  width: 100%;
  min-height: 44px;
  padding: 10px 12px;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border-strong);
  background: white;
  box-sizing: border-box;
  font: inherit;
  color: var(--color-text-primary);
}

select.input {
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
  padding-right: 42px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 12 12' fill='none'%3E%3Cpath d='M2.25 4.5L6 8.25L9.75 4.5' stroke='%2320203a' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 14px center;
  background-size: 12px 12px;
}

select.input::-ms-expand {
  display: none;
}

.error {
  margin: 0;
  color: #a11d2d;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.primary,
.secondary {
  min-height: 44px;
  padding: 0 16px;
  border-radius: var(--radius-md);
  border: 1px solid transparent;
  cursor: pointer;
}

.primary {
  background: var(--color-dark-secondary);
  color: white;
}

.secondary {
  background: var(--color-bg-secondary);
  border-color: var(--color-border);
  color: var(--color-text-primary);
}

@media (max-width: 720px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
