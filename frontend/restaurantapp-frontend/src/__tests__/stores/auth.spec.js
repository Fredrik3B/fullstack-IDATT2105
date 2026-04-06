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
    delete: vi.fn(),
  },
}))

// Import the mocked api AFTER the vi.mock declarations
import api from '@/api/axiosInstance'

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
      })
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
  })

})
