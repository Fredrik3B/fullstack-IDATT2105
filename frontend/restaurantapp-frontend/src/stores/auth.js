import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { useRouter } from 'vue-router'
import axios from 'axios'
import api from '@/api/axiosInstance'

// Keys used for localStorage persistence
const ACCESS_TOKEN_KEY = 'iksystem_access_token'
const SESSION_KEY = 'iksystem_session'

export const useAuthStore = defineStore('auth', () => {
  const router = useRouter()

  // ── State ──────────────────────────────────────────────────────────────────

  /**
   * The logged-in user's basic info.
   * Shape: { id, name, email }
   */
  const user = ref(null)

  /**
   * The restaurant (organization) the user belongs to.
   * Populated when the backend confirms an active membership.
   * Shape: { id: UUID, name: string, joinCode: string } | null
   */
  const restaurant = ref(null)

  /**
   * Short-lived JWT used as the Authorization header on every API request.
   * Populated from localStorage on app boot via initAuth().
   */
  const accessToken = ref(null)
  /**
   * Non-null means the user has submitted a request that hasn't been resolved yet.
   * Shape matches the backend JoinRequestResponse, e.g.:
   *   { status: 'PENDING', restaurantName: string, createdAt: string }
   * Null when no pending request exists.
   */
  const pendingRequest = ref(null)

  // ── Computed ───────────────────────────────────────────────────────────────

  /** True when a valid access token exists in state. */
  const isAuthenticated = computed(() => !!accessToken.value)

  /** True when the user is logged in AND connected to an active restaurant. */
  const hasActiveRestaurant = computed(() => !!restaurant.value?.id)

  /** True when the user has a pending join request waiting for approval. */
  const hasPendingRequest = computed(() => !!pendingRequest.value)

  /** Roles extracted from the JWT payload (e.g. ["ROLE_ADMIN", "ROLE_STAFF"]). */

  const userRoles = computed(() => {
    const claims = _decodeToken()
    return claims?.roles ?? []
  })
  /** True when the user holds ADMIN or MANAGER role. */
  const isAdminOrManager = computed(() =>
    userRoles.value.some(r => r === 'ROLE_ADMIN' || r === 'ROLE_MANAGER'))

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

  /** Decode JWT payload without verification. */
  function _decodeToken() {
    if (!accessToken.value) return null
    try {
      return JSON.parse(atob(accessToken.value.split('.')[1]))
    } catch {
      return null
    }
  }

  /**
   * Write current session state to localStorage.
   * Called after every successful auth response (login, register, refresh).
   */
  function _persist() {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken.value)
    localStorage.setItem(SESSION_KEY, JSON.stringify({
      user: user.value,
      restaurant: restaurant.value,
    }))
  }

  /**
   * Populate state from a LoginResponse.
   * Shape: { accessToken, user: { email, name }, restaurant: { id, name, joinCode } | null }
   */
  function _saveSession(data) {
      accessToken.value = data.accessToken
    user.value = data.user ?? null
    restaurant.value = data.restaurant ?? null

    // Backend returned a restaurant, request was accepted
    if (data.restaurant) {
      pendingRequest.value = null
    }

    _persist()
  }

  function _clearSession() {
      user.value = null
      restaurant.value = null
      accessToken.value = null
      pendingRequest.value = null
      _initDone = false
      localStorage.removeItem(ACCESS_TOKEN_KEY)
      localStorage.removeItem(SESSION_KEY)
  }

  // ── Actions ────────────────────────────────────────────────────────────────

  /**
   * Called once on app boot by the router guard.
   * Restores what we can from the JWT claims (email, orgId).
   * Display values (name, restaurant name) are only available after login.
   */
  let _initDone = false

  async function initAuth() {
    if (_initDone) return
    _initDone = true

    try {
      const token = localStorage.getItem(ACCESS_TOKEN_KEY)
      if (!token) return

      const raw = localStorage.getItem(SESSION_KEY)
      if (!raw) return

      const session = JSON.parse(raw)
      accessToken.value = token
      user.value = session.user ?? null
      restaurant.value = session.restaurant ?? null
    } catch {
      _clearSession()
      return
    }

    // Logged in but no restaurant, check for a pending join request
    if (isAuthenticated.value && !hasActiveRestaurant.value) {
      await fetchPendingRequest()
    }
  }

    /**
   * Fetch the user's current join request from the backend.
   * GET /api/organizations/join-request returns:
   *   200 + body   pending request exists
   *   204          no pending request
   */
  async function fetchPendingRequest() {
    try {
      const { status, data } = await api.get('/api/organizations/join-request')
      pendingRequest.value = status === 200 ? data : null
    } catch {
      pendingRequest.value = null
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
   */
  async function login(email, password) {
    const { data } = await api.post('/api/auth/login', { email, password })

    _saveSession(data)
    _redirectAfterAuth(data)
  }

  /**
   * Register a new user account.
   * On success the account is immediately active, but has no restaurant yet.
   * Redirects to /onboarding so the user can join or create a restaurant.
   */
  async function register(name, email, password) {
    const [firstName, ...rest] = name.trim().split(/\s+/)
    const lastName = rest.join(' ')

    const { data } = await api.post('/api/auth/register', { firstName, lastName, email, password })

    _saveSession(data)
    router.push({ name: 'onboarding' })
  }

  /**
   * Called by the Axios response interceptor when a request returns 401.
   * Attempts to exchange the refresh token for a new access token.
   * Returns the new access token string on success, or throws on failure.
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

    _saveSession(data)
    return data.accessToken
  }

  /**
   * Send a join request for a restaurant by its join code.
   */
  async function lookupRestaurant(code) {
    const { data } = await api.get(`/api/organizations/lookup?code=${code}`)
    return data // { name: String }
  }

    /**
   * Send a join request for a restaurant.
   * After the request is submitted, fetches the pending request from the
   * backend so the UI has the real data (name, timestamp, status).
   * @param {string} joinCode - The restaurant's join code
   */
  async function joinRestaurant(joinCode) {
    await api.post('/api/organizations/join', { joinCode })
    await fetchPendingRequest()
  }

  /**
   * Withdraw a pending join request.
   * Clears pendingRequest so the onboarding page returns to the choose view.
   */
  async function withdrawJoinRequest() {
    await api.delete('/api/organizations/join-request')
    pendingRequest.value = null
  }


  /**
   * Create a new restaurant. The current user becomes its admin.
   * After creation, refreshes the access token so the JWT includes
   * the new organization ID and admin role.
   * @param {Object} payload { name, orgNumber, address, postalCode, city }
   * @returns {Object} Created restaurant data (includes joinCode)
   */
  async function createRestaurant(payload) {
    const { data } = await api.post('/api/organizations', payload)
    await refreshAccessToken()
    return data
  }

  /**
   * Fetch join requests for the current user's organization.
   * @param {string|null} status - 'PENDING', 'ACCEPTED', 'DECLINED', or null for all
   */
  async function fetchJoinRequests(status = null) {
    const params = status ? `?status=${status}` : ''
    const { data } = await api.get(`/api/organizations/requests${params}`)
    return data
  }

  /**
   * Accept or decline a join request.
   * @param {string} requestId - UUID of the JoinRequestModel
   * @param {'ACCEPTED'|'DECLINED'} action
   */
  async function resolveJoinRequest(requestId, action) {
    await api.post(`/api/organizations/requests/${requestId}`, { action })
  }


  /**
   * Log out the current user.
   * Notifies the backend to invalidate the refresh token (fire-and-forget),
   * then clears all local state and redirects to /login.
   */
  async function logout() {
    try {
      await api.post('/api/auth/logout')
    } catch {
      // Don't block logout on network errors
    }
    _clearSession()
    router.push({ name: 'login' })
  }


  // ── Private routing helper ─────────────────────────────────────────────────

  async function _redirectAfterAuth(data) {
    if (data.restaurant) {
      router.push({ name: 'dashboard' })
    } else {
      await fetchPendingRequest()
      router.push({ name: 'onboarding' })
    }
  }

  // ── Member management ──────────────────────────────────────────────────────

  async function fetchMembers() {
    const { data } = await api.get('/api/organizations/members')
    return data
  }

  async function removeMember(userId) {
    await api.delete(`/api/organizations/members/${userId}`)
  }

  async function updateMemberRoles(userId, roles) {
    await api.put(`/api/organizations/members/${userId}/roles`, { roles })
  }

  // ── Public API ─────────────────────────────────────────────────────────────

  return {
    // State
    user,
    accessToken,
    restaurant,
    pendingRequest,

    // Computed
    isAuthenticated,
    hasActiveRestaurant,
    hasPendingRequest,
    userRoles,
    isAdminOrManager,
    userInitials,

    // Actions
    initAuth,
    login,
    register,
    refreshAccessToken,
    logout,
    lookupRestaurant,
    joinRestaurant,
    withdrawJoinRequest,
    createRestaurant,
    fetchPendingRequest,
    fetchJoinRequests,
    resolveJoinRequest,
    fetchMembers,
    removeMember,
    updateMemberRoles,
  }
})
