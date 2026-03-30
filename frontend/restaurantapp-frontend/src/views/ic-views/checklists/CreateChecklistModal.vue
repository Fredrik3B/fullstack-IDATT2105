<template>
  <div v-if="open" class="overlay" role="dialog" aria-modal="true" :aria-label="dialogAriaLabel">
    <div class="modal">
      <header class="modal-header">
        <div>
          <div class="eyebrow">{{ moduleLabel }}</div>
          <h2>{{ modalTitle }}</h2>
          <p class="subtitle">{{ modalSubtitle }}</p>
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

            <div class="tasks">
              <div class="tasks-header">
                <div class="tasks-title">Tasks</div>
                <div class="tasks-actions">
                  <button type="button" class="ghost-button ghost-button--compact" @click="toggleAddTaskMenu(idx)">
                    + Add task
                  </button>
                </div>
              </div>

              <div v-if="section.isAddTaskMenuOpen" class="add-task-menu" role="group" aria-label="Choose task type">
                <button type="button" class="ghost-button ghost-button--compact" @click="addTask(idx, 'standard')">
                  Standard task
                </button>
                <button type="button" class="ghost-button ghost-button--compact" @click="addTask(idx, 'temperature')">
                  Temperature point
                </button>
              </div>

              <div v-if="section.tasks.length === 0" class="tasks-empty">No tasks yet.</div>

              <div v-for="(task, taskIdx) in section.tasks" :key="task.id" class="task-editor">
                <div class="task-editor-top">
                  <label class="field field--inline">
                    <span class="label">Type</span>
                    <select v-model="task.kind" class="input input--compact">
                      <option value="standard">Standard</option>
                      <option value="temperature">Temperature</option>
                    </select>
                  </label>

                  <button type="button" class="danger-ghost danger-ghost--compact" @click="removeTask(idx, taskIdx)">
                    Remove
                  </button>
                </div>

                <div class="task-grid">
                  <label class="field full">
                    <span class="label">Label</span>
                    <input
                      v-model.trim="task.label"
                      class="input"
                      type="text"
                      :placeholder="task.kind === 'temperature' ? 'e.g. Cold room 1' : 'e.g. Wash hands'"
                      required
                    />
                  </label>

                  <template v-if="task.kind === 'standard'">
                    <label class="field full">
                      <span class="label">Meta (optional)</span>
                      <input v-model.trim="task.meta" class="input" type="text" placeholder="e.g. 08:30" />
                    </label>
                  </template>

                  <template v-else>
                    <label class="field">
                      <span class="label">Min (C)</span>
                      <input v-model.number="task.targetMin" class="input" type="number" step="0.1" placeholder="e.g. 2" />
                    </label>
                    <label class="field">
                      <span class="label">Max (C)</span>
                      <input v-model.number="task.targetMax" class="input" type="number" step="0.1" placeholder="e.g. 4" />
                    </label>
                  </template>
                </div>
              </div>
            </div>
          </div>
        </div>

        <p v-if="error" class="error" role="alert">{{ error }}</p>

        <footer class="modal-footer">
          <button type="button" class="secondary" @click="handleClose">Cancel</button>
          <button type="submit" class="primary">{{ submitLabel }}</button>
        </footer>
      </form>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  open: {
    type: Boolean,
    default: false
  },
  mode: {
    type: String,
    default: 'create',
    validator: (value) => ['create', 'edit'].includes(value)
  },
  initialCard: {
    type: Object,
    default: null
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

const emit = defineEmits(['update:open', 'close', 'created', 'updated'])

function randomId(prefix = 'id') {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') return `${prefix}-${crypto.randomUUID()}`
  return `${prefix}-${Math.random().toString(16).slice(2)}-${Date.now()}`
}

const title = ref('')
const subtitle = ref('')
const period = ref('daily')
const error = ref('')

const isEditMode = computed(() => props.mode === 'edit')
const modalTitle = computed(() => (isEditMode.value ? 'Edit checklist' : 'Create checklist'))
const modalSubtitle = computed(() =>
  isEditMode.value ? 'Update the recurring checklist card.' : 'Build a new recurring checklist card.'
)
const submitLabel = computed(() => (isEditMode.value ? 'Save changes' : 'Create'))
const dialogAriaLabel = computed(() => (isEditMode.value ? 'Edit checklist' : 'Create checklist'))

const sections = ref([
  {
    id: randomId('section'),
    title: 'Tasks',
    isAddTaskMenuOpen: false,
    tasks: []
  }
])

function resetForm() {
  title.value = ''
  subtitle.value = ''
  period.value = 'daily'
  error.value = ''
  sections.value = [{ id: randomId('section'), title: 'Tasks', isAddTaskMenuOpen: false, tasks: [] }]
}

function normalizeTaskForForm(task) {
  const kind = task?.type === 'temperature' ? 'temperature' : 'standard'

  if (kind === 'temperature') {
    return {
      id: task?.id ?? randomId('task'),
      kind: 'temperature',
      label: String(task?.label ?? '').trim(),
      meta: String(task?.meta ?? ''),
      unit: String(task?.unit ?? 'C'),
      targetMin: Number.isFinite(task?.targetMin) ? task.targetMin : null,
      targetMax: Number.isFinite(task?.targetMax) ? task.targetMax : null
    }
  }

  return {
    id: task?.id ?? randomId('task'),
    kind: 'standard',
    label: String(task?.label ?? '').trim(),
    meta: String(task?.meta ?? '')
  }
}

function initFromCard(card) {
  if (!card) {
    resetForm()
    return
  }

  title.value = String(card.title ?? '').trim()
  subtitle.value = String(card.subtitle ?? '').trim()
  period.value = String(card.period ?? 'daily')
  error.value = ''

  const sourceSections = Array.isArray(card.sections) ? card.sections : []
  if (sourceSections.length === 0) {
    sections.value = [{ id: randomId('section'), title: 'Tasks', isAddTaskMenuOpen: false, tasks: [] }]
    return
  }

  sections.value = sourceSections.map((section) => {
    const items = Array.isArray(section.items) ? section.items : []
    return {
      id: randomId('section'),
      title: String(section.title ?? 'Tasks').trim() || 'Tasks',
      isAddTaskMenuOpen: false,
      tasks: items.map(normalizeTaskForForm)
    }
  })
}

watch(
  () => props.open,
  (isOpen) => {
    if (!isOpen) return
    if (isEditMode.value) initFromCard(props.initialCard)
    else resetForm()
  }
)

watch(
  () => props.initialCard,
  (nextCard) => {
    if (!props.open) return
    if (!isEditMode.value) return
    initFromCard(nextCard)
  }
)

function addSection() {
  sections.value.push({
    id: randomId('section'),
    title: `Section ${sections.value.length + 1}`,
    isAddTaskMenuOpen: false,
    tasks: []
  })
}

function removeSection(index) {
  sections.value.splice(index, 1)
}

function toggleAddTaskMenu(sectionIndex) {
  const section = sections.value[sectionIndex]
  if (!section) return
  section.isAddTaskMenuOpen = !section.isAddTaskMenuOpen
}

function addTask(sectionIndex, kind) {
  const section = sections.value[sectionIndex]
  if (!section) return

  if (kind === 'temperature') {
    section.tasks.push({
      id: randomId('task'),
      kind: 'temperature',
      label: '',
      meta: '',
      unit: 'C',
      targetMin: null,
      targetMax: null
    })
    section.isAddTaskMenuOpen = false
    return
  }

  section.tasks.push({
    id: randomId('task'),
    kind: 'standard',
    label: '',
    meta: ''
  })
  section.isAddTaskMenuOpen = false
}

function removeTask(sectionIndex, taskIndex) {
  const section = sections.value[sectionIndex]
  if (!section) return
  section.tasks.splice(taskIndex, 1)
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

  const previousCard = isEditMode.value ? props.initialCard : null
  const previousSections = previousCard && Array.isArray(previousCard.sections) ? previousCard.sections : []
  const previousTasksById = new Map()
  previousSections.forEach((section) => {
    const items = Array.isArray(section.items) ? section.items : []
    items.forEach((task) => {
      if (task?.id) previousTasksById.set(task.id, task)
    })
  })

  const sectionPayload = sections.value
    .map((section) => {
      const safeTasks = Array.isArray(section.tasks) ? section.tasks : []
      const missingLabels = safeTasks.some((task) => !String(task?.label ?? '').trim())
      if (missingLabels) return { __error: 'Fill in all task labels or remove empty tasks.' }

      const items = safeTasks
        .map((formTask) => {
          const taskId = formTask?.id ?? randomId('task')
          const previousTask = previousTasksById.get(taskId) ?? null
          const label = String(formTask?.label ?? '').trim()

          if (formTask?.kind === 'temperature') {
            const base = previousTask ? { ...previousTask } : { id: taskId, meta: '', state: 'todo' }
            return {
              ...base,
              id: taskId,
              label,
              type: 'temperature',
              unit: 'C',
              targetMin: Number.isFinite(Number(formTask?.targetMin)) ? Number(formTask.targetMin) : null,
              targetMax: Number.isFinite(Number(formTask?.targetMax)) ? Number(formTask.targetMax) : null
            }
          }

          const base = previousTask ? { ...previousTask } : { id: taskId, meta: '', state: 'todo' }
          const candidate = {
            ...base,
            id: taskId,
            label,
            meta: String(formTask?.meta ?? base.meta ?? '')
          }
          // Remove temperature-only fields if the user changed type back to standard.
          // This ensures the checklist stays declarative and measurements remain linked to `taskId`.
          // Backend should treat `taskId` as stable, and measurements should always reference that id.
          // eslint-disable-next-line no-unused-vars
          const { type, unit, targetMin, targetMax, ...clean } = candidate
          return clean
        })
        .filter((task) => Boolean(task?.label))

      return {
        title: String(section.title || 'Tasks').trim(),
        items
      }
    })
    .filter((section) => section && !section.__error && section.items.length > 0)

  const firstSectionError = sections.value
    .map((section) => {
      const safeTasks = Array.isArray(section.tasks) ? section.tasks : []
      const missingLabels = safeTasks.some((task) => !String(task?.label ?? '').trim())
      return missingLabels ? 'Fill in all task labels or remove empty tasks.' : null
    })
    .find(Boolean)

  if (firstSectionError) {
    error.value = firstSectionError
    return
  }

  if (!title.value.trim()) {
    error.value = 'Title is required.'
    return
  }

  if (sectionPayload.length === 0) {
    error.value = 'Add at least one task.'
    return
  }

  const defaultSubtitle = `${periodLabel(period.value)} checklist`

  const resolvedModuleChip = props.moduleChip || props.moduleLabel

  const nextCard = isEditMode.value
    ? {
        ...(previousCard ?? {}),
        id: previousCard?.id ?? randomId('checklist'),
        period: period.value,
        title: title.value.trim(),
        subtitle: subtitle.value.trim(),
        moduleChip: previousCard?.moduleChip ?? resolvedModuleChip,
        sections: sectionPayload
      }
    : {
        id: randomId('checklist'),
        period: period.value,
        title: title.value.trim(),
        subtitle: subtitle.value.trim() || defaultSubtitle,
        statusLabel: '',
        statusTone: 'muted',
        progress: null,
        featured: false,
        moduleChip: resolvedModuleChip,
        sections: sectionPayload
      }

  // Backend TODO:
  // Call POST/PUT /api/checklists here (or from the parent) and use the returned ids.
  //
  // IMPORTANT for temperature measurements:
  // - The backend must treat `task.id` as a stable identifier.
  // - Temperature measurements should reference { module, checklistId, taskId }.
  // - Renaming/reordering tasks must NOT create new task ids unless the task is actually deleted.
  // See: `frontend/restaurantapp-frontend/src/api/checklists.js`
  if (isEditMode.value) emit('updated', nextCard)
  else emit('created', nextCard)
  handleClose()
}
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  z-index: 60;
  background: rgba(12, 12, 24, 0.55);
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 64px 16px 32px;
  overflow: auto;
}

.modal {
  width: min(820px, 100%);
  max-height: calc(100vh - 48px);
  display: flex;
  flex-direction: column;
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
  flex: 1 1 auto;
  padding: 18px 20px 20px;
  overflow: auto;
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

.input--compact {
  padding: 10px 12px;
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

.tasks {
  display: grid;
  gap: 10px;
}

.tasks-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.tasks-title {
  font-size: 13px;
  font-weight: var(--font-weight-bold);
  color: var(--color-text-muted);
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.tasks-actions {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.add-task-menu {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tasks-empty {
  padding: 12px 14px;
  border-radius: 14px;
  border: 1px dashed var(--color-border-subtle);
  color: var(--color-text-muted);
  font-weight: var(--font-weight-bold);
  background: rgba(255, 255, 255, 0.6);
}

.task-editor {
  padding: 12px 12px 10px;
  border-radius: 16px;
  border: 1px solid var(--color-border-subtle);
  background: rgba(255, 255, 255, 0.75);
}

.task-editor-top {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.task-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.field--inline {
  margin: 0;
}

.ghost-button--compact,
.danger-ghost--compact {
  padding: 10px 12px;
}

.danger-ghost--compact {
  border-color: rgba(190, 40, 40, 0.22);
}

.task-grid .full {
  grid-column: 1 / -1;
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
  .overlay {
    padding-top: 24px;
    padding-bottom: 16px;
  }

  .grid {
    grid-template-columns: 1fr;
  }

  .task-grid {
    grid-template-columns: 1fr;
  }
}
</style>
