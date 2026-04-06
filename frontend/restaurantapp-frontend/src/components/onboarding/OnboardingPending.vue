<template>
  <div class="onboarding-card">
    <div class="pending-icon"><Clock /></div>

    <h2 class="card-title">Request sent</h2>
    <p class="card-subtitle">
      Your request to <strong>{{ restaurantName }}</strong> is pending review.
      You will be notified at <strong>{{ auth.user?.email }}</strong>.
    </p>

    <button class="btn-check-status" :disabled="checking" @click="checkStatus">
      <span v-if="!checking">Check status</span>
      <span v-else class="spinner"></span>
    </button>

    <div class="pending-meta">
      <div class="meta-row">
        <Store />
        {{ restaurantName }}
      </div>
      <div v-if="sentDate" class="meta-row">
        <Info />
        Sent {{ sentDate }}
      </div>
    </div>

    <button class="btn-secondary" @click="withdraw">Withdraw request</button>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Clock, Store, Info } from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'
import { useAuthStore } from '@/stores/auth'

const emit = defineEmits(['withdrawn'])
const auth = useAuthStore()
const router = useRouter()
const toast = useToast()

const checking = ref(false)

/** Restaurant name from the backend pending request data */
const restaurantName = computed(() =>
  auth.pendingRequest?.restaurantName ?? 'your selected restaurant'
)

/** Formatted date from the backend createdAt field */
const sentDate = computed(() => {
  const raw = auth.pendingRequest?.createdAt
  if (!raw) return null
  return new Date(raw).toLocaleDateString('en-GB', {
    day: 'numeric', month: 'long', year: 'numeric'
  })
})

async function checkStatus() {
  checking.value = true
  try {
    await auth.refreshAccessToken()
    if (auth.hasActiveRestaurant) {
      toast.success('Your request has been accepted!')
      router.push({ name: 'dashboard' })
    } else {
      toast.info('Still pending — check back later.')
    }
  } catch {
    toast.error('Could not check status. Please try again.')
  } finally {
    checking.value = false
  }
}

async function withdraw() {
  try {
    await auth.withdrawJoinRequest()
    toast.info('Request withdrawn')
    emit('withdrawn')
  } catch {
    toast.error('Could not withdraw the request. Please try again.')
  }
}
</script>

<style scoped>
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

.btn-check-status {
  height: 42px;
  border-radius: var(--radius-md);
  border: 1.5px solid #d8d7ea;
  background: #f8f8fe;
  color: #3e3b63;
  font-family: var(--font-sans);
  font-weight: var(--font-weight-medium);
  font-size: var(--font-size-sm);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn-check-status:hover {
  background: #eeeef8;
  border-color: #c5c4de;
}

.btn-check-status:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.btn-secondary {
  height: 46px;
  border-radius: var(--radius-md);
  border: 1px solid #f3cece;
  background: #fff0f0;
  color: #863131;
  font-family: var(--font-sans);
  font-weight: var(--font-weight-bold);
  cursor: pointer;
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
  to { transform: rotate(360deg); }
}

@media (max-width: 720px) {
  .onboarding-card { padding: var(--space-6); }
}
</style>
