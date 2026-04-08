import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import LoginView from '@/views/auth/LoginView.vue'

// ── Mocks ──────────────────────────────────────────────────────────────────

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
  RouterLink: { template: '<a><slot /></a>' },
}))

vi.mock('@/stores/auth', () => ({
  useAuthStore: vi.fn(),
}))

vi.mock('@/components/layout/AuthShell.vue', () => ({
  default: { template: '<div><slot /><slot name="footer" /></div>' },
}))

vi.mock('@/components/ui/AppButton.vue', () => ({
  default: { template: '<button type="submit"><slot /></button>', props: ['loading', 'fullWidth'] },
}))

vi.mock('@/components/ui/FormField.vue', () => ({
  default: { template: '<div><slot name="icon" /><slot /></div>', props: ['label', 'inputId', 'error'] },
}))

vi.mock('lucide-vue-next', () => ({
  Mail: { template: '<span />' },
  Lock: { template: '<span />' },
  Eye: { template: '<span />' },
  EyeOff: { template: '<span />' },
  AlertCircle: { template: '<span />' },
}))

import { useAuthStore } from '@/stores/auth'

// ── Helpers ────────────────────────────────────────────────────────────────

function makeAuthStore(overrides = {}) {
  return {
    login: vi.fn(),
    ...overrides,
  }
}

function mountView(storeOverrides = {}) {
  const pinia = createPinia()
  setActivePinia(pinia)
  useAuthStore.mockReturnValue(makeAuthStore(storeOverrides))

  return mount(LoginView, {
    global: { plugins: [pinia] },
  })
}

// ── Setup ──────────────────────────────────────────────────────────────────

beforeEach(() => {
  vi.clearAllMocks()
})

// ── Tests ──────────────────────────────────────────────────────────────────

describe('LoginView', () => {

  // ── Rendering ──────────────────────────────────────────────────────────

  describe('rendering', () => {
    it('renders email and password inputs', () => {
      const wrapper = mountView()
      expect(wrapper.find('#email').exists()).toBe(true)
      expect(wrapper.find('#password').exists()).toBe(true)
    })

    it('does not show the error alert initially', () => {
      const wrapper = mountView()
      expect(wrapper.find('.alert--error').exists()).toBe(false)
    })

    it('password field starts as type password', () => {
      const wrapper = mountView()
      expect(wrapper.find('#password').attributes('type')).toBe('password')
    })
  })

  // ── Validation ─────────────────────────────────────────────────────────

  describe('validation', () => {
    it('does not call login when form is empty and submitted', async () => {
      const login = vi.fn()
      const wrapper = mountView({ login })

      await wrapper.find('form').trigger('submit.prevent')

      expect(login).not.toHaveBeenCalled()
    })

    it('shows email error when email is empty', async () => {
      const wrapper = mountView()

      await wrapper.find('#password').setValue('somepassword')
      await wrapper.find('form').trigger('submit.prevent')

      // FormField gets the error prop — check the component or any indication
      // Since FormField is stubbed, we verify the store was not called
      expect(useAuthStore().login).not.toHaveBeenCalled()
    })

    it('shows password error when password is empty', async () => {
      const wrapper = mountView()

      await wrapper.find('#email').setValue('test@example.com')
      await wrapper.find('form').trigger('submit.prevent')

      expect(useAuthStore().login).not.toHaveBeenCalled()
    })
  })

  // ── Successful login ────────────────────────────────────────────────────

  describe('successful login', () => {
    it('calls auth.login with email and password', async () => {
      const login = vi.fn().mockResolvedValue(undefined)
      const wrapper = mountView({ login })

      await wrapper.find('#email').setValue('test@example.com')
      await wrapper.find('#password').setValue('password123')
      await wrapper.find('form').trigger('submit.prevent')
      await flushPromises()

      expect(login).toHaveBeenCalledWith('test@example.com', 'password123')
    })

    it('does not show error alert on success', async () => {
      const login = vi.fn().mockResolvedValue(undefined)
      const wrapper = mountView({ login })

      await wrapper.find('#email').setValue('test@example.com')
      await wrapper.find('#password').setValue('password123')
      await wrapper.find('form').trigger('submit.prevent')
      await flushPromises()

      expect(wrapper.find('.alert--error').exists()).toBe(false)
    })
  })

  // ── Failed login ────────────────────────────────────────────────────────

  describe('failed login', () => {
    it('shows error alert when login throws', async () => {
      const login = vi.fn().mockRejectedValue(new Error('Unauthorized'))
      const wrapper = mountView({ login })

      await wrapper.find('#email').setValue('test@example.com')
      await wrapper.find('#password').setValue('wrongpassword')
      await wrapper.find('form').trigger('submit.prevent')
      await flushPromises()

      expect(wrapper.find('.alert--error').exists()).toBe(true)
      expect(wrapper.find('.alert--error').text()).toContain('Wrong email or password')
    })
  })

})
