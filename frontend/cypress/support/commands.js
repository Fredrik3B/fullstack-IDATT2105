// ***********************************************
// Custom Cypress commands for the ICMSS app.
// ***********************************************

/** Shared token storage between setAuthState and visitAuthenticated. */
let _currentTestToken = null
let _currentSession = null
const LAST_ACTIVE_KEY = 'iksystem_last_active'

/**
 * Build a minimal, structurally-valid JWT for testing.
 * The app decodes the payload to extract `roles`, so the payload must be
 * real base64url-encoded JSON — the signature can be anything.
 */
function makeTestJwt(roles = []) {
  const header = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9'
  const payload = btoa(JSON.stringify({ sub: '1', roles, exp: 9999999999 }))
    .replace(/=/g, '')
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
  return `${header}.${payload}.test_sig`
}

/**
 * Store token + session so `cy.visitAuthenticated()` can inject auth state
 * into localStorage before page load.
 *
 * Options:
 *   roles           {string[]}  JWT roles, e.g. ['ROLE_ADMIN']   (default: ['ROLE_STAFF'])
 *   user            {object}    { id, name, email }
 *   restaurantStatus {string|null}  null | 'pending' | 'active'
 *   restaurantId    {number|null}
 *   restaurantName  {string|null}
 *   restaurantJoinCode {string|null}
 */
Cypress.Commands.add('setAuthState', (options = {}) => {
  const roles = options.roles ?? ['ROLE_STAFF']
  const token = makeTestJwt(roles)
  // Store token so visitAuthenticated() can read it synchronously
  _currentTestToken = token

  _currentSession = {
    user: options.user ?? { id: 1, name: 'Test User', email: 'test@example.com' },
    restaurant: options.restaurantId || options.restaurantName || options.restaurantJoinCode
      ? {
          id: options.restaurantId ?? null,
          name: options.restaurantName ?? null,
          joinCode: options.restaurantJoinCode ?? null,
        }
      : null,
    restaurantStatus: options.restaurantStatus ?? null,
  }
})

/**
 * Visit a route with the test token already set in localStorage,
 * so the router guard sees the user as authenticated.
 *
 * Always call cy.setAuthState() before cy.visitAuthenticated().
 */
Cypress.Commands.add('visitAuthenticated', (url, visitOptions = {}) => {
  const token = _currentTestToken
  cy.visit(url, {
    ...visitOptions,
    onBeforeLoad(win) {
      win.localStorage.setItem('iksystem_access_token', token)
      if (_currentSession) {
        win.localStorage.setItem('iksystem_session', JSON.stringify(_currentSession))
      }
      // The router guard treats a missing activity timestamp as an expired session.
      win.localStorage.setItem(LAST_ACTIVE_KEY, String(Date.now()))
      visitOptions.onBeforeLoad?.(win)
    },
  })
})
