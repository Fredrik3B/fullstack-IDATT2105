<template>
  <AuthShell
    tag="Login"
    eyebrow="Welcome back"
    title="Sign in to your workspace"
    subtitle="Pick up where your team left off"
    aside-title="Built for calm service shifts"
    aside-body="Track routines, close gaps, and keep operations smooth without adding noise to your day."
    :features="[
      'Quick sign-in for daily checks',
      'Clear status and fewer surprises',
      'Shared accountability across your team'
    ]"
  >
    <form class="auth-form" @submit.prevent="handleLogin">
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
          placeholder="Enter your password"
          autocomplete="current-password"
          @input="clearError('password')"
        />
        <button type="button" class="toggle-password" @click="showPassword = !showPassword" tabindex="-1">
          <Eye v-if="!showPassword" />
          <EyeOff v-else />
        </button>
      </FormField>

      <div v-if="loginError" class="alert alert--error">
        <AlertCircle />
        {{ loginError }}
      </div>

      <AppButton type="submit" :loading="isLoading" full-width>Log in</AppButton>
    </form>

    <template #footer>
      <div class="auth-footer">
        <span class="auth-footer-text">Don't have an account?</span>
        <RouterLink to="/register" class="auth-link">Create one</RouterLink>
      </div>
    </template>
  </AuthShell>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Mail, Lock, Eye, EyeOff, AlertCircle } from 'lucide-vue-next'
import AuthShell from '@/components/layout/AuthShell.vue'
import AppButton from '@/components/ui/AppButton.vue'
import FormField from '@/components/ui/FormField.vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

const form = reactive({ email: '', password: '' })
const errors = reactive({ email: '', password: '' })
const showPassword = ref(false)
const isLoading = ref(false)
const loginError = ref('')

function clearError(field) {
  errors[field] = ''
  loginError.value = ''
}

function validate() {
  let valid = true
  if (!form.email.trim()) { errors.email = 'Email is required'; valid = false }
  if (!form.password) { errors.password = 'Password is required'; valid = false }
  return valid
}

async function handleLogin() {
  if (!validate()) return
  isLoading.value = true
  loginError.value = ''
  try {
    await auth.login(form.email, form.password)
  } catch {
    loginError.value = 'Wrong email or password. Try again.'
  } finally {
    isLoading.value = false
  }
}
</script>
