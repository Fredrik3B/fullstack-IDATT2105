function makeComplianceStats(overrides = {}) {
  return {
    completedTasks: 18,
    totalTasks: 20,
    completionRate: 90,
    deviatedTasks: 1,
    temperatureReadings: 6,
    outOfRangeReadings: 1,
    ...overrides,
  }
}

function makeInspectionReport(overrides = {}) {
  return {
    organization: {
      name: 'Test Restaurant',
      adminNames: ['Admin User'],
      managerNames: ['Manager User'],
      totalStaff: 12,
    },
    period: {
      from: '2026-03-01T00:00:00',
      to: '2026-03-31T23:59:59',
    },
    generatedAt: '2026-04-08T10:15:00',
    foodStats: makeComplianceStats(),
    alcoholStats: makeComplianceStats({
      completedTasks: 9,
      totalTasks: 10,
      deviatedTasks: 0,
      temperatureReadings: null,
      outOfRangeReadings: null,
    }),
    checklists: {
      checklists: [
        {
          name: 'Opening checklist',
          complianceArea: 'IK_MAT',
          frequency: 'DAILY',
          completionsInPeriod: 12,
          expectedRuns: 14,
          averageCompletionRate: 92.5,
          deviatedTasks: 2,
        },
      ],
    },
    temperatureLog: [],
    deviationsByDay: [],
    missedTasks: [
      {
        checklistName: 'Opening checklist',
        taskName: 'Check fridge',
        complianceArea: 'IK_MAT',
        missedCount: 2,
      },
    ],
    ...overrides,
  }
}

function makeSummaryReport(overrides = {}) {
  return {
    period: {
      from: '2026-03-01T00:00:00',
      to: '2026-03-31T23:59:59',
    },
    foodStats: makeComplianceStats(),
    alcoholStats: makeComplianceStats({
      completedTasks: 7,
      totalTasks: 8,
      completionRate: 87.5,
      deviatedTasks: 1,
      temperatureReadings: null,
      outOfRangeReadings: null,
    }),
    unresolvedItems: [
      {
        name: 'Missing cleaning signature',
        notDoneBy: '2026-03-28T08:30:00',
      },
    ],
    ...overrides,
  }
}

describe('Reports Page', () => {
  beforeEach(() => {
    cy.setAuthState({
      roles: ['ROLE_ADMIN'],
      user: { id: 1, name: 'Admin User', email: 'admin@example.com' },
      restaurantStatus: 'active',
      restaurantId: 1,
      restaurantName: 'Test Restaurant',
    })
  })

  it('renders the initial empty state before a report is generated', () => {
    cy.visitAuthenticated('/reports')

    cy.contains('Reports & Deviations').should('be.visible')
    cy.contains('No report generated').should('be.visible')
    cy.contains('Select a date range and click generate.').should('be.visible')
  })

  it('generates and renders an inspection report', () => {
    cy.intercept('GET', '/api/reports/full-report*', { statusCode: 200, body: makeInspectionReport() }).as('getInspectionReport')

    cy.visitAuthenticated('/reports')
    cy.contains('Generate report').click()

    cy.wait('@getInspectionReport').its('request.url').should('include', '/api/reports/full-report')
    cy.contains('Test Restaurant').should('be.visible')
    cy.contains('Compliance overview').should('be.visible')
    cy.contains('Checklist performance').should('be.visible')
    cy.contains('Opening checklist').should('be.visible')
    cy.contains('Most missed tasks').should('be.visible')
    cy.contains('Export PDF').should('be.visible')
  })

  it('switches to summary report and renders unresolved items', () => {
    cy.intercept('GET', '/api/reports/summary*', { statusCode: 200, body: makeSummaryReport() }).as('getSummaryReport')

    cy.visitAuthenticated('/reports')
    cy.get('.filter-select').select('summary')
    cy.contains('Generate report').click()

    cy.wait('@getSummaryReport').its('request.url').should('include', '/api/reports/summary')
    cy.contains('Internal summary').should('be.visible')
    cy.contains('Unresolved items (1)').should('be.visible')
    cy.contains('Missing cleaning signature').should('be.visible')
  })

  it('shows an error state when report generation fails', () => {
    cy.intercept('GET', '/api/reports/full-report*', { statusCode: 500, body: { detail: 'Backend unavailable' } }).as('getInspectionReportFail')

    cy.visitAuthenticated('/reports')
    cy.contains('Generate report').click()

    cy.wait('@getInspectionReportFail')
    cy.contains('Failed to load report').should('be.visible')
    cy.contains('Backend unavailable').should('be.visible')
  })

  it('opens and closes the deviation modal', () => {
    cy.visitAuthenticated('/reports')

    cy.contains('Report deviation').click()
    cy.get('[role="dialog"]').should('be.visible')
    cy.contains('Report deviation').should('be.visible')
    cy.contains('Complete all sections before submitting.').should('be.visible')

    cy.contains('Cancel').click()
    cy.get('[role="dialog"]').should('not.exist')
  })

  it('submits a deviation report from the modal', () => {
    cy.intercept('POST', '/api/reports/deviations', (req) => {
      expect(req.body).to.deep.include({
        deviationName: 'Broken hand wash sink',
        severity: 'MAJOR',
        noticedBy: 'Jamie',
        reportedTo: 'Pat',
        processedBy: 'Morgan',
      })
      req.reply({
        statusCode: 201,
        body: { id: 44, ...req.body },
      })
    }).as('createDeviation')

    cy.visitAuthenticated('/reports?action=deviation')

    cy.get('[role="dialog"]').should('be.visible')
    cy.get('#dev-name').type('Broken hand wash sink')
    cy.get('#dev-severity').select('MAJOR')
    cy.get('#dev-time').type('2026-04-08T09:30')
    cy.get('#dev-desc').type('The sink in the back room stopped draining during the morning shift.')
    cy.get('#dev-noticed').type('Jamie')
    cy.get('#dev-reported').type('Pat')
    cy.get('#dev-processed').type('Morgan')
    cy.get('#dev-immediate').type('Area was paused and staff were redirected to another sink.')
    cy.get('#dev-cause').type('Drain blockage caused by debris buildup.')
    cy.get('#dev-measures').type('Schedule drain checks and remind staff about disposal routines.')
    cy.get('#dev-done').type('Plumber contacted and the sink was cleared the same day.')

    cy.contains('Submit report').click()

    cy.wait('@createDeviation')
    cy.get('[role="dialog"]').should('not.exist')
  })
})
