import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'

// ── Mocks ──────────────────────────────────────────────────────────────────

const mockPush = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush }),
}))

vi.mock('@/api/axiosInstance', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

vi.mock('axios', () => ({
  default: { post: vi.fn() },
}))

// Import the mocked modules AFTER the vi.mock declarations
import api from '@/api/axiosInstance'
import axios from 'axios'

// ── Helpers ────────────────────────────────────────────────────────────────

const ACCESS_TOKEN_KEY = 'iksystem_access_token'
const SESSION_KEY = 'iksystem_session'

function makeAuthResponse(overrides = {}) {
  return {
    data: {
      accessToken: 'fake-access-token',
      user: { id: 1, email: 'test@example.com', name: 'Test User' },
      restaurant: null,
      ...overrides,
    },
  }
}

// ── Setup ──────────────────────────────────────────────────────────────────

beforeEach(() => {
  setActivePinia(createPinia())
  localStorage.clear()
  vi.clearAllMocks()
  mockPush.mockClear()
})

// ── Tests ──────────────────────────────────────────────────────────────────

describe('useAuthStore', () => {

  // ── Initial state ──────────────────────────────────────────────────────

  describe('initial state', () => {
    it('has no user or token when localStorage is empty', () => {
      const auth = useAuthStore()
      expect(auth.user).toBeNull()
      expect(auth.accessToken).toBeNull()
      expect(auth.restaurant).toBeNull()
      expect(auth.pendingRequest).toBeNull()
    })

    it('does not hydrate token before initAuth runs', () => {
      localStorage.setItem(ACCESS_TOKEN_KEY, 'persisted-token')
      const auth = useAuthStore()
      expect(auth.accessToken).toBeNull()
    })
  })

  // ── isAuthenticated ────────────────────────────────────────────────────

  describe('isAuthenticated', () => {
    it('is false when there is no token', () => {
      const auth = useAuthStore()
      expect(auth.isAuthenticated).toBe(false)
    })

    it('is true when a token exists in localStorage', () => {
      const auth = useAuthStore()

      auth.accessToken = 'some-token'
      expect(auth.isAuthenticated).toBe(true)
    })
  })

  // ── userInitials ───────────────────────────────────────────────────────

  describe('userInitials', () => {
    it('returns "?" when there is no user', () => {
      const auth = useAuthStore()
      expect(auth.userInitials).toBe('?')
    })

    it('returns first + last initial for a full name', () => {
      const auth = useAuthStore()
      auth.user = { name: 'Fredrik Bakken', email: null }
      expect(auth.userInitials).toBe('FB')
    })

    it('uses first and last word for names with middle names', () => {
      const auth = useAuthStore()
      auth.user = { name: 'Anna Marie Olsen', email: null }
      expect(auth.userInitials).toBe('AO')
    })

    it('returns first two chars for a single-word name', () => {
      const auth = useAuthStore()
      auth.user = { name: 'Admin', email: null }
      expect(auth.userInitials).toBe('AD')
    })

    it('falls back to first two chars of email when name is absent', () => {
      const auth = useAuthStore()
      auth.user = { name: null, email: 'fredrik@example.com' }
      expect(auth.userInitials).toBe('FR')
    })
  })

  // ── login ──────────────────────────────────────────────────────────────

  describe('login', () => {
    it('stores the access token in state and localStorage on success', async () => {
      api.post.mockResolvedValueOnce(makeAuthResponse())
      api.get.mockResolvedValueOnce({ status: 204, data: null })
      const auth = useAuthStore()

      await auth.login('test@example.com', 'password123')

      expect(auth.accessToken).toBe('fake-access-token')
      expect(localStorage.getItem(ACCESS_TOKEN_KEY)).toBe('fake-access-token')
      expect(localStorage.getItem(SESSION_KEY)).toContain('test@example.com')
    })

    it('populates user and restaurant state from response', async () => {
      api.post.mockResolvedValueOnce(makeAuthResponse({
        user: { id: 2, email: 'admin@example.com', name: 'Admin User' },
        restaurant: { id: 7, name: 'Pizza Palace', joinCode: 'PIZ-1000' },
      }))
      const auth = useAuthStore()

      await auth.login('admin@example.com', 'password123')

      expect(auth.user).toMatchObject({
        email: 'admin@example.com',
        name: 'Admin User',
      })
      expect(auth.restaurant).toMatchObject({ id: 7, name: 'Pizza Palace', joinCode: 'PIZ-1000' })
    })

    it('calls POST /api/auth/login with the correct credentials', async () => {
      api.post.mockResolvedValueOnce(makeAuthResponse())
      api.get.mockResolvedValueOnce({ status: 204, data: null })
      const auth = useAuthStore()

      await auth.login('test@example.com', 'password123')

      expect(api.post).toHaveBeenCalledWith('/api/auth/login', {
        email: 'test@example.com',
        password: 'password123',
      })
    })

    it('redirects to onboarding when login response has no active restaurant', async () => {
      api.post.mockResolvedValueOnce(makeAuthResponse({ restaurant: null }))
      api.get.mockResolvedValueOnce({
        status: 200,
        data: { status: 'PENDING', restaurantName: 'Pending Place', createdAt: '2026-04-06T12:00:00Z' },
      })
      const auth = useAuthStore()

      await auth.login('test@example.com', 'password123')

      expect(mockPush).toHaveBeenCalledWith({ name: 'onboarding' })
      expect(auth.pendingRequest).toMatchObject({ restaurantName: 'Pending Place' })
    })

    it('redirects to dashboard when login response includes an active restaurant', async () => {
      api.post.mockResolvedValueOnce(makeAuthResponse({
        restaurant: { id: 9, name: 'Best Restaurant', joinCode: 'BST-0001' },
      }))
      const auth = useAuthStore()

      await auth.login('test@example.com', 'password123')

      expect(mockPush).toHaveBeenCalledWith({ name: 'dashboard' })
    })

    it('throws when the API call fails', async () => {
      api.post.mockRejectedValueOnce(new Error('Invalid credentials'))
      const auth = useAuthStore()

      await expect(auth.login('wrong@example.com', 'badpass')).rejects.toThrow()
    })

    it('leaves state unchanged when login fails', async () => {
      api.post.mockRejectedValueOnce(new Error('Invalid credentials'))
      const auth = useAuthStore()

      try { await auth.login('wrong@example.com', 'badpass') } catch { /* expected */ }

      expect(auth.accessToken).toBeNull()
      expect(auth.user).toBeNull()
      expect(localStorage.getItem(ACCESS_TOKEN_KEY)).toBeNull()
    })
  })

  // ── logout ─────────────────────────────────────────────────────────────

  describe('logout', () => {
    it('clears token from state and localStorage', async () => {
      api.post.mockResolvedValueOnce(makeAuthResponse())   // login
      api.get.mockResolvedValueOnce({ status: 204, data: null })
      api.post.mockResolvedValueOnce({})                  // logout endpoint

      const auth = useAuthStore()
      await auth.login('test@example.com', 'password123')

      await auth.logout()

      expect(auth.accessToken).toBeNull()
      expect(localStorage.getItem(ACCESS_TOKEN_KEY)).toBeNull()
    })

    it('resets all user state', async () => {
      api.post.mockResolvedValueOnce(makeAuthResponse({
        restaurant: { id: 3, name: 'Reset Place', joinCode: 'RST-3000' },
      }))
      api.post.mockResolvedValueOnce({})

      const auth = useAuthStore()
      await auth.login('test@example.com', 'password123')

      await auth.logout()

      expect(auth.user).toBeNull()
      expect(auth.restaurant).toBeNull()
      expect(auth.pendingRequest).toBeNull()
    })

    it('redirects to /login after logout', async () => {
      api.post.mockResolvedValueOnce(makeAuthResponse())
      api.get.mockResolvedValueOnce({ status: 204, data: null })
      api.post.mockResolvedValueOnce({})

      const auth = useAuthStore()
      await auth.login('test@example.com', 'password123')
      mockPush.mockClear()

      await auth.logout()

      expect(mockPush).toHaveBeenCalledWith({ name: 'login' })
    })

    it('still clears state even when the logout API call fails', async () => {
      api.post.mockResolvedValueOnce(makeAuthResponse())
      api.get.mockResolvedValueOnce({ status: 204, data: null })
      api.post.mockRejectedValueOnce(new Error('Network error'))

      const auth = useAuthStore()
      await auth.login('test@example.com', 'password123')

      await auth.logout()

      expect(auth.accessToken).toBeNull()
      expect(auth.user).toBeNull()
    })
  })

  // ── initAuth ───────────────────────────────────────────────────────────

  describe('initAuth', () => {
    it('does nothing when there is no token', async () => {
      const auth = useAuthStore()
      await auth.initAuth()
      expect(api.get).not.toHaveBeenCalled()
    })

    it('restores state from persisted local session', async () => {
      localStorage.setItem(ACCESS_TOKEN_KEY, 'valid-token')
      localStorage.setItem(SESSION_KEY, JSON.stringify({
        user: { id: 1, email: 'test@example.com', name: 'Test User' },
        restaurant: { id: 5, name: 'Pizza Palace', joinCode: 'PIZ-1234' },
      }))

      const auth = useAuthStore()
      await auth.initAuth()

      expect(auth.accessToken).toBe('valid-token')
      expect(auth.user).toMatchObject({ email: 'test@example.com' })
      expect(auth.restaurant).toMatchObject({ id: 5, name: 'Pizza Palace', joinCode: 'PIZ-1234' })
      expect(api.get).not.toHaveBeenCalled()
    })

    it('hydrates pending request when logged in without restaurant', async () => {
      localStorage.setItem(ACCESS_TOKEN_KEY, 'valid-token')
      localStorage.setItem(SESSION_KEY, JSON.stringify({
        user: { id: 1, email: 'test@example.com', name: 'Test User' },
        restaurant: null,
      }))
      api.get.mockResolvedValueOnce({
        status: 200,
        data: { status: 'PENDING', restaurantName: 'Pending Place', createdAt: '2026-04-06T12:00:00Z' },
      })

      const auth = useAuthStore()
      await auth.initAuth()

      expect(auth.pendingRequest).toMatchObject({ restaurantName: 'Pending Place' })
    })

    it('clears state when persisted session JSON is malformed', async () => {
      localStorage.setItem(ACCESS_TOKEN_KEY, 'expired-token')
      localStorage.setItem(SESSION_KEY, '{bad-json')

      const auth = useAuthStore()
      await auth.initAuth()

      expect(auth.accessToken).toBeNull()
      expect(auth.user).toBeNull()
      expect(localStorage.getItem(ACCESS_TOKEN_KEY)).toBeNull()
      expect(localStorage.getItem(SESSION_KEY)).toBeNull()
    })

    it('does not run twice when called multiple times', async () => {
      localStorage.setItem(ACCESS_TOKEN_KEY, 'valid-token')
      localStorage.setItem(SESSION_KEY, JSON.stringify({
        user: { id: 1, email: 'test@example.com', name: 'Test User' },
        restaurant: { id: 5, name: 'Pizza Palace', joinCode: 'PIZ-1234' },
      }))

      const auth = useAuthStore()
      await auth.initAuth()
      await auth.initAuth()

      expect(api.get).not.toHaveBeenCalled()
    })
  })

  // ── register ───────────────────────────────────────────────────────────

  describe('register', () => {
    it('calls POST /api/auth/register with split first/last name', async () => {
      api.post.mockResolvedValueOnce(makeAuthResponse())
      const auth = useAuthStore()

      await auth.register('Jane Doe', 'jane@example.com', 'pass1234')

      expect(api.post).toHaveBeenCalledWith('/api/auth/register', {
        firstName: 'Jane',
        lastName: 'Doe',
        email: 'jane@example.com',
        password: 'pass1234',
      })
    })

    it('handles single-word name by sending empty lastName', async () => {
      api.post.mockResolvedValueOnce(makeAuthResponse())
      const auth = useAuthStore()

      await auth.register('Admin', 'admin@example.com', 'pass1234')

      expect(api.post).toHaveBeenCalledWith('/api/auth/register', expect.objectContaining({
        firstName: 'Admin',
        lastName: '',
      }))
    })

    it('saves token and user after successful registration', async () => {
      api.post.mockResolvedValueOnce(makeAuthResponse())
      const auth = useAuthStore()

      await auth.register('Jane Doe', 'jane@example.com', 'pass1234')

      expect(auth.accessToken).toBe('fake-access-token')
      expect(auth.user).toMatchObject({ email: 'test@example.com' })
    })

    it('redirects to onboarding after registration', async () => {
      api.post.mockResolvedValueOnce(makeAuthResponse())
      const auth = useAuthStore()

      await auth.register('Jane Doe', 'jane@example.com', 'pass1234')

      expect(mockPush).toHaveBeenCalledWith({ name: 'onboarding' })
    })

    it('throws on registration failure', async () => {
      api.post.mockRejectedValueOnce(new Error('Email taken'))
      const auth = useAuthStore()

      await expect(auth.register('Jane Doe', 'jane@example.com', 'pass1234')).rejects.toThrow()
    })
  })

  // ── refreshAccessToken ─────────────────────────────────────────────────

  describe('refreshAccessToken', () => {
    it('returns the new access token on success', async () => {
      const { data } = makeAuthResponse({ accessToken: 'new-token' })
      axios.post = vi.fn().mockResolvedValueOnce({ data })

      const auth = useAuthStore()
      const token = await auth.refreshAccessToken()

      expect(token).toBe('new-token')
    })
  })

  // ── fetchPendingRequest ────────────────────────────────────────────────

  describe('fetchPendingRequest', () => {
    it('sets pendingRequest when backend returns 200', async () => {
      api.get.mockResolvedValueOnce({
        status: 200,
        data: { status: 'PENDING', restaurantName: 'My Place', createdAt: '2026-04-01T10:00:00Z' },
      })
      const auth = useAuthStore()

      await auth.fetchPendingRequest()

      expect(auth.pendingRequest).toMatchObject({ restaurantName: 'My Place' })
    })

    it('sets pendingRequest to null when backend returns 204', async () => {
      api.get.mockResolvedValueOnce({ status: 204, data: null })
      const auth = useAuthStore()
      auth.pendingRequest = { restaurantName: 'Old' }

      await auth.fetchPendingRequest()

      expect(auth.pendingRequest).toBeNull()
    })

    it('sets pendingRequest to null on network error', async () => {
      api.get.mockRejectedValueOnce(new Error('Network error'))
      const auth = useAuthStore()
      auth.pendingRequest = { restaurantName: 'Old' }

      await auth.fetchPendingRequest()

      expect(auth.pendingRequest).toBeNull()
    })
  })

  // ── joinRestaurant ─────────────────────────────────────────────────────

  describe('joinRestaurant', () => {
    it('posts to /api/organizations/join with the code', async () => {
      api.post.mockResolvedValueOnce({})
      api.get.mockResolvedValueOnce({ status: 204, data: null })
      const auth = useAuthStore()

      await auth.joinRestaurant('EVR-1234')

      expect(api.post).toHaveBeenCalledWith('/api/organizations/join', { joinCode: 'EVR-1234' })
    })

    it('fetches the pending request after posting', async () => {
      api.post.mockResolvedValueOnce({})
      api.get.mockResolvedValueOnce({
        status: 200,
        data: { status: 'PENDING', restaurantName: 'Everest', createdAt: '2026-04-01T00:00:00Z' },
      })
      const auth = useAuthStore()

      await auth.joinRestaurant('EVR-1234')

      expect(auth.pendingRequest).toMatchObject({ restaurantName: 'Everest' })
    })
  })

  // ── withdrawJoinRequest ────────────────────────────────────────────────

  describe('withdrawJoinRequest', () => {
    it('calls DELETE /api/organizations/join-request', async () => {
      api.delete.mockResolvedValueOnce({})
      const auth = useAuthStore()
      auth.pendingRequest = { restaurantName: 'Some Place' }

      await auth.withdrawJoinRequest()

      expect(api.delete).toHaveBeenCalledWith('/api/organizations/join-request')
    })

    it('clears pendingRequest after withdrawal', async () => {
      api.delete.mockResolvedValueOnce({})
      const auth = useAuthStore()
      auth.pendingRequest = { restaurantName: 'Some Place' }

      await auth.withdrawJoinRequest()

      expect(auth.pendingRequest).toBeNull()
    })
  })

  // ── createRestaurant ───────────────────────────────────────────────────

  describe('createRestaurant', () => {
    it('posts to /api/organizations and returns the created restaurant data', async () => {
      const createdRestaurant = { id: 'uuid-1', name: 'New Place', joinCode: 'NEW-0001' }
      api.post.mockResolvedValueOnce({ data: createdRestaurant })
      // refreshAccessToken uses plain axios
      axios.post = vi.fn().mockResolvedValueOnce({ data: makeAuthResponse().data })

      const auth = useAuthStore()
      const result = await auth.createRestaurant({ name: 'New Place', orgNumber: '123456789', address: 'Gate 1', postalCode: '0150', city: 'Oslo' })

      expect(result).toMatchObject({ joinCode: 'NEW-0001' })
    })
  })

  // ── member management ──────────────────────────────────────────────────

  describe('member management', () => {
    it('fetchMembers calls GET /api/organizations/members', async () => {
      const members = [{ id: 1, name: 'Alice' }]
      api.get.mockResolvedValueOnce({ data: members })
      const auth = useAuthStore()

      const result = await auth.fetchMembers()

      expect(api.get).toHaveBeenCalledWith('/api/organizations/members')
      expect(result).toEqual(members)
    })

    it('removeMember calls DELETE with the user id', async () => {
      api.delete.mockResolvedValueOnce({})
      const auth = useAuthStore()

      await auth.removeMember(42)

      expect(api.delete).toHaveBeenCalledWith('/api/organizations/members/42')
    })

    it('updateMemberRoles calls PUT with the new roles', async () => {
      api.put = vi.fn().mockResolvedValueOnce({})
      const auth = useAuthStore()

      await auth.updateMemberRoles(42, ['ROLE_ADMIN'])

      expect(api.put).toHaveBeenCalledWith('/api/organizations/members/42/roles', { roles: ['ROLE_ADMIN'] })
    })
  })

  // ── userRoles / isAdminOrManager ───────────────────────────────────────

  describe('userRoles and isAdminOrManager', () => {
    function makeJwt(payload) {
      const encoded = btoa(JSON.stringify(payload))
      return `header.${encoded}.sig`
    }

    it('returns empty roles when there is no token', () => {
      const auth = useAuthStore()
      expect(auth.userRoles).toEqual([])
    })

    it('extracts roles from the JWT payload', () => {
      const auth = useAuthStore()
      auth.accessToken = makeJwt({ roles: ['ROLE_STAFF'] })
      expect(auth.userRoles).toEqual(['ROLE_STAFF'])
    })

    it('isAdminOrManager is true for ROLE_ADMIN', () => {
      const auth = useAuthStore()
      auth.accessToken = makeJwt({ roles: ['ROLE_ADMIN'] })
      expect(auth.isAdminOrManager).toBe(true)
    })

    it('isAdminOrManager is true for ROLE_MANAGER', () => {
      const auth = useAuthStore()
      auth.accessToken = makeJwt({ roles: ['ROLE_MANAGER'] })
      expect(auth.isAdminOrManager).toBe(true)
    })

    it('isAdminOrManager is false for ROLE_STAFF', () => {
      const auth = useAuthStore()
      auth.accessToken = makeJwt({ roles: ['ROLE_STAFF'] })
      expect(auth.isAdminOrManager).toBe(false)
    })

    it('returns empty roles when JWT is malformed', () => {
      const auth = useAuthStore()
      auth.accessToken = 'not.a.jwt'
      expect(auth.userRoles).toEqual([])
    })
  })

})
