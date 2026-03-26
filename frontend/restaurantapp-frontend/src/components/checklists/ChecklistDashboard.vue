<template>
  <section class="dashboard-shell">
    <ChecklistPageHeader
      :module-label="moduleLabel"
      :title="title"
      :date-label="dateLabel"
      :active-period="activePeriod"
      :create-label="createLabel"
    />

    <div class="cards-grid">
      <ChecklistCard
        v-for="card in cards"
        :key="card.title"
        v-bind="card"
      />
    </div>
  </section>
</template>

<script setup>
import ChecklistCard from './ChecklistCard.vue'
import ChecklistPageHeader from './ChecklistPageHeader.vue'

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
  activePeriod: {
    type: String,
    default: 'Daglig'
  },
  createLabel: {
    type: String,
    default: '+ Ny sjekkliste'
  },
  cards: {
    type: Array,
    required: true
  }
})
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
