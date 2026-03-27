<template>
  <div class="auth-page">
    <div class="auth-card">

      <div class="brand">
        <div class="brand-logo">
          <span class="brand-icon">IK</span>
        </div>
        <div class="brand-text">
          <span class="brand-name">IKSystem</span>
          <span class="brand-sub">Internkontrollsystem</span>
        </div>
      </div>

      <div class="card-header">
        <h1 class="card-title">Opprett konto</h1>
        <p class="card-subtitle">Fyll ut skjemaet for å opprette din brukerkonto</p>
      </div>

      <form class="auth-form" @submit.prevent="handleSubmit">

        <div class="field-group" :class="{ 'has-error': errors.name }">
          <label class="field-label" for="name">Fullt navn</label>
          <div class="field-wrapper">
            <svg class="field-icon" viewBox="0 0 20 20" fill="none">
              <path d="M10 10C12.2091 10 14 8.20914 14 6C14 3.79086 12.2091 2 10 2C7.79086 2 6 3.79086 6 6C6 8.20914 7.79086 10 10 10Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M3 18C3 15.2386 6.13401 13 10 13C13.866 13 17 15.2386 17 18" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <input id="name" v-model="form.name" type="text" class="field-input"
              placeholder="Kari Nordmann" autocomplete="name" @input="clearError('name')" />
          </div>
          <span v-if="errors.name" class="field-error">{{ errors.name }}</span>
        </div>

        <div class="field-group" :class="{ 'has-error': errors.email }">
          <label class="field-label" for="email">E-post</label>
          <div class="field-wrapper">
            <svg class="field-icon" viewBox="0 0 20 20" fill="none">
              <rect x="2" y="5" width="16" height="11" rx="2" stroke="currentColor" stroke-width="1.5"/>
              <path d="M2 7L10 12L18 7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <input id="email" v-model="form.email" type="email" class="field-input"
              placeholder="navn@restaurant.no" autocomplete="email" @input="clearError('email')" />
          </div>
          <span v-if="errors.email" class="field-error">{{ errors.email }}</span>
        </div>

        <div class="field-group" :class="{ 'has-error': errors.password }">
          <label class="field-label" for="password">Passord</label>
          <div class="field-wrapper">
            <svg class="field-icon" viewBox="0 0 20 20" fill="none">
              <rect x="3" y="9" width="14" height="9" rx="2" stroke="currentColor" stroke-width="1.5"/>
              <path d="M6 9V6C6 3.79086 7.79086 2 10 2C12.2091 2 14 3.79086 14 6V9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              <circle cx="10" cy="13.5" r="1" fill="currentColor"/>
            </svg>
            <input id="password" v-model="form.password"
              :type="showPassword ? 'text' : 'password'" class="field-input"
              placeholder="Minst 8 tegn" autocomplete="new-password" @input="clearError('password')" />
            <button type="button" class="toggle-password" @click="showPassword = !showPassword" tabindex="-1">
              <svg v-if="!showPassword" viewBox="0 0 20 20" fill="none">
                <path d="M2 10C2 10 5 4 10 4C15 4 18 10 18 10C18 10 15 16 10 16C5 16 2 10 2 10Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                <circle cx="10" cy="10" r="2.5" stroke="currentColor" stroke-width="1.5"/>
              </svg>
              <svg v-else viewBox="0 0 20 20" fill="none">
                <path d="M3 3L17 17M8.5 8.68C8.18 9.01 8 9.48 8 10C8 11.1 8.9 12 10 12C10.52 12 10.99 11.82 11.32 11.5M6.5 5.17C7.59 4.44 8.76 4 10 4C15 4 18 10 18 10C17.47 10.94 16.8 11.78 16.04 12.48M2 10C2 10 2.78 8.4 4.28 7.05" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
            </button>
          </div>
          <div v-if="form.password" class="password-strength">
            <div class="strength-bar">
              <div class="strength-fill" :class="passwordStrength.level" :style="{ width: passwordStrength.width }"></div>
            </div>
            <span class="strength-label" :class="passwordStrength.level">{{ passwordStrength.label }}</span>
          </div>
          <span v-if="errors.password" class="field-error">{{ errors.password }}</span>
        </div>

        <div class="field-group" :class="{ 'has-error': errors.confirmPassword }">
          <label class="field-label" for="confirmPassword">Bekreft passord</label>
          <div class="field-wrapper">
            <svg class="field-icon" viewBox="0 0 20 20" fill="none">
              <rect x="3" y="9" width="14" height="9" rx="2" stroke="currentColor" stroke-width="1.5"/>
              <path d="M6 9V6C6 3.79086 7.79086 2 10 2C12.2091 2 14 3.79086 14 6V9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              <circle cx="10" cy="13.5" r="1" fill="currentColor"/>
            </svg>
            <input id="confirmPassword" v-model="form.confirmPassword"
              :type="showConfirmPassword ? 'text' : 'password'" class="field-input"
              placeholder="Gjenta passordet" autocomplete="new-password" @input="clearError('confirmPassword')" />
            <button type="button" class="toggle-password" @click="showConfirmPassword = !showConfirmPassword" tabindex="-1">
              <svg v-if="!showConfirmPassword" viewBox="0 0 20 20" fill="none">
                <path d="M2 10C2 10 5 4 10 4C15 4 18 10 18 10C18 10 15 16 10 16C5 16 2 10 2 10Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                <circle cx="10" cy="10" r="2.5" stroke="currentColor" stroke-width="1.5"/>
              </svg>
              <svg v-else viewBox="0 0 20 20" fill="none">
                <path d="M3 3L17 17M8.5 8.68C8.18 9.01 8 9.48 8 10C8 11.1 8.9 12 10 12C10.52 12 10.99 11.82 11.32 11.5M6.5 5.17C7.59 4.44 8.76 4 10 4C15 4 18 10 18 10C17.47 10.94 16.8 11.78 16.04 12.48M2 10C2 10 2.78 8.4 4.28 7.05" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
            </button>
          </div>
          <span v-if="errors.confirmPassword" class="field-error">{{ errors.confirmPassword }}</span>
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
          <span v-if="!isLoading">Opprett konto</span>
          <span v-else class="spinner"></span>
        </button>

      </form>

      <div class="auth-footer">
        <span class="auth-footer-text">Har du allerede konto?</span>
        <RouterLink to="/login" class="auth-link">Logg inn</RouterLink>
      </div>

    </div>

    <p class="page-footer">
      IKSystem &copy; {{ new Date().getFullYear() }} &mdash; Internkontroll for næringsmiddelvirksomheter
    </p>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const form = reactive({ name: '', email: '', password: '', confirmPassword: '' })
const errors = reactive({ name: '', email: '', password: '', confirmPassword: '' })
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const isLoading = ref(false)
const submitError = ref('')

const passwordStrength = computed(() => {
  const p = form.password
  if (!p) return { level: '', width: '0%', label: '' }
  let score = 0
  if (p.length >= 8)  score++
  if (p.length >= 12) score++
  if (/[A-Z]/.test(p)) score++
  if (/[0-9]/.test(p)) score++
  if (/[^A-Za-z0-9]/.test(p)) score++
  if (score <= 1) return { level: 'weak',   width: '25%',  label: 'Svakt' }
  if (score <= 2) return { level: 'fair',   width: '50%',  label: 'Greit' }
  if (score <= 3) return { level: 'good',   width: '75%',  label: 'Bra' }
  return              { level: 'strong', width: '100%', label: 'Sterkt' }
})

function clearError(field) { errors[field] = '' }

function validate() {
  let valid = true
  if (!form.name.trim()) { errors.name = 'Fullt navn er påkrevd'; valid = false }
  if (!form.email.trim()) { errors.email = 'E-post er påkrevd'; valid = false }
  if (!form.password) { errors.password = 'Passord er påkrevd'; valid = false }
  else if (form.password.length < 8) { errors.password = 'Passordet må være minst 8 tegn'; valid = false }
  if (!form.confirmPassword) { errors.confirmPassword = 'Bekreft passordet'; valid = false }
  else if (form.password !== form.confirmPassword) { errors.confirmPassword = 'Passordene stemmer ikke overens'; valid = false }
  return valid
}

async function handleSubmit() {
  if (!validate()) return
  isLoading.value = true
  submitError.value = ''
  try {
    // TODO: POST /api/auth/register — user is created and immediately active
    await new Promise(resolve => setTimeout(resolve, 800))
    router.push({ name: 'onboarding' })
  } catch {
    submitError.value = 'Noe gikk galt. Prøv igjen.'
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(145deg, var(--color-dark-primary) 0%, var(--color-dark-secondary) 100%);
  padding: var(--space-6);
  gap: var(--space-6);
}

.auth-card {
  width: 100%;
  max-width: 420px;
  background: var(--color-bg-primary);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg), 0 0 0 1px rgba(255,255,255,0.06);
  padding: var(--space-10) var(--space-8);
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.brand { display: flex; align-items: center; gap: var(--space-3); }

.brand-logo {
  width: 44px; height: 44px;
  background: var(--color-dark-primary);
  border-radius: var(--radius-md);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}

.brand-icon {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--color-accent);
  letter-spacing: 0.5px;
}

.brand-text { display: flex; flex-direction: column; gap: 1px; }

.brand-name {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  line-height: var(--line-height-tight);
}

.brand-sub {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
  line-height: var(--line-height-tight);
  letter-spacing: 0.3px;
}

.card-header { display: flex; flex-direction: column; gap: var(--space-1); }

.card-title {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  line-height: var(--line-height-tight);
  margin: 0;
}

.card-subtitle { font-size: var(--font-size-md); color: var(--color-text-muted); margin: 0; }

.auth-form { display: flex; flex-direction: column; gap: var(--space-4); }

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
  pointer-events: none; flex-shrink: 0;
}

.field-input {
  width: 100%; height: 44px;
  padding: 0 var(--space-10) 0 40px;
  font-size: var(--font-size-md); font-family: var(--font-sans);
  color: var(--color-text-primary);
  background: var(--color-bg-secondary);
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  outline: none;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast), background var(--transition-fast);
  box-sizing: border-box;
}

.field-input::placeholder { color: var(--color-text-hint); }

.field-input:focus {
  border-color: var(--color-dark-secondary);
  box-shadow: 0 0 0 3px rgba(45, 43, 85, 0.12);
  background: var(--color-bg-primary);
}

.has-error .field-input { border-color: var(--color-danger); }
.has-error .field-input:focus { box-shadow: 0 0 0 3px rgba(204, 51, 51, 0.12); }

.toggle-password {
  position: absolute; right: var(--space-3);
  width: 20px; height: 20px;
  background: none; border: none; padding: 0; cursor: pointer;
  color: var(--color-text-hint);
  display: flex; align-items: center; justify-content: center;
  transition: color var(--transition-fast);
}

.toggle-password:hover { color: var(--color-text-secondary); }
.toggle-password svg { width: 16px; height: 16px; }

.field-error { font-size: var(--font-size-xs); color: var(--color-danger); }

.password-strength { display: flex; align-items: center; gap: var(--space-2); }

.strength-bar {
  flex: 1; height: 3px;
  background: var(--color-border);
  border-radius: 2px; overflow: hidden;
}

.strength-fill {
  height: 100%; border-radius: 2px;
  transition: width var(--transition-normal), background var(--transition-normal);
}

.strength-fill.weak   { background: var(--color-danger); }
.strength-fill.fair   { background: var(--color-warning); }
.strength-fill.good   { background: #88BB00; }
.strength-fill.strong { background: var(--color-success-text); }

.strength-label {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
  min-width: 40px; text-align: right;
}

.strength-label.weak   { color: var(--color-danger-text); }
.strength-label.fair   { color: var(--color-warning-text); }
.strength-label.good   { color: #557700; }
.strength-label.strong { color: var(--color-success-text); }

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
  margin-top: var(--space-2);
}

.btn-primary:hover:not(:disabled) { background: var(--color-dark-secondary); }
.btn-primary:active:not(:disabled) { transform: scale(0.99); }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }

.spinner {
  width: 18px; height: 18px;
  border: 2px solid rgba(212, 232, 53, 0.3);
  border-top-color: var(--color-accent);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.auth-footer {
  display: flex; align-items: center; justify-content: center;
  gap: var(--space-2);
  padding-top: var(--space-2);
  border-top: 1px solid var(--color-border-subtle);
}

.auth-footer-text { font-size: var(--font-size-sm); color: var(--color-text-muted); }

.auth-link {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-dark-secondary);
  text-decoration: none;
  transition: color var(--transition-fast);
}

.auth-link:hover { color: var(--color-text-primary); }

.page-footer {
  font-size: var(--font-size-xs);
  color: rgba(255, 255, 255, 0.3);
  text-align: center; margin: 0;
}

@media (max-width: 480px) {
  .auth-card { padding: var(--space-8) var(--space-6); }
  .card-title { font-size: var(--font-size-xl); }
}
</style>
