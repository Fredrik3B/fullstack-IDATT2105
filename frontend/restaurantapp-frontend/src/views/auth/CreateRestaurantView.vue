<template>
  <div class="create-page">
    <div class="ambient" aria-hidden="true">
      <span class="blob blob--a"></span>
      <span class="blob blob--b"></span>
    </div>

    <header class="topbar">
      <div class="topbar-brand">
        <div class="brand-logo"><span class="brand-icon">IC</span></div>
        <div>
          <span class="brand-name">ICSystem</span>
          <p class="brand-sub">Restaurant setup</p>
        </div>
      </div>
      <div class="topbar-user">
        <div class="user-avatar">{{ userInitials }}</div>
        <span class="user-name">{{ userEmail }}</span>
        <button class="btn-logout" @click="handleLogout">Log out</button>
      </div>
    </header>

    <main class="create-body">
      <section class="intro">
        <p class="intro-tag">Organization setup</p>
        <h1 class="intro-title">Create your restaurant workspace</h1>
        <p class="intro-subtitle">You can invite staff right after setup using your generated join code.</p>
      </section>

      <template v-if="submitted">
        <section class="result-card">
          <div class="result-icon"><CheckCircle /></div>
          <h2 class="result-title">Workspace created</h2>
          <p class="result-body">
            <strong>{{ form.name }}</strong> is now active in ICSystem and your account is set as administrator.
          </p>

          <div class="join-code-display">
            <span class="join-code-label">Restaurant join code</span>
            <div class="join-code-value">{{ joinCode }}</div>
            <span class="join-code-hint">Share this code with team members so they can request access.</span>
          </div>

          <button class="btn-primary" @click="$router.push({ name: 'dashboard' })">
            Go to dashboard
            <ArrowRight />
          </button>
        </section>
      </template>

      <template v-else>
        <section class="form-card">
          <div class="form-head">
            <RouterLink to="/onboarding" class="back-link">
              <ChevronLeft />
              Back to onboarding
            </RouterLink>

            <div class="stepper" role="list" aria-label="Create restaurant progress">
              <span class="step active">Business details</span>
              <span class="step">Address</span>
            </div>
          </div>

          <form class="restaurant-form" @submit.prevent="handleSubmit">
            <div class="form-section">
              <span class="section-label">Business details</span>

              <div class="field-group" :class="{ 'has-error': errors.name }">
                <label class="field-label" for="restName">Restaurant name</label>
                <div class="field-wrapper">
                  <Store class="field-icon" />
                  <input
                    id="restName"
                    v-model="form.name"
                    type="text"
                    class="field-input"
                    placeholder="Everest Sushi and Fusion AS"
                    autocomplete="organization"
                    @input="clearError('name')"
                  />
                </div>
                <span v-if="errors.name" class="field-error">{{ errors.name }}</span>
              </div>

              <div class="field-group" :class="{ 'has-error': errors.orgNumber }">
                <label class="field-label" for="orgNumber">
                  Organization number
                  <span class="label-hint">9 digits</span>
                </label>
                <div class="field-wrapper">
                  <FileText class="field-icon" />
                  <input
                    id="orgNumber"
                    v-model="form.orgNumber"
                    type="text"
                    class="field-input"
                    placeholder="123 456 789"
                    maxlength="11"
                    inputmode="numeric"
                    @input="handleOrgInput"
                  />
                </div>
                <span v-if="errors.orgNumber" class="field-error">{{ errors.orgNumber }}</span>
              </div>
            </div>

            <div class="form-section">
              <span class="section-label">Address</span>

              <div class="field-group" :class="{ 'has-error': errors.address }">
                <label class="field-label" for="address">Street address</label>
                <div class="field-wrapper">
                  <MapPin class="field-icon" />
                  <input
                    id="address"
                    v-model="form.address"
                    type="text"
                    class="field-input"
                    placeholder="Storgata 1"
                    autocomplete="street-address"
                    @input="clearError('address')"
                  />
                </div>
                <span v-if="errors.address" class="field-error">{{ errors.address }}</span>
              </div>

              <div class="field-row">
                <div class="field-group field-group--narrow" :class="{ 'has-error': errors.postalCode }">
                  <label class="field-label" for="postalCode">Postal code</label>
                  <input
                    id="postalCode"
                    v-model="form.postalCode"
                    type="text"
                    class="field-input field-input--plain"
                    placeholder="0150"
                    maxlength="4"
                    inputmode="numeric"
                    @input="handlePostalInput"
                  />
                  <span v-if="errors.postalCode" class="field-error">{{ errors.postalCode }}</span>
                </div>

                <div class="field-group field-group--grow" :class="{ 'has-error': errors.city }">
                  <label class="field-label" for="city">City</label>
                  <input
                    id="city"
                    v-model="form.city"
                    type="text"
                    class="field-input field-input--plain"
                    placeholder="Oslo"
                    autocomplete="address-level2"
                    @input="clearError('city')"
                  />
                  <span v-if="errors.city" class="field-error">{{ errors.city }}</span>
                </div>
              </div>
            </div>

            <div v-if="submitError" class="alert alert--error">
              <AlertCircle />
              {{ submitError }}
            </div>

            <button type="submit" class="btn-primary" :disabled="isLoading">
              <span v-if="!isLoading">Create restaurant</span>
              <span v-else class="spinner"></span>
            </button>
          </form>
        </section>
      </template>
    </main>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { CheckCircle, ArrowRight, ChevronLeft, Store, FileText, MapPin, AlertCircle } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

const userEmail = computed(() => auth.user?.email ?? '')
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

const isLoading = ref(false)
const submitError = ref('')
const submitted = ref(false)
const joinCode = ref('')

function clearError(field) {
  errors[field] = ''
}

function handleOrgInput(e) {
  const digits = e.target.value.replace(/\D/g, '').slice(0, 9)
  form.orgNumber = digits.replace(/(\d{3})(\d{1,3})?(\d{1,3})?/, (_, a, b, c) => [a, b, c].filter(Boolean).join(' '))
  clearError('orgNumber')
}

function handlePostalInput(e) {
  form.postalCode = e.target.value.replace(/\D/g, '').slice(0, 4)
  clearError('postalCode')
}

function validate() {
  let valid = true
  if (!form.name.trim()) { errors.name = 'Restaurant name is required'; valid = false }

  const orgDigits = form.orgNumber.replace(/\s/g, '')
  if (!orgDigits) { errors.orgNumber = 'Organization number is required'; valid = false }
  else if (orgDigits.length !== 9) { errors.orgNumber = 'Organization number must be 9 digits'; valid = false }

  if (!form.address.trim()) { errors.address = 'Street address is required'; valid = false }
  if (!form.postalCode) { errors.postalCode = 'Postal code is required'; valid = false }
  else if (form.postalCode.length !== 4) { errors.postalCode = 'Invalid postal code'; valid = false }
  if (!form.city.trim()) { errors.city = 'City is required'; valid = false }

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
    submitError.value = 'Something went wrong. Please try again.'
  } finally {
    isLoading.value = false
  }
}

function handleLogout() {
  auth.logout()
}
</script>

<style scoped>
.create-page {
  min-height: 100vh;
  position: relative;
  background: linear-gradient(145deg, #17162f 0%, #24224b 52%, #2e2b59 100%);
  overflow: hidden;
}

.ambient {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.blob {
  position: absolute;
  border-radius: 9999px;
  filter: blur(4px);
}

.blob--a {
  width: 360px;
  height: 360px;
  background: radial-gradient(circle, rgba(212, 232, 53, 0.25), rgba(212, 232, 53, 0));
  top: -120px;
  right: -90px;
}

.blob--b {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.11), rgba(255, 255, 255, 0));
  bottom: -120px;
  left: -70px;
}

.topbar {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4) var(--space-8);
  border-bottom: 1px solid rgba(255, 255, 255, 0.09);
}

.topbar-brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.brand-logo {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.1);
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.brand-icon {
  color: var(--color-accent);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
}

.brand-name {
  color: #fff;
  font-weight: var(--font-weight-bold);
}

.brand-sub {
  margin: 1px 0 0;
  color: rgba(255, 255, 255, 0.5);
  font-size: 11px;
}

.topbar-user {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.13);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: var(--color-accent);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: var(--font-weight-bold);
}

.user-name {
  color: rgba(255, 255, 255, 0.76);
  font-size: var(--font-size-sm);
}

.btn-logout {
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: transparent;
  color: rgba(255, 255, 255, 0.7);
  border-radius: var(--radius-sm);
  padding: 4px 10px;
  font-size: 11px;
  cursor: pointer;
}

.btn-logout:hover {
  color: #fff;
  border-color: rgba(255, 255, 255, 0.38);
}

.create-body {
  position: relative;
  z-index: 1;
  max-width: 860px;
  margin: 0 auto;
  padding: var(--space-10) var(--space-6) var(--space-12);
}

.intro {
  text-align: center;
  margin-bottom: var(--space-8);
}

.intro-tag {
  margin: 0;
  color: var(--color-accent);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-size: 11px;
  font-weight: var(--font-weight-bold);
}

.intro-title {
  margin: var(--space-2) 0 var(--space-2);
  color: #fff;
  font-family: var(--font-display);
  font-size: clamp(1.7rem, 4.2vw, 2.4rem);
  line-height: 1.13;
}

.intro-subtitle {
  margin: 0;
  color: rgba(255, 255, 255, 0.65);
}

.form-card,
.result-card {
  background: rgba(255, 255, 255, 0.97);
  border-radius: var(--radius-xl);
  padding: var(--space-8);
  box-shadow: 0 22px 36px rgba(0, 0, 0, 0.24);
}

.form-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-3);
  margin-bottom: var(--space-5);
}

.back-link {
  color: #5f5d86;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}

.back-link:hover {
  color: #3f3d61;
}

.stepper {
  display: inline-flex;
  gap: var(--space-2);
  padding: 5px;
  border-radius: 999px;
  background: #f0eff8;
}

.step {
  padding: 6px 12px;
  border-radius: 999px;
  color: #8a87ac;
  font-size: 12px;
}

.step.active {
  background: #e7f188;
  color: #36411a;
  font-weight: var(--font-weight-bold);
}

.restaurant-form {
  display: grid;
  gap: var(--space-6);
}

.form-section {
  display: grid;
  gap: var(--space-4);
}

.section-label {
  color: #7a789b;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.09em;
  font-weight: var(--font-weight-bold);
}

.field-group {
  display: grid;
  gap: var(--space-2);
}

.field-row {
  display: grid;
  gap: var(--space-3);
  grid-template-columns: 150px 1fr;
}

.field-label {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  color: #3f3d61;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}

.label-hint {
  color: #9391b3;
  font-size: 11px;
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
  padding: 0 14px 0 40px;
  font-size: var(--font-size-md);
  font-family: var(--font-sans);
}

.field-input--plain {
  padding-left: 14px;
}

.field-input:focus {
  outline: none;
  border-color: #4b4a72;
  box-shadow: 0 0 0 3px rgba(75, 74, 114, 0.16);
}

.has-error .field-input {
  border-color: var(--color-danger);
}

.field-error {
  color: var(--color-danger);
  font-size: var(--font-size-xs);
}

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
  gap: var(--space-2);
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

.result-card {
  text-align: center;
  display: grid;
  gap: var(--space-4);
}

.result-icon {
  width: 62px;
  height: 62px;
  border-radius: 50%;
  background: #edf8c9;
  border: 1px solid #cddd73;
  color: #4d7223;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
}

.result-title {
  margin: 0;
  color: #1f1d3a;
  font-size: var(--font-size-xl);
}

.result-body {
  margin: 0;
  color: #626084;
}

.join-code-display {
  padding: var(--space-5);
  border-radius: var(--radius-lg);
  border: 1px solid #e4e2f2;
  background: #f9f9fe;
  display: grid;
  gap: var(--space-2);
}

.join-code-label {
  color: #7e7ba0;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.join-code-value {
  width: fit-content;
  margin: 0 auto;
  background: #1b1a35;
  color: var(--color-accent);
  border-radius: var(--radius-md);
  padding: 8px 20px;
  letter-spacing: 1px;
  font-weight: var(--font-weight-bold);
}

.join-code-hint {
  color: #767399;
  font-size: var(--font-size-xs);
}

@media (max-width: 760px) {
  .topbar {
    padding: var(--space-3) var(--space-4);
  }

  .user-name {
    display: none;
  }

  .create-body {
    padding: var(--space-8) var(--space-4) var(--space-10);
  }

  .form-card,
  .result-card {
    padding: var(--space-6);
  }

  .field-row {
    grid-template-columns: 1fr;
  }
}
</style>
