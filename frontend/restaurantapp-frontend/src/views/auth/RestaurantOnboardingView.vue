<template>
  <div class="onboarding-page">

    <!-- Top bar — minimal, shows logged-in user -->
    <div class="topbar">
      <div class="topbar-brand">
        <div class="brand-logo"><span class="brand-icon">IK</span></div>
        <span class="brand-name">IKSystem</span>
      </div>
      <div class="topbar-user">
        <div class="user-avatar">{{ userInitials }}</div>
        <span class="user-name">{{ userName }}</span>
        <button class="btn-logout" @click="handleLogout">Logg ut</button>
      </div>
    </div>

    <div class="onboarding-body">

      <!-- ── PENDING STATE ── -->
      <template v-if="view === 'pending'">
        <div class="onboarding-card">
          <div class="pending-icon">
            <svg viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.5"/>
              <path d="M12 7V12L15 14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h1 class="onboarding-title">Venter på godkjenning</h1>
          <p class="onboarding-subtitle">
            Din forespørsel til <strong>{{ pendingRestaurantName }}</strong> er sendt.
            En administrator vil godkjenne eller avslå forespørselen.
            Du vil bli varslet på <strong>{{ userEmail }}</strong>.
          </p>
          <div class="pending-meta">
            <div class="meta-row">
              <svg viewBox="0 0 16 16" fill="none">
                <path d="M3 10C3 10 5 5 8 5H11C11.55 5 12 5.45 12 6V10C12 10.55 11.55 11 11 11H8C5 11 3 10 3 10Z" stroke="currentColor" stroke-width="1.2" stroke-linejoin="round"/>
                <circle cx="10.5" cy="8" r="1" stroke="currentColor" stroke-width="1"/>
              </svg>
              {{ pendingRestaurantName }}
            </div>
            <div class="meta-row">
              <svg viewBox="0 0 16 16" fill="none">
                <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.2"/>
                <path d="M8 5V8" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
                <circle cx="8" cy="10.5" r="0.6" fill="currentColor"/>
              </svg>
              Sendt {{ pendingSentDate }}
            </div>
          </div>
          <div class="pending-actions">
            <button class="btn-ghost btn-ghost--danger" @click="withdrawRequest">
              Trekk tilbake forespørsel
            </button>
          </div>
        </div>
        <p class="help-text">
          Feil restaurant? Du kan
          <button class="inline-link" @click="view = 'choose'">velge en annen</button>
          eller
          <RouterLink to="/onboarding/create" class="inline-link">opprette en ny restaurant</RouterLink>.
        </p>
      </template>

      <!-- ── CHOOSE STATE ── -->
      <template v-else-if="view === 'choose'">
        <div class="choose-header">
          <h1 class="onboarding-title">Koble til restaurant</h1>
          <p class="onboarding-subtitle">
            For å bruke IKSystem må kontoen din være tilknyttet en restaurant.
            Du kan bli med i en eksisterende eller opprette en ny.
          </p>
        </div>

        <div class="option-grid">
          <button class="option-card" @click="view = 'join'">
            <div class="option-icon option-icon--join">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M17 12H7M7 12L11 8M7 12L11 16" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M3 6C3 4.89543 3.89543 4 5 4H19C20.1046 4 21 4.89543 21 6V18C21 19.1046 20.1046 20 19 20H5C3.89543 20 3 19.1046 3 18V6Z" stroke="currentColor" stroke-width="1.5"/>
              </svg>
            </div>
            <div class="option-text">
              <span class="option-title">Bli med i restaurant</span>
              <span class="option-desc">Skriv inn restaurantkoden du har fått fra din leder</span>
            </div>
            <svg class="option-arrow" viewBox="0 0 16 16" fill="none">
              <path d="M6 4L10 8L6 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>

          <button class="option-card" @click="$router.push({ name: 'create-restaurant' })">
            <div class="option-icon option-icon--create">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M12 5V19M5 12H19" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="option-text">
              <span class="option-title">Opprett ny restaurant</span>
              <span class="option-desc">Registrer en ny restaurant og bli administrator</span>
            </div>
            <svg class="option-arrow" viewBox="0 0 16 16" fill="none">
              <path d="M6 4L10 8L6 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
      </template>

      <!-- ── JOIN STATE ── -->
      <template v-else-if="view === 'join'">
        <div class="onboarding-card">
          <button class="back-btn" @click="view = 'choose'">
            <svg viewBox="0 0 16 16" fill="none">
              <path d="M10 4L6 8L10 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            Tilbake
          </button>

          <h1 class="onboarding-title">Bli med i restaurant</h1>
          <p class="onboarding-subtitle">
            Skriv inn koden du har fått fra din daglige leder. Formatet er <strong>XXX-0000</strong>.
          </p>

          <div class="join-form">
            <div class="field-group" :class="{ 'has-error': joinError }">
              <label class="field-label" for="joinCode">Restaurantkode</label>
              <div class="field-wrapper">
                <svg class="field-icon" viewBox="0 0 20 20" fill="none">
                  <path d="M3 10C3 10 6 4 10 4H14C15.1 4 16 4.9 16 6V14C16 15.1 15.1 16 14 16H10C6 16 3 10 3 10Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                  <circle cx="13" cy="10" r="1.5" stroke="currentColor" stroke-width="1.2"/>
                </svg>
                <input
                  id="joinCode"
                  v-model="joinCode"
                  type="text"
                  class="field-input field-input--code"
                  placeholder="EVR-2847"
                  maxlength="8"
                  autocomplete="off"
                  @input="handleCodeInput"
                />
                <span v-if="codeStatus === 'valid'" class="code-badge code-badge--valid">
                  <svg viewBox="0 0 12 12" fill="none">
                    <path d="M2 6L5 9L10 3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </span>
                <span v-else-if="codeStatus === 'invalid'" class="code-badge code-badge--invalid">
                  <svg viewBox="0 0 12 12" fill="none">
                    <path d="M3 3L9 9M9 3L3 9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </span>
              </div>
              <span v-if="codeStatus === 'valid'" class="field-hint--valid">{{ resolvedRestaurantName }}</span>
              <span v-if="joinError" class="field-error">{{ joinError }}</span>
            </div>

            <button class="btn-primary" :disabled="isLoading || codeStatus !== 'valid'" @click="sendJoinRequest">
              <span v-if="!isLoading">Send tilgangsforespørsel</span>
              <span v-else class="spinner"></span>
            </button>
          </div>
        </div>
      </template>

    </div>

    <p class="page-footer">
      IKSystem &copy; {{ new Date().getFullYear() }} &mdash; Internkontroll for næringsmiddelvirksomheter
    </p>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/api/axiosInstance'

const auth = useAuthStore()

// ── User info from store ──
const userName    = computed(() => auth.user?.name  ?? '')
const userEmail   = computed(() => auth.user?.email ?? '')
const userInitials = computed(() => auth.userInitials)

// ── View state: 'choose' | 'join' | 'pending' ──
// Start in pending if the user already has a pending request
const view = ref(auth.restaurantStatus === 'pending' ? 'pending' : 'choose')

// ── Join flow ──
const joinCode             = ref('')
const codeStatus           = ref('')   // '' | 'loading' | 'valid' | 'invalid'
const joinError            = ref('')
const resolvedRestaurantName = ref('')
const isLoading            = ref(false)

// ── Pending state ──
// If the user arrives already pending, we don't know the restaurant name yet —
// the backend should return it via GET /api/auth/me. For now show a fallback.
const pendingRestaurantName = ref(auth.restaurantStatus === 'pending' ? 'restauranten' : '')
const pendingSentDate       = ref('')

// ── Code lookup ──
let lookupTimeout = null

async function handleCodeInput(e) {
  const raw = e.target.value.toUpperCase().replace(/[^A-Z0-9-]/g, '')
  joinCode.value = raw
  joinError.value = ''
  codeStatus.value = ''
  resolvedRestaurantName.value = ''

  if (raw.length !== 8) return

  // Debounce slightly so we don't fire on every keystroke
  clearTimeout(lookupTimeout)
  lookupTimeout = setTimeout(async () => {
    codeStatus.value = 'loading'
    try {
      const { data } = await api.get(`/api/restaurants/lookup?code=${raw}`)
      codeStatus.value = 'valid'
      resolvedRestaurantName.value = data.name
    } catch (err) {
      codeStatus.value = err.response?.status === 404 ? 'invalid' : 'invalid'
    }
  }, 300)
}

async function sendJoinRequest() {
  if (codeStatus.value !== 'valid') {
    joinError.value = 'Ugyldig restaurantkode'
    return
  }
  isLoading.value = true
  try {
    await auth.joinRestaurant(joinCode.value)
    pendingRestaurantName.value = resolvedRestaurantName.value
    pendingSentDate.value = new Date().toLocaleDateString('nb-NO', { day: 'numeric', month: 'long', year: 'numeric' })
    view.value = 'pending'
  } catch {
    joinError.value = 'Noe gikk galt. Prøv igjen.'
  } finally {
    isLoading.value = false
  }
}

async function withdrawRequest() {
  await auth.withdrawJoinRequest()
  pendingRestaurantName.value = ''
  pendingSentDate.value = ''
  joinCode.value = ''
  codeStatus.value = ''
  resolvedRestaurantName.value = ''
  view.value = 'choose'
}

function handleLogout() {
  auth.logout()
}
</script>

<style scoped>
/* ── Page ── */
.onboarding-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(145deg, var(--color-dark-primary) 0%, var(--color-dark-secondary) 100%);
}

/* ── Topbar ── */
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) var(--space-8);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.topbar-brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.brand-logo {
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-icon {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  color: var(--color-accent);
  letter-spacing: 0.5px;
}

.brand-name {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  color: rgba(255, 255, 255, 0.9);
}

.topbar-user {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-dark-tertiary);
  border: 1.5px solid rgba(255,255,255,0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  color: var(--color-accent);
}

.user-name {
  font-size: var(--font-size-sm);
  color: rgba(255, 255, 255, 0.7);
}

.btn-logout {
  font-size: var(--font-size-xs);
  color: rgba(255, 255, 255, 0.4);
  background: none;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: var(--radius-sm);
  padding: var(--space-1) var(--space-3);
  cursor: pointer;
  transition: color var(--transition-fast), border-color var(--transition-fast);
  font-family: var(--font-sans);
}

.btn-logout:hover {
  color: rgba(255, 255, 255, 0.7);
  border-color: rgba(255, 255, 255, 0.25);
}

/* ── Body ── */
.onboarding-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-12) var(--space-6);
  gap: var(--space-6);
}

/* ── Choose header ── */
.choose-header {
  text-align: center;
  max-width: 480px;
}

.onboarding-title {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: #ffffff;
  margin: 0 0 var(--space-3);
  line-height: var(--line-height-tight);
}

.onboarding-subtitle {
  font-size: var(--font-size-md);
  color: rgba(255, 255, 255, 0.55);
  margin: 0;
  line-height: var(--line-height-normal);
}

/* ── Option grid ── */
.option-grid {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  width: 100%;
  max-width: 480px;
}

.option-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5) var(--space-6);
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-xl);
  cursor: pointer;
  text-align: left;
  transition: background var(--transition-fast), border-color var(--transition-fast), transform var(--transition-fast);
  font-family: var(--font-sans);
}

.option-card:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.2);
  transform: translateY(-1px);
}

.option-card:active { transform: translateY(0); }

.option-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.option-icon svg { width: 24px; height: 24px; }

.option-icon--join {
  background: rgba(212, 232, 53, 0.12);
  color: var(--color-accent);
}

.option-icon--create {
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.7);
}

.option-text {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  flex: 1;
}

.option-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: #ffffff;
  line-height: var(--line-height-tight);
}

.option-desc {
  font-size: var(--font-size-sm);
  color: rgba(255, 255, 255, 0.5);
  line-height: var(--line-height-normal);
}

.option-arrow {
  width: 16px;
  height: 16px;
  color: rgba(255, 255, 255, 0.3);
  flex-shrink: 0;
}

/* ── Onboarding card (join + pending) ── */
.onboarding-card {
  width: 100%;
  max-width: 480px;
  background: var(--color-bg-primary);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  padding: var(--space-10) var(--space-8);
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

/* pending card overrides */
.onboarding-card .onboarding-title { color: var(--color-text-primary); }
.onboarding-card .onboarding-subtitle { color: var(--color-text-muted); }

/* ── Back button ── */
.back-btn {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  background: none;
  border: none;
  padding: 0;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-muted);
  cursor: pointer;
  transition: color var(--transition-fast);
  font-family: var(--font-sans);
  align-self: flex-start;
}

.back-btn:hover { color: var(--color-text-secondary); }
.back-btn svg { width: 14px; height: 14px; }

/* ── Join form ── */
.join-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.field-group { display: flex; flex-direction: column; gap: var(--space-2); }

.field-label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-secondary);
}

.field-wrapper { position: relative; display: flex; align-items: center; }

.field-icon {
  position: absolute; left: var(--space-3);
  width: 16px; height: 16px;
  color: var(--color-text-hint);
  pointer-events: none;
}

.field-input {
  width: 100%; height: 44px;
  padding: 0 40px 0 40px;
  font-size: var(--font-size-md); font-family: var(--font-sans);
  color: var(--color-text-primary);
  background: var(--color-bg-secondary);
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  outline: none;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast), background var(--transition-fast);
  box-sizing: border-box;
}

.field-input--code {
  letter-spacing: 3px;
  font-weight: var(--font-weight-medium);
  text-transform: uppercase;
}

.field-input::placeholder { color: var(--color-text-hint); letter-spacing: normal; }

.field-input:focus {
  border-color: var(--color-dark-secondary);
  box-shadow: 0 0 0 3px rgba(45, 43, 85, 0.12);
  background: var(--color-bg-primary);
}

.has-error .field-input { border-color: var(--color-danger); }

.code-badge {
  position: absolute; right: var(--space-3);
  width: 22px; height: 22px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}

.code-badge svg { width: 10px; height: 10px; }

.code-badge--valid {
  background: var(--color-success-bg);
  border: 1px solid var(--color-success-border);
  color: var(--color-success-text);
}

.code-badge--invalid {
  background: var(--color-danger-bg);
  border: 1px solid var(--color-danger-border);
  color: var(--color-danger-text);
}

.field-hint--valid {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
  color: var(--color-success-text);
}

.field-error { font-size: var(--font-size-xs); color: var(--color-danger); }

/* ── Pending state ── */
.pending-icon {
  width: 64px; height: 64px;
  background: var(--color-warning-bg);
  border: 1.5px solid var(--color-warning-border);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: var(--color-warning);
  align-self: center;
}

.pending-icon svg { width: 32px; height: 32px; }

.pending-meta {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--space-4);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
}

.meta-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.meta-row svg {
  width: 14px; height: 14px;
  color: var(--color-text-muted);
  flex-shrink: 0;
}

.pending-actions {
  display: flex;
  justify-content: center;
}

.btn-ghost {
  background: none;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-5);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  font-family: var(--font-sans);
  cursor: pointer;
  transition: all var(--transition-fast);
  color: var(--color-text-secondary);
}

.btn-ghost--danger { border-color: var(--color-danger-border); color: var(--color-danger-text); }
.btn-ghost--danger:hover { background: var(--color-danger-bg); }

/* ── Primary button ── */
.btn-primary {
  height: 48px; width: 100%;
  background: var(--color-dark-primary);
  color: var(--color-accent);
  border: none; border-radius: var(--radius-md);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  font-family: var(--font-sans);
  cursor: pointer;
  transition: background var(--transition-fast), transform var(--transition-fast);
  display: flex; align-items: center; justify-content: center;
}

.btn-primary:hover:not(:disabled) { background: var(--color-dark-secondary); }
.btn-primary:active:not(:disabled) { transform: scale(0.99); }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }

.spinner {
  width: 18px; height: 18px;
  border: 2px solid rgba(212, 232, 53, 0.3);
  border-top-color: var(--color-accent);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

/* ── Help text below card ── */
.help-text {
  font-size: var(--font-size-sm);
  color: rgba(255, 255, 255, 0.4);
  text-align: center;
  margin: 0;
}

.inline-link {
  background: none;
  border: none;
  padding: 0;
  font: inherit;
  color: rgba(255, 255, 255, 0.65);
  text-decoration: underline;
  text-underline-offset: 2px;
  cursor: pointer;
  transition: color var(--transition-fast);
}

.inline-link:hover { color: rgba(255, 255, 255, 0.9); }

/* ── Page footer ── */
.page-footer {
  font-size: var(--font-size-xs);
  color: rgba(255, 255, 255, 0.2);
  text-align: center;
  padding: var(--space-6);
  margin: 0;
}

/* ── Responsive ── */
@media (max-width: 600px) {
  .topbar { padding: var(--space-3) var(--space-4); }
  .user-name { display: none; }
  .onboarding-card { padding: var(--space-8) var(--space-5); }
  .option-card { padding: var(--space-4); }
  .option-icon { width: 40px; height: 40px; }
  .option-icon svg { width: 20px; height: 20px; }
}
</style>
