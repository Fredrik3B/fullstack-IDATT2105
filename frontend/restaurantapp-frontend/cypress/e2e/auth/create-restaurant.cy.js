// Create Restaurant page E2E tests
// Covers form validation, successful creation (shows join code), server errors,
// loading state, back link, and "Go to dashboard" navigation.

describe('Create Restaurant Page', () => {
  beforeEach(() => {
    cy.setAuthState({
      roles: ['ROLE_STAFF'],
      user: { id: 1, name: 'New Admin', email: 'admin@example.com' },
      restaurantStatus: null,
      restaurantId: null,
    })
    cy.visitAuthenticated('/onboarding/create')
  })

  // ── Rendering ─────────────────────────────────────────────────────────────

  it('renders all form fields', () => {
    cy.get('#restName').should('be.visible')
    cy.get('#orgNumber').should('be.visible')
    cy.get('#address').should('be.visible')
    cy.get('#postalCode').should('be.visible')
    cy.get('#city').should('be.visible')
    cy.get('button[type="submit"]').should('contain', 'Create restaurant')
  })

  it('shows the back link to onboarding', () => {
    cy.contains('Back to onboarding').should('be.visible')
  })

  it('displays the logged-in user email in the top bar', () => {
    cy.contains('admin@example.com').should('be.visible')
  })

  // ── Client-side validation ─────────────────────────────────────────────────

  it('shows all five field errors when submitting a completely empty form', () => {
    cy.get('button[type="submit"]').click()
    cy.contains('Restaurant name is required').should('be.visible')
    cy.contains('Organization number is required').should('be.visible')
    cy.contains('Street address is required').should('be.visible')
    cy.contains('Postal code is required').should('be.visible')
    cy.contains('City is required').should('be.visible')
  })

  it('shows an error when the org number has fewer than 9 digits', () => {
    cy.get('#restName').type('My Restaurant')
    cy.get('#orgNumber').type('12345')
    cy.get('#address').type('Storgata 1')
    cy.get('#postalCode').type('0150')
    cy.get('#city').type('Oslo')
    cy.get('button[type="submit"]').click()
    cy.contains('Organization number must be 9 digits').should('be.visible')
  })

  it('shows an error when the postal code has fewer than 4 digits', () => {
    cy.get('#restName').type('My Restaurant')
    cy.get('#orgNumber').type('123456789')
    cy.get('#address').type('Storgata 1')
    cy.get('#postalCode').type('015')
    cy.get('#city').type('Oslo')
    cy.get('button[type="submit"]').click()
    cy.contains('Invalid postal code').should('be.visible')
  })

  it('clears a field error once the user starts typing in that field', () => {
    cy.get('button[type="submit"]').click()
    cy.contains('Restaurant name is required').should('be.visible')
    cy.get('#restName').type('M')
    cy.contains('Restaurant name is required').should('not.exist')
  })

  it('accepts and formats org number input (spaces every 3 digits)', () => {
    cy.get('#orgNumber').type('123456789')
    cy.get('#orgNumber').should('have.value', '123 456 789')
  })

  // ── Successful creation ───────────────────────────────────────────────────

  it('shows the success card with join code after creating a restaurant', () => {
    cy.intercept('POST', '/api/organizations', {
      statusCode: 201,
      body: { id: 10, joinCode: 'EVR-2847' },
    }).as('createOrg')
    // refreshAccessToken() is called after create — uses the base API URL directly
    cy.intercept('POST', '**/api/auth/refresh', {
      statusCode: 200,
      body: buildRefreshResponse(),
    }).as('refresh')

    fillForm()
    cy.get('button[type="submit"]').click()
    cy.wait('@createOrg')
    cy.wait('@refresh')

    cy.contains('Workspace created').should('be.visible')
    cy.contains('My Test Restaurant').should('be.visible')
    cy.contains('EVR-2847').should('be.visible')
    cy.contains('Restaurant join code').should('be.visible')
    cy.contains('Go to dashboard').should('be.visible')
  })

  it('navigates to the dashboard after clicking "Go to dashboard"', () => {
    cy.intercept('POST', '/api/organizations', {
      statusCode: 201,
      body: { id: 10, joinCode: 'EVR-2847' },
    }).as('createOrg')
    cy.intercept('POST', '**/api/auth/refresh', {
      statusCode: 200,
      body: buildRefreshResponse(),
    }).as('refresh')

    fillForm()
    cy.get('button[type="submit"]').click()
    cy.wait('@createOrg')
    cy.wait('@refresh')
    cy.contains('Go to dashboard').click()
    cy.url().should('eq', Cypress.config('baseUrl') + '/')
  })

  // ── Error and loading state ───────────────────────────────────────────────

  it('shows an error alert when the server returns an error', () => {
    cy.intercept('POST', '/api/organizations', { statusCode: 500 }).as('createFail')

    fillForm()
    cy.get('button[type="submit"]').click()
    cy.wait('@createFail')
    cy.contains('Something went wrong').should('be.visible')
  })

  it('shows an error when refresh fails after a successful restaurant creation', () => {
    cy.intercept('POST', '/api/organizations', {
      statusCode: 201,
      body: { id: 10, joinCode: 'EVR-2847' },
    }).as('createOrg')
    cy.intercept('POST', '**/api/auth/refresh', {
      statusCode: 500,
      body: {},
    }).as('refreshFail')

    fillForm()
    cy.get('button[type="submit"]').click()
    cy.wait('@createOrg')
    cy.wait('@refreshFail')

    cy.contains('Something went wrong').should('be.visible')
    cy.contains('Workspace created').should('not.exist')
  })

  it('disables the submit button while the request is in flight', () => {
    cy.intercept('POST', '/api/organizations', (req) => {
      req.on('response', (res) => { res.setDelay(300) })
    }).as('createSlow')
    cy.intercept('POST', '**/api/auth/refresh', { statusCode: 200, body: buildRefreshResponse() })

    fillForm()
    cy.get('button[type="submit"]').click()
    cy.get('button[type="submit"]').should('be.disabled')
    cy.wait('@createSlow')
  })

  // ── Navigation ────────────────────────────────────────────────────────────

  it('navigates back to /onboarding when clicking the back link', () => {
    cy.contains('Back to onboarding').click()
    cy.url().should('include', '/onboarding')
  })

  it('logs out when clicking Log out', () => {
    cy.intercept('POST', '/api/auth/logout', { statusCode: 204 }).as('logout')
    cy.contains('Log out').click()
    cy.wait('@logout')
    cy.url().should('include', '/login')
  })
})

// ── Helpers ────────────────────────────────────────────────────────────────

/** Fill in all required fields with valid data. */
function fillForm() {
  cy.get('#restName').type('My Test Restaurant')
  cy.get('#orgNumber').type('123456789')
  cy.get('#address').type('Storgata 1')
  cy.get('#postalCode').type('0150')
  cy.get('#city').type('Oslo')
}

function makeJwt(roles) {
  const header = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9'
  const payload = btoa(JSON.stringify({ sub: '1', roles, exp: 9999999999 }))
    .replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_')
  return `${header}.${payload}.test_sig`
}

function buildAdminToken() { return makeJwt(['ROLE_ADMIN']) }

function buildRefreshResponse() {
  return {
    accessToken: buildAdminToken(),
    user: { id: 1, name: 'New Admin', email: 'admin@example.com' },
    restaurant: { id: 10, name: 'My Test Restaurant', joinCode: 'EVR-2847' },
  }
}
