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

function makeLoginResponse(overrides = {}) {
  return {
    data: {
      accessToken: 'fake-access-token',
      email: 'test@example.com',
      name: 'Test User',
      role: 'EMPLOYEE',
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
      expect(auth.restaurantStatus).toBeNull()
    })

    it('reads an existing token from localStorage on creation', () => {
      localStorage.setItem(ACCESS_TOKEN_KEY, 'persisted-token')
      const auth = useAuthStore()
      expect(auth.accessToken).toBe('persisted-token')
    })
  })

  // ── isAuthenticated ────────────────────────────────────────────────────

  describe('isAuthenticated', () => {
    it('is false when there is no token', () => {
      const auth = useAuthStore()
      expect(auth.isAuthenticated).toBe(false)
    })

    it('is true when a token exists in localStorage', () => {
      localStorage.setItem(ACCESS_TOKEN_KEY, 'some-token')
      const auth = useAuthStore()
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
      api.post.mockResolvedValueOnce(makeLoginResponse())
      const auth = useAuthStore()

      await auth.login('test@example.com', 'password123')

      expect(auth.accessToken).toBe('fake-access-token')
      expect(localStorage.getItem(ACCESS_TOKEN_KEY)).toBe('fake-access-token')
    })

    it('populates user state with email, name and role from response', async () => {
      api.post.mockResolvedValueOnce(makeLoginResponse())
      const auth = useAuthStore()

      await auth.login('test@example.com', 'password123')

      expect(auth.user).toMatchObject({
        email: 'test@example.com',
        name: 'Test User',
        role: 'EMPLOYEE',
      })
    })

    it('calls POST /api/auth/login with the correct credentials', async () => {
      api.post.mockResolvedValueOnce(makeLoginResponse())
      const auth = useAuthStore()

      await auth.login('test@example.com', 'password123')

      expect(api.post).toHaveBeenCalledWith('/api/auth/login', {
        email: 'test@example.com',
        password: 'password123',
      })
    })

    it('redirects to onboarding when restaurantStatus is null after login', async () => {
      api.post.mockResolvedValueOnce(makeLoginResponse())
      const auth = useAuthStore()

      await auth.login('test@example.com', 'password123')

      expect(mockPush).toHaveBeenCalledWith({ name: 'onboarding' })
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
      api.post.mockResolvedValueOnce(makeLoginResponse()) // login
      api.post.mockResolvedValueOnce({})                  // logout endpoint

      const auth = useAuthStore()
      await auth.login('test@example.com', 'password123')

      await auth.logout()

      expect(auth.accessToken).toBeNull()
      expect(localStorage.getItem(ACCESS_TOKEN_KEY)).toBeNull()
    })

    it('resets all user state', async () => {
      api.post.mockResolvedValueOnce(makeLoginResponse())
      api.post.mockResolvedValueOnce({})

      const auth = useAuthStore()
      await auth.login('test@example.com', 'password123')
      auth.restaurantStatus = 'active'
      auth.restaurantId = 42

      await auth.logout()

      expect(auth.user).toBeNull()
      expect(auth.restaurantStatus).toBeNull()
      expect(auth.restaurantId).toBeNull()
    })

    it('redirects to /login after logout', async () => {
      api.post.mockResolvedValueOnce(makeLoginResponse())
      api.post.mockResolvedValueOnce({})

      const auth = useAuthStore()
      await auth.login('test@example.com', 'password123')
      mockPush.mockClear()

      await auth.logout()

      expect(mockPush).toHaveBeenCalledWith({ name: 'login' })
    })

    it('still clears state even when the logout API call fails', async () => {
      api.post.mockResolvedValueOnce(makeLoginResponse())
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

    it('fetches current user and populates state when token is valid', async () => {
      localStorage.setItem(ACCESS_TOKEN_KEY, 'valid-token')
      api.get.mockResolvedValueOnce({
        data: {
          user: { id: 1, email: 'test@example.com', name: 'Test User' },
          restaurantStatus: 'active',
          restaurantId: 5,
          restaurantName: 'Pizza Palace',
        },
      })

      const auth = useAuthStore()
      await auth.initAuth()

      expect(auth.user).toMatchObject({ email: 'test@example.com' })
      expect(auth.restaurantStatus).toBe('active')
      expect(auth.restaurantId).toBe(5)
      expect(auth.restaurantName).toBe('Pizza Palace')
    })

    it('resets state when the token is invalid or expired', async () => {
      localStorage.setItem(ACCESS_TOKEN_KEY, 'expired-token')
      api.get.mockRejectedValueOnce(new Error('401 Unauthorized'))

      const auth = useAuthStore()
      await auth.initAuth()

      expect(auth.accessToken).toBeNull()
      expect(auth.user).toBeNull()
      expect(localStorage.getItem(ACCESS_TOKEN_KEY)).toBeNull()
    })
  })

})
