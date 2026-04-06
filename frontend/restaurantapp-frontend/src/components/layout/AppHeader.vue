<template>
  <header class="app-header" ref="headerRef">
    <div class="header-inner">

      <!-- Logo -->
      <RouterLink to="/" class="header-brand" aria-label="Go to dashboard">
        <div class="brand-text">
          <span class="brand-name">ICSystem</span>
          <span class="brand-tenant">{{ auth.restaurant?.name ?? '—' }}</span>
        </div>
      </RouterLink>

      <button
        class="mobile-menu-toggle"
        type="button"
        :aria-expanded="mobileMenuOpen"
        aria-controls="mobile-menu-panel"
        :aria-label="mobileMenuOpen ? 'Close navigation menu' : 'Open navigation menu'"
        @click="toggleMobileMenu"
      >
        <svg v-if="!mobileMenuOpen" width="18" height="18" viewBox="0 0 18 18" fill="none" aria-hidden="true">
          <path d="M2 4h14M2 9h14M2 14h14" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
        </svg>
        <svg v-else width="18" height="18" viewBox="0 0 18 18" fill="none" aria-hidden="true">
          <path d="M4 4l10 10M14 4L4 14" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
        </svg>
      </button>

      <!-- Navigation -->
      <nav class="header-nav">
        <RouterLink to="/" class="nav-item" exact-active-class="active">Dashboard</RouterLink>
        <RouterLink to="/ic-food" class="nav-item" active-class="active">IC-Food</RouterLink>
        <RouterLink to="/ic-alcohol" class="nav-item" active-class="active">IC-Alcohol</RouterLink>
        <RouterLink to="/reports" class="nav-item" active-class="active">Reports</RouterLink>
        <RouterLink to="/documents" class="nav-item" active-class="active">Documents</RouterLink>
      </nav>

      <!-- User chip + dropdown -->
      <div class="header-actions">
        <div class="user-chip-wrapper" ref="chipWrapper">
          <div class="user-chip" :class="{ open: dropdownOpen }" @click="toggleDropdown">
            <div class="user-avatar">{{ auth.userInitials }}</div>
            <div class="user-info">
              <span class="user-name">{{ auth.user?.name ?? auth.user?.email ?? 'Unknown user' }}</span>
              <span class="user-role">{{ primaryRole }}</span>
            </div>
            <svg class="caret-icon" :class="{ rotated: dropdownOpen }" width="12" height="12" viewBox="0 0 12 12" fill="none">
              <path d="M2 4L6 8L10 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>

          <div v-if="dropdownOpen" class="user-dropdown">
            <!-- User info header -->
            <div class="dropdown-header">
              <div class="dropdown-avatar">{{ auth.userInitials }}</div>
              <div class="dropdown-user-info">
                <span class="dropdown-name">{{ auth.user?.name ?? auth.user?.email }}</span>
                <span class="dropdown-email">{{ auth.user?.email }}</span>
              </div>
            </div>

            <div class="dropdown-divider" />

            <!-- Admin / Manager only -->
            <RouterLink
              v-if="auth.isAdminOrManager"
              to="/admin/requests"
              class="dropdown-item"
              @click="dropdownOpen = false"
            >
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none" class="item-icon">
                <circle cx="5" cy="4" r="2" stroke="currentColor" stroke-width="1.4"/>
                <path d="M1 11c0-2.21 1.79-4 4-4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
                <path d="M9 8v4M7 10h4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
              </svg>
              Admin Panel
            </RouterLink>

            <div v-if="auth.isAdminOrManager" class="dropdown-divider" />

            <button class="dropdown-item dropdown-item--danger" @click="handleLogout">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none" class="item-icon">
                <path d="M5 2H3a1 1 0 00-1 1v8a1 1 0 001 1h2" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
                <path d="M9 10l3-3-3-3M12 7H6" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              Log out
            </button>
          </div>
        </div>
      </div>

    </div>

    <transition name="mobile-menu">
      <div v-if="mobileMenuOpen" id="mobile-menu-panel" class="mobile-menu-panel">
        <nav class="mobile-nav" aria-label="Mobile navigation">
          <RouterLink to="/" class="nav-item" exact-active-class="active" @click="mobileMenuOpen = false">Dashboard</RouterLink>
          <RouterLink to="/ic-food" class="nav-item" active-class="active" @click="mobileMenuOpen = false">IC-Food</RouterLink>
          <RouterLink to="/ic-alcohol" class="nav-item" active-class="active" @click="mobileMenuOpen = false">IC-Alcohol</RouterLink>
          <RouterLink to="/reports" class="nav-item" active-class="active" @click="mobileMenuOpen = false">Reports</RouterLink>
          <RouterLink to="/documents" class="nav-item" active-class="active" @click="mobileMenuOpen = false">Documents</RouterLink>
        </nav>
      </div>
    </transition>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'

const auth = useAuthStore()
const toast = useToast()
const route = useRoute()

// ── Dropdown ──────────────────────────────────────────────────────────────

const headerRef = ref(null)
const chipWrapper = ref(null)
const dropdownOpen = ref(false)
const mobileMenuOpen = ref(false)

function toggleDropdown() {
  dropdownOpen.value = !dropdownOpen.value
  if (dropdownOpen.value) {
    mobileMenuOpen.value = false
  }
}

function toggleMobileMenu() {
  mobileMenuOpen.value = !mobileMenuOpen.value
  if (mobileMenuOpen.value) {
    dropdownOpen.value = false
  }
}

function handleClickOutside(e) {
  if (headerRef.value && !headerRef.value.contains(e.target)) {
    dropdownOpen.value = false
    mobileMenuOpen.value = false
  }
}

onMounted(() => document.addEventListener('mousedown', handleClickOutside))
onUnmounted(() => document.removeEventListener('mousedown', handleClickOutside))

watch(
  () => route.fullPath,
  () => {
    dropdownOpen.value = false
    mobileMenuOpen.value = false
  },
)

// ── Derived state ─────────────────────────────────────────────────────────

const primaryRole = computed(() => {
  const roles = auth.userRoles
  if (roles.includes('ROLE_ADMIN'))   return 'Admin'
  if (roles.includes('ROLE_MANAGER')) return 'Manager'
  if (roles.includes('ROLE_HR'))      return 'HR'
  if (roles.includes('ROLE_STAFF'))   return 'Staff'
  return ''
})

// ── Actions ───────────────────────────────────────────────────────────────

function handleLogout() {
  dropdownOpen.value = false
  toast.info('You are now logged out')
  auth.logout()
}
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
  min-height: var(--nav-height);
  padding: 0 var(--space-6);
  gap: var(--space-6);
  max-width: var(--max-width);
  margin: 0 auto;
}

.mobile-menu-toggle {
  display: none;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: 1px solid var(--color-dark-tertiary);
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--color-dark-border);
  cursor: pointer;
  transition: background var(--transition-fast), color var(--transition-fast), border-color var(--transition-fast);
}

.mobile-menu-toggle:hover {
  background: var(--color-dark-secondary);
  color: #ffffff;
  border-color: var(--color-dark-border);
}

/* ── Brand ── */
.header-brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-shrink: 0;
  text-decoration: none;
  cursor: pointer;
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

/* ── Actions ── */
.header-actions {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-shrink: 0;
}

/* ── User chip ── */
.user-chip-wrapper {
  position: relative;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-1) var(--space-2) var(--space-1) var(--space-1);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-dark-tertiary);
  cursor: pointer;
  transition: background var(--transition-fast), border-color var(--transition-fast);
  user-select: none;
}

.user-chip:hover,
.user-chip.open {
  background: var(--color-dark-secondary);
  border-color: var(--color-dark-border);
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
  flex-shrink: 0;
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

.caret-icon {
  color: var(--color-dark-border);
  flex-shrink: 0;
  transition: transform var(--transition-fast);
}

.caret-icon.rotated {
  transform: rotate(180deg);
}

/* ── Dropdown ── */
.user-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: min(220px, calc(100vw - 24px));
  max-width: calc(100vw - 24px);
  background: var(--color-dark-secondary);
  border: 1px solid var(--color-dark-tertiary);
  border-radius: var(--radius-lg);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.35);
  overflow: hidden;
  z-index: 200;
}

.dropdown-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
}

.dropdown-avatar {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  background: var(--color-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--color-dark-primary);
  flex-shrink: 0;
}

.dropdown-user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.dropdown-name {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: #ffffff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropdown-email {
  font-size: var(--font-size-xs);
  color: var(--color-dark-border);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropdown-divider {
  height: 1px;
  background: var(--color-dark-tertiary);
  margin: 0;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  width: 100%;
  padding: var(--space-3) var(--space-4);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-dark-border);
  text-decoration: none;
  background: transparent;
  border: none;
  cursor: pointer;
  font-family: var(--font-sans);
  transition: background var(--transition-fast), color var(--transition-fast);
  text-align: left;
}

.dropdown-item:hover {
  background: rgba(255, 255, 255, 0.06);
  color: #ffffff;
}

.dropdown-item--danger {
  color: var(--color-danger);
}

.dropdown-item--danger:hover {
  background: rgba(var(--color-danger-rgb, 220, 53, 69), 0.1);
  color: var(--color-danger);
}

.item-icon {
  flex-shrink: 0;
  opacity: 0.7;
}

.mobile-menu-panel {
  border-top: 1px solid var(--color-dark-secondary);
  padding: var(--space-2) var(--space-4) var(--space-4);
}

.mobile-nav {
  display: grid;
  gap: var(--space-1);
}

.mobile-nav .nav-item {
  width: 100%;
  justify-content: flex-start;
  padding: var(--space-3);
}

.mobile-menu-enter-active,
.mobile-menu-leave-active {
  transition: opacity var(--transition-normal), transform var(--transition-normal);
}

.mobile-menu-enter-from,
.mobile-menu-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

/* ── Responsive ── */
@media (max-width: 900px) {
  .header-inner {
    padding: 0 var(--space-4);
    gap: var(--space-3);
  }

  .header-nav {
    display: none;
  }

  .mobile-menu-toggle {
    display: inline-flex;
  }

  .header-actions {
    margin-left: auto;
  }

  .brand-tenant {
    display: none;
  }

  .user-info {
    display: none;
  }

  .caret-icon {
    display: none;
  }

  .user-chip {
    padding: var(--space-1);
  }
}

@media (max-width: 520px) {
  .header-inner {
    padding: 0 var(--space-3);
  }

  .mobile-menu-panel {
    padding-left: var(--space-3);
    padding-right: var(--space-3);
  }
}
</style>
