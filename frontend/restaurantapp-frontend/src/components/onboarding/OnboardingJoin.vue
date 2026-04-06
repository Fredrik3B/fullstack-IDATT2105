<template>
  <div class="onboarding-card">
    <button class="back-btn" @click="$emit('back')">
      <ChevronLeft /> Back
    </button>

    <h2 class="card-title">Enter invitation code</h2>
    <p class="card-subtitle">Use the format <strong>XXX-0000</strong>.</p>

    <div class="join-form">
      <div class="field-group" :class="{ 'has-error': error }">
        <label class="field-label" for="joinCode">Restaurant code</label>
        <div class="field-wrapper">
          <KeyRound class="field-icon" />
          <input
            id="joinCode"
            v-model="code"
            type="text"
            class="field-input field-input--code"
            placeholder="EVR-2847"
            maxlength="8"
            autocomplete="off"
          />
        </div>
        <span v-if="error" class="field-error">{{ error }}</span>
      </div>

      <button class="btn-primary" :disabled="loading || code.length < 8" @click="submit">
        <span v-if="!loading">Send access request</span>
        <span v-else class="spinner"></span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ChevronLeft, KeyRound } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'

const emit = defineEmits(['back', 'sent'])
const auth = useAuthStore()

const code = ref('')
const error = ref('')
const loading = ref(false)

async function submit() {
  loading.value = true
  error.value = ''
  try {
    await auth.lookupRestaurant(code.value)
    await auth.joinRestaurant(code.value)
    emit('sent')
  } catch {
    error.value = 'Invalid restaurant code. Please try again.'
  } finally {
    loading.value = false
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

.btn-primary {
  height: 46px;
  border-radius: var(--radius-md);
  border: none;
  font-family: var(--font-sans);
  font-weight: var(--font-weight-bold);
  cursor: pointer;
  background: linear-gradient(135deg, var(--color-dark-primary), var(--color-dark-secondary));
  color: var(--color-accent);
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
  to { transform: rotate(360deg); }
}

@media (max-width: 720px) {
  .onboarding-card { padding: var(--space-6); }
}
</style>
