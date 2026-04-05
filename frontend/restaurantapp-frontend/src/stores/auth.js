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

  /**
   * Join code for the restaurant. Only populated for active ADMIN/MANAGER users.
   * Used on the admin requests page so the admin can share the code with staff.
   */
  const restaurantJoinCode = ref(null)

  // ── Computed ───────────────────────────────────────────────────────────────

  /** True when a valid access token exists in state. */
  const isAuthenticated = computed(() => !!accessToken.value)

  /** True when the user is logged in AND connected to an active restaurant. */
  const hasActiveRestaurant = computed(() => restaurantStatus.value === 'active')

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
   * Populate state from a LoginResponse.
   * Shape: { accessToken, user: { email, name }, restaurant: { id, name, joinCode } | null }
   */
  function _saveSession(data) {
    accessToken.value = data.accessToken
    localStorage.setItem(ACCESS_TOKEN_KEY, data.accessToken)

    user.value = data.user

    if (data.restaurant) {
      restaurantStatus.value = 'active'
      restaurantId.value = data.restaurant.id
      restaurantName.value = data.restaurant.name
      restaurantJoinCode.value = data.restaurant.joinCode
    } else {
      restaurantStatus.value = null
      restaurantId.value = null
      restaurantName.value = null
      restaurantJoinCode.value = null
    }
  }

  function _resetState() {
    user.value = null
    restaurantStatus.value = null
    restaurantId.value = null
    restaurantName.value = null
    restaurantJoinCode.value = null
    accessToken.value = null
    _initPromise = null
    localStorage.removeItem(ACCESS_TOKEN_KEY)
  }

  // ── Actions ────────────────────────────────────────────────────────────────

  /**
   * Called once on app boot by the router guard.
   * Restores what we can from the JWT claims (email, orgId).
   * Display values (name, restaurant name) are only available after login.
   * NEEDS TO BE LOOKED INTO
   */
  let _initPromise = null

  async function initAuth() {
    if (_initPromise) return _initPromise
    if (!accessToken.value) return

    _initPromise = Promise.resolve().then(() => {
      const claims = _decodeToken()
      if (!claims) { _resetState(); return }

      // Restore essentials from JWT claims
      user.value = { email: claims.sub, name: null }

      if (claims.organizationId) {
        restaurantStatus.value = 'active'
        restaurantId.value = claims.organizationId
      }
    })
    return _initPromise
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
    _redirectAfterAuth()
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
   * Sets restaurantStatus to 'pending' on success.
   */
  async function lookupRestaurant(code) {
    const { data } = await api.get(`/api/organizations/lookup?code=${code}`)
    return data // { name: String }
  }

  async function joinRestaurant(joinCode, name) {
    const { data } = await api.post('/api/organizations/join', { joinCode })

    restaurantStatus.value = 'pending'
    restaurantId.value     = data.id
    restaurantName.value   = name ?? null
  }

  /**
   * Withdraw a pending join request.
   * Resets restaurantStatus and restaurantId to null.
   */
  async function withdrawJoinRequest() {
    await api.delete('/api/organizations/join-request')

    restaurantStatus.value = null
    restaurantId.value     = null
  }

  /**
   * Create a new restaurant. The user becomes its admin on success.
   * Sets restaurantStatus to 'active' immediately.
   */
  async function createRestaurant(payload) {
    // payload: { name, orgNumber, address, postalCode, city }
    const { data } = await api.post('/api/organizations', payload)

    restaurantStatus.value   = 'active'
    restaurantId.value       = data.id
    restaurantName.value     = payload.name
    restaurantJoinCode.value = data.joinCode ?? null

    await refreshAccessToken()

    return data // let the view display the joinCode returned by the backend
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
   * Optionally notifies the backend to invalidate the refresh token.
   * Always clears local state and redirects to /login.
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
    restaurantStatus,
    restaurantId,
    restaurantName,
    restaurantJoinCode,

    // Computed
    isAuthenticated,
    hasActiveRestaurant,
    userInitials,
    userRoles,
    isAdminOrManager,

    // Actions
    initAuth,
    login,
    register,
    refreshAccessToken,
    lookupRestaurant,
    joinRestaurant,
    withdrawJoinRequest,
    createRestaurant,
    fetchJoinRequests,
    resolveJoinRequest,
    fetchMembers,
    removeMember,
    updateMemberRoles,
    logout,
  }
})
