import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import CreateRestaurantView from '@/views/auth/CreateRestaurantView.vue'

// ── Mocks ──────────────────────────────────────────────────────────────────

const mockRouterPush = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockRouterPush }),
  RouterLink: { template: '<a href="#"><slot /></a>' },
}))

vi.mock('@/stores/auth', () => ({
  useAuthStore: vi.fn(),
}))

vi.mock('@/components/layout/SetupShell.vue', () => ({
  default: { template: '<div><slot /></div>', props: ['subtitle'] },
}))

vi.mock('@/components/ui/AppButton.vue', () => ({
  default: {
    template: '<button @click="$emit(\'click\')"><slot /></button>',
    props: ['loading', 'fullWidth'],
    emits: ['click'],
  },
}))

vi.mock('@/components/ui/FormField.vue', () => ({
  default: { template: '<div><slot name="icon" /><slot /></div>', props: ['label', 'labelHint', 'inputId', 'error'] },
}))

vi.mock('lucide-vue-next', () => ({
  CheckCircle: { template: '<span />' },
  ArrowRight: { template: '<span />' },
  ChevronLeft: { template: '<span />' },
  Store: { template: '<span />' },
  FileText: { template: '<span />' },
  MapPin: { template: '<span />' },
  AlertCircle: { template: '<span />' },
}))

import { useAuthStore } from '@/stores/auth'

// ── Helpers ────────────────────────────────────────────────────────────────

function makeAuthStore(overrides = {}) {
  return {
    createRestaurant: vi.fn(),
    ...overrides,
  }
}

function mountView(storeOverrides = {}) {
  const pinia = createPinia()
  setActivePinia(pinia)
  useAuthStore.mockReturnValue(makeAuthStore(storeOverrides))

  return mount(CreateRestaurantView, {
    global: { plugins: [pinia] },
  })
}

async function fillValidForm(wrapper) {
  await wrapper.find('#restName').setValue('My Restaurant')
  // Simulate org number input — set value directly since handleOrgInput uses e.target
  const orgInput = wrapper.find('#orgNumber')
  await orgInput.setValue('123456789')
  // Trigger input event so the handler runs
  await orgInput.trigger('input')
  await wrapper.find('#address').setValue('Storgata 1')
  const postalInput = wrapper.find('#postalCode')
  await postalInput.setValue('0150')
  await postalInput.trigger('input')
  await wrapper.find('#city').setValue('Oslo')
}

// ── Setup ──────────────────────────────────────────────────────────────────

beforeEach(() => {
  vi.clearAllMocks()
  mockRouterPush.mockClear()
})

// ── Tests ──────────────────────────────────────────────────────────────────

describe('CreateRestaurantView', () => {

  // ── Rendering ──────────────────────────────────────────────────────────

  describe('rendering', () => {
    it('renders the restaurant form initially', () => {
      const wrapper = mountView()
      expect(wrapper.find('.restaurant-form').exists()).toBe(true)
      expect(wrapper.find('.result-card').exists()).toBe(false)
    })

    it('renders all required input fields', () => {
      const wrapper = mountView()
      expect(wrapper.find('#restName').exists()).toBe(true)
      expect(wrapper.find('#orgNumber').exists()).toBe(true)
      expect(wrapper.find('#address').exists()).toBe(true)
      expect(wrapper.find('#postalCode').exists()).toBe(true)
      expect(wrapper.find('#city').exists()).toBe(true)
    })

    it('does not show error alert initially', () => {
      const wrapper = mountView()
      expect(wrapper.find('.alert--error').exists()).toBe(false)
    })
  })

  // ── Validation ─────────────────────────────────────────────────────────

  describe('validation', () => {
    it('does not call createRestaurant when form is empty', async () => {
      const createRestaurant = vi.fn()
      const wrapper = mountView({ createRestaurant })

      await wrapper.find('form').trigger('submit.prevent')

      expect(createRestaurant).not.toHaveBeenCalled()
    })

    it('does not call createRestaurant when org number is too short', async () => {
      const createRestaurant = vi.fn()
      const wrapper = mountView({ createRestaurant })

      await wrapper.find('#restName').setValue('My Restaurant')
      await wrapper.find('#orgNumber').setValue('123')
      await wrapper.find('#address').setValue('Storgata 1')
      await wrapper.find('#postalCode').setValue('0150')
      await wrapper.find('#city').setValue('Oslo')
      await wrapper.find('form').trigger('submit.prevent')

      expect(createRestaurant).not.toHaveBeenCalled()
    })

    it('does not call createRestaurant when postal code is not 4 digits', async () => {
      const createRestaurant = vi.fn()
      const wrapper = mountView({ createRestaurant })

      await wrapper.find('#restName').setValue('My Restaurant')
      await wrapper.find('#orgNumber').setValue('123456789')
      await wrapper.find('#address').setValue('Storgata 1')
      await wrapper.find('#postalCode').setValue('015')
      await wrapper.find('#city').setValue('Oslo')
      await wrapper.find('form').trigger('submit.prevent')

      expect(createRestaurant).not.toHaveBeenCalled()
    })
  })

  // ── Successful creation ─────────────────────────────────────────────────

  describe('successful creation', () => {
    it('calls auth.createRestaurant with stripped org number', async () => {
      const createRestaurant = vi.fn().mockResolvedValue({ joinCode: 'NEW-0001' })
      const wrapper = mountView({ createRestaurant })

      await fillValidForm(wrapper)
      await wrapper.find('form').trigger('submit.prevent')
      await flushPromises()

      expect(createRestaurant).toHaveBeenCalledWith(expect.objectContaining({
        name: 'My Restaurant',
        address: 'Storgata 1',
        city: 'Oslo',
      }))
      // org number should have spaces stripped
      const call = createRestaurant.mock.calls[0][0]
      expect(call.orgNumber).not.toContain(' ')
    })

    it('shows the result card with join code after success', async () => {
      const createRestaurant = vi.fn().mockResolvedValue({ joinCode: 'NEW-0001' })
      const wrapper = mountView({ createRestaurant })

      await fillValidForm(wrapper)
      await wrapper.find('form').trigger('submit.prevent')
      await flushPromises()

      expect(wrapper.find('.result-card').exists()).toBe(true)
      expect(wrapper.find('.join-code-value').text()).toContain('NEW-0001')
    })

    it('hides the form after successful submission', async () => {
      const createRestaurant = vi.fn().mockResolvedValue({ joinCode: 'NEW-0001' })
      const wrapper = mountView({ createRestaurant })

      await fillValidForm(wrapper)
      await wrapper.find('form').trigger('submit.prevent')
      await flushPromises()

      expect(wrapper.find('.restaurant-form').exists()).toBe(false)
    })
  })

  // ── Failed creation ─────────────────────────────────────────────────────

  describe('failed creation', () => {
    it('shows error alert when createRestaurant throws', async () => {
      const createRestaurant = vi.fn().mockRejectedValue(new Error('Server error'))
      const wrapper = mountView({ createRestaurant })

      await fillValidForm(wrapper)
      await wrapper.find('form').trigger('submit.prevent')
      await flushPromises()

      expect(wrapper.find('.alert--error').exists()).toBe(true)
      expect(wrapper.find('.alert--error').text()).toContain('Something went wrong')
    })

    it('keeps the form visible after a failed submission', async () => {
      const createRestaurant = vi.fn().mockRejectedValue(new Error('Server error'))
      const wrapper = mountView({ createRestaurant })

      await fillValidForm(wrapper)
      await wrapper.find('form').trigger('submit.prevent')
      await flushPromises()

      expect(wrapper.find('.restaurant-form').exists()).toBe(true)
    })
  })

})
