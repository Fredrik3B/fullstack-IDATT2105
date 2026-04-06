// Register page E2E tests
// Covers form validation, password strength, successful registration redirect,
// and navigation link back to login.

describe('Register Page', () => {
  beforeEach(() => {
    cy.visit('/register')
  })

  // ── Rendering ─────────────────────────────────────────────────────────────

  it('renders all form fields and the submit button', () => {
    cy.get('#name').should('be.visible')
    cy.get('#email').should('be.visible')
    cy.get('#password').should('be.visible')
    cy.get('#confirmPassword').should('be.visible')
    cy.get('button[type="submit"]').should('contain', 'Create account')
    cy.contains('Already have an account?').should('be.visible')
    cy.contains('Log in').should('be.visible')
  })

  // ── Client-side validation ─────────────────────────────────────────────────

  it('shows all four validation errors when submitting a completely empty form', () => {
    cy.get('button[type="submit"]').click()
    cy.contains('Full name is required').should('be.visible')
    cy.contains('Email is required').should('be.visible')
    cy.contains('Password is required').should('be.visible')
    cy.contains('Please confirm your password').should('be.visible')
  })

  it('shows an error when the password is shorter than 8 characters', () => {
    cy.get('#name').type('Jane Doe')
    cy.get('#email').type('jane@example.com')
    cy.get('#password').type('short')
    cy.get('#confirmPassword').type('short')
    cy.get('button[type="submit"]').click()
    cy.contains('Password must be at least 8 characters').should('be.visible')
  })

  it('shows an error when the passwords do not match', () => {
    cy.get('#name').type('Jane Doe')
    cy.get('#email').type('jane@example.com')
    cy.get('#password').type('password123')
    cy.get('#confirmPassword').type('differentpassword')
    cy.get('button[type="submit"]').click()
    cy.contains('Passwords do not match').should('be.visible')
  })

  it('clears a field error once the user starts typing in that field', () => {
    cy.get('button[type="submit"]').click()
    cy.contains('Full name is required').should('be.visible')
    cy.get('#name').type('J')
    cy.contains('Full name is required').should('not.exist')
  })

  // ── Password strength indicator ───────────────────────────────────────────

  it('shows "Weak" strength for a short simple password', () => {
    cy.get('#password').type('abc')
    cy.contains('Weak').should('be.visible')
  })

  it('shows "Strong" strength for a complex password', () => {
    cy.get('#password').type('MyStr0ng!Pass#99')
    cy.contains('Strong').should('be.visible')
  })

  it('does not show the strength bar before the user types a password', () => {
    cy.get('.password-strength').should('not.exist')
  })

  // ── Password visibility toggles ───────────────────────────────────────────

  it('toggles password field visibility', () => {
    cy.get('#password').type('secret')
    cy.get('#password').should('have.attr', 'type', 'password')
    // The first .toggle-password belongs to the password field
    cy.get('.toggle-password').first().click()
    cy.get('#password').should('have.attr', 'type', 'text')
  })

  it('toggles confirm-password field visibility independently', () => {
    cy.get('#confirmPassword').type('secret')
    cy.get('#confirmPassword').should('have.attr', 'type', 'password')
    cy.get('.toggle-password').last().click()
    cy.get('#confirmPassword').should('have.attr', 'type', 'text')
  })

  // ── Successful registration ───────────────────────────────────────────────

  it('redirects to /onboarding after a successful registration', () => {
    cy.intercept('POST', '/api/auth/register', {
      statusCode: 201,
      body: {
        accessToken: makeJwt(['ROLE_STAFF']),
        user: { id: 3, name: 'Jane Doe', email: 'jane@example.com' },
        restaurant: null,
        restaurantStatus: null,
      },
    }).as('registerOk')

    cy.get('#name').type('Jane Doe')
    cy.get('#email').type('jane@example.com')
    cy.get('#password').type('password123')
    cy.get('#confirmPassword').type('password123')
    cy.get('button[type="submit"]').click()
    cy.wait('@registerOk')
    cy.url().should('include', '/onboarding')
  })

  it('shows an error alert when the server returns an error', () => {
    cy.intercept('POST', '/api/auth/register', { statusCode: 409 }).as('registerFail')

    cy.get('#name').type('Jane Doe')
    cy.get('#email').type('existing@example.com')
    cy.get('#password').type('password123')
    cy.get('#confirmPassword').type('password123')
    cy.get('button[type="submit"]').click()
    cy.wait('@registerFail')
    cy.contains('Something went wrong').should('be.visible')
  })

  it('disables the submit button while the request is in flight', () => {
    cy.intercept('POST', '/api/auth/register', (req) => {
      req.on('response', (res) => { res.setDelay(300) })
    }).as('registerSlow')

    cy.get('#name').type('Jane Doe')
    cy.get('#email').type('jane@example.com')
    cy.get('#password').type('password123')
    cy.get('#confirmPassword').type('password123')
    cy.get('button[type="submit"]').click()
    cy.get('button[type="submit"]').should('be.disabled')
    cy.wait('@registerSlow')
  })

  // ── Navigation ────────────────────────────────────────────────────────────

  it('navigates to /login when clicking "Log in"', () => {
    cy.contains('Log in').click()
    cy.url().should('include', '/login')
  })
})

// ── Helpers ────────────────────────────────────────────────────────────────

function makeJwt(roles) {
  const header = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9'
  const payload = btoa(JSON.stringify({ sub: '1', roles, exp: 9999999999 }))
    .replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_')
  return `${header}.${payload}.test_sig`
}
