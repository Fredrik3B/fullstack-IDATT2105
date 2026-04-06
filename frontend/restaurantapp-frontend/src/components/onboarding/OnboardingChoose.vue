<template>
  <div class="option-grid">
    <button class="option-card" @click="$emit('join')">
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

<script setup>
import { LogIn, Plus, ChevronRight } from 'lucide-vue-next'
defineEmits(['join'])
</script>

<style scoped>
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
</style>
