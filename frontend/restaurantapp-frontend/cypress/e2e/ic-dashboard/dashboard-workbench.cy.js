import {
  createChecklist,
  getChecklistCard,
  setIcDashboardAuth,
  stubIcDashboardApi,
  visitIcDashboard,
} from './icDashboardTestUtils'

function getTodayPeriodKey() {
  const today = new Date()
  const yyyy = today.getFullYear()
  const mm = String(today.getMonth() + 1).padStart(2, '0')
  const dd = String(today.getDate()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
}

describe('IC dashboard workbench actions', () => {
  beforeEach(() => {
    setIcDashboardAuth()
  })

  it('marks a task as completed and updates progress across the workbench', () => {
    stubIcDashboardApi({
      module: 'IC_FOOD',
      checklists: [
        createChecklist({
          id: 'food-opening',
          module: 'IC_FOOD',
          title: 'Opening checklist',
          sections: [
            {
              title: 'Opening checks',
              items: [
                {
                  id: 'food-opening-task-1',
                  templateId: 'template-1',
                  label: 'Verify hand wash station',
                  meta: 'Opening shift',
                  state: 'todo',
                  highlighted: false,
                  sectionType: 'OPENING_CHECKS',
                  latestMeasurement: null,
                },
                {
                  id: 'food-opening-task-2',
                  templateId: 'template-2',
                  label: 'Stock sanitizer',
                  meta: '',
                  state: 'todo',
                  highlighted: false,
                  sectionType: 'OPENING_CHECKS',
                  latestMeasurement: null,
                },
              ],
            },
          ],
        }),
      ],
    })

    visitIcDashboard('IC_FOOD')

    getChecklistCard('Opening checklist').within(() => {
      cy.contains('.task-row', 'Verify hand wash station').within(() => {
        cy.get('.task-marker').click()
      })
    })

    cy.wait('@toggleTask')

    getChecklistCard('Opening checklist').within(() => {
      cy.contains('.task-row.completed', 'Verify hand wash station').should('be.visible')
      cy.contains('50% complete').should('be.visible')
    })
    cy.contains('Completed').closest('.legend-item').should('contain', '1')
    cy.contains('Not started').closest('.legend-item').should('contain', '1')
  })

  it('flags a task for follow-up and updates the checklist status', () => {
    stubIcDashboardApi({
      module: 'IC_FOOD',
      checklists: [
        createChecklist({
          id: 'food-flag',
          module: 'IC_FOOD',
          title: 'Mid-shift follow-up',
          sections: [
            {
              title: 'Hygiene',
              items: [
                {
                  id: 'food-flag-task-1',
                  templateId: 'template-1',
                  label: 'Review glove station',
                  meta: 'Needs staff sign-off',
                  state: 'todo',
                  highlighted: false,
                  sectionType: 'HYGIENE',
                  latestMeasurement: null,
                },
              ],
            },
          ],
        }),
      ],
    })

    visitIcDashboard('IC_FOOD')

    getChecklistCard('Mid-shift follow-up').within(() => {
      cy.contains('.task-row', 'Review glove station').within(() => {
        cy.contains('button', 'Flag').click()
      })
    })

    cy.wait('@togglePending')

    getChecklistCard('Mid-shift follow-up').within(() => {
      cy.contains('.task-row.pending', 'Review glove station').should('be.visible')
      cy.contains('button', 'Flagged').should('be.visible')
    })
    cy.contains('Flagged').closest('.legend-item').should('contain', '1')
  })

  it('logs a temperature reading after confirmation and updates the report card', () => {
    stubIcDashboardApi({
      module: 'IC_FOOD',
      checklists: [
        createChecklist({
          id: 'food-temp',
          module: 'IC_FOOD',
          title: 'Cold storage checks',
          sections: [
            {
              title: 'Temperature control',
              items: [
                {
                  id: 'food-temp-task-1',
                  templateId: 'template-1',
                  label: 'Walk-in fridge',
                  meta: '',
                  state: 'todo',
                  highlighted: false,
                  sectionType: 'TEMPERATURE_CONTROL',
                  type: 'temperature',
                  unit: 'C',
                  targetMin: 1,
                  targetMax: 4,
                  latestMeasurement: null,
                },
              ],
            },
          ],
        }),
      ],
    })

    visitIcDashboard('IC_FOOD')

    getChecklistCard('Cold storage checks').within(() => {
      cy.get('input[aria-label="Log temperature for Walk-in fridge"]').type('5.7')
      cy.contains('button', 'Save').click()
    })

    cy.get('[role="dialog"][aria-label="Save 5.7 C for Walk-in fridge?"]').within(() => {
      cy.contains('button', 'Save reading').click()
    })

    cy.wait('@createTemperatureMeasurement')

    getChecklistCard('Cold storage checks').within(() => {
      cy.contains('Last: 5.7 C').should('be.visible')
    })
    cy.contains('.report-row', 'Walk-in fridge').should('contain', '5.7 C')
    cy.contains('1 deviations').should('be.visible')
    cy.contains('Temperature reading saved.').should('be.visible')
  })

  it('submits a checklist and loads a fresh period after confirmation', () => {
    stubIcDashboardApi({
      module: 'IC_FOOD',
      checklists: [
        createChecklist({
          id: 'food-submit',
          module: 'IC_FOOD',
          title: 'Closing checklist',
          activePeriodKey: getTodayPeriodKey(),
          sections: [
            {
              title: 'Closing checks',
              items: [
                {
                  id: 'food-submit-task-1',
                  templateId: 'template-1',
                  label: 'Lock storage room',
                  meta: '',
                  state: 'completed',
                  highlighted: false,
                  sectionType: 'CLOSING_CHECKS',
                  latestMeasurement: null,
                },
              ],
            },
          ],
        }),
      ],
    })

    visitIcDashboard('IC_FOOD')

    getChecklistCard('Closing checklist').within(() => {
      cy.get('.submit-button').should('not.be.disabled').click()
    })

    cy.get('[role="dialog"]').then(($dialog) => {
      const label = $dialog.attr('aria-label')
      expect(['Submit this checklist now?', 'Start the next checklist period?']).to.include(label)
      cy.wrap($dialog).within(() => {
        cy.get('button').contains(/Submit checklist|Submit and continue/).click()
      })
    })

    cy.wait('@submitChecklist')

    getChecklistCard('Closing checklist').within(() => {
      cy.contains('.task-row', 'Lock storage room').should('not.have.class', 'completed')
      cy.contains('0% complete').should('be.visible')
    })
    cy.contains('Started a fresh Closing checklist checklist period.').should('be.visible')
  })
})
