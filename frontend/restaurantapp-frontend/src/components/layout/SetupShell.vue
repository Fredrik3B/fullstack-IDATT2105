<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

defineProps({
  subtitle: {
    type: String,
    default: '',
  },
  contentMaxWidth: {
    type: String,
    default: '860px',
  },
})

const auth = useAuthStore()
const userEmail = computed(() => auth.user?.email ?? '')
const userInitials = computed(() => auth.userInitials)

function handleLogout() {
  auth.logout()
}
</script>

<template>
  <div class="setup-page">
    <div class="ambient" aria-hidden="true">
      <span class="blob blob--a"></span>
      <span class="blob blob--b"></span>
    </div>

    <header class="topbar">
      <div class="topbar-brand">
        <div class="brand-logo"><span class="brand-icon">IC</span></div>
        <div>
          <span class="brand-name">ICSystem</span>
          <p v-if="subtitle" class="brand-sub">{{ subtitle }}</p>
        </div>
      </div>
      <div class="topbar-user">
        <div class="user-avatar">{{ userInitials }}</div>
        <span class="user-name">{{ userEmail }}</span>
        <button class="btn-logout" type="button" @click="handleLogout">Log out</button>
      </div>
    </header>

    <main class="setup-body" :style="{ maxWidth: contentMaxWidth }">
      <slot />
    </main>
  </div>
</template>

<style scoped>
.setup-page {
  min-height: 100vh;
  position: relative;
  background: linear-gradient(145deg, #17162f 0%, #24224b 52%, #2e2b59 100%);
  overflow-x: hidden;
  overflow-y: auto;
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
  width: clamp(220px, 42vw, 360px);
  height: clamp(220px, 42vw, 360px);
  background: radial-gradient(circle, rgba(212, 232, 53, 0.25), rgba(212, 232, 53, 0));
  top: -120px;
  right: -90px;
}

.blob--b {
  width: clamp(180px, 34vw, 300px);
  height: clamp(180px, 34vw, 300px);
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

.setup-body {
  position: relative;
  z-index: 1;
  margin: 0 auto;
  padding: var(--space-10) var(--space-6) var(--space-12);
}

@media (max-width: 760px) {
  .topbar {
    padding: var(--space-3) var(--space-4);
    flex-wrap: wrap;
    gap: var(--space-2);
  }

  .user-name {
    display: none;
  }

  .topbar-user {
    margin-left: auto;
  }

  .setup-body {
    padding: var(--space-8) var(--space-4) var(--space-10);
  }
}
</style>
