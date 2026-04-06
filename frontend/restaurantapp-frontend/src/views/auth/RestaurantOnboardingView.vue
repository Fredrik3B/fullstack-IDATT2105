<template>
  <div class="onboarding-page">
    <div class="ambient" aria-hidden="true">
      <span class="blob blob--a"></span>
      <span class="blob blob--b"></span>
    </div>

    <OnboardingTopbar />

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

      <OnboardingPending v-if="view === 'pending'" @withdrawn="view = 'choose'" />
      <OnboardingChoose v-else-if="view === 'choose'" @join="view = 'join'" />
      <OnboardingJoin v-else-if="view === 'join'" @back="view = 'choose'" @sent="view = 'pending'" />
    </main>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import OnboardingTopbar from '@/components/onboarding/OnboardingTopbar.vue'
import OnboardingChoose from '@/components/onboarding/OnboardingChoose.vue'
import OnboardingJoin from '@/components/onboarding/OnboardingJoin.vue'
import OnboardingPending from '@/components/onboarding/OnboardingPending.vue'

const auth = useAuthStore()
const view = ref('choose')

// When pendingRequest loads async (e.g. after page refresh), switch to pending view
watch(() => auth.hasPendingRequest, (has) => {
  if (has) view.value = 'pending'
}, { immediate: true })

const currentStep = computed(() => {
  return (view.value === 'pending' || view.value === 'join') ? 2 : 1
})
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

@media (max-width: 720px) {
  .onboarding-body {
    padding: var(--space-8) var(--space-4) var(--space-10);
  }
}
</style>
