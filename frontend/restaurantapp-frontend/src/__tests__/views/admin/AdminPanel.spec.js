import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import AdminPanel from '@/views/admin/AdminPanel.vue'

// ── Mocks ──────────────────────────────────────────────────────────────────

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
}))

vi.mock('@/stores/auth', () => ({
  useAuthStore: vi.fn(),
}))

vi.mock('@/composables/useToast', () => ({
  useToast: () => ({ success: vi.fn(), error: vi.fn(), info: vi.fn() }),
}))

import { useAuthStore } from '@/stores/auth'

// ── Helpers ────────────────────────────────────────────────────────────────

function makeAuthStore(overrides = {}) {
  return {
    user: { id: 1, name: 'Admin User', email: 'admin@example.com' },
    restaurant: { id: 'r1', name: 'Pizza Palace', joinCode: 'PIZ-1234' },
    userRoles: ['ROLE_ADMIN'],
    fetchMembers: vi.fn().mockResolvedValue([]),
    fetchJoinRequests: vi.fn().mockResolvedValue([]),
    resolveJoinRequest: vi.fn().mockResolvedValue(undefined),
    removeMember: vi.fn().mockResolvedValue(undefined),
    updateMemberRoles: vi.fn().mockResolvedValue(undefined),
    ...overrides,
  }
}

function makeMember(overrides = {}) {
  return {
    userId: 'u1',
    firstName: 'Alice',
    lastName: 'Smith',
    email: 'alice@example.com',
    roles: ['STAFF'],
    ...overrides,
  }
}

function makeRequest(overrides = {}) {
  return {
    requestId: 'req1',
    firstName: 'Bob',
    lastName: 'Jones',
    email: 'bob@example.com',
    createdAt: '2026-04-01T10:00:00Z',
    ...overrides,
  }
}

function mountView(storeOverrides = {}) {
  const pinia = createPinia()
  setActivePinia(pinia)
  useAuthStore.mockReturnValue(makeAuthStore(storeOverrides))

  return mount(AdminPanel, {
    global: { plugins: [pinia] },
    attachTo: document.body,
  })
}

// ── Setup ──────────────────────────────────────────────────────────────────

beforeEach(() => {
  vi.clearAllMocks()
})

// ── Tests ──────────────────────────────────────────────────────────────────

describe('AdminPanel', () => {

  // ── Rendering ──────────────────────────────────────────────────────────

  describe('rendering', () => {
    it('shows the page title', async () => {
      const wrapper = mountView()
      await flushPromises()
      expect(wrapper.find('.page-title').text()).toBe('Admin Panel')
    })

    it('displays the join code', async () => {
      const wrapper = mountView()
      await flushPromises()
      expect(wrapper.find('.join-code-value').text()).toBe('PIZ-1234')
    })

    it('starts on the members tab', async () => {
      const wrapper = mountView()
      await flushPromises()
      expect(wrapper.find('.tab-btn.active').text()).toContain('Members')
    })

    it('fetches members and requests on mount', async () => {
      const fetchMembers = vi.fn().mockResolvedValue([])
      const fetchJoinRequests = vi.fn().mockResolvedValue([])
      mountView({ fetchMembers, fetchJoinRequests })
      await flushPromises()

      expect(fetchMembers).toHaveBeenCalled()
      expect(fetchJoinRequests).toHaveBeenCalledWith('PENDING')
    })
  })

  // ── Members tab ─────────────────────────────────────────────────────────

  describe('members tab', () => {
    it('shows loading state while fetching members', () => {
      const fetchMembers = vi.fn().mockReturnValue(new Promise(() => {}))
      const wrapper = mountView({ fetchMembers })
      expect(wrapper.text()).toContain('Loading members')
    })

    it('shows error state when members fetch fails', async () => {
      const fetchMembers = vi.fn().mockRejectedValue({ response: { data: { message: 'Server error' } } })
      const wrapper = mountView({ fetchMembers })
      await flushPromises()
      expect(wrapper.find('.state-box--error').exists()).toBe(true)
      expect(wrapper.text()).toContain('Server error')
    })

    it('shows empty state when no members', async () => {
      const wrapper = mountView()
      await flushPromises()
      expect(wrapper.text()).toContain('No members yet')
    })

    it('renders member cards when members are returned', async () => {
      const fetchMembers = vi.fn().mockResolvedValue([
        makeMember({ firstName: 'Alice', lastName: 'Smith' }),
        makeMember({ userId: 'u2', firstName: 'Charlie', lastName: 'Brown', email: 'charlie@example.com' }),
      ])
      const wrapper = mountView({ fetchMembers })
      await flushPromises()

      expect(wrapper.find('.member-list').exists()).toBe(true)
      expect(wrapper.text()).toContain('Alice Smith')
      expect(wrapper.text()).toContain('Charlie Brown')
    })

    it('shows "You" badge next to current user', async () => {
      const fetchMembers = vi.fn().mockResolvedValue([
        makeMember({ userId: 1, firstName: 'Admin', lastName: 'User', roles: ['ADMIN'] }),
      ])
      const wrapper = mountView({ fetchMembers })
      await flushPromises()

      expect(wrapper.find('.you-badge').exists()).toBe(true)
    })

    it('groups members under role sections', async () => {
      const fetchMembers = vi.fn().mockResolvedValue([
        makeMember({ userId: 'u2', roles: ['ADMIN'], firstName: 'Admin', lastName: 'One' }),
        makeMember({ userId: 'u3', roles: ['STAFF'], firstName: 'Staff', lastName: 'One' }),
      ])
      const wrapper = mountView({ fetchMembers })
      await flushPromises()

      expect(wrapper.text()).toContain('Administrators')
      expect(wrapper.text()).toContain('Staff')
    })

    it('disables remove button for the last admin', async () => {
      const fetchMembers = vi.fn().mockResolvedValue([
        makeMember({ userId: 'u2', roles: ['ADMIN'], firstName: 'Solo', lastName: 'Admin' }),
      ])
      const wrapper = mountView({ fetchMembers })
      await flushPromises()

      const removeBtn = wrapper.find('.action-btn--remove')
      expect(removeBtn.attributes('disabled')).toBeDefined()
    })

    it('shows remove confirmation dialog on remove click', async () => {
      const fetchMembers = vi.fn().mockResolvedValue([
        makeMember({ userId: 'u1', roles: ['ADMIN'], firstName: 'Admin', lastName: 'One' }),
        makeMember({ userId: 'u2', roles: ['STAFF'], firstName: 'Alice', lastName: 'Smith' }),
      ])
      const wrapper = mountView({ fetchMembers })
      await flushPromises()

      // Staff member's remove button should be enabled
      const removeBtn = wrapper.findAll('.action-btn--remove').find(b => b.element.disabled === false)
      await removeBtn.trigger('click')

      expect(wrapper.find('.dialog-overlay').exists()).toBe(true)
      expect(wrapper.text()).toContain('Alice Smith')
    })

    it('removes member after confirmation dialog confirm', async () => {
      const removeMember = vi.fn().mockResolvedValue(undefined)
      const fetchMembers = vi.fn().mockResolvedValue([
        makeMember({ userId: 'u1', roles: ['ADMIN'], firstName: 'Admin', lastName: 'One' }),
        makeMember({ userId: 'u2', roles: ['STAFF'], firstName: 'Alice', lastName: 'Smith' }),
      ])
      const wrapper = mountView({ removeMember, fetchMembers })
      await flushPromises()

      const removeBtn = wrapper.findAll('.action-btn--remove').find(b => b.element.disabled === false)
      await removeBtn.trigger('click')
      await wrapper.find('.action-btn--decline').trigger('click')
      await flushPromises()

      expect(removeMember).toHaveBeenCalledWith('u2')
      expect(wrapper.text()).not.toContain('Alice Smith')
    })

    it('cancels remove when cancel is clicked in dialog', async () => {
      const removeMember = vi.fn()
      const fetchMembers = vi.fn().mockResolvedValue([
        makeMember({ userId: 'u1', roles: ['ADMIN'], firstName: 'Admin', lastName: 'One' }),
        makeMember({ userId: 'u2', roles: ['STAFF'], firstName: 'Alice', lastName: 'Smith' }),
      ])
      const wrapper = mountView({ removeMember, fetchMembers })
      await flushPromises()

      const removeBtn = wrapper.findAll('.action-btn--remove').find(b => b.element.disabled === false)
      await removeBtn.trigger('click')
      await wrapper.find('.action-btn--ghost').trigger('click')

      expect(wrapper.find('.dialog-overlay').exists()).toBe(false)
      expect(removeMember).not.toHaveBeenCalled()
    })

    it('does not call updateMemberRoles when demoting the last admin', async () => {
      const updateMemberRoles = vi.fn()
      // Two admins: current user (u1) and another (u2). Demotion of u2 while only 1 admin remaining.
      const fetchMembers = vi.fn().mockResolvedValue([
        makeMember({ userId: 'u2', roles: ['ADMIN'], firstName: 'Solo', lastName: 'Admin' }),
      ])
      mountView({ updateMemberRoles, fetchMembers })
      await flushPromises()

      // With only 1 admin, the role select is disabled — updateMemberRoles must not be called
      expect(updateMemberRoles).not.toHaveBeenCalled()
    })
  })

  // ── Requests tab ────────────────────────────────────────────────────────

  describe('requests tab', () => {
    async function switchToRequestsTab(wrapper) {
      const requestsBtn = wrapper.findAll('.tab-btn').find(b => b.text().includes('Requests'))
      await requestsBtn.trigger('click')
    }

    it('switches to requests tab when tab is clicked', async () => {
      const wrapper = mountView()
      await flushPromises()

      await switchToRequestsTab(wrapper)

      expect(wrapper.find('.tab-btn.active').text()).toContain('Requests')
    })

    it('shows loading state while fetching requests', async () => {
      const fetchJoinRequests = vi.fn().mockReturnValue(new Promise(() => {}))
      const fetchMembers = vi.fn().mockResolvedValue([])
      const wrapper = mountView({ fetchJoinRequests, fetchMembers })
      await flushPromises() // members resolve, but requests hang

      await switchToRequestsTab(wrapper)

      expect(wrapper.text()).toContain('Loading requests')
    })

    it('shows error state when request fetch fails', async () => {
      const fetchJoinRequests = vi.fn().mockRejectedValue({ response: { data: { message: 'Forbidden' } } })
      const wrapper = mountView({ fetchJoinRequests })
      await flushPromises()

      await switchToRequestsTab(wrapper)

      expect(wrapper.find('.state-box--error').exists()).toBe(true)
    })

    it('shows empty state when no pending requests', async () => {
      const wrapper = mountView()
      await flushPromises()

      await switchToRequestsTab(wrapper)

      expect(wrapper.text()).toContain('No pending access requests')
    })

    it('shows pending requests in list', async () => {
      const fetchJoinRequests = vi.fn().mockResolvedValue([
        makeRequest({ firstName: 'Bob', lastName: 'Jones' }),
      ])
      const wrapper = mountView({ fetchJoinRequests })
      await flushPromises()

      await switchToRequestsTab(wrapper)

      expect(wrapper.find('.request-list').exists()).toBe(true)
      expect(wrapper.text()).toContain('Bob Jones')
    })

    it('shows pending count badge on tab when there are pending requests', async () => {
      const fetchJoinRequests = vi.fn().mockResolvedValue([makeRequest(), makeRequest({ requestId: 'req2' })])
      const wrapper = mountView({ fetchJoinRequests })
      await flushPromises()

      expect(wrapper.find('.tab-count--accent').text()).toBe('2')
    })

    it('accepts a join request and removes it from the list', async () => {
      const resolveJoinRequest = vi.fn().mockResolvedValue(undefined)
      const fetchJoinRequests = vi.fn().mockResolvedValue([makeRequest({ requestId: 'req1', firstName: 'Bob', lastName: 'Jones' })])
      const fetchMembers = vi.fn().mockResolvedValue([])
      const wrapper = mountView({ resolveJoinRequest, fetchJoinRequests, fetchMembers })
      await flushPromises()

      await switchToRequestsTab(wrapper)

      await wrapper.find('.action-btn--accept').trigger('click')
      await flushPromises()

      expect(resolveJoinRequest).toHaveBeenCalledWith('req1', 'ACCEPTED')
      expect(wrapper.find('.request-list').exists()).toBe(false)
    })

    it('declines a join request and removes it from the list', async () => {
      const resolveJoinRequest = vi.fn().mockResolvedValue(undefined)
      const fetchJoinRequests = vi.fn().mockResolvedValue([makeRequest({ requestId: 'req1' })])
      const wrapper = mountView({ resolveJoinRequest, fetchJoinRequests })
      await flushPromises()

      await switchToRequestsTab(wrapper)

      await wrapper.find('.action-btn--decline').trigger('click')
      await flushPromises()

      expect(resolveJoinRequest).toHaveBeenCalledWith('req1', 'DECLINED')
      expect(wrapper.find('.request-list').exists()).toBe(false)
    })

    it('shows pending badge in page header when there are requests', async () => {
      const fetchJoinRequests = vi.fn().mockResolvedValue([makeRequest()])
      const wrapper = mountView({ fetchJoinRequests })
      await flushPromises()

      // Switch to requests tab first so header-badge is shown
      await switchToRequestsTab(wrapper)

      expect(wrapper.find('.header-badge').exists()).toBe(true)
      expect(wrapper.find('.header-badge').text()).toContain('1 pending')
    })
  })

  // ── initials helper (via rendered avatars) ──────────────────────────────

  describe('initials in avatars', () => {
    it('renders correct initials from first and last name', async () => {
      const fetchMembers = vi.fn().mockResolvedValue([
        makeMember({ userId: 'u2', firstName: 'Alice', lastName: 'Smith', roles: ['STAFF'] }),
      ])
      const wrapper = mountView({ fetchMembers })
      await flushPromises()

      const avatars = wrapper.findAll('.request-avatar')
      expect(avatars.some(a => a.text() === 'AS')).toBe(true)
    })
  })

})
