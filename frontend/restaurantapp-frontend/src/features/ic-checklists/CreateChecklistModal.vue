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

        <section class="task-pool">
          <div class="task-pool-header">
            <div>
              <h3>Task pool</h3>
              <p>Select existing tasks for this checklist. Tasks are grouped and ordered automatically.</p>
            </div>
          </div>

          <div class="task-pool-linkbar">
            <span>Need to add or remove company tasks first?</span>
            <button type="button" class="link-button" @click="emit('manage-tasks')">Open task pool manager</button>
          </div>

          <div v-if="loadingTasks" class="tasks-state">Loading tasks...</div>
          <div v-else-if="groupedTasks.length === 0" class="tasks-state">
            No tasks in the pool yet. Create one first.
          </div>
          <div v-else class="task-groups">
            <section v-for="group in groupedTasks" :key="group.sectionType" class="task-group">
              <div class="task-group-title">{{ group.title }}</div>
              <label v-for="task in group.items" :key="task.id" class="task-option">
                <input v-model="selectedTaskIds" type="checkbox" :value="task.id" />
                <div class="task-option-copy">
                  <span class="task-option-title">{{ task.title }}</span>
                  <span v-if="taskSummary(task)" class="task-option-meta">{{ taskSummary(task) }}</span>
                </div>
              </label>
            </section>
          </div>
        </section>

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
import { fetchTasks } from '../../api/tasks'
import { formatSectionType } from './taskTemplateOptions'

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
  module: {
    type: String,
    required: true
  },
  moduleLabel: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:open', 'close', 'created', 'updated', 'manage-tasks'])

const title = ref('')
const subtitle = ref('')
const period = ref('daily')
const error = ref('')
const poolTasks = ref([])
const selectedTaskIds = ref([])
const loadingTasks = ref(false)

const isEditMode = computed(() => props.mode === 'edit')
const modalTitle = computed(() => (isEditMode.value ? 'Edit checklist' : 'Create checklist'))
const modalSubtitle = computed(() =>
  isEditMode.value
    ? 'Choose which reusable tasks belong to this checklist.'
    : 'Build a checklist by selecting tasks from the shared pool.'
)
const submitLabel = computed(() => (isEditMode.value ? 'Save changes' : 'Create'))
const dialogAriaLabel = computed(() => (isEditMode.value ? 'Edit checklist' : 'Create checklist'))

const groupedTasks = computed(() => {
  const groups = new Map()
  for (const task of poolTasks.value) {
    const key = task.sectionType || 'UNSORTED'
    if (!groups.has(key)) {
      groups.set(key, {
        sectionType: key,
        title: formatSectionType(key),
        items: []
      })
    }
    groups.get(key).items.push(task)
  }

  return Array.from(groups.values())
    .map((group) => ({
      ...group,
      items: [...group.items].sort((a, b) => String(a.title).localeCompare(String(b.title)))
    }))
    .sort((a, b) => a.title.localeCompare(b.title))
})

function resetForm() {
  title.value = ''
  subtitle.value = ''
  period.value = 'daily'
  error.value = ''
  selectedTaskIds.value = []
}

function taskSummary(task) {
  const fragments = []
  if (task.unit) fragments.push(`Unit: ${task.unit}`)
  if (task.targetMin != null || task.targetMax != null) {
    fragments.push(`Range: ${task.targetMin ?? '...'} to ${task.targetMax ?? '...'}`)
  }
  return fragments.join(' · ')
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

  const ids = []
  const sections = Array.isArray(card.sections) ? card.sections : []
  sections.forEach((section) => {
    const items = Array.isArray(section.items) ? section.items : []
    items.forEach((item) => {
      if (item?.templateId != null) ids.push(item.templateId)
    })
  })
  selectedTaskIds.value = [...new Set(ids)]
}

async function loadTasks() {
  loadingTasks.value = true
  try {
    const data = await fetchTasks({ module: props.module })
    poolTasks.value = Array.isArray(data) ? data : []
  } catch (err) {
    console.error('Failed to fetch task pool', err)
    error.value = 'Could not load task pool.'
  } finally {
    loadingTasks.value = false
  }
}

watch(
  () => props.open,
  async (isOpen) => {
    if (!isOpen) return
    resetForm()
    if (isEditMode.value) initFromCard(props.initialCard)
    await loadTasks()
  }
)

watch(
  () => props.initialCard,
  (nextCard) => {
    if (props.open && isEditMode.value) initFromCard(nextCard)
  }
)

function handleClose() {
  emit('update:open', false)
  emit('close')
}

function handleSubmit() {
  error.value = ''

  if (!title.value.trim()) {
    error.value = 'Title is required.'
    return
  }
  if (selectedTaskIds.value.length === 0) {
    error.value = 'Select at least one task from the pool.'
    return
  }

  const payload = {
    id: isEditMode.value ? props.initialCard?.id ?? null : null,
    period: period.value,
    title: title.value.trim(),
    subtitle: subtitle.value.trim(),
    taskTemplateIds: [...selectedTaskIds.value]
  }

  if (isEditMode.value) emit('updated', payload)
  else emit('created', payload)
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
  width: 38px;
  height: 38px;
  border-radius: 999px;
  font-size: 28px;
  line-height: 1;
  cursor: pointer;
}

.modal-body {
  padding: 20px;
  overflow: auto;
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

.task-pool {
  margin-top: 22px;
  padding-top: 18px;
  border-top: 1px solid var(--color-border-subtle);
}

.task-pool-header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 12px;
}

.task-pool-header h3 {
  margin: 0;
  font-size: 18px;
}

.task-pool-header p {
  margin: 6px 0 0;
  color: var(--color-text-muted);
}

.task-pool-linkbar {
  margin-bottom: 16px;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(246, 247, 252, 0.85);
  border: 1px solid rgba(222, 226, 239, 0.9);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: var(--color-text-muted);
  font-size: 14px;
}

.link-button {
  border: 0;
  background: transparent;
  color: var(--color-text-primary);
  font-weight: 700;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.tasks-state {
  padding: 18px 0;
  color: var(--color-text-muted);
}

.task-groups {
  display: grid;
  gap: 16px;
}

.task-group {
  padding: 16px;
  border-radius: 18px;
  background: rgba(246, 247, 252, 0.85);
  border: 1px solid rgba(222, 226, 239, 0.9);
}

.task-group-title {
  margin-bottom: 12px;
  font-weight: 800;
  color: var(--color-text-primary);
}

.task-option {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 12px;
  align-items: flex-start;
  padding: 10px 0;
}

.task-option + .task-option {
  border-top: 1px solid rgba(210, 213, 230, 0.65);
}

.task-option-copy {
  display: grid;
  gap: 4px;
}

.task-option-title {
  font-weight: 700;
  color: var(--color-text-primary);
}

.task-option-meta {
  font-size: 13px;
  color: var(--color-text-muted);
}

.error {
  margin-top: 14px;
  color: #a11d2d;
}

.modal-footer {
  margin-top: 24px;
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

  .task-pool-header {
    flex-direction: column;
  }

  .task-pool-linkbar {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
