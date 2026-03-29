import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

// ── Instance ───────────────────────────────────────────────────────────────

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000, // 10 seconds — fail loudly instead of hanging forever
  withCredentials: true, // required for HTTPOnly refresh token cookie
})

// ── Request interceptor — attach JWT ──────────────────────────────────────

api.interceptors.request.use(
  (config) => {
    const auth = useAuthStore()
    if (auth.accessToken) {
      config.headers.Authorization = `Bearer ${auth.accessToken}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// ── Response interceptor — handle token expiry ────────────────────────────

/**
 * When a 401 comes back we try once to refresh the access token, then retry
 * the original request. If the refresh itself fails, the user is logged out.
 *
 * _isRetry flag on the config object prevents an infinite retry loop:
 *   request → 401 → refresh → retry → 401 again → logout  (stops here)
 */
let isRefreshing = false

/**
 * While a refresh is already in progress, any other requests that get a 401
 * are queued here instead of each firing their own refresh call.
 * Once the refresh resolves (or fails) the queue is flushed.
 */
let pendingQueue = []

function flushQueue(error, token = null) {
  pendingQueue.forEach(({ resolve, reject }) => {
    if (error) reject(error)
    else resolve(token)
  })
  pendingQueue = []
}

api.interceptors.response.use(
  // 2xx — pass straight through
  (response) => response,

  async (error) => {
    const originalRequest = error.config

    // Only attempt a refresh on 401, and only once per request
    if (error.response?.status !== 401 || originalRequest._isRetry) {
      return Promise.reject(error)
    }

    // Mark so we don't retry again if the retried request also 401s
    originalRequest._isRetry = true

    if (isRefreshing) {
      // A refresh is already running — queue this request until it finishes
      return new Promise((resolve, reject) => {
        pendingQueue.push({ resolve, reject })
      })
        .then((newToken) => {
          originalRequest.headers.Authorization = `Bearer ${newToken}`
          return api(originalRequest)
        })
        .catch((err) => Promise.reject(err))
    }

    isRefreshing = true

    try {
      const auth = useAuthStore()
      const newToken = await auth.refreshAccessToken()

      // Update the header on the original request and retry it
      originalRequest.headers.Authorization = `Bearer ${newToken}`

      // Unblock any requests that were queued while we were refreshing
      flushQueue(null, newToken)

      return api(originalRequest)
    } catch (refreshError) {
      // Refresh failed (expired or revoked) — log the user out
      flushQueue(refreshError)
      const auth = useAuthStore()
      await auth.logout()
      return Promise.reject(refreshError)
    } finally {
      isRefreshing = false
    }
  }
)

export default api
