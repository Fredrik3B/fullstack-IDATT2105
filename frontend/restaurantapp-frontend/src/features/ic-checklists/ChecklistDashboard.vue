<template>
  <section class="dashboard-shell">
    <ChecklistPageHeader
      :module-label="moduleLabel"
      :title="title"
      :date-label="dateLabel"
      :periods="periods"
      :active-period="activePeriod"
      :create-label="createLabel"
      :manage-label="manageLabel"
      @update:activePeriod="emit('update:activePeriod', $event)"
      @open-library="emit('open-library')"
      @manage-tasks="emit('manage-tasks')"
      @create="emit('create')"
    />

    <div class="cards-grid">
      <ChecklistCard
        v-for="(card, cardIndex) in cards"
        :key="card.id ?? card.title"
        v-bind="card"
        :now="now"
        :highlighted-workbench="String(highlightedChecklistId ?? '') === String(card.id ?? '')"
        @toggle-task="emit('toggle-task', { cardIndex: card.__sourceIndex ?? cardIndex, ...$event })"
        @toggle-pending="emit('toggle-pending', { cardIndex: card.__sourceIndex ?? cardIndex, ...$event })"
        @edit-checklist="emit('edit-checklist', { cardIndex: card.__sourceIndex ?? cardIndex })"
        @submit-checklist="emit('submit-checklist', { cardIndex: card.__sourceIndex ?? cardIndex, ...$event })"
        @log-temperature="emit('log-temperature', $event)"
      />
    </div>

    <TemperatureReportCard :cards="cards" />
    <ChecklistSummaryCard :cards="cards" />
  </section>
</template>

<script setup>
import ChecklistCard from './ChecklistCard.vue'
import ChecklistPageHeader from './ChecklistPageHeader.vue'
import ChecklistSummaryCard from './ChecklistSummaryCard.vue'
import TemperatureReportCard from './TemperatureReportCard.vue'

defineProps({
  moduleLabel: {
    type: String,
    default: ''
  },
  title: {
    type: String,
    required: true
  },
  dateLabel: {
    type: String,
    required: true
  },
  periods: {
    type: Array,
    default: () => ['Daily', 'Weekly', 'Monthly']
  },
  activePeriod: {
    type: String,
    default: 'Daily'
  },
  createLabel: {
    type: String,
    default: 'Checklists'
  },
  manageLabel: {
    type: String,
    default: 'Task pool'
  },
  cards: {
    type: Array,
    required: true
  },
  now: {
    type: [Date, String, Number],
    default: null
  },
  highlightedChecklistId: {
    type: [String, Number],
    default: null
  }
})

const emit = defineEmits(['toggle-task', 'toggle-pending', 'edit-checklist', 'submit-checklist', 'log-temperature', 'update:activePeriod', 'create', 'open-library', 'manage-tasks'])

</script>

<style scoped>
.dashboard-shell {
  max-width: 1240px;
  margin: 0 auto;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

@media (max-width: 980px) {
  .cards-grid {
    grid-template-columns: 1fr;
  }
}
</style>
