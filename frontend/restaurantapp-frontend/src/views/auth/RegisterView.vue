<template>
  <AuthShell
    tag="Sign up"
    eyebrow="New workspace member"
    title="Create your profile"
    subtitle="A few details and you are ready to join your restaurant"
    aside-title="Great teams start with clear routines"
    aside-body="Set up your account once, then move straight into onboarding and daily quality checks."
    :features="[
      'Fast setup in under a minute',
      'Guided onboarding to the right restaurant',
      'Get your team connected with shared access'
    ]"
  >
    <form class="auth-form" @submit.prevent="handleSubmit">
      <div class="field-group" :class="{ 'has-error': errors.name }">
        <label class="field-label" for="name">Full name</label>
        <div class="field-wrapper">
          <User class="field-icon" />
          <input
            id="name"
            v-model="form.name"
            type="text"
            class="field-input"
            placeholder="Jane Doe"
            autocomplete="name"
            @input="clearError('name')"
          />
        </div>
        <span v-if="errors.name" class="field-error">{{ errors.name }}</span>
      </div>

      <div class="field-group" :class="{ 'has-error': errors.email }">
        <label class="field-label" for="email">Email</label>
        <div class="field-wrapper">
          <Mail class="field-icon" />
          <input
            id="email"
            v-model="form.email"
            type="email"
            class="field-input"
            placeholder="name@restaurant.com"
            autocomplete="email"
            @input="clearError('email')"
          />
        </div>
        <span v-if="errors.email" class="field-error">{{ errors.email }}</span>
      </div>

      <div class="field-group" :class="{ 'has-error': errors.password }">
        <label class="field-label" for="password">Password</label>
        <div class="field-wrapper">
          <Lock class="field-icon" />
          <input
            id="password"
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            class="field-input"
            placeholder="At least 8 characters"
            autocomplete="new-password"
            @input="clearError('password')"
          />
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
          <input
            id="confirmPassword"
            v-model="form.confirmPassword"
            :type="showConfirmPassword ? 'text' : 'password'"
            class="field-input"
            placeholder="Repeat password"
            autocomplete="new-password"
            @input="clearError('confirmPassword')"
          />
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

    <template #footer>
      <div class="auth-footer">
        <span class="auth-footer-text">Already have an account?</span>
        <RouterLink to="/login" class="auth-link">Log in</RouterLink>
      </div>
    </template>
  </AuthShell>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { User, Mail, Lock, Eye, EyeOff, AlertCircle } from 'lucide-vue-next'
import AuthShell from '@/components/layout/AuthShell.vue'
import { useAuthStore } from '@/stores/auth'

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
  if (p.length >= 8) score++
  if (p.length >= 12) score++
  if (/[A-Z]/.test(p)) score++
  if (/[0-9]/.test(p)) score++
  if (/[^A-Za-z0-9]/.test(p)) score++
  if (score <= 1) return { level: 'weak', width: '25%', label: 'Weak' }
  if (score <= 2) return { level: 'fair', width: '50%', label: 'Fair' }
  if (score <= 3) return { level: 'good', width: '75%', label: 'Good' }
  return { level: 'strong', width: '100%', label: 'Strong' }
})

function clearError(field) {
  errors[field] = ''
}

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
  } catch (err) {
    if (err?.response?.status === 409) {
      errors.email = 'An account with this email already exists.'
    } else {
      submitError.value = 'Something went wrong. Please try again.'
    }
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.auth-form {
  display: grid;
  gap: var(--space-4);
}

.field-group {
  display: grid;
  gap: var(--space-2);
}

.field-label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: #3f3d61;
}

.field-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.field-icon {
  position: absolute;
  left: var(--space-3);
  width: 16px;
  height: 16px;
  color: #8b89ad;
  pointer-events: none;
}

.field-input {
  width: 100%;
  height: 46px;
  border-radius: var(--radius-md);
  border: 1.5px solid #d8d7ea;
  background: #fbfbff;
  color: #1c1a36;
  padding: 0 40px 0 40px;
  font-size: var(--font-size-md);
  font-family: var(--font-sans);
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast), background var(--transition-fast);
}

.field-input::placeholder {
  color: #aaa7c7;
}

.field-input:focus {
  outline: none;
  border-color: #4b4a72;
  box-shadow: 0 0 0 3px rgba(75, 74, 114, 0.16);
  background: #ffffff;
}

.has-error .field-input {
  border-color: var(--color-danger);
}

.toggle-password {
  position: absolute;
  right: var(--space-3);
  width: 20px;
  height: 20px;
  border: none;
  background: none;
  color: #8b89ad;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.toggle-password svg {
  width: 16px;
  height: 16px;
}

.field-error {
  font-size: var(--font-size-xs);
  color: var(--color-danger);
}

.password-strength {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.strength-bar {
  flex: 1;
  height: 4px;
  background: #dddced;
  border-radius: 999px;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  transition: width var(--transition-fast), background var(--transition-fast);
}

.strength-fill.weak { background: #d44545; }
.strength-fill.fair { background: #d9a935; }
.strength-fill.good { background: #89b739; }
.strength-fill.strong { background: #4d8f3c; }

.strength-label {
  min-width: 52px;
  text-align: right;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
}

.strength-label.weak { color: #a82e2e; }
.strength-label.fair { color: #8c6a1f; }
.strength-label.good { color: #5f7f28; }
.strength-label.strong { color: #33682b; }

.alert {
  display: flex;
  gap: var(--space-2);
  align-items: center;
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
}

.alert--error {
  background: var(--color-danger-bg);
  border: 1px solid var(--color-danger-border);
  color: var(--color-danger-text);
}

.alert svg {
  width: 16px;
  height: 16px;
}

.btn-primary {
  height: 48px;
  border: none;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, var(--color-dark-primary), var(--color-dark-secondary));
  color: var(--color-accent);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  font-family: var(--font-sans);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: transform var(--transition-fast), filter var(--transition-fast);
}

.btn-primary:hover:not(:disabled) {
  filter: brightness(1.06);
  transform: translateY(-1px);
}

.btn-primary:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.spinner {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid rgba(212, 232, 53, 0.3);
  border-top-color: var(--color-accent);
  animation: spin 700ms linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.auth-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
}

.auth-footer-text {
  font-size: var(--font-size-sm);
  color: #6a688a;
}

.auth-link {
  color: #2e2c59;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  text-decoration: none;
}

.auth-link:hover {
  text-decoration: underline;
}
</style>
