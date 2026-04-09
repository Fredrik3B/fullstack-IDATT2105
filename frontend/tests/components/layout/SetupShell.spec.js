import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import SetupShell from '@/components/layout/SetupShell.vue'

// ── Mocks ──────────────────────────────────────────────────────────────────

vi.mock('@/stores/auth', () => ({
  useAuthStore: vi.fn(),
}))

import { useAuthStore } from '@/stores/auth'

// ── Helpers ────────────────────────────────────────────────────────────────

function makeAuthStore(overrides = {}) {
  return {
    user: { email: 'jane@example.com' },
    userInitials: 'JD',
    logout: vi.fn(),
    ...overrides,
  }
}

function mountShell(props = {}, storeOverrides = {}) {
  const pinia = createPinia()
  setActivePinia(pinia)
  useAuthStore.mockReturnValue(makeAuthStore(storeOverrides))

  return mount(SetupShell, {
    global: { plugins: [pinia] },
    props,
  })
}

// ── Setup ──────────────────────────────────────────────────────────────────

beforeEach(() => {
  vi.clearAllMocks()
})

// ── Tests ──────────────────────────────────────────────────────────────────

describe('SetupShell', () => {

  // ── Brand ──────────────────────────────────────────────────────────────

  describe('brand', () => {
    it('shows "ICMS" as the brand name', () => {
      const wrapper = mountShell()
      expect(wrapper.find('.brand-name').text()).toBe('ICMS')
    })

    it('does not render brand-sub when subtitle is not provided', () => {
      const wrapper = mountShell()
      expect(wrapper.find('.brand-sub').exists()).toBe(false)
    })

    it('renders brand-sub with the subtitle text when provided', () => {
      const wrapper = mountShell({ subtitle: 'Restaurant setup' })
      expect(wrapper.find('.brand-sub').text()).toBe('Restaurant setup')
    })
  })

  // ── User section ───────────────────────────────────────────────────────

  describe('user section', () => {
    it('displays the user email', () => {
      const wrapper = mountShell({}, { user: { email: 'test@example.com' } })
      expect(wrapper.find('.user-name').text()).toBe('test@example.com')
    })

    it('shows empty string when user is null', () => {
      const wrapper = mountShell({}, { user: null })
      expect(wrapper.find('.user-name').text()).toBe('')
    })

    it('displays user initials in the avatar', () => {
      const wrapper = mountShell({}, { userInitials: 'AB' })
      expect(wrapper.find('.user-avatar').text()).toBe('AB')
    })
  })

  // ── Slot ──────────────────────────────────────────────────────────────

  describe('default slot', () => {
    it('renders slot content inside setup-body', () => {
      const wrapper = mountShell({}, {})
      // Reuse mount directly to pass slots
      const pinia = createPinia()
      setActivePinia(pinia)
      useAuthStore.mockReturnValue(makeAuthStore())
      const w = mount(SetupShell, {
        global: { plugins: [pinia] },
        slots: { default: '<div class="page-content">Hello</div>' },
      })
      expect(w.find('.setup-body .page-content').exists()).toBe(true)
      expect(w.find('.page-content').text()).toBe('Hello')
    })
  })

  // ── contentMaxWidth ────────────────────────────────────────────────────

  describe('contentMaxWidth', () => {
    it('applies default max-width of 860px to setup-body', () => {
      const wrapper = mountShell()
      expect(wrapper.find('.setup-body').attributes('style')).toContain('max-width: 860px')
    })

    it('applies a custom contentMaxWidth to setup-body', () => {
      const wrapper = mountShell({ contentMaxWidth: '600px' })
      expect(wrapper.find('.setup-body').attributes('style')).toContain('max-width: 600px')
    })
  })

  // ── Logout ─────────────────────────────────────────────────────────────

  describe('logout', () => {
    it('calls auth.logout when Log out button is clicked', async () => {
      const logout = vi.fn()
      const wrapper = mountShell({}, { logout })
      await wrapper.find('.btn-logout').trigger('click')
      expect(logout).toHaveBeenCalledOnce()
    })
  })

})
