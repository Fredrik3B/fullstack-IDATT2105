// Documents page E2E tests
// Covers: rendering, filtering, upload modal, delete, expiry alerts

// ── Fixtures ───────────────────────────────────────────────────────────────

function makeDoc(overrides = {}) {
  return {
    id: 1,
    name: 'Hygiene Policy',
    originalFileName: 'hygiene.pdf',
    category: 'GUIDELINES',
    module: 'SHARED',
    fileType: 'application/pdf',
    fileSize: 204800,
    uploadedAt: '2025-03-01T10:00:00Z',
    uploadedByName: 'Admin User',
    expiryDate: null,
    externalUrl: null,
    description: '',
    ...overrides,
  }
}

function stubDocuments(docs) {
  cy.intercept('GET', '/api/documents*', { statusCode: 200, body: docs }).as('getDocuments')
}

// ── Tests ──────────────────────────────────────────────────────────────────

describe('Documents Page', () => {

  // ── As admin ─────────────────────────────────────────────────────────────

  describe('as admin', () => {
    beforeEach(() => {
      cy.setAuthState({
        roles: ['ROLE_ADMIN'],
        user: { id: 1, name: 'Admin User', email: 'admin@example.com' },
        restaurantStatus: 'active',
        restaurantId: 1,
        restaurantName: 'Test Restaurant',
      })
    })

    it('renders the page banner and heading', () => {
      stubDocuments([])
      cy.visitAuthenticated('/documents')
      cy.contains('Document storage').should('be.visible')
      cy.contains('certificates').should('be.visible')
    })

    it('shows upload button for admin', () => {
      stubDocuments([])
      cy.visitAuthenticated('/documents')
      cy.contains('Upload document').should('be.visible')
    })

    it('renders document cards when documents are returned', () => {
      stubDocuments([makeDoc({ id: 1, name: 'Hygiene Policy' })])
      cy.visitAuthenticated('/documents')
      cy.wait('@getDocuments')
      cy.contains('Hygiene Policy').should('be.visible')
    })

    it('shows empty state for a category with no documents', () => {
      stubDocuments([])
      cy.visitAuthenticated('/documents')
      cy.wait('@getDocuments')
      cy.contains('No guidelines uploaded').should('be.visible')
    })

    it('shows error state when API fails', () => {
      cy.intercept('GET', '/api/documents*', { statusCode: 500 }).as('getDocsFail')
      cy.visitAuthenticated('/documents')
      cy.wait('@getDocsFail')
      cy.contains('Failed to load documents').should('be.visible')
    })

    // ── Search & filter ──────────────────────────────────────────────────

    it('filters documents by search query', () => {
      stubDocuments([
        makeDoc({ id: 1, name: 'Hygiene Policy', originalFileName: 'hygiene.pdf' }),
        makeDoc({ id: 2, name: 'Fire Safety Plan', originalFileName: 'fire.pdf', category: 'EMERGENCY' }),
      ])
      cy.visitAuthenticated('/documents')
      cy.wait('@getDocuments')
      cy.get('.search-input').type('hygiene')
      cy.contains('Hygiene Policy').should('be.visible')
      cy.contains('Fire Safety Plan').should('not.exist')
    })

    it('filters documents by category dropdown', () => {
      stubDocuments([
        makeDoc({ id: 1, name: 'Training Doc', category: 'TRAINING' }),
        makeDoc({ id: 2, name: 'Hygiene Policy', category: 'GUIDELINES' }),
      ])
      cy.visitAuthenticated('/documents')
      cy.wait('@getDocuments')
      cy.get('#filter-cat').select('TRAINING')
      cy.contains('Training Doc').should('be.visible')
      cy.contains('Hygiene Policy').should('not.exist')
    })

    it('filters documents by module dropdown', () => {
      stubDocuments([
        makeDoc({ id: 1, name: 'IC Food Doc', module: 'IC_FOOD' }),
        makeDoc({ id: 2, name: 'Shared Doc', module: 'SHARED' }),
      ])
      cy.visitAuthenticated('/documents')
      cy.wait('@getDocuments')
      cy.get('#filter-module').select('IC_FOOD')
      cy.contains('IC Food Doc').should('be.visible')
      cy.contains('Shared Doc').should('not.exist')
    })

    // ── Upload modal ─────────────────────────────────────────────────────

    it('opens upload modal when upload button is clicked', () => {
      stubDocuments([])
      cy.visitAuthenticated('/documents')
      cy.contains('Upload document').click()
      cy.get('[role="dialog"]').should('be.visible')
      cy.contains('Upload document').should('be.visible')
    })
    it('closes upload modal when cancel is clicked', () => {
      stubDocuments([])
      cy.visitAuthenticated('/documents')
      cy.contains('Upload document').click()
      cy.get('[role="dialog"]').should('be.visible')
      cy.contains('Cancel').click()
      cy.get('[role="dialog"]').should('not.exist')
    })

    it('closes modal when backdrop is clicked', () => {
      stubDocuments([])
      cy.visitAuthenticated('/documents')
      cy.contains('Upload document').click()
      cy.get('.modal-backdrop').click({ force: true })
      cy.get('[role="dialog"]').should('not.exist')
    })

    it('switches between file and link upload modes', () => {
      stubDocuments([])
      cy.visitAuthenticated('/documents')
      cy.contains('Upload document').click()
      cy.get('.drop-zone').should('be.visible')
      cy.contains('Link to URL').click()
      cy.get('#upload-url').should('be.visible')
      cy.get('.drop-zone').should('not.exist')
    })

    it('shows expiry date field only when CERTIFICATE category is selected', () => {
      stubDocuments([])
      cy.visitAuthenticated('/documents')
      cy.contains('Upload document').click()
      cy.get('#upload-expiry').should('not.exist')
      cy.get('#upload-category').select('CERTIFICATE')
      cy.get('#upload-expiry').should('be.visible')
    })

    it('successfully uploads a file and shows the new document', () => {
      stubDocuments([])
      const newDoc = makeDoc({ id: 99, name: 'New Policy' })
      cy.intercept('POST', '/api/documents', { statusCode: 201, body: newDoc }).as('upload')

      cy.visitAuthenticated('/documents')
      cy.contains('Upload document').click()

      cy.get('#upload-name').type('New Policy')
      cy.get('#upload-category').select('GUIDELINES')
      cy.get('#upload-module').select('SHARED')
      cy.get('input[type="file"]').selectFile(
        { contents: Cypress.Buffer.from('file contents'), fileName: 'policy.pdf', mimeType: 'application/pdf' },
        { force: true }
      )
      cy.get('button[type="submit"]').click()
      cy.wait('@upload')
      cy.get('[role="dialog"]').should('not.exist')
      cy.contains('New Policy').should('be.visible')
    })

    it('shows upload error message when upload fails', () => {
      stubDocuments([])
      cy.intercept('POST', '/api/documents', { statusCode: 500 }).as('uploadFail')

      cy.visitAuthenticated('/documents')
      cy.contains('Upload document').click()

      cy.get('#upload-name').type('Bad Doc')
      cy.get('#upload-category').select('GUIDELINES')
      cy.get('#upload-module').select('SHARED')
      cy.get('input[type="file"]').selectFile(
        { contents: Cypress.Buffer.from('file'), fileName: 'bad.pdf', mimeType: 'application/pdf' },
        { force: true }
      )
      cy.get('button[type="submit"]').click()
      cy.wait('@uploadFail')
      cy.contains('Upload failed').should('be.visible')
    })


    it('does not delete when user cancels the confirmation dialog', () => {
      stubDocuments([makeDoc({ id: 5, name: 'Keep Me' })])

      cy.visitAuthenticated('/documents')
      cy.wait('@getDocuments')
      cy.on('window:confirm', () => false)
      cy.contains('Delete').click()
      cy.contains('Keep Me').should('be.visible')
    })

    // ── Expiry alerts ─────────────────────────────────────────────────────

    it('shows certificate expiry banner for certificate expiring soon', () => {
      const soon = new Date()
      soon.setDate(soon.getDate() + 7)
      stubDocuments([
        makeDoc({
          id: 1, name: 'Food Safety Cert',
          category: 'CERTIFICATE',
          expiryDate: soon.toISOString().split('T')[0],
        }),
      ])
      cy.visitAuthenticated('/documents')
      cy.wait('@getDocuments')
      cy.get('.expiry-banner').should('be.visible')
      cy.contains('Food Safety Cert').should('be.visible')
    })

    it('dismisses expiry banner when X is clicked', () => {
      const soon = new Date()
      soon.setDate(soon.getDate() + 7)
      stubDocuments([
        makeDoc({ id: 1, name: 'Expiring Cert', category: 'CERTIFICATE', expiryDate: soon.toISOString().split('T')[0] }),
      ])
      cy.visitAuthenticated('/documents')
      cy.wait('@getDocuments')
      cy.get('.expiry-banner').should('be.visible')
      cy.get('.expiry-banner-close').click()
      cy.get('.expiry-banner').should('not.exist')
    })
  })

  // ── As staff ──────────────────────────────────────────────────────────────

  describe('as staff', () => {
    beforeEach(() => {
      cy.setAuthState({
        roles: ['ROLE_STAFF'],
        user: { id: 2, name: 'Staff User', email: 'staff@example.com' },
        restaurantStatus: 'active',
        restaurantId: 1,
        restaurantName: 'Test Restaurant',
      })
    })

    it('hides upload button for staff', () => {
      stubDocuments([])
      cy.visitAuthenticated('/documents')
      cy.contains('Upload document').should('not.exist')
    })

    it('hides delete button for staff', () => {
      stubDocuments([makeDoc({ id: 1, name: 'Some Doc' })])
      cy.visitAuthenticated('/documents')
      cy.wait('@getDocuments')
      cy.contains('Delete').should('not.exist')
    })

    it('shows download button for documents', () => {
      stubDocuments([makeDoc({ id: 1, name: 'Some Doc' })])
      cy.intercept('GET', '/api/documents/1/download', {
        statusCode: 200,
        body: new Blob(['file content'], { type: 'application/pdf' }),
      }).as('download')
      cy.visitAuthenticated('/documents')
      cy.wait('@getDocuments')
      cy.contains('Download').should('be.visible')
    })

    it('shows Open link for external URL documents', () => {
      stubDocuments([makeDoc({ id: 1, name: 'External Link', externalUrl: 'https://example.com' })])
      cy.visitAuthenticated('/documents')
      cy.wait('@getDocuments')
      cy.get('.doc-btn').contains('Open').should('exist')
    })
  })

})
