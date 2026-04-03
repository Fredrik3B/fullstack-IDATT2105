<template>
  <div class="admin-root">

    <!-- Page header -->
    <div class="page-header">
      <div class="page-header-inner">
        <div>
          <h1 class="page-title">Admin Panel</h1>
          <p class="page-sub">Manage your team members and access requests.</p>
        </div>
        <div class="header-badge" v-if="activeTab === 'requests' && pending.length > 0">
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

      <!-- Tabs -->
      <div class="tab-bar">
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'members' }"
          @click="switchTab('members')"
        >
          Members
          <span v-if="members.length > 0" class="tab-count">{{ members.length }}</span>
        </button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'requests' }"
          @click="switchTab('requests')"
        >
          Requests
          <span v-if="pending.length > 0" class="tab-count tab-count--accent">{{ pending.length }}</span>
        </button>
      </div>
    </div>

    <div class="page-body">

      <!-- ── Members tab ── -->
      <template v-if="activeTab === 'members'">
        <div v-if="membersLoading" class="state-box">
          <span class="state-text">Loading members…</span>
        </div>

        <div v-else-if="membersError" class="state-box state-box--error">
          <span class="state-text">{{ membersError }}</span>
          <button class="retry-btn" @click="loadMembers">Try again</button>
        </div>

        <div v-else-if="members.length === 0" class="state-box">
          <span class="state-text">No members yet</span>
          <span class="state-hint">Share the join code above so staff can request access.</span>
        </div>

        <div v-else class="member-list">
          <div v-for="section in memberSections" :key="section.role" class="member-section">
            <h3 v-if="section.members.length > 0" class="section-header">
              {{ section.label }}
              <span class="section-count">{{ section.members.length }}</span>
            </h3>
            <ul class="section-members">
              <li
                v-for="member in section.members"
                :key="member.userId"
                class="member-card"
                :class="{ processing: processing === member.userId }"
              >
                <div class="request-avatar">
                  {{ initials(member.firstName, member.lastName) }}
                </div>

                <div class="request-info">
                  <span class="request-name">
                    {{ member.firstName }} {{ member.lastName }}
                    <span v-if="member.userId === auth.user?.id" class="you-badge">You</span>
                  </span>
                  <span class="request-email">{{ member.email }}</span>
                  <div class="role-badges">
                    <span v-for="role in member.roles" :key="role" class="role-badge" :class="roleBadgeClass(role)">
                      {{ roleLabel(role) }}
                    </span>
                  </div>
                </div>

                <div v-if="isAdmin && member.userId !== auth.user?.id" class="member-actions">
                  <select
                    class="role-select"
                    :value="primaryRole(member.roles)"
                    :disabled="processing === member.userId"
                    @change="changeRole(member, $event.target.value)"
                  >
                    <option value="STAFF">Staff</option>
                    <option value="HR">HR</option>
                    <option value="MANAGER">Manager</option>
                    <option value="ADMIN">Admin</option>
                  </select>
                  <button
                    class="action-btn action-btn--decline"
                    :disabled="processing === member.userId"
                    @click="confirmRemove(member)"
                  >
                    Remove
                  </button>
                </div>
              </li>
            </ul>
          </div>
        </div>
      </template>

      <!-- ── Requests tab ── -->
      <template v-else>
        <div v-if="loading" class="state-box">
          <span class="state-text">Loading requests…</span>
        </div>

        <div v-else-if="error" class="state-box state-box--error">
          <span class="state-text">{{ error }}</span>
          <button class="retry-btn" @click="loadRequests">Try again</button>
        </div>

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
      </template>

    </div>

    <!-- Confirm remove dialog -->
    <div v-if="confirmTarget" class="dialog-overlay" @click.self="confirmTarget = null">
      <div class="dialog">
        <h2 class="dialog-title">Remove member?</h2>
        <p class="dialog-body">
          <strong>{{ confirmTarget.firstName }} {{ confirmTarget.lastName }}</strong> will lose access to the organization. They can request to rejoin using the join code.
        </p>
        <div class="dialog-actions">
          <button class="action-btn action-btn--ghost" @click="confirmTarget = null">Cancel</button>
          <button class="action-btn action-btn--decline" @click="removeMember">Remove</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const auth = useAuthStore()
const toast = useToast()

const activeTab = ref('members')

// ── Members state ──────────────────────────────────────────────────────────

const members       = ref([])
const membersLoading = ref(true)
const membersError  = ref(null)
const processing    = ref(null)
const confirmTarget = ref(null)

const isAdmin = computed(() => auth.userRoles?.includes('ROLE_ADMIN'))

const memberSections = computed(() => {
  const sections = [
    { role: 'ADMIN', label: 'Administrators', members: [] },
    { role: 'MANAGER', label: 'Managers', members: [] },
    { role: 'HR', label: 'HR', members: [] },
    { role: 'STAFF', label: 'Staff', members: [] },
  ]
  
  members.value.forEach(member => {
    const role = primaryRole(member.roles)
    const section = sections.find(s => s.role === role)
    if (section) {
      section.members.push(member)
    }
  })
  
  return sections
})

async function loadMembers() {
  membersLoading.value = true
  membersError.value   = null
  try {
    members.value = await auth.fetchMembers()
  } catch (e) {
    membersError.value = e?.response?.data?.message ?? 'Failed to load members.'
  } finally {
    membersLoading.value = false
  }
}

function confirmRemove(member) {
  confirmTarget.value = member
}

async function removeMember() {
  const member = confirmTarget.value
  confirmTarget.value = null
  processing.value = member.userId
  try {
    await auth.removeMember(member.userId)
    members.value = members.value.filter(m => m.userId !== member.userId)
    toast.success(`${member.firstName} ${member.lastName} has been removed.`)
  } catch (e) {
    toast.error(e?.response?.data?.message ?? 'Failed to remove member.')
  } finally {
    processing.value = null
  }
}

async function changeRole(member, newRole) {
  processing.value = member.userId
  try {
    await auth.updateMemberRoles(member.userId, [newRole])
    member.roles = [newRole]
    toast.success(`Role updated to ${roleLabel(newRole)}.`)
  } catch (e) {
    toast.error(e?.response?.data?.message ?? 'Failed to update role.')
  } finally {
    processing.value = null
  }
}

// ── Requests state ─────────────────────────────────────────────────────────

const pending   = ref([])
const loading   = ref(false)
const error     = ref(null)
const resolving = ref(null)

async function loadRequests() {
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

async function resolve(req, action) {
  resolving.value = req.requestId
  try {
    await auth.resolveJoinRequest(req.requestId, action)
    pending.value = pending.value.filter(r => r.requestId !== req.requestId)
    if (action === 'ACCEPTED') {
      toast.success(`${req.firstName} ${req.lastName} has been granted access.`)
      // Refresh members list so the new member appears
      loadMembers()
    } else {
      toast.info(`Request from ${req.firstName} ${req.lastName} was declined.`)
    }
  } catch (e) {
    toast.error(e?.response?.data?.message ?? 'Something went wrong. Please try again.')
  } finally {
    resolving.value = null
  }
}

// ── Tab switching ──────────────────────────────────────────────────────────

function switchTab(tab) {
  activeTab.value = tab
}

// ── Misc helpers ──────────────────────────────────────────────────────────

const copied = ref(false)
async function copyCode() {
  if (!auth.restaurantJoinCode) return
  await navigator.clipboard.writeText(auth.restaurantJoinCode)
  copied.value = true
  setTimeout(() => { copied.value = false }, 2000)
}

function initials(first, last) {
  return ((first?.[0] ?? '') + (last?.[0] ?? '')).toUpperCase() || '?'
}

function formatDate(iso) {
  if (!iso) return '—'
  return new Intl.DateTimeFormat('en-GB', { day: 'numeric', month: 'short', year: 'numeric' }).format(new Date(iso))
}

function primaryRole(roles) {
  if (!roles) return 'STAFF'
  const priority = ['ADMIN', 'MANAGER', 'HR', 'STAFF']
  return priority.find(r => roles.includes(r)) ?? 'STAFF'
}

function roleLabel(role) {
  const labels = { ADMIN: 'Admin', MANAGER: 'Manager', HR: 'HR', STAFF: 'Staff' }
  return labels[role] ?? role
}

function roleBadgeClass(role) {
  return {
    'role-badge--admin':   role === 'ADMIN',
    'role-badge--manager': role === 'MANAGER',
    'role-badge--hr':      role === 'HR',
    'role-badge--staff':   role === 'STAFF',
  }
}

onMounted(() => {
  loadMembers()
  loadRequests()
})
</script>

<style scoped>
.admin-root {
  min-height: 100vh;
  background: var(--color-bg-secondary);
}

/* ── Page header ── */
.page-header {
  background: var(--color-dark-primary);
  padding: var(--space-10) var(--space-6) 0;
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

.join-code-copy:hover,
.join-code-copy.copied {
  border-color: var(--color-accent);
  color: var(--color-accent);
}

/* ── Tab bar ── */
.tab-bar {
  max-width: 760px;
  margin: var(--space-6) auto 0;
  display: flex;
  gap: var(--space-1);
  border-bottom: 1px solid var(--color-dark-tertiary);
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  background: transparent;
  border: none;
  border-bottom: 2px solid transparent;
  color: var(--color-dark-border);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  font-family: var(--font-sans);
  cursor: pointer;
  transition: color var(--transition-fast), border-color var(--transition-fast);
  margin-bottom: -1px;
}

.tab-btn:hover {
  color: #ffffff;
}

.tab-btn.active {
  color: var(--color-accent);
  border-bottom-color: var(--color-accent);
}

.tab-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 var(--space-1);
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.1);
  color: var(--color-dark-border);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
}

.tab-count--accent {
  background: var(--color-accent);
  color: var(--color-dark-primary);
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

/* ── Member list ── */
.member-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.member-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.section-header {
  margin: 0;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: 0 var(--space-1);
}

.section-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 var(--space-1);
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.08);
  color: var(--color-text-hint);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  letter-spacing: normal;
}

.section-members {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.member-card {
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

.member-card.processing {
  opacity: 0.5;
  pointer-events: none;
}

.you-badge {
  display: inline-block;
  padding: 1px var(--space-2);
  border-radius: var(--radius-full);
  background: rgba(255,255,255,0.08);
  color: var(--color-text-hint);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
  margin-left: var(--space-2);
  vertical-align: middle;
}

.role-badges {
  display: flex;
  gap: var(--space-1);
  flex-wrap: wrap;
  margin-top: var(--space-1);
}

.role-badge {
  padding: 1px var(--space-2);
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
  border: 1px solid transparent;
}

.role-badge--admin   { background: rgba(255,193,7,0.15); color: #ffc107; border-color: rgba(255,193,7,0.3); }
.role-badge--manager { background: rgba(13,110,253,0.15); color: #6ea8fe; border-color: rgba(13,110,253,0.3); }
.role-badge--hr      { background: rgba(102,16,242,0.15); color: #b08fff; border-color: rgba(102,16,242,0.3); }
.role-badge--staff   { background: rgba(255,255,255,0.06); color: var(--color-text-hint); border-color: var(--color-border); }

.member-actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-shrink: 0;
}

.role-select {
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  background: var(--color-bg-secondary);
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  font-family: var(--font-sans);
  cursor: pointer;
}

.role-select:disabled {
  opacity: 0.4;
  cursor: not-allowed;
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

/* ── Shared card parts ── */
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

.action-btn--ghost {
  background: transparent;
  color: var(--color-text-muted);
  border-color: var(--color-border);
}

.action-btn--ghost:hover {
  background: rgba(255,255,255,0.05);
}

/* ── Confirm dialog ── */
.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.dialog {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-8);
  max-width: 420px;
  width: 90%;
  box-shadow: var(--shadow-lg);
}

.dialog-title {
  margin: 0 0 var(--space-3);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.dialog-body {
  margin: 0 0 var(--space-6);
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
  line-height: 1.6;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-2);
}

/* ── Responsive ── */
@media (max-width: 600px) {
  .request-card,
  .member-card {
    flex-wrap: wrap;
  }
  .request-actions,
  .member-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
