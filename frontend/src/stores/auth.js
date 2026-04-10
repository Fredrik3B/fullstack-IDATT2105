import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { useRouter } from 'vue-router'
import axios from 'axios'
import api from '@/api/axiosInstance'

/** LocalStorage key for the current JWT access token. */
const ACCESS_TOKEN_KEY = 'iksystem_access_token'
/** LocalStorage key for serialized session metadata. */
const SESSION_KEY = 'iksystem_session'
/** LocalStorage key for the last-active timestamp (epoch ms). */
const LAST_ACTIVE_KEY = 'iksystem_last_active'
/** Maximum idle time before forcing re-login (5 minutes). */
const SESSION_IDLE_LIMIT_MS = 5 * 60 * 1000

/**
 * @typedef {Object} AuthUser
 * @property {string|number} [id]
 * @property {string} [name]
 * @property {string} [email]
 */

/**
 * @typedef {Object} Restaurant
 * @property {string|number} id
 * @property {string} name
 * @property {string} [joinCode]
 */

/**
 * @typedef {Object} JoinRequest
 * @property {string} status
 * @property {string} [restaurantName]
 * @property {string} [createdAt]
 */

/**
 * @typedef {Object} AuthSessionResponse
 * @property {string} accessToken
 * @property {AuthUser|null} [user]
 * @property {Restaurant|null} [restaurant]
 */

/**
 * Authentication store handling session state, membership flow, and role-based auth helpers.
 */
export const useAuthStore = defineStore('auth', () => {
  const router = useRouter()

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

  /** Decode JWT payload without verification. */
  function _decodeToken() {
    if (!accessToken.value) return null
    try {
      return JSON.parse(atob(accessToken.value.split('.')[1]))
    } catch {
      return null
    }
  }

  function _decodeTokenValue(token) {
    if (!token) return null
    try {
      return JSON.parse(atob(token.split('.')[1]))
    } catch {
      return null
    }
  }

  function _isTokenExpired(token) {
    const claims = _decodeTokenValue(token)
    if (!claims?.exp) return false
    return claims.exp * 1000 <= Date.now()
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
    touchActivity()
  }

  /** Update the last-active timestamp. Called on every API request. */
  function touchActivity() {
    localStorage.setItem(LAST_ACTIVE_KEY, String(Date.now()))
  }

  /** True when the session has been idle longer than the allowed limit. */
  function _isSessionIdle() {
    const raw = localStorage.getItem(LAST_ACTIVE_KEY)
    if (!raw) return true
    return Date.now() - Number(raw) > SESSION_IDLE_LIMIT_MS
  }

  /**
   * Persist auth state from backend auth responses (login/register/refresh).
   *
   * @param {AuthSessionResponse} data - Backend auth response payload.
   */
  function _saveSession(data) {
    accessToken.value = data.accessToken
    user.value = data.user ?? null
    restaurant.value = data.restaurant ?? null

    if (data.restaurant) {
      pendingRequest.value = null
    }

    _persist()
  }

  /**
   * Reset all auth state and clear persisted session data.
   */
  function _clearSession() {
    user.value = null
    restaurant.value = null
    accessToken.value = null
    pendingRequest.value = null
    _initDone = false
    localStorage.removeItem(ACCESS_TOKEN_KEY)
    localStorage.removeItem(SESSION_KEY)
    localStorage.removeItem(LAST_ACTIVE_KEY)

    if (typeof window !== 'undefined') {
      window.dispatchEvent(new Event('ic-session-reset'))
    }
  }

  /**
   * Called once on app boot by the router guard.
   * Restores what we can from the JWT claims (email, orgId).
   * Display values (name, restaurant name) are only available after login.
    *
    * @returns {Promise<void>}
   */
  let _initDone = false

  async function initAuth() {
    if (_initDone) return
    _initDone = true

    let token = null
    let session = null

    try {
      token = localStorage.getItem(ACCESS_TOKEN_KEY)
      if (!token) return

      const raw = localStorage.getItem(SESSION_KEY)
      if (!raw) return

      session = JSON.parse(raw)
    } catch {
      _clearSession()
      return
    }

    if (_isSessionIdle()) {
      _clearSession()
      return
    }

    if (_isTokenExpired(token)) {
      try {
        await refreshAccessToken()
      } catch {
        _clearSession()
        return
      }
    } else {
      accessToken.value = token
      user.value = session.user ?? null
      restaurant.value = session.restaurant ?? null
    }

    if (isAuthenticated.value && !hasActiveRestaurant.value) {
      await fetchPendingRequest()
    }
  }

  /**
   * Fetch the user's current join request from the backend.
   * Returns null in state when no request exists or the call fails.
    *
    * @returns {Promise<void>}
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
  * @param {string} email - User email.
  * @param {string} password - User password.
  * @returns {Promise<void>}
  * @throws {Error} Propagates backend/auth request failures.
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
    *
    * @param {string} name - Full name entered by the user.
    * @param {string} email - User email.
    * @param {string} password - User password.
    * @returns {Promise<void>}
    * @throws {Error} Propagates backend/auth request failures.
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
    *
    * @returns {Promise<string>} New access token.
    * @throws {Error} Propagates refresh failures.
   */
  async function refreshAccessToken() {
    // Use plain axios instead of the intercepted `api` client to avoid
    // recursive interceptor calls if refresh itself fails.
    const baseURL = import.meta.env.VITE_API_BASE_URL ?? ''
    const { data } = await axios.post(`${baseURL}/api/auth/refresh`, null, {
      withCredentials: true,
    })

    _saveSession(data)
    return data.accessToken
  }

  /**
   * Send a join request for a restaurant by its join code.
    *
    * @param {string} code - Restaurant join code.
    * @returns {Promise<any>} Lookup payload from backend.
    * @throws {Error} Propagates lookup request failures.
   */
  async function lookupRestaurant(code) {
    const { data } = await api.get(`/api/organizations/lookup?code=${code}`)
    return data
  }

  /**
   * Send a join request for a restaurant.
   * Refreshes pending request state so onboarding reflects backend truth.
  *
  * @param {string} joinCode - The restaurant's join code.
  * @returns {Promise<void>}
  * @throws {Error} Propagates join request failures.
   */
  async function joinRestaurant(joinCode) {
    const [firstName, ...rest] = (user.value?.name ?? '').trim().split(/\s+/)
    const lastName = rest.join(' ') || firstName
    await api.post('/api/organizations/join', { joinCode, firstName, lastName, email: user.value?.email })
    await fetchPendingRequest()
  }

  /**
   * Withdraw a pending join request.
   * Clears pendingRequest so the onboarding page returns to the choose view.
    *
    * @returns {Promise<void>}
    * @throws {Error} Propagates withdraw request failures.
   */
  async function withdrawJoinRequest() {
    await api.delete('/api/organizations/join-request')
    pendingRequest.value = null
  }


  /**
   * Create a new restaurant. The current user becomes its admin.
   * After creation, refreshes the access token so the JWT includes
   * the new organization ID and admin role.
  *
  * @param {Object} payload - Restaurant create payload.
  * @param {string} payload.name - Restaurant name.
  * @param {string} payload.orgNumber - Organization number.
  * @param {string} payload.address - Street address.
  * @param {string} payload.postalCode - Postal code.
  * @param {string} payload.city - City name.
  * @returns {Promise<any>} Created restaurant payload.
  * @throws {Error} Propagates create/refresh request failures.
   */
  async function createRestaurant(payload) {
    const { data } = await api.post('/api/organizations', payload)
    await refreshAccessToken()
    return data
  }

  /**
   * Fetch join requests for the current user's organization.
    *
    * @param {'PENDING'|'ACCEPTED'|'DECLINED'|null} [status=null] - Optional status filter.
    * @returns {Promise<any[]>} Matching join requests.
    * @throws {Error} Propagates request failures.
   */
  async function fetchJoinRequests(status = null) {
    const params = status ? `?status=${status}` : ''
    const { data } = await api.get(`/api/organizations/requests${params}`)
    return data
  }

  /**
   * Accept or decline a join request.
    *
    * @param {string} requestId - UUID of the join request.
    * @param {'ACCEPTED'|'DECLINED'} action - Resolution action.
    * @returns {Promise<void>}
    * @throws {Error} Propagates request failures.
   */
  async function resolveJoinRequest(requestId, action) {
    await api.post(`/api/organizations/requests/${requestId}`, { action })
  }


  /**
   * Log out the current user.
   * Notifies the backend to invalidate the refresh token (fire-and-forget),
   * then clears all local state and redirects to /login.
    *
    * @returns {Promise<void>}
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


  /**
   * Route users after login/register refresh based on restaurant membership.
   *
   * @param {AuthSessionResponse} data - Latest auth/session payload.
   */
  async function _redirectAfterAuth(data) {
    if (data.restaurant) {
      router.push({ name: 'dashboard' })
    } else {
      await fetchPendingRequest()
      router.push({ name: 'onboarding' })
    }
  }

  /**
   * Fetch organization members for admin/manager views.
   *
   * @returns {Promise<any[]>}
   */
  async function fetchMembers() {
    const { data } = await api.get('/api/organizations/members')
    return data
  }

  /**
   * Remove a member from the current organization.
   *
   * @param {string|number} userId - Member identifier.
   * @returns {Promise<void>}
   */
  async function removeMember(userId) {
    await api.delete(`/api/organizations/members/${userId}`)
  }

  /**
   * Replace role assignments for a member.
   *
   * @param {string|number} userId - Member identifier.
   * @param {string[]} roles - Role values accepted by the backend.
   * @returns {Promise<void>}
   */
  async function updateMemberRoles(userId, roles) {
    await api.put(`/api/organizations/members/${userId}/roles`, { roles })
  }

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
    touchActivity,
  }
})
