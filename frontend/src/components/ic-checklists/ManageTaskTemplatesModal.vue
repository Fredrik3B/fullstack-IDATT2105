<template>
  <div v-if="open" class="overlay" role="dialog" aria-modal="true" aria-label="Manage task pool">
    <div class="modal">
      <header class="modal-header">
        <div>
          <div class="eyebrow">{{ moduleLabel }}</div>
          <h2>Task pool</h2>
          <p class="subtitle">
            Manage the shared task library used across checklists in this module.
          </p>
        </div>
        <button type="button" class="icon-button" aria-label="Close" @click="close">&times;</button>
      </header>

      <div class="modal-body">
        <section class="toolbar-panel">
          <div class="toolbar-copy">
            <span class="panel-label">Shared tasks</span>
            <p class="panel-copy">
              Keep reusable tasks tidy here so checklist editors stay focused on selection, not
              setup.
            </p>
          </div>
          <div class="toolbar-actions">
            <button type="button" class="secondary" @click="isZoneManagerOpen = true">
              Fridge items
            </button>
            <button type="button" class="primary" @click="isCreateOpen = true">New task</button>
          </div>
        </section>

        <p v-if="error" class="error" role="alert">{{ error }}</p>
        <div v-if="loading" class="state">Loading tasks...</div>
        <div v-else-if="groupedTasks.length === 0" class="state">
          <p class="state-title">No tasks yet</p>
          <p class="state-copy">Create the first shared task for this module.</p>
        </div>
        <div v-else class="groups">
          <section v-for="group in groupedTasks" :key="group.sectionType" class="group">
            <div class="group-header">
              <div class="group-title">{{ group.title }}</div>
              <div class="group-count">{{ group.items.length }} tasks</div>
            </div>
            <article v-for="task in group.items" :key="task.id" class="task-row">
              <div class="task-copy">
                <div class="task-title">{{ task.title }}</div>
                <div v-if="taskSummary(task)" class="task-meta">{{ taskSummary(task) }}</div>
              </div>
              <div class="task-actions">
                <button
                  type="button"
                  class="secondary"
                  :class="{ loading: String(savingTaskId ?? '') === String(task.id) }"
                  :disabled="
                    Boolean(deletingTaskId) || String(savingTaskId ?? '') === String(task.id)
                  "
                  @click="openEditTask(task)"
                >
                  {{ String(savingTaskId ?? '') === String(task.id) ? 'Saving...' : 'Edit' }}
                </button>
                <button
                  type="button"
                  class="danger"
                  :class="{ loading: deletingTaskId === task.id }"
                  :disabled="deletingTaskId === task.id"
                  @click="requestRemoveTask(task)"
                >
                  {{ deletingTaskId === task.id ? 'Deleting...' : 'Delete' }}
                </button>
              </div>
            </article>
          </section>
        </div>
      </div>
    </div>

    <CreateTaskTemplateModal
      v-model:open="isCreateOpen"
      :module="module"
      :module-label="moduleLabel"
      :zone-refresh-token="zoneRefreshToken"
      @created="handleCreatedTask"
      @manage-zones="isZoneManagerOpen = true"
    />

    <CreateTaskTemplateModal
      v-model:open="isEditOpen"
      mode="edit"
      :initial-task="selectedTask"
      :module="module"
      :module-label="moduleLabel"
      :zone-refresh-token="zoneRefreshToken"
      @updated="handleUpdatedTask"
      @manage-zones="isZoneManagerOpen = true"
    />

    <ManageTemperatureZonesModal
      v-model:open="isZoneManagerOpen"
      :module="module"
      :module-label="moduleLabel"
      @changed="handleZonesChanged"
    />

    <SharedConfirmDialog
      v-model:open="deleteDialog.open"
      kicker="Task pool"
      title="Delete shared task?"
      :message="deleteDialogMessage"
      detail="Any checklist using this task will lose it as well, so this is best for tasks you no longer want anyone to reuse."
      confirm-label="Delete task"
      :is-processing="Boolean(deletingTaskId)"
      tone="danger"
      @cancel="closeDeleteDialog"
      @confirm="confirmRemoveTask"
    />

  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useToast } from '@/composables/useToast'
import { createTask, deleteTask, fetchTasks, updateTask } from '@/api/tasks'
import CreateTaskTemplateModal from './CreateTaskTemplateModal.vue'
import ManageTemperatureZonesModal from './ManageTemperatureZonesModal.vue'
import { formatTemperatureZoneType } from '@/composables/ic-checklists/temperatureZoneOptions'
import SharedConfirmDialog from './SharedConfirmDialog.vue'
import { formatSectionType } from '@/composables/ic-checklists/taskTemplateOptions'

const props = defineProps({
  open: { type: Boolean, default: false },
  module: { type: String, required: true },
  moduleLabel: { type: String, default: '' },
})

const emit = defineEmits(['update:open', 'close', 'changed'])

const toast = useToast()
const tasks = ref([])
const loading = ref(false)
const error = ref('')
const isCreateOpen = ref(false)
const isEditOpen = ref(false)
const isZoneManagerOpen = ref(false)
const zoneRefreshToken = ref(0)
const selectedTask = ref(null)
const deletingTaskId = ref(null)
const savingTaskId = ref(null)
const deleteDialog = ref({
  open: false,
  task: null,
})
const deleteDialogMessage = computed(
  () => `"${deleteDialog.value.task?.title || 'This task'}" will be removed from the task pool.`,
)

const groupedTasks = computed(() => {
  const groups = new Map()
  for (const task of tasks.value) {
    const key = task.sectionType || 'UNSORTED'
    if (!groups.has(key)) {
      groups.set(key, { sectionType: key, title: formatSectionType(key), items: [] })
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

function taskSummary(task) {
  const fragments = []
  if (task.meta) {
    fragments.push(task.meta)
  }
  if (task.temperatureZoneName) {
    fragments.push(
      `${task.temperatureZoneName} · ${formatTemperatureZoneType(task.temperatureZoneType)}`,
    )
  }
  if (task.targetMin != null || task.targetMax != null) {
    fragments.push(`Celsius range: ${task.targetMin ?? '...'} to ${task.targetMax ?? '...'}`)
  }
  return fragments.join(' - ')
}

async function loadTasks() {
  loading.value = true
  error.value = ''
  try {
    const data = await fetchTasks({ module: props.module })
    tasks.value = Array.isArray(data) ? data : []
  } catch (err) {
    console.error('Failed to load task pool', err)
    error.value = 'Could not load task pool.'
  } finally {
    loading.value = false
  }
}

watch(
  () => props.open,
  async (isOpen) => {
    if (isOpen) await loadTasks()
  },
)

function close() {
  closeDeleteDialog()
  emit('update:open', false)
  emit('close')
}

function sortTasks(items) {
  return [...items].sort(
    (a, b) =>
      formatSectionType(a.sectionType).localeCompare(formatSectionType(b.sectionType)) ||
      String(a.title).localeCompare(String(b.title)),
  )
}

async function handleCreatedTask(payload) {
  try {
    const created = await createTask(payload)
    tasks.value = sortTasks([...tasks.value, created])
    emit('changed')
    toast.success(`Added "${created.title}" to the task pool.`)
  } catch (err) {
    console.error('Failed to create task', err)
    error.value = 'Could not create task.'
    toast.error(error.value)
  }
}

function openEditTask(task) {
  selectedTask.value = { ...task }
  isEditOpen.value = true
}

async function handleUpdatedTask(payload) {
  if (!payload?.id) {
    error.value = 'Could not update task.'
    return
  }
  if (String(savingTaskId.value ?? '') === String(payload.id)) return

  savingTaskId.value = payload.id

  try {
    const updated = await updateTask({
      taskId: payload.id,
      module: payload.module,
      title: payload.title,
      meta: payload.meta,
      sectionType: payload.sectionType,
      temperatureZoneId: payload.temperatureZoneId,
      targetMin: payload.targetMin,
      targetMax: payload.targetMax,
    })
    tasks.value = sortTasks(tasks.value.map((task) => (task.id === updated.id ? updated : task)))
    selectedTask.value = updated
    emit('changed')
    toast.success(`Updated "${updated.title}".`)
  } catch (err) {
    console.error('Failed to update task', err)
    error.value = err?.response?.data?.message ?? 'Could not update task.'
    toast.error(error.value)
  } finally {
    savingTaskId.value = null
  }
}

function requestRemoveTask(task) {
  deleteDialog.value = {
    open: true,
    task,
  }
}

function closeDeleteDialog() {
  deleteDialog.value = {
    open: false,
    task: null,
  }
}

async function confirmRemoveTask() {
  const task = deleteDialog.value.task
  if (!task?.id) return

  deletingTaskId.value = task.id
  error.value = ''
  try {
    await deleteTask(task.id)
    tasks.value = tasks.value.filter((entry) => entry.id !== task.id)
    closeDeleteDialog()
    toast.success(`Deleted "${task.title}".`)
    emit('changed')
  } catch (err) {
    console.error('Failed to delete task', err)
    error.value = err?.response?.data?.message ?? 'Could not delete task.'
    toast.error(error.value)
  } finally {
    deletingTaskId.value = null
  }
}

async function handleZonesChanged() {
  zoneRefreshToken.value += 1
  await loadTasks()
}
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  z-index: 75;
  background: rgba(12, 12, 24, 0.55);
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 64px 16px 32px;
  overflow: auto;
}

.modal {
  width: min(900px, 100%);
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
  display: grid;
  gap: var(--space-4);
}

.toolbar-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.toolbar-copy {
  min-width: 0;
}

.panel-label {
  display: block;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.panel-copy {
  margin: 6px 0 0;
  color: var(--color-text-secondary);
}

.primary,
.secondary,
.danger {
  min-height: 40px;
  padding: 0 14px;
  border: 1px solid transparent;
  border-radius: var(--radius-md);
  font: inherit;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  cursor: pointer;
}

.primary:disabled,
.secondary:disabled,
.danger:disabled,
.primary.loading,
.secondary.loading,
.danger.loading {
  opacity: 0.72;
  cursor: wait;
  filter: saturate(0.85);
}

.primary {
  background: var(--color-dark-secondary);
  color: white;
}

.secondary {
  background: var(--color-bg-primary);
  border-color: var(--color-border);
  color: var(--color-text-primary);
}

.danger {
  background: rgba(161, 29, 45, 0.12);
  color: #a11d2d;
}

.state,
.error {
  color: var(--color-text-muted);
}

.state {
  padding: var(--space-8) var(--space-4);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  background: var(--color-bg-secondary);
  text-align: center;
}

.state-title {
  margin: 0;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.state-copy {
  margin: var(--space-2) 0 0;
}

.error {
  color: #a11d2d;
}

.groups {
  display: grid;
  gap: 12px;
}

.group {
  padding: 0;
  border-radius: var(--radius-lg);
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  overflow: hidden;
}

.group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: 12px 14px;
  background: var(--color-bg-primary);
  border-bottom: 1px solid var(--color-border);
}

.group-title {
  font-weight: 800;
  color: var(--color-text-primary);
  font-size: var(--font-size-md);
}

.group-count {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.task-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 12px 14px;
  background: var(--color-bg-secondary);
}

.task-row + .task-row {
  border-top: 1px solid var(--color-border);
}

.task-copy {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.task-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.task-title {
  font-weight: 700;
  color: var(--color-text-primary);
}

.task-meta {
  font-size: 13px;
  color: var(--color-text-muted);
}

@media (max-width: 720px) {
  .toolbar-panel,
  .task-row,
  .task-actions,
  .group-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .toolbar-actions {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
