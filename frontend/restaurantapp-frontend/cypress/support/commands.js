// ***********************************************
// Custom Cypress commands for the ICSystem app.
// ***********************************************

/** Shared token storage between setAuthState and visitAuthenticated. */
let _currentTestToken = null

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
 * Stub the /api/auth/me endpoint and store the token so that
 * `cy.visitAuthenticated()` can inject it into localStorage before page load.
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

  cy.intercept('GET', '/api/auth/me', {
    statusCode: 200,
    body: {
      user: options.user ?? { id: 1, name: 'Test User', email: 'test@example.com' },
      restaurantStatus: options.restaurantStatus ?? null,
      restaurantId: options.restaurantId ?? null,
      restaurantName: options.restaurantName ?? null,
      restaurantJoinCode: options.restaurantJoinCode ?? null,
    },
  }).as('authMe')
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
      visitOptions.onBeforeLoad?.(win)
    },
  })
})
