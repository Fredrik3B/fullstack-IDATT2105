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
        <p class="welcome-health">{{ operationalHealthLabel }}</p>
      </div>
    </section>

    <main class="dashboard-main">
      <div class="dashboard-content">
        <section class="quick-actions">
          <h2 class="section-heading">Quick actions</h2>
          <div class="quick-action-grid">
            <button class="quick-action-card" type="button" @click="openChecklistModule('IC_FOOD')">
              <span class="quick-action-title">Start IC-Food</span>
              <span class="quick-action-hint">Open daily food controls</span>
            </button>
            <button class="quick-action-card" type="button" @click="openChecklistModule('IC_ALCOHOL')">
              <span class="quick-action-title">Start IC-Alcohol</span>
              <span class="quick-action-hint">Open alcohol compliance controls</span>
            </button>
            <button class="quick-action-card" type="button" @click="openDeviationReport">
              <span class="quick-action-title">Report deviation</span>
              <span class="quick-action-hint">Register incidents and follow-up</span>
            </button>
            <button class="quick-action-card" type="button" @click="openDocuments">
              <span class="quick-action-title">Open documents</span>
              <span class="quick-action-hint">View certificates and guidelines</span>
            </button>
          </div>
        </section>

        <section>
          <h2 class="section-heading">Today's status</h2>
          <div class="stat-grid" :class="auth.isAdminOrManager ? 'stat-grid--admin' : 'stat-grid--staff'">
            <article class="stat-card stat-card--interactive" role="button" tabindex="0" @click="openNextChecklistModule" @keydown.enter="openNextChecklistModule" @keydown.space.prevent="openNextChecklistModule">
              <span class="stat-label">Tasks remaining</span>
              <span class="stat-value">{{ remainingTasksToday }}</span>
              <span class="stat-hint">{{ tasksTodayHint }}</span>
            </article>

            <article class="stat-card stat-card--interactive" role="button" tabindex="0" @click="openNextChecklistModule" @keydown.enter="openNextChecklistModule" @keydown.space.prevent="openNextChecklistModule">
              <span class="stat-label">Tasks completed</span>
              <span class="stat-value">{{ completedTasksToday }}</span>
              <span class="stat-hint">{{ completionLabel }}</span>
            </article>

            <article class="stat-card stat-card--interactive" role="button" tabindex="0" @click="openDeviationReport" @keydown.enter="openDeviationReport" @keydown.space.prevent="openDeviationReport">
              <span class="stat-label">Active deviations</span>
              <span class="stat-value" :class="activeDeviationCount > 0 ? 'stat-value--danger' : ''">
                {{ activeDeviationCount }}
              </span>
              <span class="stat-hint">{{ activeDeviationHint }}</span>
            </article>

            <article class="stat-card stat-card--interactive" role="button" tabindex="0" @click="openCertificateDocuments" @keydown.enter="openCertificateDocuments" @keydown.space.prevent="openCertificateDocuments">
              <span class="stat-label">Expiring certificates</span>
              <span class="stat-value" :class="expiringCertificatesCount > 0 ? 'stat-value--warning' : ''">
                {{ expiringCertificatesCount }}
              </span>
              <span class="stat-hint">{{ certificateHint }}</span>
            </article>

            <article v-if="auth.isAdminOrManager" class="stat-card stat-card--interactive" role="button" tabindex="0" @click="goToRoute('admin-requests')" @keydown.enter="goToRoute('admin-requests')" @keydown.space.prevent="goToRoute('admin-requests')">
              <span class="stat-label">Pending requests</span>
              <span class="stat-value" :class="pendingJoinRequests > 0 ? 'stat-value--warning' : ''">
                {{ pendingJoinRequests }}
              </span>
              <span class="stat-hint">{{ requestsHint }}</span>
            </article>
          </div>
        </section>

        <section>
          <h2 class="section-heading">Today's checklists</h2>
          <div class="checklist-grid">
            <article class="checklist-module-card">
              <div class="checklist-header">
                <div class="checklist-header__left">
                  <span class="checklist-dot checklist-dot--food"></span>
                  <h3 class="checklist-title">IC-Food</h3>
                </div>
                <button class="module-open-btn" type="button" @click="openChecklistModule('IC_FOOD')">Open</button>
              </div>

              <div class="module-progress">
                <div class="module-progress__bar">
                  <div class="module-progress__fill" :style="{ width: `${foodCompletionRate}%` }"></div>
                </div>
                <p class="module-progress__text">{{ foodCompletedTasks }} / {{ foodTotalTasks }} tasks completed</p>
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
            </article>

            <article class="checklist-module-card">
              <div class="checklist-header">
                <div class="checklist-header__left">
                  <span class="checklist-dot checklist-dot--alcohol"></span>
                  <h3 class="checklist-title">IC-Alcohol</h3>
                </div>
                <button class="module-open-btn" type="button" @click="openChecklistModule('IC_ALCOHOL')">Open</button>
              </div>

              <div class="module-progress">
                <div class="module-progress__bar">
                  <div class="module-progress__fill module-progress__fill--alcohol" :style="{ width: `${alcoholCompletionRate}%` }"></div>
                </div>
                <p class="module-progress__text">{{ alcoholCompletedTasks }} / {{ alcoholTotalTasks }} tasks completed</p>
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
            </article>
          </div>
        </section>

        <section class="insight-grid">
          <article class="insight-card">
            <h2 class="section-heading section-heading--compact">Alerts and follow-up</h2>
            <ul v-if="alerts.length" class="insight-list">
              <li v-for="alert in alerts" :key="alert.label" class="insight-list__item">
                <button class="insight-link" type="button" @click="handleAlertAction(alert)">
                  <span class="insight-list__dot" :class="`insight-list__dot--${alert.level}`"></span>
                  <span>{{ alert.label }}</span>
                </button>
              </li>
            </ul>
            <p v-else class="empty-hint">No critical alerts right now.</p>
          </article>

          <article class="insight-card">
            <h2 class="section-heading section-heading--compact">Recent activity</h2>
            <ul class="insight-list">
              <li class="insight-list__item">
                <span class="insight-list__dot"></span>
                <span>{{ latestTemperatureLabel }}</span>
              </li>
              <li class="insight-list__item">
                <span class="insight-list__dot"></span>
                <span>{{ latestDocumentLabel }}</span>
              </li>
              <li class="insight-list__item">
                <span class="insight-list__dot"></span>
                <span>{{ latestChecklistLabel }} - Last refresh {{ lastRefreshLabel }}</span>
              </li>
            </ul>
          </article>
        </section>

        <section class="section-footer-actions">
          <button class="secondary-btn" type="button" @click="goToRoute('reports', { preset: 'week', autoload: '1' })">Open reports</button>
          <button class="secondary-btn" type="button" @click="openDocuments">Open documents</button>
          <button
            v-if="auth.isAdminOrManager"
            class="secondary-btn"
            type="button"
            @click="goToRoute('admin-requests')"
          >
            Open admin panel
          </button>
          <button class="secondary-btn secondary-btn--refresh" type="button" @click="refreshDashboard">Refresh data</button>
        </section>

        <section v-if="auth.isAdminOrManager" class="team-panel">
          <div class="team-panel__header">
            <div>
              <h2 class="section-heading section-heading--compact">Team management</h2>
              <p class="team-panel__sub">
                Manage access, keep an eye on member roles, and jump straight to pending join requests.
              </p>
            </div>
            <button class="secondary-btn" type="button" @click="goToRoute('admin-requests')">Open admin panel</button>
          </div>

          <div class="team-panel__grid">
            <article class="team-stat-card">
              <span class="team-stat-label">Members</span>
              <span class="team-stat-value">{{ teamMemberCount }}</span>
              <span class="team-stat-hint">Total people in the organization</span>
            </article>

            <article class="team-stat-card">
              <span class="team-stat-label">Pending requests</span>
              <span class="team-stat-value team-stat-value--warning">{{ pendingJoinRequests }}</span>
              <span class="team-stat-hint">{{ requestsHint }}</span>
            </article>

            <article class="team-stat-card">
              <span class="team-stat-label">Join code</span>
              <span class="team-stat-value team-stat-value--code">{{ auth.restaurantJoinCode ?? '—' }}</span>
              <span class="team-stat-hint">Share this with new team members</span>
            </article>

            <article class="team-stat-card">
              <span class="team-stat-label">Role spread</span>
              <span class="team-stat-value">{{ roleSpreadLabel }}</span>
              <span class="team-stat-hint">{{ roleSpreadHint }}</span>
            </article>
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
import { fetchDocuments } from '../api/documents'
import { fetchTemperatureMeasurements } from '../api/temperatureMeasurements'
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
const expiringCertificatesCount = ref(0)
const pendingJoinRequests = ref(0)
const lastRefreshAt = ref(new Date())
const latestUploadedDocument = ref(null)
const latestTemperatureActivity = ref(null)
const isLoadingDocuments = ref(false)
const documentsError = ref('')
const isLoadingRequests = ref(false)
const requestsError = ref('')
const isLoadingTemperatureActivity = ref(false)
const temperatureActivityError = ref('')
const teamMembers = ref([])
const isLoadingTeam = ref(false)
const teamError = ref('')

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
const completedTasksToday = computed(() => Math.max(totalTasksToday.value - remainingTasksToday.value, 0))

const foodTotalTasks = computed(() => countAllTasks(dailyFoodChecklists.value))
const foodCompletedTasks = computed(() => Math.max(foodTotalTasks.value - countRemainingTasks(dailyFoodChecklists.value), 0))
const foodRemainingTasks = computed(() => countRemainingTasks(dailyFoodChecklists.value))
const foodCompletionRate = computed(() => getCompletionRate(foodCompletedTasks.value, foodTotalTasks.value))

const alcoholTotalTasks = computed(() => countAllTasks(dailyAlcoholChecklists.value))
const alcoholCompletedTasks = computed(() =>
  Math.max(alcoholTotalTasks.value - countRemainingTasks(dailyAlcoholChecklists.value), 0)
)
const alcoholRemainingTasks = computed(() => countRemainingTasks(dailyAlcoholChecklists.value))
const alcoholCompletionRate = computed(() => getCompletionRate(alcoholCompletedTasks.value, alcoholTotalTasks.value))

const activeDeviationCount = computed(() =>
  countTemperatureDeviations(dailyFoodChecklists.value, foodTemperatureLatestByTaskId.value) +
  countTemperatureDeviations(dailyAlcoholChecklists.value, alcoholTemperatureLatestByTaskId.value)
)

const completionLabel = computed(() => {
  if (totalTasksToday.value === 0) return 'No daily tasks yet'
  const rate = getCompletionRate(completedTasksToday.value, totalTasksToday.value)
  return `${rate}% complete today`
})

const tasksTodayHint = computed(() => {
  if (totalTasksToday.value === 0) return 'No daily tasks assigned'
  return `${remainingTasksToday.value} remaining out of ${totalTasksToday.value}`
})

const activeDeviationHint = computed(() => {
  if (activeDeviationCount.value === 0) return 'No temperature deviations recorded'
  return `${activeDeviationCount.value} temperature deviations need follow-up`
})

const certificateHint = computed(() => {
  if (isLoadingDocuments.value) return 'Checking certificate expiry...'
  if (documentsError.value) return documentsError.value
  if (expiringCertificatesCount.value === 0) return 'No certificates expiring soon'
  return 'Within 30 days'
})

const requestsHint = computed(() => {
  if (!auth.isAdminOrManager) return ''
  if (isLoadingRequests.value) return 'Checking team requests...'
  if (requestsError.value) return requestsError.value
  if (pendingJoinRequests.value === 0) return 'No pending access requests'
  return 'Team access requests'
})

const teamMemberCount = computed(() => teamMembers.value.length)

const roleCounts = computed(() => {
  const counts = { ADMIN: 0, MANAGER: 0, HR: 0, STAFF: 0 }
  teamMembers.value.forEach((member) => {
    const role = primaryRole(member?.roles)
    if (counts[role] != null) counts[role] += 1
  })
  return counts
})

const roleSpreadLabel = computed(() => {
  const parts = [
    `A ${roleCounts.value.ADMIN}`,
    `M ${roleCounts.value.MANAGER}`,
    `H ${roleCounts.value.HR}`,
    `S ${roleCounts.value.STAFF}`
  ]
  return parts.join(' · ')
})

const roleSpreadHint = computed(() => {
  if (isLoadingTeam.value) return 'Loading member breakdown...'
  if (teamError.value) return teamError.value
  if (teamMembers.value.length === 0) return 'No members loaded yet'
  return 'Role distribution across your organization'
})

const operationalHealthLabel = computed(() => {
  if (activeDeviationCount.value > 0) return 'Operational focus: active deviations require follow-up.'
  if (remainingTasksToday.value > 0) return 'Operational focus: continue daily checklists to close all tasks.'
  return 'Operational status: all tracked daily tasks are currently complete.'
})

const latestChecklistLabel = computed(() => {
  const candidate = dailyChecklists.value.find((card) => typeof card?.title === 'string' && card.title.trim().length > 0)
  if (!candidate) return 'No daily checklist opened yet'
  return `Checklist focus: ${candidate.title}`
})

const latestDocumentLabel = computed(() => {
  if (isLoadingDocuments.value) return 'Loading recent document activity...'
  if (documentsError.value) return `Documents: ${documentsError.value}`
  const doc = latestUploadedDocument.value
  if (!doc) return 'No recent document upload detected'
  return `Latest document upload: ${doc.name || 'Document'} (${formatDateTimeShort(doc.uploadedAt)})`
})

const latestTemperatureLabel = computed(() => {
  if (isLoadingTemperatureActivity.value) return 'Loading latest temperature log...'
  if (temperatureActivityError.value) return `Temperature log: ${temperatureActivityError.value}`
  const activity = latestTemperatureActivity.value
  if (!activity) return 'No temperature measurements logged today'
  return `${activity.moduleLabel}: ${activity.valueC}C measured ${formatDateTimeShort(activity.measuredAt)}`
})

const lastRefreshLabel = computed(() =>
  new Intl.DateTimeFormat('en-US', {
    hour: '2-digit',
    minute: '2-digit'
  }).format(lastRefreshAt.value)
)

const alerts = computed(() => {
  const items = []
  if (activeDeviationCount.value > 0) {
    items.push({ level: 'danger', label: `${activeDeviationCount.value} active temperature deviation(s)`, action: 'deviation' })
  }
  if (expiringCertificatesCount.value > 0) {
    items.push({ level: 'warning', label: `${expiringCertificatesCount.value} certificate(s) expiring within 30 days`, action: 'certificates' })
  }
  if (remainingTasksToday.value > 0) {
    items.push({ level: 'neutral', label: `${remainingTasksToday.value} task(s) still open today`, action: 'tasks' })
  }
  return items
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

function primaryRole(roles) {
  const roleList = Array.isArray(roles) ? roles : []
  if (roleList.includes('ADMIN')) return 'ADMIN'
  if (roleList.includes('MANAGER')) return 'MANAGER'
  if (roleList.includes('HR')) return 'HR'
  return 'STAFF'
}

function getCompletionRate(done, total) {
  if (!Number.isFinite(done) || !Number.isFinite(total) || total <= 0) return 0
  return Math.round((done / total) * 100)
}

function formatPeriod(period) {
  if (!period) return 'Unknown'
  return String(period).charAt(0).toUpperCase() + String(period).slice(1).toLowerCase()
}

function formatDateTimeShort(value) {
  if (!value) return 'unknown time'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return 'unknown time'
  return new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

function toBackendDateTime(value) {
  const date = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(date.getTime())) return new Date().toISOString().slice(0, 19)
  return date.toISOString().slice(0, 19)
}

function openChecklistModule(module) {
  const routeName = module === 'IC_ALCOHOL' ? 'ic-alcohol-dashboard' : 'ic-food-dashboard'
  router.push({ name: routeName })
}

function goToRoute(name, query = undefined) {
  router.push({ name, query })
}

function openDeviationReport() {
  goToRoute('reports', { action: 'deviation' })
}

function openCertificateDocuments() {
  goToRoute('documents', { category: 'CERTIFICATE' })
}

function openDocuments() {
  goToRoute('documents')
}

function openNextChecklistModule() {
  if (alcoholRemainingTasks.value > foodRemainingTasks.value) {
    openChecklistModule('IC_ALCOHOL')
    return
  }
  openChecklistModule('IC_FOOD')
}

function handleAlertAction(alert) {
  if (alert?.action === 'deviation') {
    openDeviationReport()
    return
  }
  if (alert?.action === 'certificates') {
    openCertificateDocuments()
    return
  }
  openNextChecklistModule()
}

async function loadChecklistData() {
  isLoadingFood.value = true
  isLoadingAlcohol.value = true
  foodError.value = ''
  alcoholError.value = ''

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
}

async function loadDocumentInsights() {
  isLoadingDocuments.value = true
  documentsError.value = ''

  try {
    const docs = await fetchDocuments()
    if (!Array.isArray(docs)) {
      expiringCertificatesCount.value = 0
      latestUploadedDocument.value = null
      return
    }

    const today = new Date()
    expiringCertificatesCount.value = docs.filter((doc) => {
      if (doc?.category !== 'CERTIFICATE') return false
      if (!doc?.expiryDate) return false
      const expiry = new Date(doc.expiryDate)
      if (Number.isNaN(expiry.getTime())) return false
      const daysLeft = Math.ceil((expiry - today) / (1000 * 60 * 60 * 24))
      return daysLeft <= 30
    }).length

    latestUploadedDocument.value = docs
      .filter((doc) => doc?.uploadedAt)
      .sort((a, b) => new Date(b.uploadedAt).getTime() - new Date(a.uploadedAt).getTime())[0] ?? null
  } catch {
    expiringCertificatesCount.value = 0
    latestUploadedDocument.value = null
    documentsError.value = 'Could not load document insights'
  } finally {
    isLoadingDocuments.value = false
  }
}

async function loadPendingRequests() {
  isLoadingRequests.value = true
  requestsError.value = ''

  if (!auth.isAdminOrManager) {
    pendingJoinRequests.value = 0
    isLoadingRequests.value = false
    return
  }

  try {
    const pending = await auth.fetchJoinRequests('PENDING')
    pendingJoinRequests.value = Array.isArray(pending) ? pending.length : 0
  } catch {
    pendingJoinRequests.value = 0
    requestsError.value = 'Could not load request status'
  } finally {
    isLoadingRequests.value = false
  }
}

async function loadTeamMembers() {
  if (!auth.isAdminOrManager) {
    teamMembers.value = []
    teamError.value = ''
    return
  }

  isLoadingTeam.value = true
  teamError.value = ''

  try {
    const members = await auth.fetchMembers()
    teamMembers.value = Array.isArray(members) ? members : []
  } catch {
    teamMembers.value = []
    teamError.value = 'Could not load team overview'
  } finally {
    isLoadingTeam.value = false
  }
}

async function loadLatestTemperatureActivity() {
  isLoadingTemperatureActivity.value = true
  temperatureActivityError.value = ''
  latestTemperatureActivity.value = null

  const start = new Date()
  start.setHours(0, 0, 0, 0)
  const end = new Date()
  const from = toBackendDateTime(start)
  const to = toBackendDateTime(end)

  try {
    const [food, alcohol] = await Promise.all([
      fetchTemperatureMeasurements({ module: 'IC_FOOD', from, to }),
      fetchTemperatureMeasurements({ module: 'IC_ALCOHOL', from, to })
    ])

    const candidates = [...(Array.isArray(food) ? food : []), ...(Array.isArray(alcohol) ? alcohol : [])]
      .map((item) => {
        const measuredAt = item?.measuredAt || item?.createdAt || item?.updatedAt || null
        return {
          valueC: Number(item?.valueC),
          measuredAt,
          moduleLabel: item?.module === 'IC_ALCOHOL' ? 'IC-Alcohol' : 'IC-Food'
        }
      })
      .filter((item) => item.measuredAt && Number.isFinite(item.valueC))
      .sort((a, b) => new Date(b.measuredAt).getTime() - new Date(a.measuredAt).getTime())

    latestTemperatureActivity.value = candidates[0] ?? null
  } catch {
    temperatureActivityError.value = 'Could not load temperature activity'
  } finally {
    isLoadingTemperatureActivity.value = false
  }
}

async function refreshDashboard() {
  await Promise.all([loadChecklistData(), loadDocumentInsights(), loadPendingRequests(), loadTeamMembers(), loadLatestTemperatureActivity()])
  lastRefreshAt.value = new Date()
}

onMounted(async () => {
  await refreshDashboard()
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
  padding: var(--space-12) var(--space-6);
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
  max-width: 760px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: var(--space-4);
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

.welcome-health {
  margin: 0;
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);
  background: rgba(212, 232, 53, 0.14);
  border: 1px solid rgba(212, 232, 53, 0.45);
  color: #f5f8d1;
  font-size: var(--font-size-sm);
}

.dashboard-main {
  padding: var(--space-10) var(--space-6) var(--space-12);
}

.dashboard-content {
  max-width: 1040px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
}

.section-heading {
  margin: 0 0 var(--space-4) 0;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.section-heading--compact {
  margin-bottom: var(--space-3);
}

.quick-action-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--space-3);
}

.quick-action-card {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: linear-gradient(145deg, #ffffff 0%, #f8f8fc 100%);
  box-shadow: var(--shadow-sm);
  padding: var(--space-4);
  text-align: left;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  transition: transform var(--transition-normal), box-shadow var(--transition-normal), border-color var(--transition-normal);
}

.quick-action-card:hover,
.quick-action-card:focus-visible {
  transform: translateY(-2px);
  border-color: var(--color-dark-tertiary);
  box-shadow: var(--shadow-md);
  outline: none;
}

.quick-action-title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.quick-action-hint {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.stat-grid {
  display: grid;
  gap: var(--space-3);
}

.stat-grid--staff {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.stat-grid--admin {
  grid-template-columns: repeat(5, minmax(0, 1fr));
}

.stat-card {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  box-shadow: var(--shadow-sm);
}

.stat-card--interactive {
  cursor: pointer;
  transition: transform var(--transition-normal), box-shadow var(--transition-normal), border-color var(--transition-normal);
}

.stat-card--interactive:hover,
.stat-card--interactive:focus-visible {
  transform: translateY(-2px);
  border-color: var(--color-dark-tertiary);
  box-shadow: var(--shadow-md);
  outline: none;
}

.stat-label {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.stat-value {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.stat-value--danger {
  color: var(--color-danger);
}

.stat-value--warning {
  color: var(--color-warning-text);
}

.stat-hint {
  font-size: var(--font-size-xs);
  color: var(--color-text-hint);
}

.checklist-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
  align-items: stretch;
}

.checklist-module-card {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  min-height: 300px;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  box-shadow: var(--shadow-sm);
}

.checklist-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}

.checklist-header__left {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.checklist-dot {
  width: 10px;
  height: 10px;
  border-radius: var(--radius-full);
  flex-shrink: 0;
}

.checklist-dot--food {
  background: var(--color-accent);
}

.checklist-dot--alcohol {
  background: var(--color-dark-tertiary);
}

.checklist-title {
  margin: 0;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.module-open-btn {
  min-height: 34px;
  padding: 0 var(--space-3);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border-strong);
  background: var(--color-bg-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  cursor: pointer;
}

.module-open-btn:hover {
  border-color: var(--color-dark-tertiary);
}

.module-progress {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.module-progress__bar {
  width: 100%;
  height: 8px;
  border-radius: var(--radius-full);
  background: var(--color-bg-tertiary);
  overflow: hidden;
}

.module-progress__fill {
  height: 100%;
  border-radius: var(--radius-full);
  background: var(--color-accent);
}

.module-progress__fill--alcohol {
  background: var(--color-dark-tertiary);
}

.module-progress__text {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
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
  border-color: var(--color-dark-tertiary);
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

.insight-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
}

.insight-card {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  box-shadow: var(--shadow-sm);
}

.insight-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.insight-list__item {
  display: flex;
  align-items: stretch;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.insight-link {
  width: 100%;
  display: flex;
  align-items: center;
  gap: var(--space-2);
  border: 0;
  background: transparent;
  color: inherit;
  font: inherit;
  text-align: left;
  cursor: pointer;
  border-radius: var(--radius-sm);
  padding: var(--space-1) var(--space-2);
}

.insight-link:hover,
.insight-link:focus-visible {
  background: var(--color-bg-secondary);
  outline: none;
}

.insight-list__dot {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  background: var(--color-dark-tertiary);
}

.insight-list__dot--danger {
  background: var(--color-danger);
}

.insight-list__dot--warning {
  background: var(--color-warning);
}

.insight-list__dot--neutral {
  background: var(--color-dark-tertiary);
}

.empty-hint {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-hint);
}

.section-footer-actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
}

.team-panel {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  padding: var(--space-6);
  border-radius: var(--radius-lg);
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

.team-panel__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
}

.team-panel__sub {
  margin: 0;
  max-width: 64ch;
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.team-panel__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--space-3);
}

.team-stat-card {
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  background: linear-gradient(180deg, #ffffff 0%, #fbfbfd 100%);
  border: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.team-stat-label {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.team-stat-value {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.team-stat-value--warning {
  color: var(--color-warning-text);
}

.team-stat-value--code {
  color: var(--color-dark-secondary);
  letter-spacing: 0.08em;
}

.team-stat-hint {
  font-size: var(--font-size-xs);
  color: var(--color-text-hint);
}

.secondary-btn {
  min-height: 36px;
  padding: 0 var(--space-4);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border-strong);
  background: var(--color-bg-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  cursor: pointer;
}

.secondary-btn:hover {
  border-color: var(--color-dark-tertiary);
}

.secondary-btn--refresh {
  margin-left: auto;
}

@media (max-width: 1080px) {
  .quick-action-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .stat-grid--staff,
  .stat-grid--admin {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .checklist-grid,
  .insight-grid {
    grid-template-columns: 1fr;
  }

  .stat-grid--staff,
  .stat-grid--admin {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .secondary-btn--refresh {
    margin-left: 0;
  }

  .team-panel__header {
    flex-direction: column;
  }

  .team-panel__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .dashboard-main {
    padding-inline: var(--space-4);
  }

  .quick-action-grid,
  .stat-grid--staff,
  .stat-grid--admin,
  .team-panel__grid {
    grid-template-columns: 1fr;
  }
}
</style>
