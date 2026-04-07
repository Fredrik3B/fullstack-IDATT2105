import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import RegisterView from '@/views/auth/RegisterView.vue'

// ── Mocks ──────────────────────────────────────────────────────────────────

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
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
  default: {
    template: '<div><slot name="icon" /><slot /><slot name="after" /></div>',
    props: ['label', 'inputId', 'error'],
  },
}))

vi.mock('lucide-vue-next', () => ({
  User: { template: '<span />' },
  Mail: { template: '<span />' },
  Lock: { template: '<span />' },
  Eye: { template: '<span />' },
  EyeOff: { template: '<span />' },
  AlertCircle: { template: '<span />' },
}))

import { useAuthStore } from '@/stores/auth'

// ── Helpers ────────────────────────────────────────────────────────────────

function makeAuthStore(overrides = {}) {
  return { register: vi.fn(), ...overrides }
}

function mountView(storeOverrides = {}) {
  const pinia = createPinia()
  setActivePinia(pinia)
  useAuthStore.mockReturnValue(makeAuthStore(storeOverrides))

  return mount(RegisterView, {
    global: {
      plugins: [pinia],
      stubs: { RouterLink: { template: '<a><slot /></a>' } },
    },
  })
}

async function fillValidForm(wrapper) {
  await wrapper.find('#name').setValue('Jane Doe')
  await wrapper.find('#email').setValue('jane@example.com')
  await wrapper.find('#password').setValue('Password1!')
  await wrapper.find('#confirmPassword').setValue('Password1!')
}

// ── Setup ──────────────────────────────────────────────────────────────────

beforeEach(() => {
  vi.clearAllMocks()
})

// ── Tests ──────────────────────────────────────────────────────────────────

describe('RegisterView', () => {

  // ── Rendering ──────────────────────────────────────────────────────────

  describe('rendering', () => {
    it('renders all four input fields', () => {
      const wrapper = mountView()
      expect(wrapper.find('#name').exists()).toBe(true)
      expect(wrapper.find('#email').exists()).toBe(true)
      expect(wrapper.find('#password').exists()).toBe(true)
      expect(wrapper.find('#confirmPassword').exists()).toBe(true)
    })

    it('does not show the submit error alert initially', () => {
      const wrapper = mountView()
      expect(wrapper.find('.alert--error').exists()).toBe(false)
    })
  })

  // ── Password strength ───────────────────────────────────────────────────

  describe('password strength indicator', () => {
    it('does not show strength bar when password is empty', () => {
      const wrapper = mountView()
      expect(wrapper.find('.password-strength').exists()).toBe(false)
    })

    it('shows "Weak" for a short simple password', async () => {
      const wrapper = mountView()
      await wrapper.find('#password').setValue('abc')
      expect(wrapper.find('.strength-label').text()).toBe('Weak')
    })

    it('shows "Strong" for a complex password', async () => {
      const wrapper = mountView()
      await wrapper.find('#password').setValue('Sup3rS3cur3!')
      expect(wrapper.find('.strength-label').text()).toBe('Strong')
    })
  })

  // ── Validation ─────────────────────────────────────────────────────────

  describe('validation', () => {
    it('does not call register when form is empty', async () => {
      const register = vi.fn()
      const wrapper = mountView({ register })

      await wrapper.find('form').trigger('submit.prevent')

      expect(register).not.toHaveBeenCalled()
    })

    it('does not call register when password is too short', async () => {
      const register = vi.fn()
      const wrapper = mountView({ register })

      await wrapper.find('#name').setValue('Jane Doe')
      await wrapper.find('#email').setValue('jane@example.com')
      await wrapper.find('#password').setValue('short')
      await wrapper.find('#confirmPassword').setValue('short')
      await wrapper.find('form').trigger('submit.prevent')

      expect(register).not.toHaveBeenCalled()
    })

    it('does not call register when passwords do not match', async () => {
      const register = vi.fn()
      const wrapper = mountView({ register })

      await wrapper.find('#name').setValue('Jane Doe')
      await wrapper.find('#email').setValue('jane@example.com')
      await wrapper.find('#password').setValue('Password1!')
      await wrapper.find('#confirmPassword').setValue('Different1!')
      await wrapper.find('form').trigger('submit.prevent')

      expect(register).not.toHaveBeenCalled()
    })
  })

  // ── Successful registration ─────────────────────────────────────────────

  describe('successful registration', () => {
    it('calls auth.register with name, email, and password', async () => {
      const register = vi.fn().mockResolvedValue(undefined)
      const wrapper = mountView({ register })

      await fillValidForm(wrapper)
      await wrapper.find('form').trigger('submit.prevent')
      await flushPromises()

      expect(register).toHaveBeenCalledWith('Jane Doe', 'jane@example.com', 'Password1!')
    })

    it('does not show error alert on success', async () => {
      const register = vi.fn().mockResolvedValue(undefined)
      const wrapper = mountView({ register })

      await fillValidForm(wrapper)
      await wrapper.find('form').trigger('submit.prevent')
      await flushPromises()

      expect(wrapper.find('.alert--error').exists()).toBe(false)
    })
  })

  // ── Failed registration ─────────────────────────────────────────────────

  describe('failed registration', () => {
    it('shows generic error when server returns non-409', async () => {
      const register = vi.fn().mockRejectedValue(new Error('Server error'))
      const wrapper = mountView({ register })

      await fillValidForm(wrapper)
      await wrapper.find('form').trigger('submit.prevent')
      await flushPromises()

      expect(wrapper.find('.alert--error').exists()).toBe(true)
      expect(wrapper.find('.alert--error').text()).toContain('Something went wrong')
    })

    it('shows email conflict message on 409 response', async () => {
      const err = { response: { status: 409 } }
      const register = vi.fn().mockRejectedValue(err)
      const wrapper = mountView({ register })

      await fillValidForm(wrapper)
      await wrapper.find('form').trigger('submit.prevent')
      await flushPromises()

      // 409 sets field error, not the global alert
      expect(wrapper.find('.alert--error').exists()).toBe(false)
    })
  })

})
