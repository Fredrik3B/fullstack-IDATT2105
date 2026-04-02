<template>
  <div v-if="open" class="overlay" role="dialog" aria-modal="true" aria-label="Manage task pool">
    <div class="modal">
      <header class="modal-header">
        <div>
          <div class="eyebrow">{{ moduleLabel }}</div>
          <h2>Task pool</h2>
          <p class="subtitle">Create, review, and delete reusable company tasks for this module.</p>
        </div>
        <button type="button" class="icon-button" aria-label="Close" @click="close">×</button>
      </header>

      <div class="modal-body">
        <div class="toolbar">
          <button type="button" class="primary" @click="isCreateOpen = true">+ New task</button>
        </div>

        <p v-if="error" class="error" role="alert">{{ error }}</p>
        <div v-if="loading" class="state">Loading tasks...</div>
        <div v-else-if="groupedTasks.length === 0" class="state">No tasks yet for this module.</div>
        <div v-else class="groups">
          <section v-for="group in groupedTasks" :key="group.sectionType" class="group">
            <div class="group-title">{{ group.title }}</div>
            <article v-for="task in group.items" :key="task.id" class="task-row">
              <div class="task-copy">
                <div class="task-title">{{ task.title }}</div>
                <div v-if="taskSummary(task)" class="task-meta">{{ taskSummary(task) }}</div>
              </div>
              <button type="button" class="danger" @click="removeTask(task)">Delete</button>
            </article>
          </section>
        </div>
      </div>
    </div>

    <CreateTaskTemplateModal
      v-model:open="isCreateOpen"
      :module="module"
      :module-label="moduleLabel"
      @created="handleCreatedTask"
    />
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { createTask, deleteTask, fetchTasks } from '../../api/tasks'
import CreateTaskTemplateModal from './CreateTaskTemplateModal.vue'
import { formatSectionType } from './taskTemplateOptions'

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

const emit = defineEmits(['update:open', 'close', 'changed'])

const tasks = ref([])
const loading = ref(false)
const error = ref('')
const isCreateOpen = ref(false)

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
      items: [...group.items].sort((a, b) => String(a.title).localeCompare(String(b.title)))
    }))
    .sort((a, b) => a.title.localeCompare(b.title))
})

function taskSummary(task) {
  if (task.targetMin != null || task.targetMax != null) {
    return `Celsius range: ${task.targetMin ?? '...'} to ${task.targetMax ?? '...'}`
  }
  return ''
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
  }
)

function close() {
  emit('update:open', false)
  emit('close')
}

async function handleCreatedTask(payload) {
  try {
    const created = await createTask(payload)
    tasks.value = [...tasks.value, created]
      .sort((a, b) =>
        formatSectionType(a.sectionType).localeCompare(formatSectionType(b.sectionType)) ||
        String(a.title).localeCompare(String(b.title))
      )
    emit('changed')
  } catch (err) {
    console.error('Failed to create task', err)
    error.value = 'Could not create task.'
  }
}

async function removeTask(task) {
  try {
    await deleteTask(task.id)
    tasks.value = tasks.value.filter((entry) => entry.id !== task.id)
    emit('changed')
  } catch (err) {
    console.error('Failed to delete task', err)
    error.value = 'Could not delete task.'
  }
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
  width: min(860px, 100%);
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
  border: 0;
  background: transparent;
  font-size: 28px;
  line-height: 1;
  cursor: pointer;
}

.toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.primary,
.danger {
  border: 0;
  cursor: pointer;
  border-radius: 999px;
  min-height: 40px;
  padding: 0 14px;
}

.primary {
  background: var(--color-text-primary);
  color: white;
}

.danger {
  background: rgba(161, 29, 45, 0.12);
  color: #a11d2d;
}

.state,
.error {
  color: var(--color-text-muted);
}

.error {
  color: #a11d2d;
}

.groups {
  display: grid;
  gap: 16px;
}

.group {
  padding: 16px;
  border-radius: 18px;
  background: rgba(246, 247, 252, 0.85);
  border: 1px solid rgba(222, 226, 239, 0.9);
}

.group-title {
  margin-bottom: 12px;
  font-weight: 800;
}

.task-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 10px 0;
}

.task-row + .task-row {
  border-top: 1px solid rgba(210, 213, 230, 0.65);
}

.task-copy {
  display: grid;
  gap: 4px;
}

.task-title {
  font-weight: 700;
}

.task-meta {
  font-size: 13px;
  color: var(--color-text-muted);
}

@media (max-width: 720px) {
  .task-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
