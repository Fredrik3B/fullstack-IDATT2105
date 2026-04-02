<template>
  <div v-if="open" class="overlay" role="dialog" aria-modal="true" aria-label="Create task template">
    <div class="modal">
      <header class="modal-header">
        <div>
          <div class="eyebrow">{{ moduleLabel }}</div>
          <h2>Create task</h2>
          <p class="subtitle">Add a reusable task template to the shared pool for this module.</p>
        </div>
        <button type="button" class="icon-button" aria-label="Close" @click="close">×</button>
      </header>

      <form class="modal-body" @submit.prevent="submit">
        <div class="grid">
          <label class="field full">
            <span class="label">Task title</span>
            <input v-model.trim="title" class="input" type="text" placeholder="e.g. Check Fridge 1" required />
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

          <label class="field">
            <span class="label">Unit (optional)</span>
            <input v-model.trim="unit" class="input" type="text" maxlength="10" placeholder="e.g. C" />
          </label>

          <label class="field">
            <span class="label">Target min (optional)</span>
            <input v-model.number="targetMin" class="input" type="number" step="0.1" />
          </label>

          <label class="field">
            <span class="label">Target max (optional)</span>
            <input v-model.number="targetMax" class="input" type="number" step="0.1" />
          </label>
        </div>

        <p v-if="error" class="error" role="alert">{{ error }}</p>

        <footer class="modal-footer">
          <button type="button" class="secondary" @click="close">Cancel</button>
          <button type="submit" class="primary">Save task</button>
        </footer>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { SECTION_TYPE_OPTIONS, formatSectionType } from './taskTemplateOptions'

const props = defineProps({
  open: {
    type: Boolean,
    default: false
  },
  module: {
    type: String,
    required: true
  },
  moduleLabel: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:open', 'created', 'close'])

const title = ref('')
const sectionType = ref('')
const unit = ref('')
const targetMin = ref(null)
const targetMax = ref(null)
const error = ref('')

function reset() {
  title.value = ''
  sectionType.value = ''
  unit.value = ''
  targetMin.value = null
  targetMax.value = null
  error.value = ''
}

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) reset()
  }
)

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
  if (Number.isFinite(targetMin.value) && Number.isFinite(targetMax.value) && Number(targetMin.value) > Number(targetMax.value)) {
    error.value = 'Target min cannot be greater than target max.'
    return
  }

  emit('created', {
    module: props.module,
    title: title.value.trim(),
    sectionType: sectionType.value,
    unit: unit.value.trim() || null,
    targetMin: Number.isFinite(Number(targetMin.value)) ? Number(targetMin.value) : null,
    targetMax: Number.isFinite(Number(targetMax.value)) ? Number(targetMax.value) : null
  })
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
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.98);
  border: 1px solid rgba(210, 213, 230, 0.95);
  box-shadow: 0 30px 90px rgba(0, 0, 0, 0.25);
  overflow: hidden;
}

.modal-header,
.modal-body {
  padding: 20px;
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
  border: 0;
  background: transparent;
  font-size: 28px;
  line-height: 1;
  cursor: pointer;
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
  border-radius: 12px;
  border: 1px solid var(--color-border-subtle);
  background: white;
}

.error {
  margin: 14px 0 0;
  color: #a11d2d;
}

.modal-footer {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.primary,
.secondary {
  min-height: 44px;
  padding: 0 16px;
  border-radius: 999px;
  border: 0;
  cursor: pointer;
}

.primary {
  background: var(--color-text-primary);
  color: white;
}

.secondary {
  background: rgba(0, 0, 0, 0.06);
  color: var(--color-text-primary);
}

@media (max-width: 720px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
