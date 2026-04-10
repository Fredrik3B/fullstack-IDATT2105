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
      <FormField label="Full name" input-id="name" :error="errors.name">
        <template #icon><User /></template>
        <input
          id="name"
          v-model="form.name"
          type="text"
          class="field-input"
          placeholder="Jane Doe"
          autocomplete="name"
          @input="clearError('name')"
        />
      </FormField>

      <FormField label="Email" input-id="email" :error="errors.email">
        <template #icon><Mail /></template>
        <input
          id="email"
          v-model="form.email"
          type="email"
          class="field-input"
          placeholder="name@restaurant.com"
          autocomplete="email"
          @input="clearError('email')"
        />
      </FormField>

      <FormField label="Password" input-id="password" :error="errors.password">
        <template #icon><Lock /></template>
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

        <template #after>
          <div v-if="form.password" class="password-strength">
            <div class="strength-bar">
              <div class="strength-fill" :class="passwordStrength.level" :style="{ width: passwordStrength.width }"></div>
            </div>
            <span class="strength-label" :class="passwordStrength.level">{{ passwordStrength.label }}</span>
          </div>
        </template>
      </FormField>

      <FormField label="Confirm password" input-id="confirmPassword" :error="errors.confirmPassword">
        <template #icon><Lock /></template>
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
      </FormField>

      <div v-if="submitError" class="alert alert--error">
        <AlertCircle />
        {{ submitError }}
      </div>

      <AppButton type="submit" :loading="isLoading" full-width>Create account</AppButton>
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
import AppButton from '@/components/ui/AppButton.vue'
import FormField from '@/components/ui/FormField.vue'
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
  if (!form.name.trim()) {
    errors.name = 'Full name is required'
    valid = false
  } else if (!form.name.trim().includes(' ') || form.name.trim().split(/\s+/).length < 2 || form.name.trim().split(/\s+/).some(part => part.length < 1)) {
    errors.name = 'Please enter both your first and last name'
    valid = false
  }
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
.password-strength {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-top: var(--space-2);
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
</style>
