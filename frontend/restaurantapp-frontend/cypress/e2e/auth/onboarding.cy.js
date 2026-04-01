// Onboarding page E2E tests
// Covers the "choose path" view, joining a restaurant by code (success/error),
// pending state + withdraw, navigation to create-restaurant, and logout.
//
// All tests use cy.setAuthState() + cy.visitAuthenticated() from commands.js
// to simulate a logged-in user with no active restaurant.

describe('Restaurant Onboarding Page', () => {
  beforeEach(() => {
    cy.setAuthState({
      roles: ['ROLE_STAFF'],
      user: { id: 1, name: 'New User', email: 'newuser@example.com' },
      restaurantStatus: null,
      restaurantId: null,
    })
    cy.visitAuthenticated('/onboarding')
  })

  // ── "Choose path" view (default) ──────────────────────────────────────────

  it('shows the two path options by default', () => {
    cy.contains('Join existing restaurant').should('be.visible')
    cy.contains('Create new restaurant').should('be.visible')
  })

  it('shows "Step 1 of 2" indicator on the choose view', () => {
    cy.contains('Step 1 of 2').should('be.visible')
  })

  it('displays the logged-in user email in the top bar', () => {
    cy.contains('newuser@example.com').should('be.visible')
  })

  // ── "Join" sub-view ───────────────────────────────────────────────────────

  it('shows the join form after clicking "Join existing restaurant"', () => {
    cy.contains('Join existing restaurant').click()
    cy.contains('Enter invitation code').should('be.visible')
    cy.get('#joinCode').should('be.visible')
    cy.contains('Send access request').should('be.visible')
  })

  it('shows "Step 2 of 2" indicator on the join view', () => {
    cy.contains('Join existing restaurant').click()
    cy.contains('Step 2 of 2').should('be.visible')
  })

  it('returns to the choose view when clicking the Back button', () => {
    cy.contains('Join existing restaurant').click()
    cy.contains('Back').click()
    cy.contains('Join existing restaurant').should('be.visible')
    cy.contains('Create new restaurant').should('be.visible')
  })

  it('keeps the Send button disabled until 8 characters are entered', () => {
    cy.contains('Join existing restaurant').click()
    cy.get('#joinCode').type('ABC-123')       // 7 chars
    cy.contains('Send access request').should('be.disabled')
    cy.get('#joinCode').type('4')             // 8 chars
    cy.contains('Send access request').should('not.be.disabled')
  })

  it('shows an error when the join code is invalid (API returns error)', () => {
    cy.intercept('GET', '/api/organizations/lookup*', { statusCode: 404 }).as('lookupFail')

    cy.contains('Join existing restaurant').click()
    cy.get('#joinCode').type('BAD-9999')
    cy.contains('Send access request').click()
    cy.wait('@lookupFail')
    cy.contains('Invalid restaurant code').should('be.visible')
  })

  it('shows the pending view after a successful join request', () => {
    cy.intercept('GET', '/api/organizations/lookup*', {
      statusCode: 200,
      body: { name: 'Sunset Bistro' },
    }).as('lookup')
    cy.intercept('POST', '/api/organizations/join', {
      statusCode: 200,
      body: { id: 42 },
    }).as('joinOk')

    cy.contains('Join existing restaurant').click()
    cy.get('#joinCode').type('SUN-2025')
    cy.contains('Send access request').click()
    cy.wait('@lookup')
    cy.wait('@joinOk')

    cy.contains('Request sent').should('be.visible')
    cy.contains('Sunset Bistro').should('be.visible')
    cy.contains('Withdraw request').should('be.visible')
  })

  // ── Pending view ──────────────────────────────────────────────────────────

  describe('when user already has a pending request', () => {
    beforeEach(() => {
      // Re-stub auth with pending restaurant status
      cy.setAuthState({
        roles: ['ROLE_STAFF'],
        user: { id: 1, name: 'New User', email: 'newuser@example.com' },
        restaurantStatus: 'pending',
        restaurantId: 7,
        restaurantName: 'Pending Place',
      })
      cy.visitAuthenticated('/onboarding')
    })

    it('shows the pending card directly', () => {
      cy.contains('Request sent').should('be.visible')
      cy.contains('Withdraw request').should('be.visible')
    })

    it('returns to the choose view after withdrawing the request', () => {
      cy.intercept('DELETE', '/api/organizations/join-request', { statusCode: 204 }).as('withdraw')

      cy.contains('Withdraw request').click()
      cy.wait('@withdraw')

      cy.contains('Join existing restaurant').should('be.visible')
      cy.contains('Create new restaurant').should('be.visible')
    })
  })

  // ── Navigation to create-restaurant ───────────────────────────────────────

  it('navigates to /onboarding/create when clicking "Create new restaurant"', () => {
    cy.contains('Create new restaurant').click()
    cy.url().should('include', '/onboarding/create')
  })

  // ── Logout ────────────────────────────────────────────────────────────────

  it('redirects to /login after clicking Log out', () => {
    cy.intercept('POST', '/api/auth/logout', { statusCode: 204 }).as('logout')
    cy.contains('Log out').click()
    cy.wait('@logout')
    cy.url().should('include', '/login')
  })
})
