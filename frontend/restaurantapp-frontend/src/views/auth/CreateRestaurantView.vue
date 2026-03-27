<template>
  <div class="onboarding-page">

    <!-- Top bar -->
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

      <!-- ── SUCCESS STATE ── -->
      <template v-if="submitted">
        <div class="result-card">
          <div class="result-icon result-icon--success">
            <svg viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.5"/>
              <path d="M7.5 12.5L10.5 15.5L16.5 9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h1 class="result-title">Restaurant opprettet!</h1>
          <p class="result-body">
            <strong>{{ form.name }}</strong> er nå registrert i IKSystem.
            Du er satt som administrator. Del restaurantkoden med dine ansatte så de kan be om tilgang.
          </p>
          <div class="join-code-display">
            <span class="join-code-label">Restaurantkode</span>
            <div class="join-code-value">
              <span class="join-code-text">{{ joinCode }}</span>
            </div>
            <span class="join-code-hint">Del denne koden med ansatte så de kan be om tilgang</span>
          </div>
          <button class="btn-primary" @click="$router.push({ name: 'dashboard' })">
            Gå til dashbord
            <svg viewBox="0 0 16 16" fill="none">
              <path d="M3 8H13M9 4L13 8L9 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
      </template>

      <!-- ── FORM ── -->
      <template v-else>
        <div class="form-card">
          <div class="form-card-header">
            <RouterLink to="/onboarding" class="back-btn">
              <svg viewBox="0 0 16 16" fill="none">
                <path d="M10 4L6 8L10 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              Tilbake
            </RouterLink>
            <div>
              <h1 class="form-title">Opprett restaurant</h1>
              <p class="form-subtitle">Fyll ut opplysningene om restauranten din</p>
            </div>
          </div>

          <form class="restaurant-form" @submit.prevent="handleSubmit">

            <div class="form-section">
              <span class="section-label">Generell informasjon</span>

              <div class="field-group" :class="{ 'has-error': errors.name }">
                <label class="field-label" for="restName">Restaurantnavn</label>
                <div class="field-wrapper">
                  <svg class="field-icon" viewBox="0 0 20 20" fill="none">
                    <path d="M3 6H17M3 6C3 6 4 4 10 4C16 4 17 6 17 6M3 6V16C3 16.55 3.45 17 4 17H16C16.55 17 17 16.55 17 16V6M7 10H13M7 13H11" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <input id="restName" v-model="form.name" type="text" class="field-input"
                    placeholder="Everest Sushi & Fusion AS" autocomplete="organization"
                    @input="clearError('name')" />
                </div>
                <span v-if="errors.name" class="field-error">{{ errors.name }}</span>
              </div>

              <div class="field-group" :class="{ 'has-error': errors.orgNumber }">
                <label class="field-label" for="orgNumber">
                  Organisasjonsnummer
                  <span class="label-hint">9 siffer</span>
                </label>
                <div class="field-wrapper">
                  <svg class="field-icon" viewBox="0 0 20 20" fill="none">
                    <rect x="3" y="4" width="14" height="13" rx="2" stroke="currentColor" stroke-width="1.3"/>
                    <path d="M7 8H13M7 11H11" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>
                  </svg>
                  <input id="orgNumber" v-model="form.orgNumber" type="text" class="field-input"
                    placeholder="123 456 789" maxlength="11" inputmode="numeric"
                    @input="handleOrgInput" />
                </div>
                <span v-if="errors.orgNumber" class="field-error">{{ errors.orgNumber }}</span>
              </div>
            </div>

            <div class="form-section">
              <span class="section-label">Adresse</span>

              <div class="field-group" :class="{ 'has-error': errors.address }">
                <label class="field-label" for="address">Gateadresse</label>
                <div class="field-wrapper">
                  <svg class="field-icon" viewBox="0 0 20 20" fill="none">
                    <path d="M10 2C7.24 2 5 4.24 5 7C5 10.5 10 18 10 18C10 18 15 10.5 15 7C15 4.24 12.76 2 10 2Z" stroke="currentColor" stroke-width="1.3" stroke-linejoin="round"/>
                    <circle cx="10" cy="7" r="2" stroke="currentColor" stroke-width="1.3"/>
                  </svg>
                  <input id="address" v-model="form.address" type="text" class="field-input"
                    placeholder="Storgata 1" autocomplete="street-address"
                    @input="clearError('address')" />
                </div>
                <span v-if="errors.address" class="field-error">{{ errors.address }}</span>
              </div>

              <div class="field-row">
                <div class="field-group field-group--narrow" :class="{ 'has-error': errors.postalCode }">
                  <label class="field-label" for="postalCode">Postnummer</label>
                  <div class="field-wrapper">
                    <input id="postalCode" v-model="form.postalCode" type="text" class="field-input field-input--no-icon"
                      placeholder="0150" maxlength="4" inputmode="numeric"
                      @input="handlePostalInput" />
                  </div>
                  <span v-if="errors.postalCode" class="field-error">{{ errors.postalCode }}</span>
                </div>

                <div class="field-group field-group--grow" :class="{ 'has-error': errors.city }">
                  <label class="field-label" for="city">Sted</label>
                  <div class="field-wrapper">
                    <input id="city" v-model="form.city" type="text" class="field-input field-input--no-icon"
                      placeholder="Oslo" autocomplete="address-level2"
                      @input="clearError('city')" />
                  </div>
                  <span v-if="errors.city" class="field-error">{{ errors.city }}</span>
                </div>
              </div>
            </div>

            <div v-if="submitError" class="alert alert--error">
              <svg viewBox="0 0 20 20" fill="none">
                <circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="1.5"/>
                <path d="M10 6V10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                <circle cx="10" cy="13.5" r="0.75" fill="currentColor"/>
              </svg>
              {{ submitError }}
            </div>

            <button type="submit" class="btn-primary" :disabled="isLoading">
              <span v-if="!isLoading">Opprett restaurant</span>
              <span v-else class="spinner"></span>
            </button>

          </form>
        </div>
      </template>

    </div>

    <p class="page-footer">
      IKSystem &copy; {{ new Date().getFullYear() }} &mdash; Internkontroll for næringsmiddelvirksomheter
    </p>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

// ── User info from store ──
const userName    = computed(() => auth.user?.name ?? '')
const userInitials = computed(() => auth.userInitials)

const form = reactive({
  name: '',
  orgNumber: '',
  address: '',
  postalCode: '',
  city: ''
})

const errors = reactive({
  name: '',
  orgNumber: '',
  address: '',
  postalCode: '',
  city: ''
})

const isLoading  = ref(false)
const submitError = ref('')
const submitted  = ref(false)
const joinCode   = ref('')   // populated from backend response on success

function clearError(field) { errors[field] = '' }

function handleOrgInput(e) {
  const digits = e.target.value.replace(/\D/g, '').slice(0, 9)
  form.orgNumber = digits.replace(/(\d{3})(\d{1,3})?(\d{1,3})?/, (_, a, b, c) =>
    [a, b, c].filter(Boolean).join(' ')
  )
  clearError('orgNumber')
}

function handlePostalInput(e) {
  form.postalCode = e.target.value.replace(/\D/g, '').slice(0, 4)
  clearError('postalCode')
}

function validate() {
  let valid = true
  if (!form.name.trim()) { errors.name = 'Restaurantnavn er påkrevd'; valid = false }
  const orgDigits = form.orgNumber.replace(/\s/g, '')
  if (!orgDigits) { errors.orgNumber = 'Organisasjonsnummer er påkrevd'; valid = false }
  else if (orgDigits.length !== 9) { errors.orgNumber = 'Organisasjonsnummer må ha 9 siffer'; valid = false }
  if (!form.address.trim()) { errors.address = 'Gateadresse er påkrevd'; valid = false }
  if (!form.postalCode) { errors.postalCode = 'Postnummer er påkrevd'; valid = false }
  else if (form.postalCode.length !== 4) { errors.postalCode = 'Ugyldig postnummer'; valid = false }
  if (!form.city.trim()) { errors.city = 'Sted er påkrevd'; valid = false }
  return valid
}

async function handleSubmit() {
  if (!validate()) return
  isLoading.value = true
  submitError.value = ''
  try {
    const result = await auth.createRestaurant({ ...form, orgNumber: form.orgNumber.replace(/\s/g, '') })
    joinCode.value = result.joinCode
    submitted.value = true
  } catch {
    submitError.value = 'Noe gikk galt. Prøv igjen.'
  } finally {
    isLoading.value = false
  }
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

.topbar-brand { display: flex; align-items: center; gap: var(--space-3); }

.brand-logo {
  width: 36px; height: 36px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: var(--radius-md);
  display: flex; align-items: center; justify-content: center;
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

.topbar-user { display: flex; align-items: center; gap: var(--space-3); }

.user-avatar {
  width: 32px; height: 32px;
  border-radius: 50%;
  background: var(--color-dark-tertiary);
  border: 1.5px solid rgba(255,255,255,0.15);
  display: flex; align-items: center; justify-content: center;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  color: var(--color-accent);
}

.user-name { font-size: var(--font-size-sm); color: rgba(255, 255, 255, 0.7); }

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

.btn-logout:hover { color: rgba(255, 255, 255, 0.7); border-color: rgba(255, 255, 255, 0.25); }

/* ── Body ── */
.onboarding-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-12) var(--space-6);
}

/* ── Form card ── */
.form-card {
  width: 100%;
  max-width: 520px;
  background: var(--color-bg-primary);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  padding: var(--space-10) var(--space-8);
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
}

.form-card-header {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-muted);
  text-decoration: none;
  transition: color var(--transition-fast);
  align-self: flex-start;
}

.back-btn:hover { color: var(--color-text-secondary); }
.back-btn svg { width: 14px; height: 14px; }

.form-title {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  line-height: var(--line-height-tight);
  margin: 0 0 var(--space-1);
}

.form-subtitle { font-size: var(--font-size-md); color: var(--color-text-muted); margin: 0; }

/* ── Form sections ── */
.restaurant-form { display: flex; flex-direction: column; gap: var(--space-8); }

.form-section { display: flex; flex-direction: column; gap: var(--space-4); }

.section-label {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.8px;
}

.field-row {
  display: flex;
  gap: var(--space-3);
  align-items: flex-start;
}

.field-group { display: flex; flex-direction: column; gap: var(--space-2); }
.field-group--narrow { width: 110px; flex-shrink: 0; }
.field-group--grow { flex: 1; }

.field-label {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-secondary);
}

.label-hint {
  font-size: var(--font-size-xs);
  color: var(--color-text-hint);
  font-weight: var(--font-weight-normal);
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
  padding: 0 var(--space-4) 0 40px;
  font-size: var(--font-size-md); font-family: var(--font-sans);
  color: var(--color-text-primary);
  background: var(--color-bg-secondary);
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  outline: none;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast), background var(--transition-fast);
  box-sizing: border-box;
}

.field-input--no-icon { padding-left: var(--space-4); }

.field-input::placeholder { color: var(--color-text-hint); }

.field-input:focus {
  border-color: var(--color-dark-secondary);
  box-shadow: 0 0 0 3px rgba(45, 43, 85, 0.12);
  background: var(--color-bg-primary);
}

.has-error .field-input { border-color: var(--color-danger); }
.has-error .field-input:focus { box-shadow: 0 0 0 3px rgba(204, 51, 51, 0.12); }

.field-error { font-size: var(--font-size-xs); color: var(--color-danger); }

/* ── Alert ── */
.alert {
  display: flex; align-items: center; gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
}

.alert svg { width: 16px; height: 16px; flex-shrink: 0; }

.alert--error {
  background: var(--color-danger-bg);
  border: 1px solid var(--color-danger-border);
  color: var(--color-danger-text);
}

.alert--error svg { color: var(--color-danger); }

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
  gap: var(--space-2);
}

.btn-primary svg { width: 16px; height: 16px; }
.btn-primary:hover:not(:disabled) { background: var(--color-dark-secondary); }
.btn-primary:active:not(:disabled) { transform: scale(0.99); }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }

/* ── Spinner ── */
.spinner {
  width: 18px; height: 18px;
  border: 2px solid rgba(212, 232, 53, 0.3);
  border-top-color: var(--color-accent);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

/* ── Result card ── */
.result-card {
  width: 100%;
  max-width: 480px;
  background: var(--color-bg-primary);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  padding: var(--space-10) var(--space-8);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-5);
  text-align: center;
}

.result-icon {
  width: 64px; height: 64px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
}

.result-icon svg { width: 32px; height: 32px; }

.result-icon--success {
  background: var(--color-success-bg);
  border: 1.5px solid var(--color-success-border);
  color: var(--color-success-text);
}

.result-title {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  margin: 0;
}

.result-body {
  font-size: var(--font-size-md);
  color: var(--color-text-muted);
  line-height: var(--line-height-normal);
  margin: 0;
}

.join-code-display {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-5);
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
}

.join-code-label {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  text-transform: uppercase;
  letter-spacing: 0.8px;
  color: var(--color-text-muted);
}

.join-code-value {
  padding: var(--space-2) var(--space-6);
  background: var(--color-dark-primary);
  border-radius: var(--radius-md);
}

.join-code-text {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-accent);
  letter-spacing: 1px;
}

.join-code-hint {
  font-size: var(--font-size-xs);
  color: var(--color-text-hint);
}

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
  .form-card { padding: var(--space-8) var(--space-5); }
  .field-row { flex-direction: column; }
  .field-group--narrow { width: 100%; }
}
</style>
