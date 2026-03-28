<template>
  <div v-if="open" class="overlay" role="dialog" aria-modal="true" aria-label="Create checklist">
    <div class="modal">
      <header class="modal-header">
        <div>
          <div class="eyebrow">{{ moduleLabel }}</div>
          <h2>Create checklist</h2>
          <p class="subtitle">Build a new recurring checklist card.</p>
        </div>

        <button type="button" class="icon-button" aria-label="Close" @click="handleClose">×</button>
      </header>

      <form class="modal-body" @submit.prevent="handleSubmit">
        <div class="grid">
          <label class="field">
            <span class="label">Title</span>
            <input v-model.trim="title" class="input" type="text" placeholder="e.g. Opening checklist" required />
          </label>

          <label class="field">
            <span class="label">Period</span>
            <select v-model="period" class="input" required>
              <option value="daily">Daily</option>
              <option value="weekly">Weekly</option>
              <option value="monthly">Monthly</option>
            </select>
          </label>

          <label class="field full">
            <span class="label">Subtitle (optional)</span>
            <input v-model.trim="subtitle" class="input" type="text" placeholder="e.g. Daily - opening" />
          </label>
        </div>

        <div class="sections">
          <div class="sections-header">
            <h3>Sections</h3>
            <button type="button" class="ghost-button" @click="addSection">+ Add section</button>
          </div>

          <div v-for="(section, idx) in sections" :key="section.id" class="section">
            <div class="section-top">
              <label class="field">
                <span class="label">Section title</span>
                <input v-model.trim="section.title" class="input" type="text" placeholder="e.g. Hygiene" required />
              </label>

              <button
                v-if="sections.length > 1"
                type="button"
                class="danger-ghost"
                @click="removeSection(idx)"
              >
                Remove
              </button>
            </div>

            <label class="field full">
              <span class="label">Tasks (one per line)</span>
              <textarea
                v-model="section.tasksText"
                class="textarea"
                rows="5"
                placeholder="e.g.&#10;Wash hands&#10;Check fridge temperature"
              ></textarea>
            </label>
          </div>
        </div>

        <p v-if="error" class="error" role="alert">{{ error }}</p>

        <footer class="modal-footer">
          <button type="button" class="secondary" @click="handleClose">Cancel</button>
          <button type="submit" class="primary">Create</button>
        </footer>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  open: {
    type: Boolean,
    default: false
  },
  moduleLabel: {
    type: String,
    default: ''
  },
  moduleChip: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:open', 'close', 'created'])

function randomId(prefix = 'id') {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') return `${prefix}-${crypto.randomUUID()}`
  return `${prefix}-${Math.random().toString(16).slice(2)}-${Date.now()}`
}

const title = ref('')
const subtitle = ref('')
const period = ref('daily')
const error = ref('')

const sections = ref([
  {
    id: randomId('section'),
    title: 'Tasks',
    tasksText: ''
  }
])

function resetForm() {
  title.value = ''
  subtitle.value = ''
  period.value = 'daily'
  error.value = ''
  sections.value = [{ id: randomId('section'), title: 'Tasks', tasksText: '' }]
}

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) resetForm()
  }
)

function addSection() {
  sections.value.push({ id: randomId('section'), title: `Section ${sections.value.length + 1}`, tasksText: '' })
}

function removeSection(index) {
  sections.value.splice(index, 1)
}

function periodLabel(value) {
  if (value === 'weekly') return 'Weekly'
  if (value === 'monthly') return 'Monthly'
  return 'Daily'
}

function handleClose() {
  emit('update:open', false)
  emit('close')
}

function handleSubmit() {
  error.value = ''

  const sectionPayload = sections.value
    .map((section) => {
      const lines = String(section.tasksText ?? '')
        .split(/\r?\n/)
        .map((line) => line.trim())
        .filter(Boolean)
      return {
        title: String(section.title || 'Tasks').trim(),
        items: lines.map((label) => ({
          id: randomId('task'),
          label,
          meta: '',
          state: 'todo'
        }))
      }
    })
    .filter((section) => section.items.length > 0)

  if (!title.value.trim()) {
    error.value = 'Title is required.'
    return
  }

  if (sectionPayload.length === 0) {
    error.value = 'Add at least one task.'
    return
  }

  const defaultSubtitle = `${periodLabel(period.value)} checklist`

  const newCard = {
    id: randomId('checklist'),
    period: period.value,
    title: title.value.trim(),
    subtitle: subtitle.value.trim() || defaultSubtitle,
    statusLabel: '',
    statusTone: 'muted',
    progress: null,
    featured: false,
    moduleChip: props.moduleChip || props.moduleLabel,
    sections: sectionPayload
  }

  // Backend TODO:
  // Call POST /api/checklists here (or from the parent) and use the returned ids.
  // See: `frontend/restaurantapp-frontend/src/api/checklists.js`
  emit('created', newCard)
  handleClose()
}
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  z-index: 60;
  background: rgba(12, 12, 24, 0.55);
  display: grid;
  place-items: center;
  padding: 16px;
}

.modal {
  width: min(820px, 100%);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.98);
  border: 1px solid rgba(210, 213, 230, 0.95);
  box-shadow: 0 30px 90px rgba(0, 0, 0, 0.25);
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px 14px;
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
  background: rgba(255, 255, 255, 0.9);
  color: var(--color-text-primary);
  font: inherit;
  font-weight: var(--font-weight-bold);
  width: 40px;
  height: 40px;
  border-radius: 12px;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
}

.modal-body {
  padding: 18px 20px 20px;
}

.grid {
  display: grid;
  grid-template-columns: 1fr 220px;
  gap: 14px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field.full {
  grid-column: 1 / -1;
}

.label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-secondary);
}

.input,
.textarea {
  border-radius: 14px;
  border: 1.5px solid var(--color-border);
  background: var(--color-bg-secondary);
  color: var(--color-text-primary);
  padding: 12px 14px;
  font: inherit;
}

.textarea {
  resize: vertical;
}

.sections {
  margin-top: 18px;
  display: grid;
  gap: 12px;
}

.sections-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

h3 {
  margin: 0;
  font-size: 16px;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.section {
  padding: 14px;
  border: 1px solid var(--color-border-subtle);
  border-radius: 18px;
  background: rgba(242, 243, 250, 0.5);
}

.section-top {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.ghost-button,
.danger-ghost {
  border: 1px solid rgba(45, 43, 85, 0.14);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.96);
  color: var(--color-text-primary);
  padding: 10px 14px;
  font: inherit;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  cursor: pointer;
  box-shadow: var(--shadow-sm);
}

.danger-ghost {
  border-color: rgba(190, 40, 40, 0.25);
  color: rgba(190, 40, 40, 0.95);
}

.error {
  margin: 12px 0 0;
  color: rgba(190, 40, 40, 0.95);
  font-weight: var(--font-weight-bold);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}

.primary,
.secondary {
  border: 0;
  border-radius: 14px;
  padding: 12px 16px;
  font: inherit;
  font-weight: var(--font-weight-bold);
  cursor: pointer;
}

.primary {
  background: var(--color-dark-secondary);
  color: var(--color-accent);
  box-shadow: var(--shadow-md);
}

.secondary {
  background: rgba(255, 255, 255, 0.96);
  color: var(--color-text-primary);
  border: 1px solid rgba(45, 43, 85, 0.14);
  box-shadow: var(--shadow-sm);
}

@media (max-width: 700px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>

