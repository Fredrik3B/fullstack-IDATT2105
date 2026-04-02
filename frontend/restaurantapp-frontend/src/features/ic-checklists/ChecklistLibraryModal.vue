<template>
  <div v-if="open" class="overlay" role="dialog" aria-modal="true" aria-label="Checklist library">
    <div class="modal">
      <header class="modal-header">
        <div>
          <div class="eyebrow">{{ moduleLabel }}</div>
          <h2>Checklist library</h2>
          <p class="subtitle">Browse your saved checklists and open one directly on the workbench.</p>
        </div>
        <button type="button" class="icon-button" aria-label="Close" @click="close">×</button>
      </header>

      <div class="modal-body">
        <div v-if="sortedCards.length === 0" class="state">
          No checklists yet. Create your first one from the menu.
        </div>

        <div v-else class="library-grid">
          <article v-for="card in sortedCards" :key="card.id" class="library-card">
            <div class="library-top">
              <div>
                <div class="library-title">{{ card.title }}</div>
                <p class="library-subtitle">{{ card.subtitle || 'Reusable checklist ready for the workbench.' }}</p>
              </div>
              <div class="pill-stack">
                <span class="period-pill">{{ formatPeriod(card.period) }}</span>
                <span class="mode-pill" :class="{ recurring: card.recurring !== false }">
                  {{ card.recurring !== false ? 'Recurring' : 'Library only' }}
                </span>
              </div>
            </div>

            <div class="library-meta">
              <span>{{ countTasks(card) }} tasks</span>
              <span>{{ card.progress ?? 0 }}% done in current run</span>
            </div>

            <button
              type="button"
              class="open-button"
              :class="{ disabled: isLoaded(card) }"
              :disabled="isLoaded(card)"
              @click="emit('open-checklist', card)"
            >
              {{ isLoaded(card) ? 'Already on workbench' : 'Open on workbench' }}
            </button>
          </article>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  open: {
    type: Boolean,
    default: false
  },
  moduleLabel: {
    type: String,
    default: ''
  },
  cards: {
    type: Array,
    default: () => []
  },
  loadedChecklistIds: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:open', 'close', 'open-checklist'])

const sortedCards = computed(() =>
  [...props.cards].sort((a, b) =>
    String(a?.title ?? '').localeCompare(String(b?.title ?? '')) ||
    String(a?.period ?? '').localeCompare(String(b?.period ?? ''))
  )
)

function close() {
  emit('update:open', false)
  emit('close')
}

function formatPeriod(period) {
  const value = String(period ?? '').toLowerCase()
  if (value === 'weekly') return 'Weekly'
  if (value === 'monthly') return 'Monthly'
  return 'Daily'
}

function countTasks(card) {
  return (Array.isArray(card?.sections) ? card.sections : []).reduce((sum, section) => {
    const items = Array.isArray(section?.items) ? section.items.length : 0
    return sum + items
  }, 0)
}

function isLoaded(card) {
  return props.loadedChecklistIds.some((id) => String(id) === String(card?.id))
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
  width: min(940px, 100%);
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

.state {
  color: var(--color-text-muted);
  padding: 24px 4px;
}

.library-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.library-card {
  display: grid;
  gap: 14px;
  padding: 18px;
  border-radius: 20px;
  background: linear-gradient(180deg, rgba(248, 249, 253, 0.98) 0%, rgba(242, 244, 250, 0.98) 100%);
  border: 1px solid rgba(220, 224, 238, 0.95);
}

.library-top {
  display: flex;
  justify-content: space-between;
  gap: 14px;
}

.pill-stack {
  display: grid;
  gap: 8px;
  justify-items: end;
}

.library-title {
  font-size: 20px;
  font-weight: 800;
  color: var(--color-text-primary);
}

.library-subtitle {
  margin: 6px 0 0;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.period-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  align-self: flex-start;
  min-height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(45, 43, 85, 0.08);
  color: var(--color-text-primary);
  font-size: 12px;
  font-weight: var(--font-weight-bold);
  white-space: nowrap;
}

.mode-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(45, 43, 85, 0.08);
  color: var(--color-text-muted);
  font-size: 12px;
  font-weight: var(--font-weight-bold);
  white-space: nowrap;
}

.mode-pill.recurring {
  background: rgba(152, 197, 74, 0.16);
  color: rgba(49, 79, 8, 0.92);
}

.library-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  color: var(--color-text-muted);
  font-size: 13px;
}

.open-button {
  justify-self: start;
  border: 0;
  border-radius: 999px;
  min-height: 42px;
  padding: 0 16px;
  background: var(--color-dark-secondary);
  color: var(--color-accent);
  font: inherit;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  cursor: pointer;
}

.open-button.disabled {
  background: rgba(45, 43, 85, 0.12);
  color: rgba(45, 43, 85, 0.48);
  cursor: not-allowed;
}

@media (max-width: 820px) {
  .library-grid {
    grid-template-columns: 1fr;
  }
}
</style>
