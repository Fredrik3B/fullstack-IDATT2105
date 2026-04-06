<template>
  <header class="topbar">
    <div class="topbar-brand">
      <div class="brand-logo"><span class="brand-icon">IC</span></div>
      <div>
        <span class="brand-name">ICSystem</span>
        <p class="brand-sub">Team onboarding</p>
      </div>
    </div>
    <div class="topbar-user">
      <div class="user-avatar">{{ auth.userInitials }}</div>
      <span class="user-name">{{ auth.user?.email }}</span>
      <button class="btn-logout" @click="auth.logout()">Log out</button>
    </div>
  </header>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
const auth = useAuthStore()
</script>

<style scoped>
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

@media (max-width: 720px) {
  .topbar { padding: var(--space-3) var(--space-4); }
  .user-name { display: none; }
}
</style>
