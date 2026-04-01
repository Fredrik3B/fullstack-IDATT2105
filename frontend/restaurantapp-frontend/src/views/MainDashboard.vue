<template>
  <div class="dashboard-root">
    <section class="welcome-banner">
      <div class="welcome-glow"></div>
      <div class="welcome-inner">
        <span class="welcome-tag">Internal Control System</span>
        <h1 class="welcome-heading">
          Good day, <span class="welcome-accent">{{ displayUserName }}</span>
        </h1>
        <p class="welcome-sub">{{ restaurantDisplayName }} - {{ todayLabel }}</p>
      </div>
    </section>
    <main class="dashboard-main">
      <div class="dashboard-content">
        <section>
          <h2 class="section-heading">Today's Checklists</h2>
          <div class="checklist-grid">
            <div class="checklist-placeholder">
              <div class="checklist-header">
                <span class="checklist-dot checklist-dot--food"></span>
                <h3 class="checklist-title">IC-Food</h3>
              </div>
              <div class="checklist-body">
                <p v-if="isLoadingFood" class="checklist-hint">Loading checklists...</p>
                <p v-else-if="foodError" class="checklist-hint">{{ foodError }}</p>
                <template v-else-if="dailyFoodChecklists.length">
                  <article
                    v-for="card in dailyFoodChecklists"
                    :key="card.id ?? card.title"
                    class="checklist-preview"
                    role="button"
                    tabindex="0"
                    @click="openChecklistModule('IC_FOOD')"
                    @keydown.enter="openChecklistModule('IC_FOOD')"
                    @keydown.space.prevent="openChecklistModule('IC_FOOD')"
                  >
                    <div class="checklist-preview__top">
                      <h4 class="checklist-preview__title">{{ card.title }}</h4>
                      <span class="checklist-preview__period">{{ formatPeriod(card.period) }}</span>
                    </div>
                    <p v-if="card.subtitle" class="checklist-preview__subtitle">{{ card.subtitle }}</p>
                    <p class="checklist-preview__meta">{{ getTaskCount(card) }} tasks</p>
                  </article>
                </template>
                <p v-else class="checklist-hint">No daily IC-Food checklists found.</p>
              </div>
            </div>

            <div class="checklist-placeholder">
              <div class="checklist-header">
                <span class="checklist-dot checklist-dot--alcohol"></span>
                <h3 class="checklist-title">IC-Alcohol</h3>
              </div>
              <div class="checklist-body">
                <p v-if="isLoadingAlcohol" class="checklist-hint">Loading checklists...</p>
                <p v-else-if="alcoholError" class="checklist-hint">{{ alcoholError }}</p>
                <template v-else-if="dailyAlcoholChecklists.length">
                  <article
                    v-for="card in dailyAlcoholChecklists"
                    :key="card.id ?? card.title"
                    class="checklist-preview"
                    role="button"
                    tabindex="0"
                    @click="openChecklistModule('IC_ALCOHOL')"
                    @keydown.enter="openChecklistModule('IC_ALCOHOL')"
                    @keydown.space.prevent="openChecklistModule('IC_ALCOHOL')"
                  >
                    <div class="checklist-preview__top">
                      <h4 class="checklist-preview__title">{{ card.title }}</h4>
                      <span class="checklist-preview__period">{{ formatPeriod(card.period) }}</span>
                    </div>
                    <p v-if="card.subtitle" class="checklist-preview__subtitle">{{ card.subtitle }}</p>
                    <p class="checklist-preview__meta">{{ getTaskCount(card) }} tasks</p>
                  </article>
                </template>
                <p v-else class="checklist-hint">No daily IC-Alcohol checklists found.</p>
              </div>
            </div>
          </div>
        </section>

        <section>
          <h2 class="section-heading">Overview</h2>
          <div class="stat-grid">
            <div class="stat-card">
              <span class="stat-label">Active Deviations</span>
              <span class="stat-value stat-value--danger">{{ activeDeviationCount }}</span>
              <span class="stat-hint">{{ activeDeviationHint }}</span>
            </div>
            <div class="stat-card">
              <span class="stat-label">Tasks Today</span>
              <span class="stat-value">{{ remainingTasksToday }}</span>
              <span class="stat-hint">{{ tasksTodayHint }}</span>
            </div>
            <div class="stat-card">
              <span class="stat-label">Last Check</span>
              <span class="stat-value stat-value--muted">-</span>
              <span class="stat-hint">Not completed</span>
            </div>
            <div class="stat-card">
              <span class="stat-label">Reports</span>
              <span class="stat-value stat-value--muted">0</span>
              <span class="stat-hint">No reports</span>
            </div>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { fetchChecklists } from '../api/checklists'
import { isTemperatureDeviation, isTemperatureTask } from '../features/ic-checklists/temperature'
import { useTemperatureLog } from '../features/ic-checklists/useTemperatureLog'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const foodChecklists = ref([])
const alcoholChecklists = ref([])
const isLoadingFood = ref(true)
const isLoadingAlcohol = ref(true)
const foodError = ref('')
const alcoholError = ref('')

const { latestByTaskId: foodTemperatureLatestByTaskId } = useTemperatureLog({ module: 'IC_FOOD' })
const { latestByTaskId: alcoholTemperatureLatestByTaskId } = useTemperatureLog({ module: 'IC_ALCOHOL' })

const dailyFoodChecklists = computed(() => foodChecklists.value.filter((card) => isDailyChecklist(card)))
const dailyAlcoholChecklists = computed(() => alcoholChecklists.value.filter((card) => isDailyChecklist(card)))
const dailyChecklists = computed(() => [...dailyFoodChecklists.value, ...dailyAlcoholChecklists.value])

const displayUserName = computed(() => auth.user?.name?.trim() || auth.user?.email || 'User')
const restaurantDisplayName = computed(() => auth.restaurantName?.trim() || 'Restaurant')
const todayLabel = computed(() =>
  new Intl.DateTimeFormat('en-US', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  }).format(new Date())
)
const remainingTasksToday = computed(() => countRemainingTasks(dailyChecklists.value))
const totalTasksToday = computed(() => countAllTasks(dailyChecklists.value))
const activeDeviationCount = computed(() =>
  countTemperatureDeviations(dailyFoodChecklists.value, foodTemperatureLatestByTaskId.value) +
  countTemperatureDeviations(dailyAlcoholChecklists.value, alcoholTemperatureLatestByTaskId.value)
)
const tasksTodayHint = computed(() => {
  if (totalTasksToday.value === 0) return 'No daily tasks'
  return `${remainingTasksToday.value} remaining out of ${totalTasksToday.value}`
})
const activeDeviationHint = computed(() => {
  if (activeDeviationCount.value === 0) return 'No temperature deviations recorded'
  return `${activeDeviationCount.value} temperature deviations need follow-up`
})

function isDailyChecklist(card) {
  return String(card?.period ?? '').toLowerCase() === 'daily'
}

function getAllTasks(card) {
  const sections = Array.isArray(card?.sections) ? card.sections : []
  return sections.flatMap((section) => (Array.isArray(section?.items) ? section.items : []))
}

function getTaskCount(card) {
  return getAllTasks(card).length
}

function countAllTasks(cards) {
  return cards.reduce((total, card) => total + getTaskCount(card), 0)
}

function countRemainingTasks(cards) {
  return cards.reduce(
    (total, card) => total + getAllTasks(card).filter((task) => String(task?.state ?? 'todo') !== 'completed').length,
    0
  )
}

function countTemperatureDeviations(cards, latestByTaskId) {
  const latestMap = latestByTaskId instanceof Map ? latestByTaskId : new Map()
  let count = 0

  cards.forEach((card) => {
    getAllTasks(card).forEach((task) => {
      if (!isTemperatureTask(task)) return
      const latest = latestMap.get(task?.id)
      const valueC = Number(latest?.valueC)
      if (isTemperatureDeviation(task, valueC)) count += 1
    })
  })

  return count
}

function formatPeriod(period) {
  if (!period) return 'Unknown'
  return String(period).charAt(0).toUpperCase() + String(period).slice(1).toLowerCase()
}

function openChecklistModule(module) {
  const routeName = module === 'IC_ALCOHOL' ? 'ic-alcohol-dashboard' : 'ic-food-dashboard'
  router.push({ name: routeName })
}

onMounted(async () => {
  try {
    const data = await fetchChecklists({ module: 'IC_FOOD' })
    foodChecklists.value = Array.isArray(data) ? data : []
  } catch (error) {
    console.error('Failed to fetch IC-Food checklists', error)
    foodError.value = 'Could not fetch IC-Food checklists.'
  } finally {
    isLoadingFood.value = false
  }

  try {
    const data = await fetchChecklists({ module: 'IC_ALCOHOL' })
    alcoholChecklists.value = Array.isArray(data) ? data : []
  } catch (error) {
    console.error('Failed to fetch IC-Alcohol checklists', error)
    alcoholError.value = 'Could not fetch IC-Alcohol checklists.'
  } finally {
    isLoadingAlcohol.value = false
  }
})
</script>

<style scoped>
.dashboard-root {
  min-height: 100vh;
  background: var(--color-bg-secondary);
}

.welcome-banner {
  position: relative;
  overflow: hidden;
  background: var(--color-dark-primary);
  padding: var(--space-16) var(--space-6);
}

.welcome-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 900px;
  height: 900px;
  border-radius: 50%;
  background: radial-gradient(
    circle at center,
    var(--color-dark-secondary) 0%,
    var(--color-dark-primary) 55%,
    transparent 72%
  );
  pointer-events: none;
}

.welcome-inner {
  position: relative;
  z-index: 1;
  max-width: 680px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: var(--space-5);
}

.welcome-tag {
  display: inline-block;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--color-accent);
  border: 1px solid var(--color-accent);
  border-radius: var(--radius-full);
  padding: 4px var(--space-3);
}

.welcome-heading {
  margin: 0;
  font-size: clamp(32px, 5vw, 56px);
  font-weight: var(--font-weight-bold);
  color: #ffffff;
  line-height: var(--line-height-tight);
}

.welcome-accent {
  color: var(--color-accent);
}

.welcome-sub {
  margin: 0;
  font-size: var(--font-size-md);
  color: var(--color-dark-border);
}

.dashboard-main {
  padding: var(--space-12) var(--space-6);
}

.dashboard-content {
  max-width: 960px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-10);
}

.section-heading {
  margin: 0 0 var(--space-4) 0;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.checklist-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
  align-items: stretch;
}

.checklist-placeholder {
  background: var(--color-bg-primary);
  border: 2px dashed var(--color-border-strong);
  border-radius: var(--radius-lg);
  padding: var(--space-8) var(--space-6);
  min-height: 300px;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  box-shadow: var(--shadow-sm);
}

.checklist-header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--color-border);
}

.checklist-dot {
  width: 10px;
  height: 10px;
  border-radius: var(--radius-full);
  flex-shrink: 0;
}

.checklist-dot--food { background: var(--color-accent); }
.checklist-dot--alcohol { background: var(--color-dark-tertiary); }

.checklist-title {
  margin: 0;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.checklist-body {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.checklist-preview {
  padding: var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-bg-secondary);
  cursor: pointer;
  transition: transform 0.16s ease, box-shadow 0.16s ease, border-color 0.16s ease;
}

.checklist-preview:hover,
.checklist-preview:focus-visible {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--color-accent);
  outline: none;
}

.checklist-preview__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-3);
}

.checklist-preview__title {
  margin: 0;
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.checklist-preview__period {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-muted);
  text-transform: uppercase;
}

.checklist-preview__subtitle,
.checklist-preview__meta {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.checklist-hint {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-hint);
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
}

.stat-card {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  box-shadow: var(--shadow-sm);
}

.stat-label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-muted);
}

.stat-value {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.stat-value--danger { color: var(--color-danger); }
.stat-value--muted { color: var(--color-text-hint); }

.stat-hint {
  font-size: var(--font-size-xs);
  color: var(--color-text-hint);
}

@media (max-width: 900px) {
  .checklist-grid {
    grid-template-columns: 1fr;
  }

  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
