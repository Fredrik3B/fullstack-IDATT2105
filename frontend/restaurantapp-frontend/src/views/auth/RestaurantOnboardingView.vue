<template>
  <div class="onboarding-page">
    <div class="ambient" aria-hidden="true">
      <span class="blob blob--a"></span>
      <span class="blob blob--b"></span>
    </div>

    <header class="topbar">
      <div class="topbar-brand">
        <div class="brand-logo"><span class="brand-icon">IC</span></div>
        <div>
          <span class="brand-name">ICSystem</span>
          <p class="brand-sub">Team onboarding</p>
        </div>
      </div>
      <div class="topbar-user">
        <div class="user-avatar">{{ userInitials }}</div>
        <span class="user-name">{{ userEmail }}</span>
        <button class="btn-logout" @click="handleLogout">Log out</button>
      </div>
    </header>

    <main class="onboarding-body">
      <section class="onboarding-intro">
        <p class="intro-tag">Step {{ currentStep }} of 2</p>
        <h1 class="intro-title">Connect your account to a restaurant</h1>
        <p class="intro-subtitle">Once connected, you can access checklists, reports, and team workflows.</p>

        <div class="stepper" role="list" aria-label="Onboarding progress">
          <span class="step" :class="{ active: currentStep >= 1 }">Choose path</span>
          <span class="step" :class="{ active: currentStep >= 2 }">Send request</span>
        </div>
      </section>

      <template v-if="view === 'pending'">
        <div class="onboarding-card card--pending">
          <div class="pending-icon">
            <Clock />
          </div>
          <h2 class="card-title">Request sent</h2>
          <p class="card-subtitle">
            Your request to <strong>{{ pendingRestaurantName }}</strong> is pending review.
            You will be notified at <strong>{{ userEmail }}</strong>.
          </p>

          <div class="pending-meta">
            <div class="meta-row">
              <Store />
              {{ pendingRestaurantName }}
            </div>
            <div class="meta-row">
              <Info />
              Sent {{ pendingSentDate }}
            </div>
          </div>

          <button class="btn-secondary" @click="withdrawRequest">Withdraw request</button>
        </div>

      </template>

      <template v-else-if="view === 'choose'">
        <div class="option-grid">
          <button class="option-card" @click="view = 'join'">
            <div class="option-icon option-icon--join"><LogIn /></div>
            <div class="option-text">
              <span class="option-title">Join existing restaurant</span>
              <span class="option-desc">Use the invitation code from your manager</span>
            </div>
            <ChevronRight class="option-arrow" />
          </button>

          <button class="option-card" @click="$router.push({ name: 'create-restaurant' })">
            <div class="option-icon option-icon--create"><Plus /></div>
            <div class="option-text">
              <span class="option-title">Create new restaurant</span>
              <span class="option-desc">Set up a workspace and become the administrator</span>
            </div>
            <ChevronRight class="option-arrow" />
          </button>
        </div>
      </template>

      <template v-else-if="view === 'join'">
        <div class="onboarding-card">
          <button class="back-btn" @click="view = 'choose'">
            <ChevronLeft />
            Back
          </button>

          <h2 class="card-title">Enter invitation code</h2>
          <p class="card-subtitle">Use the format <strong>XXX-0000</strong>.</p>

          <div class="join-form">
            <div class="field-group" :class="{ 'has-error': joinError }">
              <label class="field-label" for="joinCode">Restaurant code</label>
              <div class="field-wrapper">
                <KeyRound class="field-icon" />
                <input
                  id="joinCode"
                  v-model="joinCode"
                  type="text"
                  class="field-input field-input--code"
                  placeholder="EVR-2847"
                  maxlength="8"
                  autocomplete="off"
                />
              </div>
              <span v-if="joinError" class="field-error">{{ joinError }}</span>
            </div>

            <button class="btn-primary" :disabled="isLoading || joinCode.length < 8" @click="sendJoinRequest">
              <span v-if="!isLoading">Send access request</span>
              <span v-else class="spinner"></span>
            </button>
          </div>
        </div>
      </template>
    </main>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { Clock, Store, Info, LogIn, Plus, ChevronRight, ChevronLeft, KeyRound } from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const toast = useToast()

const userEmail = computed(() => auth.user?.email ?? '')
const userInitials = computed(() => auth.userInitials)

const view = ref(auth.restaurantStatus === 'pending' ? 'pending' : 'choose')
const joinCode = ref('')
const joinError = ref('')
const isLoading = ref(false)

const pendingRestaurantName = ref(auth.restaurantStatus === 'pending' ? 'your selected restaurant' : '')
const pendingSentDate = ref('')

const currentStep = computed(() => {
  if (view.value === 'pending' || view.value === 'join') return 2
  return 1
})

async function sendJoinRequest() {
  isLoading.value = true
  joinError.value = ''
  try {
    const { name } = await auth.lookupRestaurant(joinCode.value)
    await auth.joinRestaurant(joinCode.value, name)
    pendingRestaurantName.value = name
    pendingSentDate.value = new Date().toLocaleDateString('en-GB', { day: 'numeric', month: 'long', year: 'numeric' })
    view.value = 'pending'
  } catch {
    joinError.value = 'Invalid restaurant code. Please try again.'
  } finally {
    isLoading.value = false
  }
}

async function withdrawRequest() {
  try {
    await auth.withdrawJoinRequest()
    toast.info('Request withdrawn')
  } catch {
    toast.error('Could not withdraw the request. Please try again.')
    return
  }

  pendingRestaurantName.value = ''
  pendingSentDate.value = ''
  joinCode.value = ''
  view.value = 'choose'
}

function handleLogout() {
  auth.logout()
}
</script>

<style scoped>
.onboarding-page {
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
  background: radial-gradient(circle, rgba(212, 232, 53, 0.28), rgba(212, 232, 53, 0));
  top: -120px;
  right: -80px;
}

.blob--b {
  width: 320px;
  height: 320px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.12), rgba(255, 255, 255, 0));
  bottom: -120px;
  left: -80px;
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

.onboarding-body {
  position: relative;
  z-index: 1;
  max-width: 820px;
  margin: 0 auto;
  padding: var(--space-10) var(--space-6) var(--space-12);
}

.onboarding-intro {
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

.stepper {
  margin: var(--space-5) auto 0;
  display: inline-flex;
  gap: var(--space-2);
  padding: 5px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
}

.step {
  padding: 6px 14px;
  border-radius: 999px;
  color: rgba(255, 255, 255, 0.57);
  font-size: 12px;
}

.step.active {
  background: rgba(212, 232, 53, 0.16);
  color: #edf9a0;
}

.option-grid {
  display: grid;
  gap: var(--space-4);
}

.option-card {
  width: 100%;
  text-align: left;
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5) var(--space-6);
  border-radius: var(--radius-xl);
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(255, 255, 255, 0.07);
  cursor: pointer;
  color: #fff;
  transition: transform var(--transition-fast), background var(--transition-fast), border-color var(--transition-fast);
}

.option-card:hover {
  transform: translateY(-1px);
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(255, 255, 255, 0.24);
}

.option-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.option-icon--join {
  background: rgba(212, 232, 53, 0.2);
  color: var(--color-accent);
}

.option-icon--create {
  background: rgba(255, 255, 255, 0.16);
  color: rgba(255, 255, 255, 0.88);
}

.option-text {
  display: grid;
  gap: 2px;
  flex: 1;
}

.option-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
}

.option-desc {
  font-size: var(--font-size-sm);
  color: rgba(255, 255, 255, 0.66);
}

.option-arrow {
  width: 16px;
  height: 16px;
  color: rgba(255, 255, 255, 0.5);
}

.onboarding-card {
  background: rgba(255, 255, 255, 0.97);
  border-radius: var(--radius-xl);
  padding: var(--space-8);
  box-shadow: 0 22px 36px rgba(0, 0, 0, 0.24);
  display: grid;
  gap: var(--space-5);
}

.card-title {
  margin: 0;
  color: #1b1a35;
  font-size: clamp(1.25rem, 2.8vw, 1.65rem);
}

.card-subtitle {
  margin: 0;
  color: #686685;
}

.back-btn {
  width: fit-content;
  border: none;
  background: none;
  color: #64628b;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: var(--font-size-sm);
  cursor: pointer;
  padding: 0;
}

.pending-icon {
  width: 58px;
  height: 58px;
  border-radius: 50%;
  background: #fff4d4;
  border: 1px solid #f1d991;
  color: #b78618;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.pending-meta {
  background: #f8f8fe;
  border: 1px solid #e6e5f4;
  border-radius: var(--radius-md);
  padding: var(--space-4);
  display: grid;
  gap: var(--space-2);
}

.meta-row {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  color: #4e4c71;
  font-size: var(--font-size-sm);
}

.join-form {
  display: grid;
  gap: var(--space-4);
}

.field-group {
  display: grid;
  gap: var(--space-2);
}

.field-label {
  color: #3e3b63;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
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

.field-input:focus {
  outline: none;
  border-color: #4b4a72;
  box-shadow: 0 0 0 3px rgba(75, 74, 114, 0.16);
}

.field-input--code {
  letter-spacing: 3px;
  text-transform: uppercase;
  font-weight: var(--font-weight-medium);
}

.has-error .field-input {
  border-color: var(--color-danger);
}

.field-error {
  font-size: var(--font-size-xs);
  color: var(--color-danger);
}

.btn-primary,
.btn-secondary {
  height: 46px;
  border-radius: var(--radius-md);
  border: none;
  font-family: var(--font-sans);
  font-weight: var(--font-weight-bold);
  cursor: pointer;
}

.btn-primary {
  background: linear-gradient(135deg, var(--color-dark-primary), var(--color-dark-secondary));
  color: var(--color-accent);
}

.btn-primary:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.btn-secondary {
  background: #fff0f0;
  border: 1px solid #f3cece;
  color: #863131;
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

.help-text {
  margin: var(--space-4) 0 0;
  text-align: center;
  color: rgba(255, 255, 255, 0.65);
}

.inline-link {
  color: rgba(255, 255, 255, 0.95);
  border: none;
  background: none;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 2px;
  padding: 0;
  font: inherit;
}

@media (max-width: 720px) {
  .topbar {
    padding: var(--space-3) var(--space-4);
  }

  .user-name {
    display: none;
  }

  .onboarding-body {
    padding: var(--space-8) var(--space-4) var(--space-10);
  }

  .onboarding-card {
    padding: var(--space-6);
  }
}
</style>
