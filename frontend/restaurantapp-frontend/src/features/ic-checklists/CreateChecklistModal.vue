<template>
  <div v-if="open" class="overlay" role="dialog" aria-modal="true" :aria-label="dialogAriaLabel">
    <div class="modal">
      <header class="modal-header">
        <div>
          <div class="eyebrow">{{ moduleLabel }}</div>
          <h2>{{ modalTitle }}</h2>
          <p class="subtitle">{{ modalSubtitle }}</p>
        </div>
        <button type="button" class="icon-button" aria-label="Close" @click="handleClose">
          &times;
        </button>
      </header>

      <form class="modal-body" @submit.prevent="handleSubmit">
        <section class="editor-shell">
          <aside class="editor-sidebar">
            <button
              v-for="step in steps"
              :key="step.key"
              type="button"
              class="step-button"
              :class="{ active: activeStep === step.key }"
              @click="activeStep = step.key"
            >
              <span class="step-name">{{ step.label }}</span>
              <span class="step-hint">{{ step.hint }}</span>
            </button>

            <div class="progress-card">
              <div class="progress-title">Checklist setup</div>
              <div class="progress-meta">{{ selectedTaskIds.length }} tasks selected</div>
              <p>
                {{
                  displayedOnWorkbench
                    ? 'Opens on the workbench right away.'
                    : 'Starts in the library only.'
                }}
                {{
                  recurring
                    ? ' The next run is prepared automatically.'
                    : ' Staff will reopen it in the next real period.'
                }}
              </p>
            </div>
          </aside>

          <div class="editor-content">
            <section v-show="activeStep === 'details'" class="section-card">
              <div class="section-heading">
                <h3>Details</h3>
                <p>Name the checklist and decide how it should appear for staff.</p>
              </div>

              <div class="grid">
                <label class="field">
                  <span class="label">Title</span>
                  <input
                    v-model.trim="title"
                    class="input"
                    type="text"
                    placeholder="e.g. Opening checklist"
                    required
                  />
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
                  <input
                    v-model.trim="subtitle"
                    class="input"
                    type="text"
                    placeholder="e.g. Run before opening to guests"
                  />
                </label>
              </div>

              <div class="settings-grid">
                <button
                  type="button"
                  class="setting-row"
                  :class="{ active: displayedOnWorkbench }"
                  @click="displayedOnWorkbench = !displayedOnWorkbench"
                >
                  <span class="setting-copy">
                    <span class="setting-title">Start on workbench</span>
                    <span class="setting-text">
                      {{
                        displayedOnWorkbench
                          ? 'Staff can work on it immediately.'
                          : 'Keep it in the library until someone opens it.'
                      }}
                    </span>
                  </span>
                  <span class="setting-value">{{
                    displayedOnWorkbench ? 'Yes' : 'Library only'
                  }}</span>
                </button>

                <button
                  type="button"
                  class="setting-row"
                  :class="{ active: recurring }"
                  @click="recurring = !recurring"
                >
                  <span class="setting-copy">
                    <span class="setting-title">Prepare the next run automatically</span>
                    <span class="setting-text">
                      {{
                        recurring
                          ? 'A fresh checklist is prepared right after submit.'
                          : 'Wait for the next real day, week, or month before starting again.'
                      }}
                    </span>
                  </span>
                  <span class="setting-value">{{ recurring ? 'Auto-start' : 'Wait' }}</span>
                </button>
              </div>
            </section>

            <section v-show="activeStep === 'tasks'" class="section-card">
              <div class="section-heading">
                <h3>Tasks</h3>
                <p>
                  Choose reusable tasks for this checklist, or add a new one without leaving the
                  editor.
                </p>
              </div>

              <div class="task-toolbar">
                <div class="task-toolbar__copy">
                  <strong>{{ selectedTaskIds.length }} selected</strong>
                  <span>Grouped by task type so it is easier to scan.</span>
                </div>
                <div class="task-toolbar__actions">
                  <button type="button" class="secondary" @click="toggleQuickCreate">
                    {{ isQuickCreateOpen ? 'Hide quick add' : 'Quick add task' }}
                  </button>
                  <button
                    v-if="canManageTasks"
                    type="button"
                    class="ghost"
                    @click="emit('manage-tasks')"
                  >
                    Open full task pool
                  </button>
                </div>
              </div>

              <section v-if="isQuickCreateOpen" class="quick-create">
                <div class="quick-create__heading">
                  <h4>Add a shared task</h4>
                  <p>
                    This creates a reusable task in the shared pool and selects it for this
                    checklist.
                  </p>
                </div>

                <div class="grid">
                  <label class="field full">
                    <span class="label">Task title</span>
                    <input
                      v-model.trim="quickTask.title"
                      class="input"
                      type="text"
                      placeholder="e.g. Check freezer 1"
                      :disabled="creatingTask"
                    />
                  </label>

                  <label class="field full">
                    <span class="label">Meta (optional)</span>
                    <input
                      v-model.trim="quickTask.meta"
                      class="input"
                      type="text"
                      maxlength="255"
                      :disabled="creatingTask"
                    />
                  </label>

                  <label class="field">
                    <span class="label">Section</span>
                    <select v-model="quickTask.sectionType" class="input" :disabled="creatingTask">
                      <option disabled value="">Choose section</option>
                      <option v-for="option in SECTION_TYPE_OPTIONS" :key="option" :value="option">
                        {{ formatSectionType(option) }}
                      </option>
                    </select>
                  </label>

                  <template v-if="quickTask.sectionType === 'TEMPERATURE_CONTROL'">
                    <label class="field">
                      <span class="label">Target min (optional)</span>
                      <input
                        v-model.number="quickTask.targetMin"
                        class="input"
                        type="number"
                        step="0.1"
                        :disabled="creatingTask"
                      />
                    </label>

                    <label class="field">
                      <span class="label">Target max (optional)</span>
                      <input
                        v-model.number="quickTask.targetMax"
                        class="input"
                        type="number"
                        step="0.1"
                        :disabled="creatingTask"
                      />
                    </label>
                  </template>
                </div>

                <p v-if="quickTaskError" class="error" role="alert">{{ quickTaskError }}</p>

                <div class="quick-create__actions">
                  <button
                    type="button"
                    class="secondary"
                    :disabled="creatingTask"
                    @click="resetQuickCreate"
                  >
                    Reset
                  </button>
                  <button
                    type="button"
                    class="primary"
                    :disabled="creatingTask"
                    @click="handleQuickCreateTask"
                  >
                    {{ creatingTask ? 'Adding task...' : 'Add and select task' }}
                  </button>
                </div>
              </section>

              <div v-if="loadingTasks" class="tasks-state">Loading tasks...</div>
              <div v-else-if="groupedTasks.length === 0" class="tasks-state">
                No tasks in the pool yet. Add one above or open the full task pool manager.
              </div>
              <div v-else class="task-groups">
                <section v-for="group in groupedTasks" :key="group.sectionType" class="task-group">
                  <div class="task-group-header">
                    <div class="task-group-title">{{ group.title }}</div>
                    <div class="task-group-count">{{ group.items.length }} tasks</div>
                  </div>
                  <label
                    v-for="task in group.items"
                    :key="task.id"
                    class="task-option"
                    :class="{ selected: selectedTaskIds.includes(task.id) }"
                  >
                    <input v-model="selectedTaskIds" type="checkbox" :value="task.id" />
                    <div class="task-option-copy">
                      <span class="task-option-title">{{ task.title }}</span>
                      <span v-if="taskSummary(task)" class="task-option-meta">{{
                        taskSummary(task)
                      }}</span>
                    </div>
                  </label>
                </section>
              </div>
            </section>
          </div>
        </section>

        <p v-if="error" class="error" role="alert">{{ error }}</p>

        <footer class="modal-footer">
          <div class="footer-actions">
            <button
              v-if="activeStep === 'tasks'"
              type="button"
              class="secondary"
              :disabled="isBusy"
              @click="activeStep = 'details'"
            >
              Back to details
            </button>
            <button
              v-else
              type="button"
              class="secondary"
              :disabled="isBusy"
              @click="activeStep = 'tasks'"
            >
              Continue to tasks
            </button>
            <button type="button" class="secondary" :disabled="isBusy" @click="handleClose">
              Cancel
            </button>
            <button
              type="submit"
              class="primary"
              :class="{ loading: savePending }"
              :disabled="isBusy"
            >
              {{ submitButtonLabel }}
            </button>
          </div>
        </footer>

        <section v-if="isEditMode" class="danger-zone">
          <div class="danger-zone__copy">
            <h3>Danger zone</h3>
            <p>
              Use these actions when this checklist should be removed from daily use or deleted
              completely.
            </p>
          </div>
          <div class="danger-zone__actions">
            <button
              v-if="displayedOnWorkbench"
              type="button"
              class="ghost"
              :class="{ loading: removePending }"
              :disabled="isBusy"
              @click="openRemoveFromWorkbenchConfirm"
            >
              {{ removePending ? 'Removing...' : 'Remove from workbench' }}
            </button>
            <button
              type="button"
              class="danger"
              :class="{ loading: deletePending }"
              :disabled="isBusy"
              @click="emitDelete"
            >
              {{ deletePending ? 'Deleting...' : 'Delete checklist' }}
            </button>
          </div>
        </section>
      </form>
    </div>

    <SharedConfirmDialog
      v-model:open="removeWorkbenchConfirm.open"
      :kicker="moduleLabel"
      title="Remove checklist from workbench?"
      :message="removeWorkbenchMessage"
      detail="The checklist stays in the library, but the current active run and any measurements attached to that run are removed."
      confirm-label="Remove from workbench"
      :is-processing="removePending"
      tone="warning"
      @cancel="closeRemoveFromWorkbenchConfirm"
      @confirm="confirmRemoveFromWorkbench"
    />
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useToast } from '@/composables/useToast'
import { createTask, fetchTasks } from '../../api/tasks'
import SharedConfirmDialog from './SharedConfirmDialog.vue'
import { SECTION_TYPE_OPTIONS, formatSectionType } from './taskTemplateOptions'

const props = defineProps({
  open: { type: Boolean, default: false },
  canManageChecklists: { type: Boolean, default: false },
  mode: {
    type: String,
    default: 'create',
    validator: (value) => ['create', 'edit'].includes(value),
  },
  initialCard: { type: Object, default: null },
  module: { type: String, required: true },
  moduleLabel: { type: String, default: '' },
  canManageTasks: { type: Boolean, default: false },
  savePending: { type: Boolean, default: false },
  deletePending: { type: Boolean, default: false },
  removePending: { type: Boolean, default: false },
})

const emit = defineEmits([
  'update:open',
  'close',
  'created',
  'updated',
  'delete',
  'manage-tasks',
  'remove-from-workbench',
])

const toast = useToast()
const title = ref('')
const subtitle = ref('')
const period = ref('daily')
const recurring = ref(true)
const displayedOnWorkbench = ref(true)
const error = ref('')
const poolTasks = ref([])
const selectedTaskIds = ref([])
const loadingTasks = ref(false)
const creatingTask = ref(false)
const activeStep = ref('details')
const isQuickCreateOpen = ref(false)
const quickTaskError = ref('')
const removeWorkbenchConfirm = ref({
  open: false,
  title: '',
})

const quickTask = ref(createEmptyQuickTask())

const steps = [
  { key: 'details', label: 'Details', hint: 'Name, schedule, and placement' },
  { key: 'tasks', label: 'Tasks', hint: 'Choose or add shared tasks' },
]

const isEditMode = computed(() => props.mode === 'edit')
const modalTitle = computed(() => (isEditMode.value ? 'Edit checklist' : 'New checklist'))
const modalSubtitle = computed(() =>
  isEditMode.value
    ? 'Update checklist details and task selection in one place.'
    : 'Build a reusable checklist using the shared task library.',
)
const submitLabel = computed(() => (isEditMode.value ? 'Save changes' : 'Create checklist'))
const submitButtonLabel = computed(() => {
  if (!props.savePending) return submitLabel.value
  return isEditMode.value ? 'Saving...' : 'Creating...'
})
const dialogAriaLabel = computed(() => (isEditMode.value ? 'Edit checklist' : 'Create checklist'))
const removeWorkbenchMessage = computed(
  () => `"${removeWorkbenchConfirm.value.title}" will be removed from the workbench.`,
)
const isBusy = computed(() => props.savePending || props.deletePending || props.removePending)

const groupedTasks = computed(() => {
  const groups = new Map()
  for (const task of poolTasks.value) {
    const key = task.sectionType || 'UNSORTED'
    if (!groups.has(key)) {
      groups.set(key, {
        sectionType: key,
        title: formatSectionType(key),
        items: [],
      })
    }
    groups.get(key).items.push(task)
  }

  return Array.from(groups.values())
    .map((group) => ({
      ...group,
      items: [...group.items].sort((a, b) => String(a.title).localeCompare(String(b.title))),
    }))
    .sort((a, b) => a.title.localeCompare(b.title))
})

function createEmptyQuickTask() {
  return {
    title: '',
    meta: '',
    sectionType: '',
    targetMin: null,
    targetMax: null,
  }
}

function resetForm() {
  title.value = ''
  subtitle.value = ''
  period.value = 'daily'
  recurring.value = true
  displayedOnWorkbench.value = true
  error.value = ''
  selectedTaskIds.value = []
  activeStep.value = 'details'
  isQuickCreateOpen.value = false
  resetQuickCreate()
}

function resetQuickCreate() {
  quickTask.value = createEmptyQuickTask()
  quickTaskError.value = ''
  creatingTask.value = false
}

function toggleQuickCreate() {
  isQuickCreateOpen.value = !isQuickCreateOpen.value
  if (!isQuickCreateOpen.value) resetQuickCreate()
}

function taskSummary(task) {
  const fragments = []
  if (task.meta) fragments.push(task.meta)
  if (task.targetMin != null || task.targetMax != null) {
    fragments.push(`Range: ${task.targetMin ?? '...'} to ${task.targetMax ?? '...'}`)
  }
  return fragments.join(' - ')
}

function initFromCard(card) {
  if (!card) {
    resetForm()
    return
  }
  title.value = String(card.title ?? '').trim()
  subtitle.value = String(card.subtitle ?? '').trim()
  period.value = String(card.period ?? 'daily')
  recurring.value = card.recurring !== false
  displayedOnWorkbench.value = card.displayedOnWorkbench !== false
  error.value = ''
  activeStep.value = 'details'

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
  },
)

watch(
  () => props.initialCard,
  (nextCard) => {
    if (props.open && isEditMode.value) initFromCard(nextCard)
  },
)

watch(
  () => quickTask.value.sectionType,
  (sectionType) => {
    if (sectionType !== 'TEMPERATURE_CONTROL') {
      quickTask.value.targetMin = null
      quickTask.value.targetMax = null
    }
  },
)

function handleClose() {
  if (isBusy.value) return
  closeRemoveFromWorkbenchConfirm()
  emit('update:open', false)
  emit('close')
}

async function handleQuickCreateTask() {
  quickTaskError.value = ''

  if (!quickTask.value.title.trim()) {
    quickTaskError.value = 'Task title is required.'
    return
  }
  if (!quickTask.value.sectionType) {
    quickTaskError.value = 'Section type is required.'
    return
  }
  if (
    Number.isFinite(Number(quickTask.value.targetMin)) &&
    Number.isFinite(Number(quickTask.value.targetMax)) &&
    Number(quickTask.value.targetMin) > Number(quickTask.value.targetMax)
  ) {
    quickTaskError.value = 'Target min cannot be greater than target max.'
    return
  }

  creatingTask.value = true
  try {
    const created = await createTask({
      module: props.module,
      title: quickTask.value.title.trim(),
      meta: quickTask.value.meta.trim(),
      sectionType: quickTask.value.sectionType,
      targetMin:
        quickTask.value.sectionType === 'TEMPERATURE_CONTROL' &&
        Number.isFinite(Number(quickTask.value.targetMin))
          ? Number(quickTask.value.targetMin)
          : null,
      targetMax:
        quickTask.value.sectionType === 'TEMPERATURE_CONTROL' &&
        Number.isFinite(Number(quickTask.value.targetMax))
          ? Number(quickTask.value.targetMax)
          : null,
    })

    poolTasks.value = [...poolTasks.value, created]
    selectedTaskIds.value = [...new Set([...selectedTaskIds.value, created.id])]
    resetQuickCreate()
    isQuickCreateOpen.value = false
    toast.success(`Added "${created.title}" to the shared task pool.`)
  } catch (err) {
    console.error('Failed to create task', err)
    quickTaskError.value = err?.response?.data?.message ?? 'Could not create task.'
  } finally {
    creatingTask.value = false
  }
}

async function handleSubmit() {
  if (isBusy.value) return
  error.value = ''

  if (!title.value.trim()) {
    error.value = 'Title is required.'
    activeStep.value = 'details'
    return
  }
  if (selectedTaskIds.value.length === 0) {
    error.value = 'Select at least one task from the pool.'
    activeStep.value = 'tasks'
    return
  }

  const payload = {
    id: isEditMode.value ? (props.initialCard?.id ?? null) : null,
    period: period.value,
    title: title.value.trim(),
    subtitle: subtitle.value.trim(),
    recurring: recurring.value,
    displayedOnWorkbench: displayedOnWorkbench.value,
    taskTemplateIds: [...selectedTaskIds.value],
  }

  if (isEditMode.value) {
    emit('updated', payload)
  } else {
    emit('created', payload)
  }
}

function emitDelete() {
  emit('delete', {
    id: props.initialCard?.id ?? null,
    title: title.value.trim() || props.initialCard?.title || 'this checklist',
  })
}

function emitRemoveFromWorkbench() {
  emit('remove-from-workbench', {
    id: props.initialCard?.id ?? null,
    title: title.value.trim() || props.initialCard?.title || 'this checklist',
  })
}

function openRemoveFromWorkbenchConfirm() {
  removeWorkbenchConfirm.value = {
    open: true,
    title: title.value.trim() || props.initialCard?.title || 'this checklist',
  }
}

function closeRemoveFromWorkbenchConfirm() {
  removeWorkbenchConfirm.value = {
    open: false,
    title: '',
  }
}

function confirmRemoveFromWorkbench() {
  closeRemoveFromWorkbenchConfirm()
  emitRemoveFromWorkbench()
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
  width: min(980px, 100%);
  max-height: calc(100vh - 48px);
  display: flex;
  flex-direction: column;
  border-radius: var(--radius-xl);
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  box-shadow: 0 30px 90px rgba(0, 0, 0, 0.25);
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 24px 14px;
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
  border: 1px solid var(--color-border);
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  font-size: 24px;
  line-height: 1;
  cursor: pointer;
}

.modal-body {
  padding: 24px;
  overflow: auto;
  display: grid;
  gap: var(--space-4);
}

.editor-shell {
  display: grid;
  grid-template-columns: minmax(220px, 260px) minmax(0, 1fr);
  gap: var(--space-4);
}

.editor-sidebar {
  display: grid;
  align-content: start;
  gap: var(--space-3);
}

.step-button,
.progress-card {
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
}

.step-button {
  display: grid;
  gap: 4px;
  padding: var(--space-4);
  text-align: left;
  background: var(--color-bg-secondary);
  cursor: pointer;
}

.step-button.active {
  border-color: var(--color-dark-secondary);
  background: #eef3fb;
}

.step-name {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.step-hint {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.progress-card {
  padding: var(--space-4);
  background: var(--color-bg-primary);
}

.progress-title {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-secondary);
}

.progress-meta {
  margin-top: 4px;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.progress-card p {
  margin: 10px 0 0;
  font-size: var(--font-size-sm);
  line-height: var(--line-height-normal);
  color: var(--color-text-muted);
}

.editor-content {
  display: grid;
}

.section-card {
  display: grid;
  gap: var(--space-4);
  padding: var(--space-5);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-bg-secondary);
}

.section-heading h3,
.quick-create__heading h4,
.danger-zone__copy h3 {
  margin: 0;
  font-size: var(--font-size-lg);
  color: var(--color-text-primary);
}

.section-heading p,
.quick-create__heading p,
.danger-zone__copy p {
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
  min-width: 0;
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
}

.settings-grid {
  display: grid;
  gap: var(--space-3);
}

.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  width: 100%;
  padding: var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-bg-primary);
  text-align: left;
  cursor: pointer;
}

.setting-row.active {
  border-color: var(--color-success-border);
  background: #fbfdea;
}

.setting-copy {
  display: grid;
  gap: 4px;
}

.setting-title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.setting-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.setting-value {
  flex-shrink: 0;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-secondary);
}

.task-toolbar,
.quick-create,
.danger-zone {
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  background: var(--color-bg-primary);
}

.task-toolbar {
  display: flex;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-4);
}

.task-toolbar__copy {
  display: grid;
  gap: 4px;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.task-toolbar__copy strong {
  color: var(--color-text-primary);
}

.task-toolbar__actions,
.quick-create__actions,
.footer-actions,
.danger-zone__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.quick-create {
  display: grid;
  gap: var(--space-4);
  padding: var(--space-4);
}

.tasks-state {
  padding: 18px 0;
  color: var(--color-text-muted);
}

.task-groups {
  display: grid;
  gap: 12px;
}

.task-group {
  padding: 0;
  border-radius: var(--radius-md);
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  overflow: hidden;
}

.task-group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: 12px 14px;
  background: var(--color-bg-secondary);
  border-bottom: 1px solid var(--color-border);
}

.task-group-title {
  font-weight: 800;
  color: var(--color-text-primary);
  font-size: var(--font-size-md);
}

.task-group-count {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.task-option {
  display: grid;
  grid-template-columns: 18px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
  padding: 12px 14px;
  background: var(--color-bg-primary);
}

.task-option.selected {
  background: #fbfdea;
}

.task-option + .task-option {
  border-top: 1px solid var(--color-border-subtle);
}

.task-option-copy {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.task-option-title {
  font-weight: 700;
  color: var(--color-text-primary);
  font-size: var(--font-size-md);
}

.task-option-meta {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.task-option input {
  width: 16px;
  height: 16px;
  margin: 0;
}

.error {
  margin: 0;
  color: #a11d2d;
}

.modal-footer {
  padding-top: 4px;
}

.danger-zone {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-5);
  border-color: rgba(161, 29, 45, 0.22);
  background: #fff8f8;
}

.danger-zone__copy {
  min-width: 0;
}

.ghost,
.danger,
.primary,
.secondary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  padding: 0 16px;
  border-radius: var(--radius-md);
  border: 1px solid transparent;
  text-align: center;
  white-space: normal;
  cursor: pointer;
}

.ghost.loading,
.danger.loading,
.primary.loading,
.secondary.loading,
.ghost:disabled,
.danger:disabled,
.primary:disabled,
.secondary:disabled,
.icon-button:disabled {
  opacity: 0.72;
  cursor: wait;
  filter: saturate(0.85);
}

.ghost {
  background: #eef3fb;
  border-color: #cad7ea;
  color: #39516d;
}

.danger {
  background: rgba(255, 232, 232, 0.98);
  color: #a11d2d;
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

@media (max-width: 900px) {
  .editor-shell {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .grid {
    grid-template-columns: 1fr;
  }

  .setting-row,
  .task-toolbar,
  .task-toolbar__actions,
  .footer-actions,
  .danger-zone,
  .danger-zone__actions,
  .task-group-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .ghost,
  .danger,
  .primary,
  .secondary {
    width: 100%;
  }
}
</style>
