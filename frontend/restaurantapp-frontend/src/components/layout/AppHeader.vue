<template>
  <header class="app-header">
    <div class="header-inner">

      <!-- Logo -->
      <div class="header-brand">
        <div class="brand-text">
          <span class="brand-name">IKSystem</span>
          <span class="brand-tenant">{{ auth.restaurantName ?? '—' }}</span>
        </div>
      </div>

      <!-- Navigation -->
      <nav class="header-nav">
        <RouterLink to="/" class="nav-item" exact-active-class="active">Dashbord</RouterLink>
        <RouterLink to="/ic-food" class="nav-item" active-class="active">IC-Food</RouterLink>
        <RouterLink to="/ic-alcohol" class="nav-item" active-class="active">IC-Alcohol</RouterLink>
        <RouterLink to="/reports" class="nav-item" active-class="active">Reports</RouterLink>
        <RouterLink to="/documents" class="nav-item" active-class="active">Documents</RouterLink>
      </nav>

      <div class="header-actions">
        <div class="user-chip" @click="auth.logout()">
          <div class="user-avatar">{{ auth.userInitials }}</div>
          <div class="user-info">
            <span class="user-name">{{ auth.user?.name ?? auth.user?.email ?? 'Ukjent bruker' }}</span>
            <span class="user-role">{{ auth.user?.role ?? 'Logg ut' }}</span>
          </div>
        </div>
      </div>

    </div>

  </header>
</template>

<script setup>
import { RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
</script>

<style scoped>
.app-header {
  background: var(--color-dark-primary);
  border-bottom: 1px solid var(--color-dark-secondary);
  position: sticky;
  top: 0;
  width: 100%;
  z-index: 100;
}

.header-inner {
  display: flex;
  align-items: center;
  height: var(--nav-height);
  padding: 0 var(--space-6);
  gap: var(--space-6);
  max-width: var(--max-width);
  margin: 0 auto;
}

/* ── Brand ── */
.header-brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-shrink: 0;
}

.brand-text {
  display: flex;
  flex-direction: column;
  line-height: var(--line-height-tight);
}

.brand-name {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  color: #ffffff;
}

.brand-tenant {
  font-size: var(--font-size-xs);
  color: var(--color-dark-border);
}

/* ── Nav ── */
.header-nav {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  flex: 1;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-md);
  text-decoration: none;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-dark-border);
  transition: background var(--transition-fast), color var(--transition-fast);
}

.nav-item:hover {
  background: var(--color-dark-secondary);
  color: #ffffff;
}

.nav-item.active {
  background: var(--color-dark-secondary);
  color: var(--color-accent);
}

.nav-badge {
  background: var(--color-danger);
  color: #ffffff;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  border-radius: var(--radius-full);
  padding: 1px var(--space-1);
  line-height: 1.4;
}

/* ── Actions ── */
.header-actions {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-shrink: 0;
}

.notif-btn {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-dark-tertiary);
  background: transparent;
  color: var(--color-dark-border);
  cursor: pointer;
  transition: background var(--transition-fast), color var(--transition-fast);
}

.notif-btn:hover {
  background: var(--color-dark-secondary);
  color: #ffffff;
}

.notif-badge {
  position: absolute;
  top: -3px;
  right: -3px;
  background: var(--color-danger);
  color: #ffffff;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  border-radius: var(--radius-full);
  padding: 1px var(--space-1);
  line-height: 1.5;
  border: 1.5px solid var(--color-dark-primary);
}

.user-chip {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-1) var(--space-3) var(--space-1) var(--space-1);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-dark-tertiary);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.user-chip:hover {
  background: var(--color-dark-secondary);
}

.user-avatar {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-sm);
  background: var(--color-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  color: var(--color-dark-primary);
}

.user-info {
  display: flex;
  flex-direction: column;
  line-height: var(--line-height-tight);
}

.user-name {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: #ffffff;
}

.user-role {
  font-size: var(--font-size-xs);
  color: var(--color-dark-border);
}

/* ── Module bar ── */
.module-bar {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  height: 34px;
  padding: 0 var(--space-6);
  background: var(--color-dark-secondary);
  max-width: var(--max-width);
  margin: 0 auto;
}

.module-pill {
  padding: 2px var(--space-3);
  border-radius: var(--radius-full);
  border: 1px solid var(--color-dark-tertiary);
  background: transparent;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
  color: var(--color-dark-border);
  cursor: pointer;
  font-family: var(--font-sans);
  transition: all var(--transition-fast);
}

.module-pill:hover {
  border-color: var(--color-accent);
  color: var(--color-accent);
}

.module-pill.active {
  background: var(--color-accent);
  border-color: var(--color-accent);
  color: var(--color-dark-primary);
}

.last-sync {
  margin-left: auto;
  font-size: var(--font-size-xs);
  color: var(--color-dark-tertiary);
}

/* ── Responsiv ── */
@media (max-width: 768px) {
  .header-nav { display: none; }
  .brand-tenant { display: none; }
  .user-info { display: none; }
}
</style>
