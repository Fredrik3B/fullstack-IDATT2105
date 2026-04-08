<template>
  <SetupShell subtitle="Restaurant setup">
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

        <AppButton full-width @click="$router.push({ name: 'dashboard' })">
          Go to dashboard
          <ArrowRight />
        </AppButton>
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

            <FormField label="Restaurant name" input-id="restName" :error="errors.name">
              <template #icon><Store /></template>
              <input
                id="restName"
                v-model="form.name"
                type="text"
                class="field-input"
                placeholder="Everest Sushi and Fusion AS"
                autocomplete="organization"
                @input="clearError('name')"
              />
            </FormField>

            <FormField label="Organization number" label-hint="9 digits" input-id="orgNumber" :error="errors.orgNumber">
              <template #icon><FileText /></template>
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
            </FormField>
          </div>

          <div class="form-section">
            <span class="section-label">Address</span>

            <FormField label="Street address" input-id="address" :error="errors.address">
              <template #icon><MapPin /></template>
              <input
                id="address"
                v-model="form.address"
                type="text"
                class="field-input"
                placeholder="Storgata 1"
                autocomplete="street-address"
                @input="clearError('address')"
              />
            </FormField>

            <div class="field-row">
              <FormField label="Postal code" input-id="postalCode" :error="errors.postalCode">
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
              </FormField>

              <FormField label="City" input-id="city" :error="errors.city">
                <input
                  id="city"
                  v-model="form.city"
                  type="text"
                  class="field-input field-input--plain"
                  placeholder="Oslo"
                  autocomplete="address-level2"
                  @input="clearError('city')"
                />
              </FormField>
            </div>
          </div>

          <div v-if="submitError" class="alert alert--error">
            <AlertCircle />
            {{ submitError }}
          </div>

          <AppButton type="submit" :loading="isLoading" full-width>Create restaurant</AppButton>
        </form>
      </section>
    </template>
  </SetupShell>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { CheckCircle, ArrowRight, ChevronLeft, Store, FileText, MapPin, AlertCircle } from 'lucide-vue-next'
import SetupShell from '@/components/layout/SetupShell.vue'
import AppButton from '@/components/ui/AppButton.vue'
import FormField from '@/components/ui/FormField.vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

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
</script>

<style scoped>
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

.field-row {
  display: grid;
  gap: var(--space-3);
  grid-template-columns: 150px 1fr;
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
  .form-card,
  .result-card {
    padding: var(--space-6);
  }

  .field-row {
    grid-template-columns: 1fr;
  }
}
</style>
