<template>
  <div class="auth-page">
    <div class="auth-card">

      <div class="brand">
        <div class="brand-logo">
          <span class="brand-icon">IC</span>
        </div>
        <div class="brand-text">
          <span class="brand-name">ICSystem</span>
          <span class="brand-sub">Internal controll system</span>
        </div>
      </div>

      <div class="card-header">
        <h1 class="card-title">Create account</h1>
        <p class="card-subtitle">Fill out the form to create your account</p>
      </div>

      <form class="auth-form" @submit.prevent="handleSubmit">

        <div class="field-group" :class="{ 'has-error': errors.name }">
          <label class="field-label" for="name">Full name</label>
          <div class="field-wrapper">
            <User class="field-icon" />
            <input id="name" v-model="form.name" type="text" class="field-input"
              placeholder="Jane Doe" autocomplete="name" @input="clearError('name')" />
          </div>
          <span v-if="errors.name" class="field-error">{{ errors.name }}</span>
        </div>

        <div class="field-group" :class="{ 'has-error': errors.email }">
          <label class="field-label" for="email">Email</label>
          <div class="field-wrapper">
            <Mail class="field-icon" />
            <input id="email" v-model="form.email" type="email" class="field-input"
              placeholder="name@restaurant.com" autocomplete="email" @input="clearError('email')" />
          </div>
          <span v-if="errors.email" class="field-error">{{ errors.email }}</span>
        </div>

        <div class="field-group" :class="{ 'has-error': errors.password }">
          <label class="field-label" for="password">Password</label>
          <div class="field-wrapper">
            <Lock class="field-icon" />
            <input id="password" v-model="form.password"
              :type="showPassword ? 'text' : 'password'" class="field-input"
              placeholder="At least 8 characters" autocomplete="new-password" @input="clearError('password')" />
            <button type="button" class="toggle-password" @click="showPassword = !showPassword" tabindex="-1">
              <Eye v-if="!showPassword" />
              <EyeOff v-else />
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
          <label class="field-label" for="confirmPassword">Confirm password</label>
          <div class="field-wrapper">
            <Lock class="field-icon" />
            <input id="confirmPassword" v-model="form.confirmPassword"
              :type="showConfirmPassword ? 'text' : 'password'" class="field-input"
              placeholder="Repeat password" autocomplete="new-password" @input="clearError('confirmPassword')" />
            <button type="button" class="toggle-password" @click="showConfirmPassword = !showConfirmPassword" tabindex="-1">
              <Eye v-if="!showConfirmPassword" />
              <EyeOff v-else />
            </button>
          </div>
          <span v-if="errors.confirmPassword" class="field-error">{{ errors.confirmPassword }}</span>
        </div>

        <div v-if="submitError" class="alert alert--error">
          <AlertCircle />
          {{ submitError }}
        </div>

        <button type="submit" class="btn-primary" :disabled="isLoading">
          <span v-if="!isLoading">Create account</span>
          <span v-else class="spinner"></span>
        </button>

      </form>

      <div class="auth-footer">
        <span class="auth-footer-text">Already have an account?</span>
        <RouterLink to="/login" class="auth-link">Log in</RouterLink>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { User, Mail, Lock, Eye, EyeOff, AlertCircle } from 'lucide-vue-next'

const auth = useAuthStore()

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
  if (score <= 1) return { level: 'weak',   width: '25%',  label: 'Weak' }
  if (score <= 2) return { level: 'fair',   width: '50%',  label: 'Fair' }
  if (score <= 3) return { level: 'good',   width: '75%',  label: 'Good' }
  return              { level: 'strong', width: '100%', label: 'Strong' }
})

function clearError(field) { errors[field] = '' }

function validate() {
  let valid = true
  if (!form.name.trim()) { errors.name = 'Full name is required'; valid = false }
  if (!form.email.trim()) { errors.email = 'Email is required'; valid = false }
  if (!form.password) { errors.password = 'Password is required'; valid = false }
  else if (form.password.length < 8) { errors.password = 'Password must be at least 8 characters'; valid = false }
  if (!form.confirmPassword) { errors.confirmPassword = 'Please confirm your password'; valid = false }
  else if (form.password !== form.confirmPassword) { errors.confirmPassword = 'Passwords do not match'; valid = false }
  return valid
}

async function handleSubmit() {
  if (!validate()) return
  isLoading.value = true
  submitError.value = ''
  try {
    await auth.register(form.name, form.email, form.password)
    // auth.register() handles the redirect to /onboarding
  } catch {
    submitError.value = 'Something went wrong. Please try again.'
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
