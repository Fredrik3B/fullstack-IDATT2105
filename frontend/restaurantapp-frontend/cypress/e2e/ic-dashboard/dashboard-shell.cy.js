import {
  MODULE_CONFIG,
  createChecklist,
  createTaskTemplate,
  openChecklistLibrary,
  setIcDashboardAuth,
  stubIcDashboardApi,
  visitIcDashboard,
} from './icDashboardTestUtils'

describe('IC dashboard shell', () => {
  beforeEach(() => {
    setIcDashboardAuth()
  })

  Object.values(MODULE_CONFIG).forEach((moduleConfig) => {
    it(`renders the ${moduleConfig.label} dashboard route with module-specific content`, () => {
      stubIcDashboardApi({
        module: moduleConfig.module,
        checklists: [
          createChecklist({
            id: `${moduleConfig.module}-opening`,
            module: moduleConfig.module,
            title: `${moduleConfig.label} opening`,
            sections: [
              {
                title: 'Opening checks',
                items: [
                  {
                    id: `${moduleConfig.module}-task-1`,
                    templateId: 'template-1',
                    label: 'Verify service area',
                    meta: 'Start of shift',
                    state: 'completed',
                    highlighted: false,
                    sectionType: 'OPENING_CHECKS',
                    latestMeasurement: null,
                  },
                ],
              },
              {
                title: 'Temperature control',
                items: [
                {
                  id: `${moduleConfig.module}-task-2`,
                  templateId: 'template-2',
                  label: 'Check cold storage',
                  meta: '',
                  state: 'todo',
                  highlighted: false,
                  sectionType: 'TEMPERATURE_CONTROL',
                  type: 'temperature',
                  unit: 'C',
                  targetMin: 1,
                  targetMax: 4,
                  latestMeasurement: {
                    id: 'measurement-1',
                    checklistId: `${moduleConfig.module}-opening`,
                      taskId: `${moduleConfig.module}-task-2`,
                      valueC: 3.2,
                      deviation: false,
                    },
                  },
                ],
              },
            ],
          }),
        ],
      })

      visitIcDashboard(moduleConfig.module)

      cy.contains(moduleConfig.label).should('be.visible')
      cy.contains('Checklists').should('be.visible')
      cy.contains(moduleConfig.description).should('be.visible')
      cy.contains('Checklist summary').should('be.visible')
      cy.contains('Temperature log').should('be.visible')
      cy.contains('No deviations').should('be.visible')
      cy.contains(`${moduleConfig.label} opening`).should('be.visible')
      cy.contains('Check cold storage').should('be.visible')
      cy.contains('3.2 C').should('be.visible')
    })
  })

  it('filters visible cards by period and keeps the overview widgets in sync', () => {
    stubIcDashboardApi({
      module: 'IC_FOOD',
      checklists: [
        createChecklist({
          id: 'daily-food',
          module: 'IC_FOOD',
          title: 'Daily prep',
          period: 'daily',
          sections: [
            {
              title: 'Opening checks',
              items: [
                {
                  id: 'daily-food-task-1',
                  templateId: 'template-1',
                  label: 'Stock soap dispensers',
                  meta: '',
                  state: 'completed',
                  highlighted: false,
                  sectionType: 'OPENING_CHECKS',
                  latestMeasurement: null,
                },
              ],
            },
          ],
        }),
        createChecklist({
          id: 'weekly-food',
          module: 'IC_FOOD',
          title: 'Weekly audit',
          period: 'weekly',
          sections: [
            {
              title: 'Hygiene',
              items: [
                {
                  id: 'weekly-food-task-1',
                  templateId: 'template-2',
                  label: 'Review cleaning log',
                  meta: '',
                  state: 'pending',
                  highlighted: true,
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

    cy.contains('Daily prep').should('be.visible')
    cy.contains('Weekly audit').should('not.exist')
    cy.contains('1 tasks').should('be.visible')
    cy.contains('Completed').closest('.legend-item').should('contain', '1')

    cy.contains('button', 'Weekly').click()

    cy.contains('Weekly audit').should('be.visible')
    cy.contains('Daily prep').should('not.exist')
    cy.contains('Flagged').closest('.legend-item').should('contain', '1')
  })

  it('shows the empty workbench state when no checklist is loaded for the active period', () => {
    stubIcDashboardApi({
      module: 'IC_FOOD',
      checklists: [
        createChecklist({
          id: 'library-only',
          module: 'IC_FOOD',
          title: 'Library checklist',
          displayedOnWorkbench: false,
        }),
      ],
    })

    visitIcDashboard('IC_FOOD')

    cy.contains('No checklists on the workbench').should('be.visible')
    cy.contains('Create a checklist or load one from the library').should('be.visible')
  })

  it('shows the error state when checklist loading fails', () => {
    cy.intercept('GET', '**/api/checklists*', {
      statusCode: 500,
      body: { message: 'Dashboard fetch failed' },
    }).as('getChecklists')

    visitIcDashboard('IC_FOOD')

    cy.contains('Could not load the workbench').should('be.visible')
    cy.contains('Dashboard fetch failed').should('be.visible')
  })

  it('opens the checklist library and loads a saved checklist onto the workbench', () => {
    stubIcDashboardApi({
      module: 'IC_FOOD',
      checklists: [
        createChecklist({
          id: 'workbench-checklist',
          module: 'IC_FOOD',
          title: 'Daily opening',
          displayedOnWorkbench: true,
        }),
        createChecklist({
          id: 'library-checklist',
          module: 'IC_FOOD',
          title: 'Deep clean follow-up',
          displayedOnWorkbench: false,
        }),
      ],
      tasks: [
        createTaskTemplate({
          id: 'food-task-1',
          module: 'IC_FOOD',
          title: 'Check dishwasher',
        }),
      ],
    })

    visitIcDashboard('IC_FOOD')
    openChecklistLibrary()

    cy.contains('[role="dialog"]', 'Checklist library').within(() => {
      cy.contains('Deep clean follow-up').should('be.visible')
      cy.contains('Open on workbench').click()
    })

    cy.wait('@setWorkbench')
    cy.wait('@getChecklists')

    cy.get('[role="dialog"][aria-label="Checklist library"]').should('not.exist')
    cy.contains('Deep clean follow-up').should('be.visible')
  })

  it('hides the task pool admin action for non-admin users', () => {
    setIcDashboardAuth({
      roles: ['ROLE_STAFF'],
      user: { id: 2, name: 'Dashboard Staff', email: 'staff@example.com' },
    })

    stubIcDashboardApi({
      module: 'IC_FOOD',
      tasks: [
        createTaskTemplate({
          id: 'food-task-1',
          module: 'IC_FOOD',
          title: 'Check dishwasher',
        }),
      ],
    })

    visitIcDashboard('IC_FOOD')

    cy.contains('Admin').should('not.exist')
    cy.contains('button', 'Task pool').should('not.exist')

    cy.contains('button', 'New checklist').click()
    cy.wait('@getTasks')

    cy.get('[role="dialog"][aria-label="Create checklist"]').within(() => {
      cy.contains('button', 'Open full task pool').should('not.exist')
    })

    cy.get('[role="dialog"][aria-label="Manage task pool"]').should('not.exist')
  })
})
