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
          <span class="task-label">{{ task.label }}</span>
          <div class="task-actions">
            <span v-if="task.meta" class="task-meta">{{ task.meta }}</span>
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
  </article>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
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
  }
})

const emit = defineEmits(['toggle-task', 'toggle-pending', 'edit-checklist'])

function handleToggle(sectionIndex, taskIndex) {
  emit('toggle-task', { sectionIndex, taskIndex })
}

function handleFlag(sectionIndex, taskIndex) {
  emit('toggle-pending', { sectionIndex, taskIndex })
}

function handleEditChecklist() {
  emit('edit-checklist')
}
</script>

<style scoped>
.checklist-card {
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
</style>
