// Login page E2E tests
// Covers form validation, credential errors, successful login redirects,
// UI interactions (password toggle, navigation link).

describe('Login Page', () => {
  beforeEach(() => {
    // No token in localStorage → initAuth() returns early → router stays on /login
    cy.visit('/login')
  })

  // ── Rendering ─────────────────────────────────────────────────────────────

  it('renders the login form', () => {
    cy.get('#email').should('be.visible')
    cy.get('#password').should('be.visible')
    cy.get('button[type="submit"]').should('contain', 'Log in')
    cy.contains("Don't have an account?").should('be.visible')
    cy.contains('Create one').should('be.visible')
  })

  // ── Client-side validation ─────────────────────────────────────────────────

  it('shows both validation errors when submitting a completely empty form', () => {
    cy.get('button[type="submit"]').click()
    cy.contains('Email is required').should('be.visible')
    cy.contains('Password is required').should('be.visible')
  })

  it('shows only email error when only email is missing', () => {
    cy.get('#password').type('somepassword')
    cy.get('button[type="submit"]').click()
    cy.contains('Email is required').should('be.visible')
    cy.contains('Password is required').should('not.exist')
  })

  it('shows only password error when only password is missing', () => {
    cy.get('#email').type('user@example.com')
    cy.get('button[type="submit"]').click()
    cy.contains('Email is required').should('not.exist')
    cy.contains('Password is required').should('be.visible')
  })

  it('clears email validation error once the user starts typing', () => {
    cy.get('button[type="submit"]').click()
    cy.contains('Email is required').should('be.visible')
    cy.get('#email').type('a')
    cy.contains('Email is required').should('not.exist')
  })

  // ── API error handling ────────────────────────────────────────────────────

  it('shows error alert when credentials are wrong (API returns 401)', () => {
    cy.intercept('POST', '/api/auth/login', { statusCode: 401 }).as('loginFail')
    cy.get('#email').type('wrong@example.com')
    cy.get('#password').type('wrongpassword')
    cy.get('button[type="submit"]').click()
    cy.wait('@loginFail')
    cy.contains('Wrong email or password').should('be.visible')
  })

  it('disables the submit button while the request is in flight', () => {
    cy.intercept('POST', '/api/auth/login', (req) => {
      req.on('response', (res) => { res.setDelay(300) })
    }).as('loginSlow')
    cy.get('#email').type('user@example.com')
    cy.get('#password').type('password123')
    cy.get('button[type="submit"]').click()
    cy.get('button[type="submit"]').should('be.disabled')
    cy.wait('@loginSlow')
  })

  // ── Successful login ───────────────────────────────────────────────────────

  it('redirects to /onboarding after login when user has no restaurant', () => {
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: { accessToken: buildStaffToken(), email: 'user@example.com', name: 'Test User', role: 'ROLE_STAFF' },
    }).as('loginOk')
    cy.intercept('GET', '/api/auth/me', {
      statusCode: 200,
      body: { user: { id: 1, name: 'Test User', email: 'user@example.com' }, restaurantStatus: null, restaurantId: null, restaurantName: null, restaurantJoinCode: null },
    }).as('authMe')

    cy.get('#email').type('user@example.com')
    cy.get('#password').type('password123')
    cy.get('button[type="submit"]').click()
    cy.wait('@loginOk')
    cy.url().should('include', '/onboarding')
  })

  it('redirects to dashboard after login when user has an active restaurant', () => {
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: { accessToken: buildAdminToken(), email: 'admin@example.com', name: 'Admin User', role: 'ROLE_ADMIN' },
    }).as('loginOk')
    cy.intercept('GET', '/api/auth/me', {
      statusCode: 200,
      body: { user: { id: 2, name: 'Admin User', email: 'admin@example.com' }, restaurantStatus: 'active', restaurantId: 1, restaurantName: 'Best Restaurant', restaurantJoinCode: 'BST-0001' },
    }).as('authMe')

    cy.get('#email').type('admin@example.com')
    cy.get('#password').type('password123')
    cy.get('button[type="submit"]').click()
    cy.wait('@loginOk')
    cy.url().should('eq', Cypress.config('baseUrl') + '/')
  })

  // ── UI interactions ───────────────────────────────────────────────────────

  it('toggles password field visibility when clicking the eye button', () => {
    cy.get('#password').type('secret123')
    cy.get('#password').should('have.attr', 'type', 'password')
    cy.get('.toggle-password').click()
    cy.get('#password').should('have.attr', 'type', 'text')
    cy.get('.toggle-password').click()
    cy.get('#password').should('have.attr', 'type', 'password')
  })

  it('navigates to /register when clicking "Create one"', () => {
    cy.contains('Create one').click()
    cy.url().should('include', '/register')
  })
})

// ── Helpers ────────────────────────────────────────────────────────────────

function makeJwt(roles) {
  const header = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9'
  const payload = btoa(JSON.stringify({ sub: '1', roles, exp: 9999999999 }))
    .replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_')
  return `${header}.${payload}.test_sig`
}

function buildStaffToken() { return makeJwt(['ROLE_STAFF']) }
function buildAdminToken() { return makeJwt(['ROLE_ADMIN']) }
