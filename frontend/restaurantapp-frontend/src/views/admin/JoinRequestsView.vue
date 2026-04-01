<template>
  <div class="requests-root">

    <div class="page-header">
      <div class="page-header-inner">
        <div>
          <h1 class="page-title">Access Requests</h1>
          <p class="page-sub">Review and manage pending requests to join your restaurant.</p>
        </div>
        <div class="header-badge" v-if="pending.length > 0">
          {{ pending.length }} pending
        </div>
      </div>

      <!-- Join code -->
      <div v-if="auth.restaurantJoinCode" class="join-code-bar">
        <span class="join-code-label">Join code</span>
        <span class="join-code-value">{{ auth.restaurantJoinCode }}</span>
        <button class="join-code-copy" @click="copyCode" :class="{ copied }">
          {{ copied ? 'Copied!' : 'Copy' }}
        </button>
      </div>
    </div>

    <div class="page-body">

      <!-- Loading -->
      <div v-if="loading" class="state-box">
        <span class="state-text">Loading requests…</span>
      </div>

      <!-- Error -->
      <div v-else-if="error" class="state-box state-box--error">
        <span class="state-text">{{ error }}</span>
        <button class="retry-btn" @click="load">Try again</button>
      </div>

      <!-- Empty -->
      <div v-else-if="pending.length === 0" class="state-box">
        <div class="empty-icon">
          <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
            <circle cx="20" cy="20" r="19" stroke="var(--color-border-strong)" stroke-width="1.5"/>
            <circle cx="16" cy="16" r="5" stroke="var(--color-text-hint)" stroke-width="1.5"/>
            <path d="M8 32c0-4.42 3.58-8 8-8" stroke="var(--color-text-hint)" stroke-width="1.5" stroke-linecap="round"/>
            <path d="M26 22v8M22 26h8" stroke="var(--color-text-hint)" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </div>
        <span class="state-text">No pending access requests</span>
        <span class="state-hint">New requests will appear here when employees request to join.</span>
      </div>

      <!-- Request list -->
      <ul v-else class="request-list">
        <li
          v-for="req in pending"
          :key="req.requestId"
          class="request-card"
          :class="{ resolving: resolving === req.requestId }"
        >
          <div class="request-avatar">
            {{ initials(req.firstName, req.lastName) }}
          </div>

          <div class="request-info">
            <span class="request-name">{{ req.firstName }} {{ req.lastName }}</span>
            <span class="request-email">{{ req.email }}</span>
            <span class="request-date">Requested {{ formatDate(req.createdAt) }}</span>
          </div>

          <div class="request-actions">
            <button
              class="action-btn action-btn--accept"
              :disabled="resolving === req.requestId"
              @click="resolve(req, 'ACCEPTED')"
            >
              Accept
            </button>
            <button
              class="action-btn action-btn--decline"
              :disabled="resolving === req.requestId"
              @click="resolve(req, 'DECLINED')"
            >
              Decline
            </button>
          </div>
        </li>
      </ul>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const auth = useAuthStore()
const toast = useToast()

const pending   = ref([])
const loading   = ref(true)
const error     = ref(null)
const resolving = ref(null) // requestId currently being processed
const copied    = ref(false)

async function copyCode() {
  if (!auth.restaurantJoinCode) return
  await navigator.clipboard.writeText(auth.restaurantJoinCode)
  copied.value = true
  setTimeout(() => { copied.value = false }, 2000)
}

// ── Data loading ──────────────────────────────────────────────────────────

async function load() {
  loading.value = true
  error.value   = null
  try {
    pending.value = await auth.fetchJoinRequests('PENDING')
  } catch (e) {
    error.value = e?.response?.data?.message ?? 'Failed to load requests.'
  } finally {
    loading.value = false
  }
}

onMounted(load)

// ── Actions ───────────────────────────────────────────────────────────────

async function resolve(req, action) {
  resolving.value = req.requestId
  try {
    await auth.resolveJoinRequest(req.requestId, action)
    // Optimistically remove from list
    pending.value = pending.value.filter(r => r.requestId !== req.requestId)
    if (action === 'ACCEPTED') {
      toast.success(`${req.firstName} ${req.lastName} has been granted access.`)
    } else {
      toast.info(`Request from ${req.firstName} ${req.lastName} was declined.`)
    }
  } catch (e) {
    toast.error(e?.response?.data?.message ?? 'Something went wrong. Please try again.')
  } finally {
    resolving.value = null
  }
}

// ── Helpers ───────────────────────────────────────────────────────────────

function initials(first, last) {
  return ((first?.[0] ?? '') + (last?.[0] ?? '')).toUpperCase() || '?'
}

function formatDate(iso) {
  if (!iso) return '—'
  return new Intl.DateTimeFormat('en-GB', { day: 'numeric', month: 'short', year: 'numeric' }).format(new Date(iso))
}
</script>

<style scoped>
.requests-root {
  min-height: 100vh;
  background: var(--color-bg-secondary);
}

/* ── Page header ── */
.page-header {
  background: var(--color-dark-primary);
  padding: var(--space-10) var(--space-6);
}

.page-header-inner {
  max-width: 760px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
}

.page-title {
  margin: 0 0 var(--space-1) 0;
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: #ffffff;
}

.page-sub {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--color-dark-border);
}

.header-badge {
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  background: var(--color-accent);
  color: var(--color-dark-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  white-space: nowrap;
}

/* ── Join code bar ── */
.join-code-bar {
  max-width: 760px;
  margin: var(--space-5) auto 0;
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--color-dark-tertiary);
  border-radius: var(--radius-md);
}

.join-code-label {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
  color: var(--color-dark-border);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  flex-shrink: 0;
}

.join-code-value {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  color: var(--color-accent);
  letter-spacing: 0.1em;
  flex: 1;
}

.join-code-copy {
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-dark-tertiary);
  background: transparent;
  color: var(--color-dark-border);
  font-size: var(--font-size-xs);
  font-family: var(--font-sans);
  cursor: pointer;
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.join-code-copy:hover {
  border-color: var(--color-accent);
  color: var(--color-accent);
}

.join-code-copy.copied {
  border-color: var(--color-accent);
  color: var(--color-accent);
}

/* ── Page body ── */
.page-body {
  max-width: 760px;
  margin: 0 auto;
  padding: var(--space-8) var(--space-6);
}

/* ── State boxes ── */
.state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-16) var(--space-6);
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  text-align: center;
}

.state-box--error {
  border-color: var(--color-danger);
}

.empty-icon {
  opacity: 0.5;
}

.state-text {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-muted);
}

.state-hint {
  font-size: var(--font-size-sm);
  color: var(--color-text-hint);
  max-width: 340px;
}

.retry-btn {
  margin-top: var(--space-2);
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-danger);
  background: transparent;
  color: var(--color-danger);
  font-size: var(--font-size-sm);
  font-family: var(--font-sans);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.retry-btn:hover {
  background: rgba(220, 53, 69, 0.08);
}

/* ── Request list ── */
.request-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.request-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  transition: opacity var(--transition-fast);
}

.request-card.resolving {
  opacity: 0.5;
  pointer-events: none;
}

.request-avatar {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  background: var(--color-dark-secondary);
  color: var(--color-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  flex-shrink: 0;
}

.request-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.request-name {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}

.request-email {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.request-date {
  font-size: var(--font-size-xs);
  color: var(--color-text-hint);
}

/* ── Action buttons ── */
.request-actions {
  display: flex;
  gap: var(--space-2);
  flex-shrink: 0;
}

.action-btn {
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  font-family: var(--font-sans);
  cursor: pointer;
  transition: background var(--transition-fast), opacity var(--transition-fast);
  border: 1px solid transparent;
}

.action-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.action-btn--accept {
  background: var(--color-accent);
  color: var(--color-dark-primary);
  border-color: var(--color-accent);
}

.action-btn--accept:hover:not(:disabled) {
  filter: brightness(1.1);
}

.action-btn--decline {
  background: transparent;
  color: var(--color-danger);
  border-color: var(--color-danger);
}

.action-btn--decline:hover:not(:disabled) {
  background: rgba(220, 53, 69, 0.08);
}

/* ── Responsive ── */
@media (max-width: 600px) {
  .request-card {
    flex-wrap: wrap;
  }
  .request-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
