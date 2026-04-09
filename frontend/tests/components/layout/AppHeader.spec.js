import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import AppHeader from '@/components/layout/AppHeader.vue'

// ── Mocks ──────────────────────────────────────────────────────────────────

vi.mock('vue-router', () => ({
  RouterLink: { template: '<a :href="to"><slot /></a>', props: ['to'] },
  useRoute: () => ({ fullPath: '/' }),
}))

vi.mock('@/stores/auth', () => ({
  useAuthStore: vi.fn(),
}))

vi.mock('@/composables/useToast', () => ({
  useToast: () => ({ info: vi.fn(), success: vi.fn(), error: vi.fn() }),
}))

import { useAuthStore } from '@/stores/auth'

// ── Helpers ────────────────────────────────────────────────────────────────

function makeAuthStore(overrides = {}) {
  return {
    user: { id: 1, name: 'Jane Doe', email: 'jane@example.com' },
    restaurant: { id: 'r1', name: 'Pizza Palace' },
    userInitials: 'JD',
    userRoles: ['ROLE_STAFF'],
    isAdminOrManager: false,
    logout: vi.fn(),
    ...overrides,
  }
}

function mountHeader(storeOverrides = {}) {
  const pinia = createPinia()
  setActivePinia(pinia)
  useAuthStore.mockReturnValue(makeAuthStore(storeOverrides))

  return mount(AppHeader, {
    global: { plugins: [pinia] },
    attachTo: document.body,
  })
}

// ── Setup ──────────────────────────────────────────────────────────────────

beforeEach(() => {
  vi.clearAllMocks()
})

// ── Tests ──────────────────────────────────────────────────────────────────

describe('AppHeader', () => {

  // ── Brand / restaurant name ────────────────────────────────────────────

  describe('brand', () => {
    it('displays the restaurant name in the brand tenant', () => {
      const wrapper = mountHeader()
      expect(wrapper.find('.brand-tenant').text()).toBe('Pizza Palace')
    })

    it('shows "—" when restaurant is null', () => {
      const wrapper = mountHeader({ restaurant: null })
      expect(wrapper.find('.brand-tenant').text()).toBe('—')
    })

    it('shows "ICMS" as the brand name', () => {
      const wrapper = mountHeader()
      expect(wrapper.find('.brand-name').text()).toBe('ICMS')
    })
  })

  // ── User chip ──────────────────────────────────────────────────────────

  describe('user chip', () => {
    it('displays user initials in the avatar', () => {
      const wrapper = mountHeader({ userInitials: 'JD' })
      expect(wrapper.find('.user-avatar').text()).toBe('JD')
    })

    it('displays user name', () => {
      const wrapper = mountHeader()
      expect(wrapper.find('.user-name').text()).toBe('Jane Doe')
    })

    it('falls back to email when user has no name', () => {
      const wrapper = mountHeader({ user: { name: null, email: 'jane@example.com' } })
      expect(wrapper.find('.user-name').text()).toBe('jane@example.com')
    })

    it('shows "Staff" role label for ROLE_STAFF', () => {
      const wrapper = mountHeader({ userRoles: ['ROLE_STAFF'] })
      expect(wrapper.find('.user-role').text()).toBe('Staff')
    })

    it('shows "Admin" role label for ROLE_ADMIN', () => {
      const wrapper = mountHeader({ userRoles: ['ROLE_ADMIN'] })
      expect(wrapper.find('.user-role').text()).toBe('Admin')
    })

    it('shows "Manager" role label for ROLE_MANAGER', () => {
      const wrapper = mountHeader({ userRoles: ['ROLE_MANAGER'] })
      expect(wrapper.find('.user-role').text()).toBe('Manager')
    })
  })

  // ── Dropdown ───────────────────────────────────────────────────────────

  describe('dropdown', () => {
    it('dropdown is hidden by default', () => {
      const wrapper = mountHeader()
      expect(wrapper.find('.user-dropdown').exists()).toBe(false)
    })

    it('opens dropdown when user chip is clicked', async () => {
      const wrapper = mountHeader()
      await wrapper.find('.user-chip').trigger('click')
      expect(wrapper.find('.user-dropdown').exists()).toBe(true)
    })

    it('closes dropdown on second chip click', async () => {
      const wrapper = mountHeader()
      await wrapper.find('.user-chip').trigger('click')
      await wrapper.find('.user-chip').trigger('click')
      expect(wrapper.find('.user-dropdown').exists()).toBe(false)
    })

    it('shows user name and email in dropdown header', async () => {
      const wrapper = mountHeader()
      await wrapper.find('.user-chip').trigger('click')
      expect(wrapper.find('.dropdown-name').text()).toBe('Jane Doe')
      expect(wrapper.find('.dropdown-email').text()).toBe('jane@example.com')
    })

    it('shows user initials in dropdown avatar', async () => {
      const wrapper = mountHeader({ userInitials: 'JD' })
      await wrapper.find('.user-chip').trigger('click')
      expect(wrapper.find('.dropdown-avatar').text()).toBe('JD')
    })

    it('hides Admin Panel link for non-admin users', async () => {
      const wrapper = mountHeader({ isAdminOrManager: false })
      await wrapper.find('.user-chip').trigger('click')
      const items = wrapper.findAll('.dropdown-item')
      expect(items.every(i => !i.text().includes('Admin Panel'))).toBe(true)
    })

    it('shows Admin Panel link for admin users', async () => {
      const wrapper = mountHeader({ isAdminOrManager: true })
      await wrapper.find('.user-chip').trigger('click')
      const adminLink = wrapper.findAll('.dropdown-item').find(i => i.text().includes('Admin Panel'))
      expect(adminLink).toBeTruthy()
    })
  })

  // ── Logout ─────────────────────────────────────────────────────────────

  describe('logout', () => {
    it('calls auth.logout when Log out is clicked', async () => {
      const logout = vi.fn()
      const wrapper = mountHeader({ logout })
      await wrapper.find('.user-chip').trigger('click')
      await wrapper.find('.dropdown-item--danger').trigger('click')
      expect(logout).toHaveBeenCalledOnce()
    })

    it('closes dropdown after logout click', async () => {
      const wrapper = mountHeader({ logout: vi.fn() })
      await wrapper.find('.user-chip').trigger('click')
      await wrapper.find('.dropdown-item--danger').trigger('click')
      expect(wrapper.find('.user-dropdown').exists()).toBe(false)
    })
  })

  // ── Mobile menu ────────────────────────────────────────────────────────

  describe('mobile menu', () => {
    it('mobile menu panel is hidden by default', () => {
      const wrapper = mountHeader()
      expect(wrapper.find('.mobile-menu-panel').exists()).toBe(false)
    })

    it('opens mobile menu when toggle button is clicked', async () => {
      const wrapper = mountHeader()
      await wrapper.find('.mobile-menu-toggle').trigger('click')
      expect(wrapper.find('.mobile-menu-panel').exists()).toBe(true)
    })

    it('closes mobile menu on second toggle click', async () => {
      const wrapper = mountHeader()
      await wrapper.find('.mobile-menu-toggle').trigger('click')
      await wrapper.find('.mobile-menu-toggle').trigger('click')
      expect(wrapper.find('.mobile-menu-panel').exists()).toBe(false)
    })

    it('closes dropdown when mobile menu is opened', async () => {
      const wrapper = mountHeader()
      await wrapper.find('.user-chip').trigger('click')
      expect(wrapper.find('.user-dropdown').exists()).toBe(true)
      await wrapper.find('.mobile-menu-toggle').trigger('click')
      expect(wrapper.find('.user-dropdown').exists()).toBe(false)
    })

    it('closes mobile menu when user chip is clicked', async () => {
      const wrapper = mountHeader()
      await wrapper.find('.mobile-menu-toggle').trigger('click')
      expect(wrapper.find('.mobile-menu-panel').exists()).toBe(true)
      await wrapper.find('.user-chip').trigger('click')
      expect(wrapper.find('.mobile-menu-panel').exists()).toBe(false)
    })
  })

  // ── Click outside ──────────────────────────────────────────────────────

  describe('click outside', () => {
    it('closes dropdown when clicking outside the header', async () => {
      const wrapper = mountHeader()
      await wrapper.find('.user-chip').trigger('click')
      expect(wrapper.find('.user-dropdown').exists()).toBe(true)

      document.body.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }))
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.user-dropdown').exists()).toBe(false)
    })
  })

})
