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
        <span class="status-pill" :class="statusTone">{{ statusLabel }}</span>
        <div v-if="progress !== null" class="progress-track">
          <span class="progress-fill" :style="{ width: `${progress}%` }"></span>
        </div>
      </div>
    </header>

    <div v-for="section in sections" :key="section.title" class="section">
      <div class="section-label">{{ section.title }}</div>

      <ul class="task-list">
        <li
          v-for="task in section.items"
          :key="task.label"
          class="task-row"
          :class="[task.state, { highlighted: task.highlighted }]"
        >
          <span class="task-marker"></span>
          <span class="task-label">{{ task.label }}</span>
          <span class="task-meta">{{ task.meta }}</span>
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
    default: ''
  }
})
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
}

.task-row:first-child {
  border-top: 0;
}

.task-row.highlighted {
  background: linear-gradient(90deg, #fffceb 0%, #fffef8 100%);
}

.task-marker {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid #d7d6e7;
}

.task-row.completed .task-marker {
  border-color: var(--color-success);
  background: radial-gradient(circle at center, var(--color-success) 0 4px, transparent 4px);
}

.task-row.pending .task-marker {
  border-color: var(--color-warning);
}

.task-row.todo .task-marker {
  border-color: #d7d6e7;
}

.task-label {
  font-size: 15px;
  color: var(--color-text-primary);
}

.task-meta {
  color: var(--color-text-hint);
  font-size: var(--font-size-sm);
  white-space: nowrap;
}

.task-row.completed .task-label,
.task-row.completed .task-meta,
.task-row.todo .task-label,
.task-row.todo .task-meta {
  color: #c2bfd9;
}

.task-row.pending .task-meta {
  color: var(--color-warning-text);
  font-weight: var(--font-weight-medium);
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

  .task-meta {
    grid-column: 2;
  }
}
</style>
