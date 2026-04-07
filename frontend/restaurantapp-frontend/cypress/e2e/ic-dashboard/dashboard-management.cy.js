import {
  createChecklist,
  createTaskTemplate,
  createTemperatureZone,
  getChecklistCard,
  openCreateChecklistModal,
  openTaskPoolModal,
  setIcDashboardAuth,
  stubIcDashboardApi,
  visitIcDashboard,
} from './icDashboardTestUtils'

describe('IC dashboard management flows', () => {
  beforeEach(() => {
    setIcDashboardAuth()
  })

  it('creates a checklist from the shared task pool', () => {
    stubIcDashboardApi({
      module: 'IC_FOOD',
      checklists: [],
      tasks: [
        createTaskTemplate({
          id: 'food-template-1',
          module: 'IC_FOOD',
          title: 'Sanitize prep benches',
          sectionType: 'HYGIENE',
        }),
        createTaskTemplate({
          id: 'food-template-2',
          module: 'IC_FOOD',
          title: 'Check walk-in cooler',
          sectionType: 'TEMPERATURE_CONTROL',
          temperatureZoneId: 'zone-1',
        }),
      ],
      temperatureZones: [
        createTemperatureZone({
          id: 'zone-1',
          module: 'IC_FOOD',
          name: 'Walk-in cooler',
          zoneType: 'FRIDGE',
          targetMin: 1,
          targetMax: 4,
        }),
      ],
    })

    visitIcDashboard('IC_FOOD')
    cy.contains('button', 'New checklist').should('be.visible')
    openCreateChecklistModal()
    cy.wait('@getTasks')
    cy.wait('@getTemperatureZones')

    cy.get('[role="dialog"][aria-label="Create checklist"]').within(() => {
      cy.contains('.field', 'Title').find('input').type('Opening line check')
      cy.contains('.field', 'Subtitle').find('input').type('Run before opening to guests')
      cy.contains('.field', 'Period').find('select').select('Weekly')
      cy.contains('button', 'Continue to tasks').click()
      cy.contains('.task-option', 'Sanitize prep benches').find('input').check()
      cy.contains('.task-option', 'Check walk-in cooler').find('input').check()
      cy.contains('button', 'Create checklist').click()
    })

    cy.wait('@createChecklist')
    cy.wait('@getChecklists')

    cy.get('[role="dialog"][aria-label="Create checklist"]').should('not.exist')
    cy.contains('button', 'Weekly').click()
    getChecklistCard('Opening line check').should('be.visible')
    cy.contains('Run before opening to guests').should('be.visible')
    cy.contains('button', 'Weekly').should('have.class', 'active')
  })

  it('edits an existing checklist and saves the updated details', () => {
    stubIcDashboardApi({
      module: 'IC_FOOD',
      checklists: [
        createChecklist({
          id: 'food-edit',
          module: 'IC_FOOD',
          title: 'Current opening checklist',
          subtitle: 'Original subtitle',
          sections: [
            {
              title: 'Opening checks',
              items: [
                {
                  id: 'food-edit-task-1',
                  templateId: 'food-template-1',
                  label: 'Verify hand wash station',
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
      tasks: [
        createTaskTemplate({
          id: 'food-template-1',
          module: 'IC_FOOD',
          title: 'Verify hand wash station',
          sectionType: 'OPENING_CHECKS',
        }),
        createTaskTemplate({
          id: 'food-template-2',
          module: 'IC_FOOD',
          title: 'Review fridge temperatures',
          sectionType: 'TEMPERATURE_CONTROL',
          temperatureZoneId: 'zone-1',
        }),
      ],
      temperatureZones: [
        createTemperatureZone({
          id: 'zone-1',
          module: 'IC_FOOD',
          name: 'Prep fridge',
          zoneType: 'FRIDGE',
          targetMin: 1,
          targetMax: 4,
        }),
      ],
    })

    visitIcDashboard('IC_FOOD')
    cy.contains('button', 'Edit').should('be.visible')

    getChecklistCard('Current opening checklist').within(() => {
      cy.contains('button', 'Edit').click()
    })

    cy.wait('@getTasks')
    cy.wait('@getTemperatureZones')

    cy.get('[role="dialog"][aria-label="Edit checklist"]').within(() => {
      cy.contains('.field', 'Title').find('input').clear()
      cy.contains('.field', 'Title').find('input').type('Updated opening checklist')
      cy.contains('.field', 'Subtitle').find('input').clear()
      cy.contains('.field', 'Subtitle').find('input').type('Updated subtitle')
      cy.contains('button', 'Continue to tasks').click()
      cy.contains('.task-option', 'Review fridge temperatures').find('input').check()
      cy.contains('button', 'Save changes').click()
    })

    cy.wait('@updateChecklist')
    cy.wait('@getChecklists')

    cy.get('[role="dialog"][aria-label="Edit checklist"]').should('not.exist')
    getChecklistCard('Updated opening checklist').should('be.visible')
    cy.contains('Updated subtitle').should('be.visible')
    cy.contains('Review fridge temperatures').should('be.visible')
  })

  it('deletes a checklist through the edit modal and confirmation dialog', () => {
    stubIcDashboardApi({
      module: 'IC_FOOD',
      checklists: [
        createChecklist({
          id: 'food-delete',
          module: 'IC_FOOD',
          title: 'Temporary checklist',
        }),
      ],
      tasks: [
        createTaskTemplate({
          id: 'food-template-1',
          module: 'IC_FOOD',
          title: 'Verify hand wash station',
          sectionType: 'OPENING_CHECKS',
        }),
      ],
    })

    visitIcDashboard('IC_FOOD')

    getChecklistCard('Temporary checklist').within(() => {
      cy.contains('button', 'Edit').click()
    })

    cy.wait('@getTasks')
    cy.wait('@getTemperatureZones')

    cy.get('[role="dialog"][aria-label="Edit checklist"]').within(() => {
      cy.contains('button', 'Delete checklist').click()
    })

    cy.get('[role="dialog"][aria-label="Delete checklist?"]').within(() => {
      cy.contains('button', 'Delete checklist').click()
    })

    cy.wait('@deleteChecklist')
    cy.wait('@getChecklists')

    cy.contains('Temporary checklist').should('not.exist')
    cy.contains('Checklist deleted.').should('be.visible')
  })

  it('manages the task pool by creating and deleting a shared temperature task', () => {
    stubIcDashboardApi({
      module: 'IC_FOOD',
      tasks: [
        createTaskTemplate({
          id: 'food-task-1',
          module: 'IC_FOOD',
          title: 'Verify opening station',
          sectionType: 'OPENING_CHECKS',
        }),
      ],
      temperatureZones: [
        createTemperatureZone({
          id: 'zone-1',
          module: 'IC_FOOD',
          name: 'Walk-in freezer',
          zoneType: 'FREEZER',
          targetMin: -22,
          targetMax: -18,
        }),
      ],
    })

    visitIcDashboard('IC_FOOD')
    cy.contains('button', 'Task pool').should('be.visible')
    openTaskPoolModal()
    cy.wait('@getTasks')

    cy.get('[role="dialog"][aria-label="Manage task pool"]').within(() => {
      cy.contains('button', 'New task').click()
    })

    cy.get('[role="dialog"][aria-label="Create task template"]').within(() => {
      cy.contains('.field', 'Task title').find('input').type('Log freezer temperature')
      cy.contains('.field', 'Section').find('select').select('TEMPERATURE_CONTROL')
      cy.wait('@getTemperatureZones')
      cy.contains('.field', 'Fridge item').find('select').select('zone-1')
      cy.contains('button', 'Save task').click()
    })

    cy.wait('@createTask')
    cy.get('[role="dialog"][aria-label="Manage task pool"]').within(() => {
      cy.contains('.task-row', 'Log freezer temperature').should('be.visible')
      cy.contains('.task-row', 'Log freezer temperature').within(() => {
        cy.contains('button', 'Delete').click()
      })
    })

    cy.get('[role="dialog"][aria-label="Delete shared task?"]').within(() => {
      cy.contains('button', 'Delete task').click()
    })

    cy.wait('@deleteTask')
    cy.contains('Deleted "Log freezer temperature".').should('be.visible')
    cy.get('[role="dialog"][aria-label="Manage task pool"]').within(() => {
      cy.contains('Log freezer temperature').should('not.exist')
      cy.contains('Verify opening station').should('be.visible')
    })
  })

  it('keeps task pool access available to admins from the checklist editor', () => {
    stubIcDashboardApi({
      module: 'IC_FOOD',
      tasks: [
        createTaskTemplate({
          id: 'food-task-1',
          module: 'IC_FOOD',
          title: 'Verify opening station',
          sectionType: 'OPENING_CHECKS',
        }),
      ],
    })

    visitIcDashboard('IC_FOOD')
    openCreateChecklistModal()
    cy.wait('@getTasks')
    cy.wait('@getTemperatureZones')

    cy.get('[role="dialog"][aria-label="Create checklist"]').within(() => {
      cy.contains('button', 'Continue to tasks').click()
      cy.contains('button', 'Open full task pool').should('be.visible').click()
    })

    cy.wait('@getTasks')
    cy.get('[role="dialog"][aria-label="Manage task pool"]').should('be.visible')
  })

  it('refreshes checklist tasks after task-pool changes and removes deleted selections', () => {
    stubIcDashboardApi({
      module: 'IC_FOOD',
      tasks: [
        createTaskTemplate({
          id: 'food-task-1',
          module: 'IC_FOOD',
          title: 'Verify opening station',
          sectionType: 'OPENING_CHECKS',
        }),
      ],
    })

    visitIcDashboard('IC_FOOD')
    openCreateChecklistModal()
    cy.wait('@getTasks')
    cy.wait('@getTemperatureZones')

    cy.get('[role="dialog"][aria-label="Create checklist"]').within(() => {
      cy.contains('button', 'Continue to tasks').click()
      cy.contains('.task-option', 'Verify opening station').find('input').check()
      cy.contains('button', 'Open full task pool').click()
    })

    cy.wait('@getTasks')
    cy.get('[role="dialog"][aria-label="Manage task pool"]').within(() => {
      cy.contains('.task-row', 'Verify opening station').within(() => {
        cy.contains('button', 'Delete').click()
      })
    })

    cy.get('[role="dialog"][aria-label="Delete shared task?"]').within(() => {
      cy.contains('button', 'Delete task').click()
    })

    cy.wait('@deleteTask')
    cy.wait('@getTasks')
    cy.wait('@getTemperatureZones')

    cy.get('[role="dialog"][aria-label="Manage task pool"]').within(() => {
      cy.get('button[aria-label="Close"]').click()
    })

    cy.get('[role="dialog"][aria-label="Create checklist"]').within(() => {
      cy.contains('.task-option', 'Verify opening station').should('not.exist')
      cy.contains('.tasks-state', 'No tasks in the pool yet.').should('exist')
      cy.contains('.progress-meta', '0 tasks selected').should('exist')
    })
  })

  it('creates a fridge item from quick add for temperature tasks', () => {
    stubIcDashboardApi({
      module: 'IC_FOOD',
      tasks: [],
      temperatureZones: [],
    })

    visitIcDashboard('IC_FOOD')
    openCreateChecklistModal()
    cy.wait('@getTasks')
    cy.wait('@getTemperatureZones')

    cy.get('[role="dialog"][aria-label="Create checklist"]').within(() => {
      cy.contains('button', 'Continue to tasks').click()
      cy.contains('button', 'Quick add task').click()
      cy.contains('.field', 'Task title').find('input').type('Check dessert fridge')
      cy.contains('.field', 'Section').find('select').select('TEMPERATURE_CONTROL')
      cy.contains('button', 'New fridge item').click()
    })

    cy.get('[role="dialog"][aria-label="Create temperature item"]').within(() => {
      cy.contains('.field', 'Item name').find('input').type('Dessert fridge')
      cy.contains('.field', 'Type').find('select').select('FRIDGE')
      cy.contains('.field', 'Target min').find('input').clear().type('1')
      cy.contains('.field', 'Target max').find('input').clear().type('4')
      cy.contains('button', 'Save item').click()
    })

    cy.wait('@createTemperatureZone')
    cy.wait('@getTemperatureZones')

    cy.get('[role="dialog"][aria-label="Manage fridge items"]').within(() => {
      cy.get('button[aria-label="Close"]').click()
    })

    cy.get('[role="dialog"][aria-label="Create checklist"]').within(() => {
      cy.contains('.field', 'Fridge item').find('select').select('zone-51')
      cy.contains('button', 'Add and select task').click()
    })

    cy.wait('@createTask')
    cy.get('[role="dialog"][aria-label="Create checklist"]').within(() => {
      cy.contains('.task-option', 'Check dessert fridge').should('be.visible')
      cy.contains('.task-option', 'Check dessert fridge').find('input').should('be.checked')
      cy.contains('.task-option', 'Check dessert fridge').should('contain', 'Dessert fridge')
    })
  })
})
