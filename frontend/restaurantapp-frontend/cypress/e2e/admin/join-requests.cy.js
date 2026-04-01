// Join Requests (admin) page E2E tests
// Covers: loading/empty/error states, pending request list rendering,
// accept/decline actions (card removed, toast shown), join code display,
// and non-admin redirect guard.

const PENDING_REQUESTS = [
  {
    requestId: 'req-001',
    firstName: 'Alice',
    lastName: 'Hansen',
    email: 'alice@example.com',
    createdAt: '2025-03-15T10:00:00Z',
  },
  {
    requestId: 'req-002',
    firstName: 'Bob',
    lastName: 'Nilsen',
    email: 'bob@example.com',
    createdAt: '2025-03-16T14:30:00Z',
  },
]

describe('Join Requests page — as admin', () => {
  beforeEach(() => {
    cy.setAuthState({
      roles: ['ROLE_ADMIN'],
      user: { id: 99, name: 'Admin User', email: 'admin@restaurant.com' },
      restaurantStatus: 'active',
      restaurantId: 1,
      restaurantName: 'Best Restaurant',
      restaurantJoinCode: 'BST-0001',
    })
  })

  // ── Loading state ──────────────────────────────────────────────────────────

  it('shows a loading indicator while the request is in flight', () => {
    cy.intercept('GET', '/api/organizations/requests*', (req) => {
      req.on('response', (res) => { res.setDelay(500) })
      req.reply({ statusCode: 200, body: [] })
    }).as('loadSlow')

    cy.visitAuthenticated('/admin/requests')
    cy.contains('Loading requests').should('be.visible')
    cy.wait('@loadSlow')
  })

  // ── Empty state ────────────────────────────────────────────────────────────

  it('shows the empty state when there are no pending requests', () => {
    cy.intercept('GET', '/api/organizations/requests*', {
      statusCode: 200,
      body: [],
    }).as('loadEmpty')

    cy.visitAuthenticated('/admin/requests')
    cy.wait('@loadEmpty')

    cy.contains('No pending access requests').should('be.visible')
  })


  // ── Request list ───────────────────────────────────────────────────────────

  it('renders all pending requests with name, email, and initials', () => {
    cy.intercept('GET', '/api/organizations/requests*', {
      statusCode: 200,
      body: PENDING_REQUESTS,
    }).as('loadRequests')

    cy.visitAuthenticated('/admin/requests')
    cy.wait('@loadRequests')

    cy.contains('Alice Hansen').should('be.visible')
    cy.contains('alice@example.com').should('be.visible')
    cy.contains('Bob Nilsen').should('be.visible')
    cy.contains('bob@example.com').should('be.visible')

    // Avatar initials
    cy.contains('AH').should('be.visible')
    cy.contains('BN').should('be.visible')
  })

  it('shows the pending count badge in the header', () => {
    cy.intercept('GET', '/api/organizations/requests*', {
      statusCode: 200,
      body: PENDING_REQUESTS,
    }).as('loadRequests')

    cy.visitAuthenticated('/admin/requests')
    cy.wait('@loadRequests')

    cy.contains(`${PENDING_REQUESTS.length} pending`).should('be.visible')
  })

  it('shows Accept and Decline buttons for each request', () => {
    cy.intercept('GET', '/api/organizations/requests*', {
      statusCode: 200,
      body: PENDING_REQUESTS,
    }).as('loadRequests')

    cy.visitAuthenticated('/admin/requests')
    cy.wait('@loadRequests')

    cy.get('.action-btn--accept').should('have.length', 2)
    cy.get('.action-btn--decline').should('have.length', 2)
  })

  it('shows the formatted request date', () => {
    cy.intercept('GET', '/api/organizations/requests*', {
      statusCode: 200,
      body: PENDING_REQUESTS,
    }).as('loadRequests')

    cy.visitAuthenticated('/admin/requests')
    cy.wait('@loadRequests')

    // 2025-03-15 → "15 Mar 2025"
    cy.contains('15 Mar 2025').should('be.visible')
  })

  // ── Join code bar ──────────────────────────────────────────────────────────

  it('displays the restaurant join code in the header bar', () => {
    cy.intercept('GET', '/api/organizations/requests*', {
      statusCode: 200,
      body: [],
    }).as('loadEmpty')

    cy.visitAuthenticated('/admin/requests')
    cy.wait('@loadEmpty')

    cy.contains('BST-0001').should('be.visible')
    cy.contains('Copy').should('be.visible')
  })

  // ── Accept action ─────────────────────────────────────────────────────────

  it('removes the card and shows a toast after accepting a request', () => {
    cy.intercept('GET', '/api/organizations/requests*', {
      statusCode: 200,
      body: PENDING_REQUESTS,
    }).as('loadRequests')

    cy.visitAuthenticated('/admin/requests')
    cy.wait('@loadRequests')

    cy.intercept('POST', '/api/organizations/requests/req-001', {
      statusCode: 200,
      body: {},
    }).as('accept')

    // Click Accept on the first request (Alice)
    cy.get('.action-btn--accept').first().click()
    cy.wait('@accept')

    // Alice's card should disappear; Bob's should remain
    cy.contains('alice@example.com').should('not.exist')
    cy.contains('Bob Nilsen').should('be.visible')

    // Only 1 request card remains
    cy.get('.request-card').should('have.length', 1)
  })

  it('shows a success toast message after accepting', () => {
    cy.intercept('GET', '/api/organizations/requests*', {
      statusCode: 200,
      body: [PENDING_REQUESTS[0]],
    }).as('loadRequests')
    cy.intercept('POST', '/api/organizations/requests/req-001', {
      statusCode: 200,
      body: {},
    }).as('accept')

    cy.visitAuthenticated('/admin/requests')
    cy.wait('@loadRequests')
    cy.get('.action-btn--accept').click()
    cy.wait('@accept')

    cy.contains('has been granted access').should('be.visible')
  })

  // ── Decline action ────────────────────────────────────────────────────────

  it('removes the card after declining a request', () => {
    cy.intercept('GET', '/api/organizations/requests*', {
      statusCode: 200,
      body: PENDING_REQUESTS,
    }).as('loadRequests')

    cy.visitAuthenticated('/admin/requests')
    cy.wait('@loadRequests')

    cy.intercept('POST', '/api/organizations/requests/req-001', {
      statusCode: 200,
      body: {},
    }).as('decline')

    cy.get('.action-btn--decline').first().click()
    cy.wait('@decline')

    cy.contains('alice@example.com').should('not.exist')
    cy.contains('Bob Nilsen').should('be.visible')
  })

  it('shows an info toast message after declining', () => {
    cy.intercept('GET', '/api/organizations/requests*', {
      statusCode: 200,
      body: [PENDING_REQUESTS[0]],
    }).as('loadRequests')
    cy.intercept('POST', '/api/organizations/requests/req-001', {
      statusCode: 200,
      body: {},
    }).as('decline')

    cy.visitAuthenticated('/admin/requests')
    cy.wait('@loadRequests')
    cy.get('.action-btn--decline').click()
    cy.wait('@decline')

    cy.contains('was declined').should('be.visible')
  })

  // ── Resolving state ───────────────────────────────────────────────────────

  it('disables both buttons on a card while its request is being resolved', () => {
    cy.intercept('GET', '/api/organizations/requests*', {
      statusCode: 200,
      body: [PENDING_REQUESTS[0]],
    }).as('loadRequests')
    cy.intercept('POST', '/api/organizations/requests/req-001', (req) => {
      req.on('response', (res) => { res.setDelay(400) })
      req.reply({ statusCode: 200, body: {} })
    }).as('acceptSlow')

    cy.visitAuthenticated('/admin/requests')
    cy.wait('@loadRequests')
    cy.get('.action-btn--accept').click()

    cy.get('.action-btn--accept').should('be.disabled')
    cy.get('.action-btn--decline').should('be.disabled')
    cy.wait('@acceptSlow')
  })

  // ── Action error ──────────────────────────────────────────────────────────

  it('shows an error toast and keeps the card when resolving fails', () => {
    cy.intercept('GET', '/api/organizations/requests*', {
      statusCode: 200,
      body: [PENDING_REQUESTS[0]],
    }).as('loadRequests')
    cy.intercept('POST', '/api/organizations/requests/req-001', {
      statusCode: 500,
      body: {},
    }).as('acceptFail')

    cy.visitAuthenticated('/admin/requests')
    cy.wait('@loadRequests')
    cy.get('.action-btn--accept').click()
    cy.wait('@acceptFail')

    // Card stays
    cy.contains('Alice Hansen').should('be.visible')
    // Error toast
    cy.contains('Something went wrong').should('be.visible')
  })
})

// ── Non-admin access guard ─────────────────────────────────────────────────

describe('Join Requests page — as regular staff', () => {
  it('redirects to dashboard when a non-admin tries to access /admin/requests', () => {
    cy.setAuthState({
      roles: ['ROLE_STAFF'],
      user: { id: 2, name: 'Staff User', email: 'staff@example.com' },
      restaurantStatus: 'active',
      restaurantId: 1,
      restaurantName: 'Best Restaurant',
      restaurantJoinCode: null,
    })
    cy.visitAuthenticated('/admin/requests')
    cy.url().should('eq', Cypress.config('baseUrl') + '/')
  })
})
