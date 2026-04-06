<template>
  <SetupShell subtitle="Team onboarding" content-max-width="820px">
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

        <AppButton variant="danger" full-width @click="withdrawRequest">Withdraw request</AppButton>
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
          <FormField label="Restaurant code" input-id="joinCode" :error="joinError">
            <template #icon><KeyRound /></template>
            <input
              id="joinCode"
              v-model="joinCode"
              type="text"
              class="field-input field-input--code"
              placeholder="EVR-2847"
              maxlength="8"
              autocomplete="off"
            />
          </FormField>

          <AppButton full-width :loading="isLoading" :disabled="joinCode.length < 8" @click="sendJoinRequest">
            Send access request
          </AppButton>
        </div>
      </div>
    </template>
  </SetupShell>
</template>

<script setup>
import { computed, ref } from 'vue'
import { Clock, Store, Info, LogIn, Plus, ChevronRight, ChevronLeft, KeyRound } from 'lucide-vue-next'
import SetupShell from '@/components/layout/SetupShell.vue'
import AppButton from '@/components/ui/AppButton.vue'
import FormField from '@/components/ui/FormField.vue'
import { useToast } from '@/composables/useToast'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const toast = useToast()

const userEmail = computed(() => auth.user?.email ?? '')

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
</script>

<style scoped>
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

@media (max-width: 720px) {
  .onboarding-card {
    padding: var(--space-6);
  }
}
</style>
