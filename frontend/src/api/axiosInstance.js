import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

/**
 * Shared Axios client for all frontend API modules.
 *
 * It attaches the current access token on requests and retries once after a
 * token refresh when the backend returns 401.
 */

/** @type {import('axios').AxiosInstance} */
const api = axios.create({
  // Default to same-origin `/api` requests so local dev can use the Vite proxy
  // without hitting browser CORS restrictions.
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '',
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
  withCredentials: true,
})

/**
 * Attach the current bearer token to outgoing API requests.
 */
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

/**
 * Tracks whether a refresh call is already running.
 *
 * The `_isRetry` flag on the request config prevents infinite retry loops.
 */
let isRefreshing = false

/**
 * Queue of requests waiting for the outcome of an in-flight refresh.
 */
let pendingQueue = []

/**
 * Resolve or reject all queued requests.
 *
 * @param {Error|null} error - Refresh error, if one occurred.
 * @param {string|null} [token=null] - New access token when refresh succeeds.
 */
function flushQueue(error, token = null) {
  pendingQueue.forEach(({ resolve, reject }) => {
    if (error) reject(error)
    else resolve(token)
  })
  pendingQueue = []
}

api.interceptors.response.use(
  (response) => {
    const auth = useAuthStore()
    auth.touchActivity()
    return response
  },

  async (error) => {
    const originalRequest = error.config

    const isAuthEndpoint = originalRequest.url?.includes('/api/auth/')
    if (error.response?.status !== 401 || originalRequest._isRetry || isAuthEndpoint) {
      return Promise.reject(error)
    }

    // Mark the request so a retried 401 does not restart the refresh flow.
    originalRequest._isRetry = true

    if (isRefreshing) {
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

      originalRequest.headers.Authorization = `Bearer ${newToken}`

      flushQueue(null, newToken)

      return api(originalRequest)
    } catch (refreshError) {
      flushQueue(refreshError)
      const auth = useAuthStore()
      await auth.logout()
      return Promise.reject(refreshError)
    } finally {
      isRefreshing = false
    }
  }
)

/**
 * Exported as the single HTTP client used by every API module in the app.
 */
export default api
