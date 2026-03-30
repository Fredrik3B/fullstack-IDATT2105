import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { useRouter } from 'vue-router'
import axios from 'axios'
import api from '@/api/axiosInstance'

// Keys used for localStorage persistence
const ACCESS_TOKEN_KEY = 'iksystem_access_token'

export const useAuthStore = defineStore('auth', () => {
  const router = useRouter()

  // ── State ──────────────────────────────────────────────────────────────────

  /**
   * The logged-in user's basic info.
   * Shape: { id, name, email }
   */
  const user = ref(null)

  /**
   * Short-lived JWT used as the Authorization header on every API request.
   * Populated from localStorage on app boot via initAuth().
   */
  const accessToken = ref(localStorage.getItem(ACCESS_TOKEN_KEY) ?? null)

  /**
   * The user's relationship to a restaurant.
   *   null      — no request made yet
   *   'pending' — request sent, waiting for admin approval
   *   'active'  — approved and connected to a restaurant
   */
  const restaurantStatus = ref(null)

  /**
   * ID of the restaurant the user is connected to (active or pending).
   * Null when restaurantStatus is null.
   */
  const restaurantId = ref(null)

  /**
   * Display name of the restaurant the user is connected to.
   * Populated on createRestaurant, joinRestaurant, and initAuth.
   */
  const restaurantName = ref(null)

  // ── Computed ───────────────────────────────────────────────────────────────

  /** True when a valid access token exists in state. */
  const isAuthenticated = computed(() => !!accessToken.value)

  /** True when the user is logged in AND connected to an active restaurant. */
  const hasActiveRestaurant = computed(() => restaurantStatus.value === 'active')

  /** Initials derived from the user's name (FL), falling back to email. */
  const userInitials = computed(() => {
    if (user.value?.name) {
      const parts = user.value.name.trim().split(/\s+/)
      if (parts.length >= 2) return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
      return parts[0].slice(0, 2).toUpperCase()
    }
    if (user.value?.email) return user.value.email.slice(0, 2).toUpperCase()
    return '?'
  })

  // ── Internal helpers ───────────────────────────────────────────────────────

  /** Remove access token from state and localStorage. */
  function _clearTokens() {
    accessToken.value = null
    localStorage.removeItem(ACCESS_TOKEN_KEY)
  }

  /** Reset all state to initial/logged-out values. */
  function _resetState() {
    user.value             = null
    restaurantStatus.value = null
    restaurantId.value     = null
    restaurantName.value   = null
    _clearTokens()
  }

  // ── Actions ────────────────────────────────────────────────────────────────

  /**
   Called once on app boot (main.js or App.vue).
   If a token is already in localStorage, fetches the current user from the
   backend to validate the token and repopulate state.
   if he token is invalid/expired, or the refresh also fails, state is reset
   */
  async function initAuth() {
    if (!accessToken.value) return

    try {
      const { data } = await api.get('/api/auth/me')
      user.value             = data.user
      restaurantStatus.value = data.restaurantStatus
      restaurantId.value     = data.restaurantId
      restaurantName.value   = data.restaurantName ?? null
    } catch {
      // Token is invalid or expired and refresh also failed — force logout
      _resetState()
    }
  }

  /**
   * Log in with email and password.
   * On success, stores tokens, populates user state, then redirects based on
   * restaurant status:
   *   active  → dashboard
   *   pending → onboarding (pending view)
   *   null    → onboarding (choose view)
   *
   * Throws on failure so the calling component can show an error message.
   *
   * TODO: replace the placeholder with a real API call, e.g.:
   *   import api from '@/api/axiosInstance'
   *   const { data } = await api.post('/api/auth/login', { email, password })
   */
  async function login(email, password) {
    const { data } = await api.post('/api/auth/login', { email, password })

    accessToken.value = data.accessToken
    localStorage.setItem(ACCESS_TOKEN_KEY, data.accessToken)

    user.value             = { email: data.email, name: data.name ?? null, role: data.role ?? null }
    restaurantStatus.value = null
    restaurantId.value     = null

    _redirectAfterAuth()
  }

  /**
   * Register a new user account.
   * On success the account is immediately active, but has no restaurant yet.
   * Redirects to /onboarding so the user can join or create a restaurant.
   *
   * TODO: replace the placeholder with a real API call, e.g.:
   *   import api from '@/api/axiosInstance'
   *   const { data } = await api.post('/api/auth/register', { name, email, password })
   */
  async function register(name, email, password) {
    const [firstName, ...rest] = name.trim().split(/\s+/)
    const lastName = rest.join(' ')

    const { data } = await api.post('/api/auth/register', { firstName, lastName, email, password })

    accessToken.value = data.accessToken
    localStorage.setItem(ACCESS_TOKEN_KEY, data.accessToken)

    user.value             = { email: data.email, name: `${firstName} ${lastName}`.trim(), role: null }
    restaurantStatus.value = null
    restaurantId.value     = null

    router.push({ name: 'onboarding' })
  }

  /**
   * Called by the Axios response interceptor when a request returns 401.
   * Attempts to exchange the refresh token for a new access token.
   * Returns the new access token string on success, or throws on failure.
   *
   * NOTE: this function is intentionally NOT async-safe against parallel calls.
   * The Axios interceptor should queue concurrent requests while a refresh is
   * in progress. That logic lives in axiosInstance.js.
   *
   */
  async function refreshAccessToken() {
    // Uses plain axios — NOT the intercepted api instance.
    // If we used api here, a failed refresh would trigger the interceptor
    // again, which would call refreshAccessToken() again → infinite loop.
    // The HTTPOnly refresh token cookie is sent automatically by the browser.
    const baseURL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
    const { data } = await axios.post(`${baseURL}/api/auth/refresh`, null, {
      withCredentials: true,
    })

    accessToken.value = data.accessToken
    localStorage.setItem(ACCESS_TOKEN_KEY, data.accessToken)
    return data.accessToken
  }

  /**
   * Send a join request for a restaurant by its join code.
   * Sets restaurantStatus to 'pending' on success.
   *
   * TODO: replace the placeholder with a real API call, e.g.:
   *   import api from '@/api/axiosInstance'
   *   const { data } = await api.post('/api/restaurants/join', { joinCode })
   */
  async function lookupRestaurant(code) {
    const { data } = await api.get(`/api/restaurants/lookup?code=${code}`)
    return data // { name: String }
  }

  async function joinRestaurant(joinCode, name) {
    const { data } = await api.post('/api/restaurants/join', { joinCode })

    restaurantStatus.value = 'pending'
    restaurantId.value     = data.restaurantId
    restaurantName.value   = name ?? null
  }

  /**
   * Withdraw a pending join request.
   * Resets restaurantStatus and restaurantId to null.
   *
   * TODO: replace the placeholder with a real API call, e.g.:
   *   import api from '@/api/axiosInstance'
   *   await api.delete('/api/restaurants/join-request')
   */
  async function withdrawJoinRequest() {
    await api.delete('/api/restaurants/join-request')

    restaurantStatus.value = null
    restaurantId.value     = null
  }

  /**
   * Create a new restaurant. The user becomes its admin on success.
   * Sets restaurantStatus to 'active' immediately.
   *
   * TODO: replace the placeholder with a real API call, e.g.:
   *   import api from '@/api/axiosInstance'
   *   const { data } = await api.post('/api/restaurants', payload)
   */
  async function createRestaurant(payload) {
    // payload: { name, orgNumber, address, postalCode, city }
    const { data } = await api.post('/api/restaurants', payload)

    restaurantStatus.value = 'active'
    restaurantId.value     = data.restaurantId
    restaurantName.value   = payload.name

    return data // let the view display the joinCode returned by the backend
  }

  /**
   * Log out the current user.
   * Optionally notifies the backend to invalidate the refresh token.
   * Always clears local state and redirects to /login.
   *
   * TODO: optionally call POST /api/auth/logout to invalidate server-side session
   */
  async function logout() {
    try {
      // Fire and forget — don't block logout on a network error
      await api.post('/api/auth/logout')
    } catch {
      // Ignore errors — we're logging out regardless
    } finally {
      _resetState()
      router.push({ name: 'login' })
    }
  }

  // ── Private routing helper ─────────────────────────────────────────────────

  function _redirectAfterAuth() {
    if (restaurantStatus.value === 'active') {
      router.push({ name: 'dashboard' })
    } else {
      // null or 'pending' — both handled inside RestaurantOnboardingView
      router.push({ name: 'onboarding' })
    }
  }

  // ── Public API ─────────────────────────────────────────────────────────────

  return {
    // State
    user,
    accessToken,
    restaurantStatus,
    restaurantId,
    restaurantName,

    // Computed
    isAuthenticated,
    hasActiveRestaurant,
    userInitials,

    // Actions
    initAuth,
    login,
    register,
    refreshAccessToken,
    lookupRestaurant,
    joinRestaurant,
    withdrawJoinRequest,
    createRestaurant,
    logout,
  }
})
