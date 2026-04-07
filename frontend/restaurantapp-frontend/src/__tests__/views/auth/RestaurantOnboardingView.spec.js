import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import RestaurantOnboardingView from '@/views/auth/RestaurantOnboardingView.vue'

// ── Mocks ──────────────────────────────────────────────────────────────────

const mockRouterPush = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockRouterPush }),
  RouterLink: { template: '<a><slot /></a>' },
}))

vi.mock('@/stores/auth', () => ({
  useAuthStore: vi.fn(),
}))

vi.mock('@/composables/useToast', () => ({
  useToast: () => ({ info: vi.fn(), success: vi.fn(), error: vi.fn() }),
}))

vi.mock('@/components/layout/SetupShell.vue', () => ({
  default: { template: '<div><slot /></div>', props: ['subtitle', 'contentMaxWidth'] },
}))

vi.mock('@/components/ui/AppButton.vue', () => ({
  default: {
    template: '<button @click="$emit(\'click\')"><slot /></button>',
    props: ['loading', 'fullWidth', 'variant', 'disabled'],
    emits: ['click'],
  },
}))

vi.mock('@/components/ui/FormField.vue', () => ({
  default: { template: '<div><slot name="icon" /><slot /><span class="field-error">{{ error }}</span></div>', props: ['label', 'inputId', 'error'] },
}))

vi.mock('lucide-vue-next', () => ({
  Clock: { template: '<span />' },
  Store: { template: '<span />' },
  Info: { template: '<span />' },
  LogIn: { template: '<span />' },
  Plus: { template: '<span />' },
  ChevronRight: { template: '<span />' },
  ChevronLeft: { template: '<span />' },
  KeyRound: { template: '<span />' },
}))

import { useAuthStore } from '@/stores/auth'

// ── Helpers ────────────────────────────────────────────────────────────────

function makeAuthStore(overrides = {}) {
  return {
    user: { id: 1, email: 'test@example.com', name: 'Test User' },
    hasPendingRequest: false,
    hasActiveRestaurant: false,
    pendingRequest: null,
    lookupRestaurant: vi.fn(),
    joinRestaurant: vi.fn(),
    withdrawJoinRequest: vi.fn(),
    refreshAccessToken: vi.fn(),
    ...overrides,
  }
}

function mountView(storeOverrides = {}) {
  const pinia = createPinia()
  setActivePinia(pinia)
  useAuthStore.mockReturnValue(makeAuthStore(storeOverrides))

  return mount(RestaurantOnboardingView, {
    global: { plugins: [pinia] },
  })
}

// ── Setup ──────────────────────────────────────────────────────────────────

beforeEach(() => {
  vi.clearAllMocks()
  mockRouterPush.mockClear()
})

// ── Tests ──────────────────────────────────────────────────────────────────

describe('RestaurantOnboardingView', () => {

  // ── Choose view (default) ───────────────────────────────────────────────

  describe('choose view', () => {
    it('shows the choose view when user has no pending request', () => {
      const wrapper = mountView()
      expect(wrapper.find('.option-grid').exists()).toBe(true)
    })

    it('shows step 1 of 2 in the stepper', () => {
      const wrapper = mountView()
      expect(wrapper.text()).toContain('Step 1 of 2')
    })
  })

  // ── Pending view ────────────────────────────────────────────────────────

  describe('pending view', () => {
    it('shows pending card when user has a pending request', () => {
      const wrapper = mountView({
        hasPendingRequest: true,
        pendingRequest: {
          restaurantName: 'Pizza Palace',
          createdAt: '2026-04-01T10:00:00Z',
          status: 'PENDING',
        },
      })

      expect(wrapper.find('.card--pending').exists()).toBe(true)
      expect(wrapper.text()).toContain('Pizza Palace')
    })

    it('shows step 2 of 2 when in pending view', () => {
      const wrapper = mountView({
        hasPendingRequest: true,
        pendingRequest: { restaurantName: 'Pizza Palace', createdAt: '2026-04-01T10:00:00Z' },
      })

      expect(wrapper.text()).toContain('Step 2 of 2')
    })

    it('calls withdrawJoinRequest when withdraw button is clicked', async () => {
      const withdrawJoinRequest = vi.fn().mockResolvedValue(undefined)
      const wrapper = mountView({
        hasPendingRequest: true,
        pendingRequest: { restaurantName: 'Pizza Palace', createdAt: '2026-04-01T10:00:00Z' },
        withdrawJoinRequest,
      })

      const withdrawBtn = wrapper.findAll('button').find(b => b.text().includes('Withdraw'))
      await withdrawBtn.trigger('click')
      await flushPromises()

      expect(withdrawJoinRequest).toHaveBeenCalled()
    })

    it('reverts to choose view after withdrawing request', async () => {
      const withdrawJoinRequest = vi.fn().mockResolvedValue(undefined)
      const wrapper = mountView({
        hasPendingRequest: true,
        pendingRequest: { restaurantName: 'Pizza Palace', createdAt: '2026-04-01T10:00:00Z' },
        withdrawJoinRequest,
      })

      const withdrawBtn = wrapper.findAll('button').find(b => b.text().includes('Withdraw'))
      await withdrawBtn.trigger('click')
      await flushPromises()

      expect(wrapper.find('.option-grid').exists()).toBe(true)
    })
  })

  // ── Join view ───────────────────────────────────────────────────────────

  describe('join view', () => {
    async function goToJoinView(wrapper) {
      const joinCard = wrapper.findAll('.option-card')[0]
      await joinCard.trigger('click')
    }

    it('switches to join view when "Join existing restaurant" is clicked', async () => {
      const wrapper = mountView()
      await goToJoinView(wrapper)
      expect(wrapper.find('#joinCode').exists()).toBe(true)
    })

    it('shows step 2 of 2 in join view', async () => {
      const wrapper = mountView()
      await goToJoinView(wrapper)
      expect(wrapper.text()).toContain('Step 2 of 2')
    })

    it('returns to choose view when back button is clicked', async () => {
      const wrapper = mountView()
      await goToJoinView(wrapper)

      const backBtn = wrapper.findAll('button').find(b => b.text().includes('Back'))
      await backBtn.trigger('click')

      expect(wrapper.find('.option-grid').exists()).toBe(true)
    })

    it('calls lookupRestaurant and joinRestaurant on submit with valid code', async () => {
      const lookupRestaurant = vi.fn().mockResolvedValue({ name: 'Everest' })
      const joinRestaurant = vi.fn().mockResolvedValue(undefined)
      const wrapper = mountView({ lookupRestaurant, joinRestaurant, pendingRequest: { restaurantName: 'Everest', createdAt: '2026-04-01T00:00:00Z' } })
      await goToJoinView(wrapper)

      await wrapper.find('#joinCode').setValue('EVR-1234')
      const sendBtn = wrapper.findAll('button').find(b => b.text().includes('Send access request'))
      await sendBtn.trigger('click')
      await flushPromises()

      expect(lookupRestaurant).toHaveBeenCalledWith('EVR-1234')
      expect(joinRestaurant).toHaveBeenCalledWith('EVR-1234')
    })

    it('shows error message when lookup fails', async () => {
      const lookupRestaurant = vi.fn().mockRejectedValue(new Error('Not found'))
      const wrapper = mountView({ lookupRestaurant })
      await goToJoinView(wrapper)

      await wrapper.find('#joinCode').setValue('BAD-1234')
      const sendBtn = wrapper.findAll('button').find(b => b.text().includes('Send access request'))
      await sendBtn.trigger('click')
      await flushPromises()

      expect(wrapper.text()).toContain('Invalid restaurant code')
    })
  })

  // ── Check status ────────────────────────────────────────────────────────

  describe('check status', () => {
    it('redirects to dashboard when request has been accepted', async () => {
      const refreshAccessToken = vi.fn().mockResolvedValue(undefined)
      const wrapper = mountView({
        hasPendingRequest: true,
        hasActiveRestaurant: true,
        pendingRequest: { restaurantName: 'Pizza Palace', createdAt: '2026-04-01T10:00:00Z' },
        refreshAccessToken,
      })

      const checkBtn = wrapper.findAll('button').find(b => b.text().includes('Check status'))
      await checkBtn.trigger('click')
      await flushPromises()

      expect(mockRouterPush).toHaveBeenCalledWith({ name: 'dashboard' })
    })
  })

})
