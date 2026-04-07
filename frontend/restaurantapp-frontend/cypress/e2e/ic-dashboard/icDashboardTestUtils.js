/* global cy */

const MODULE_CONFIG = {
  IC_FOOD: {
    module: 'IC_FOOD',
    label: 'IC-Food',
    path: '/ic-food',
    description:
      'Food safety routines, hygiene controls, and temperature follow-up for daily operations.',
  },
  IC_ALCOHOL: {
    module: 'IC_ALCOHOL',
    label: 'IC-Alcohol',
    path: '/ic-alcohol',
    description:
      'Alcohol service routines, compliance checkpoints, and operational follow-up for staff.',
  },
}

const SECTION_TITLES = {
  OPENING_CHECKS: 'Opening checks',
  TEMPERATURE_CONTROL: 'Temperature control',
  CLOSING_CHECKS: 'Closing checks',
  HYGIENE: 'Hygiene',
  SERVICE_CONTROLS: 'Service controls',
  UNSORTED: 'General',
}

function clone(value) {
  return JSON.parse(JSON.stringify(value))
}

function slugify(value) {
  return String(value ?? '')
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
}

function sectionTitle(sectionType) {
  return SECTION_TITLES[sectionType] ?? SECTION_TITLES.UNSORTED
}

function normalizeTaskTemplate(template) {
  return {
    id: String(template.id),
    title: template.title,
    meta: template.meta ?? '',
    module: template.module,
    sectionType: template.sectionType ?? 'UNSORTED',
    targetMin: template.targetMin ?? null,
    targetMax: template.targetMax ?? null,
  }
}

function buildChecklistTask(template, checklistId, index) {
  return {
    id: `${checklistId}-task-${index + 1}`,
    templateId: String(template.id),
    label: template.title,
    meta: template.meta ?? '',
    state: 'todo',
    highlighted: false,
    sectionType: template.sectionType ?? 'UNSORTED',
    targetMin: template.targetMin ?? null,
    targetMax: template.targetMax ?? null,
    latestMeasurement: null,
  }
}

function buildSectionsFromTaskTemplateIds(taskTemplateIds, allTasks, checklistId) {
  const grouped = new Map()
  const selectedTasks = taskTemplateIds
    .map((taskId) => allTasks.find((task) => String(task.id) === String(taskId)))
    .filter(Boolean)

  selectedTasks.forEach((template, index) => {
    const key = template.sectionType ?? 'UNSORTED'
    if (!grouped.has(key)) {
      grouped.set(key, [])
    }
    grouped.get(key).push(buildChecklistTask(template, checklistId, index))
  })

  return Array.from(grouped.entries()).map(([type, items]) => ({
    title: sectionTitle(type),
    items,
  }))
}

function recalculateChecklistProgress(checklist) {
  const tasks = (checklist.sections ?? []).flatMap((section) => section.items ?? [])
  const total = tasks.length
  const completed = tasks.filter((task) => task.state === 'completed').length
  checklist.progress = total ? Math.round((completed / total) * 100) : 0

  if (total === 0) {
    checklist.statusLabel = 'No tasks'
    checklist.statusTone = 'muted'
    return
  }

  if (completed === total) {
    checklist.statusLabel = 'Ready to submit'
    checklist.statusTone = 'success'
    return
  }

  const pending = tasks.filter((task) => task.state === 'pending').length
  if (pending > 0) {
    checklist.statusLabel = `${pending} flagged`
    checklist.statusTone = 'warning'
    return
  }

  checklist.statusLabel = 'In progress'
  checklist.statusTone = 'success'
}

function createChecklistRecord(data, allTasks) {
  const checklistId = String(data.id)
  const checklist = {
    id: checklistId,
    title: data.title,
    subtitle: data.subtitle ?? '',
    period: data.period ?? 'daily',
    recurring: data.recurring !== false,
    displayedOnWorkbench: data.displayedOnWorkbench !== false,
    activePeriodKey: data.activePeriodKey ?? `${data.period ?? 'daily'}-2026-04`,
    moduleChip: MODULE_CONFIG[data.module]?.label ?? MODULE_CONFIG.IC_FOOD.label,
    statusLabel: data.statusLabel ?? 'In progress',
    statusTone: data.statusTone ?? 'success',
    progress: data.progress ?? 0,
    sections:
      data.sections != null
        ? clone(data.sections)
        : buildSectionsFromTaskTemplateIds(data.taskTemplateIds ?? [], allTasks, checklistId),
  }

  recalculateChecklistProgress(checklist)
  return checklist
}

export function createTaskTemplate(overrides = {}) {
  return normalizeTaskTemplate({
    id: overrides.id ?? `task-${Math.random().toString(16).slice(2, 8)}`,
    title: overrides.title ?? 'Verify station setup',
    meta: overrides.meta ?? 'Record findings in shift log',
    module: overrides.module ?? 'IC_FOOD',
    sectionType: overrides.sectionType ?? 'OPENING_CHECKS',
    targetMin: overrides.targetMin ?? null,
    targetMax: overrides.targetMax ?? null,
  })
}

export function createChecklist(overrides = {}) {
  const module = overrides.module ?? 'IC_FOOD'
  const id = String(overrides.id ?? `checklist-${Math.random().toString(16).slice(2, 8)}`)
  const sections = overrides.sections ?? [
    {
      title: 'Opening checks',
      items: [
        {
          id: `${id}-task-1`,
          templateId: 'template-1',
          label: 'Verify hand wash station',
          meta: 'Opening shift',
          state: 'todo',
          highlighted: false,
          sectionType: 'OPENING_CHECKS',
          latestMeasurement: null,
        },
      ],
    },
  ]

  const checklist = {
    id,
    module,
    title: overrides.title ?? 'Opening checklist',
    subtitle: overrides.subtitle ?? 'Complete before service begins.',
    period: overrides.period ?? 'daily',
    recurring: overrides.recurring !== false,
    displayedOnWorkbench: overrides.displayedOnWorkbench !== false,
    activePeriodKey: overrides.activePeriodKey ?? `${overrides.period ?? 'daily'}-2026-04`,
    moduleChip: overrides.moduleChip ?? MODULE_CONFIG[module]?.label ?? MODULE_CONFIG.IC_FOOD.label,
    statusLabel: overrides.statusLabel ?? 'In progress',
    statusTone: overrides.statusTone ?? 'success',
    progress: overrides.progress ?? 0,
    sections,
  }

  recalculateChecklistProgress(checklist)
  return checklist
}

export function setIcDashboardAuth(overrides = {}) {
  cy.setAuthState({
    roles: ['ROLE_ADMIN'],
    user: { id: 1, name: 'Dashboard Admin', email: 'admin@example.com' },
    restaurantStatus: 'active',
    restaurantId: 1,
    restaurantName: 'North Star',
    ...overrides,
  })
}

export function stubIcDashboardApi({ module = 'IC_FOOD', checklists = [], tasks = [] } = {}) {
  const moduleConfig = MODULE_CONFIG[module]
  const normalizedTasks = clone(tasks).map(normalizeTaskTemplate)
  const state = {
    taskSeq: 400,
    checklistSeq: 90,
    measurementSeq: 900,
    submitSeq: 2,
    tasks: normalizedTasks,
    checklists: clone(checklists).map((entry) =>
      createChecklistRecord({ ...entry, module: entry.module ?? module }, normalizedTasks),
    ),
  }

  cy.intercept('GET', '**/api/checklists*', (req) => {
    if (req.query.module !== module) {
      req.continue()
      return
    }
    req.reply({ statusCode: 200, body: clone(state.checklists) })
  }).as('getChecklists')

  cy.intercept('GET', '**/api/tasks*', (req) => {
    if (req.query.module !== module) {
      req.continue()
      return
    }
    req.reply({ statusCode: 200, body: clone(state.tasks) })
  }).as('getTasks')

  cy.intercept('POST', '**/api/tasks', (req) => {
    if (req.body.module !== module) {
      req.continue()
      return
    }

    state.taskSeq += 1
    const created = normalizeTaskTemplate({
      id: `task-${state.taskSeq}`,
      ...req.body,
    })
    state.tasks.push(created)
    req.reply({ statusCode: 201, body: clone(created) })
  }).as('createTask')

  cy.intercept('PUT', '**/api/tasks/*', (req) => {
    const match = req.url.match(/\/api\/tasks\/([^/]+)$/)
    if (!match) {
      req.continue()
      return
    }

    const task = state.tasks.find((entry) => String(entry.id) === String(match[1]))
    if (!task) {
      req.reply({ statusCode: 404, body: { message: 'Task not found' } })
      return
    }

    Object.assign(task, normalizeTaskTemplate({ id: task.id, ...req.body }))
    req.reply({ statusCode: 200, body: clone(task) })
  }).as('updateTask')

  cy.intercept('DELETE', '**/api/tasks/*', (req) => {
    const match = req.url.match(/\/api\/tasks\/([^/]+)$/)
    if (!match) {
      req.continue()
      return
    }

    state.tasks = state.tasks.filter((entry) => String(entry.id) !== String(match[1]))
    state.checklists = state.checklists.map((checklist) => {
      const nextSections = (checklist.sections ?? [])
        .map((section) => ({
          ...section,
          items: (section.items ?? []).filter(
            (item) => String(item.templateId) !== String(match[1]),
          ),
        }))
        .filter((section) => section.items.length > 0)

      const nextChecklist = { ...checklist, sections: nextSections }
      recalculateChecklistProgress(nextChecklist)
      return nextChecklist
    })
    req.reply({ statusCode: 204, body: '' })
  }).as('deleteTask')

  cy.intercept('POST', '**/api/checklists', (req) => {
    if (req.body.module !== module) {
      req.continue()
      return
    }

    state.checklistSeq += 1
    const created = createChecklistRecord(
      {
        id: `created-${state.checklistSeq}`,
        module,
        title: req.body.title,
        subtitle: req.body.subtitle,
        period: req.body.period,
        recurring: req.body.recurring,
        displayedOnWorkbench: req.body.displayedOnWorkbench,
        taskTemplateIds: req.body.taskTemplateIds,
        activePeriodKey: `${req.body.period}-created-${state.checklistSeq}`,
      },
      state.tasks,
    )
    state.checklists.push(created)
    req.reply({ statusCode: 201, body: clone(created) })
  }).as('createChecklist')

  cy.intercept('PUT', '**/api/checklists/*', (req) => {
    const match = req.url.match(/\/api\/checklists\/([^/]+)$/)
    if (!match) {
      req.continue()
      return
    }

    const checklistId = match[1]
    const checklist = state.checklists.find((entry) => String(entry.id) === String(checklistId))
    if (!checklist) {
      req.reply({ statusCode: 404, body: { message: 'Checklist not found' } })
      return
    }

    checklist.title = req.body.title
    checklist.subtitle = req.body.subtitle ?? ''
    checklist.period = req.body.period ?? checklist.period
    checklist.recurring = req.body.recurring !== false
    checklist.displayedOnWorkbench = req.body.displayedOnWorkbench !== false
    checklist.sections = buildSectionsFromTaskTemplateIds(
      req.body.taskTemplateIds ?? [],
      state.tasks,
      checklist.id,
    )
    checklist.activePeriodKey = `${checklist.period}-edited-${state.checklistSeq}`
    recalculateChecklistProgress(checklist)

    req.reply({ statusCode: 200, body: clone(checklist) })
  }).as('updateChecklist')

  cy.intercept('DELETE', '**/api/checklists/*', (req) => {
    const match = req.url.match(/\/api\/checklists\/([^/]+)$/)
    if (!match) {
      req.continue()
      return
    }

    const checklistId = match[1]
    state.checklists = state.checklists.filter((entry) => String(entry.id) !== String(checklistId))
    req.reply({ statusCode: 204, body: '' })
  }).as('deleteChecklist')

  cy.intercept('PUT', '**/api/checklists/*/workbench', (req) => {
    const match = req.url.match(/\/api\/checklists\/([^/]+)\/workbench$/)
    if (!match) {
      req.continue()
      return
    }

    const checklist = state.checklists.find((entry) => String(entry.id) === String(match[1]))
    if (!checklist) {
      req.reply({ statusCode: 404, body: { message: 'Checklist not found' } })
      return
    }

    checklist.displayedOnWorkbench = req.body.displayedOnWorkbench !== false
    req.reply({ statusCode: 200, body: clone(checklist) })
  }).as('setWorkbench')

  cy.intercept('PUT', '**/api/checklists/*/tasks/*/completion', (req) => {
    const match = req.url.match(/\/api\/checklists\/([^/]+)\/tasks\/([^/]+)\/completion$/)
    if (!match) {
      req.continue()
      return
    }

    const checklist = state.checklists.find((entry) => String(entry.id) === String(match[1]))
    const task = checklist?.sections
      ?.flatMap((section) => section.items ?? [])
      .find((entry) => String(entry.id) === String(match[2]))

    if (!task) {
      req.reply({ statusCode: 404, body: { message: 'Task not found' } })
      return
    }

    task.state = req.body.state
    task.highlighted = false
    task.pendingForPeriodKey = null
    task.completedForPeriodKey = req.body.state === 'completed' ? req.body.periodKey : null
    task.completedAt = req.body.completedAt ?? null
    recalculateChecklistProgress(checklist)

    req.reply({
      statusCode: 200,
      body: {
        state: task.state,
        highlighted: task.highlighted,
        pendingForPeriodKey: task.pendingForPeriodKey,
        completedForPeriodKey: task.completedForPeriodKey,
        completedAt: task.completedAt,
        latestMeasurement: task.latestMeasurement,
      },
    })
  }).as('toggleTask')

  cy.intercept('PUT', '**/api/checklists/*/tasks/*/flag', (req) => {
    const match = req.url.match(/\/api\/checklists\/([^/]+)\/tasks\/([^/]+)\/flag$/)
    if (!match) {
      req.continue()
      return
    }

    const checklist = state.checklists.find((entry) => String(entry.id) === String(match[1]))
    const task = checklist?.sections
      ?.flatMap((section) => section.items ?? [])
      .find((entry) => String(entry.id) === String(match[2]))

    if (!task) {
      req.reply({ statusCode: 404, body: { message: 'Task not found' } })
      return
    }

    task.state = req.body.state
    task.highlighted = req.body.state === 'pending'
    task.pendingForPeriodKey = req.body.state === 'pending' ? req.body.periodKey : null
    task.completedForPeriodKey = null
    task.completedAt = null
    recalculateChecklistProgress(checklist)

    req.reply({
      statusCode: 200,
      body: {
        state: task.state,
        highlighted: task.highlighted,
        pendingForPeriodKey: task.pendingForPeriodKey,
        completedForPeriodKey: task.completedForPeriodKey,
        completedAt: task.completedAt,
        latestMeasurement: task.latestMeasurement,
      },
    })
  }).as('togglePending')

  cy.intercept('POST', '**/api/temperature-measurements', (req) => {
    if (req.body.module !== module) {
      req.continue()
      return
    }

    const checklist = state.checklists.find(
      (entry) => String(entry.id) === String(req.body.checklistId),
    )
    const task = checklist?.sections
      ?.flatMap((section) => section.items ?? [])
      .find((entry) => String(entry.id) === String(req.body.taskId))

    if (!task) {
      req.reply({ statusCode: 404, body: { message: 'Task not found' } })
      return
    }

    state.measurementSeq += 1
    const measurement = {
      id: `measurement-${state.measurementSeq}`,
      checklistId: String(req.body.checklistId),
      taskId: String(req.body.taskId),
      valueC: Number(req.body.valueC),
      measuredAt: '2026-04-05T09:15:00Z',
      periodKey: req.body.periodKey ?? checklist.activePeriodKey,
      deviation:
        (task.targetMin != null && Number(req.body.valueC) < Number(task.targetMin)) ||
        (task.targetMax != null && Number(req.body.valueC) > Number(task.targetMax)),
    }

    task.latestMeasurement = measurement
    req.reply({ statusCode: 201, body: clone(measurement) })
  }).as('createTemperatureMeasurement')

  cy.intercept('PUT', '**/api/checklists/*/submit', (req) => {
    const match = req.url.match(/\/api\/checklists\/([^/]+)\/submit$/)
    if (!match) {
      req.continue()
      return
    }

    const checklistIndex = state.checklists.findIndex(
      (entry) => String(entry.id) === String(match[1]),
    )

    if (checklistIndex < 0) {
      req.reply({ statusCode: 404, body: { message: 'Checklist not found' } })
      return
    }

    const existing = state.checklists[checklistIndex]
    state.submitSeq += 1
    const refreshed = createChecklistRecord(
      {
        ...existing,
        id: existing.id,
        activePeriodKey: `${existing.period}-submitted-${state.submitSeq}`,
        sections: existing.sections.map((section, sectionIndex) => ({
          title: section.title,
          items: (section.items ?? []).map((task, taskIndex) => ({
            ...task,
            id: `${existing.id}-submitted-${state.submitSeq}-${sectionIndex}-${taskIndex}`,
            state: 'todo',
            highlighted: false,
            completedForPeriodKey: null,
            completedAt: null,
            pendingForPeriodKey: null,
            latestMeasurement: null,
          })),
        })),
      },
      state.tasks,
    )

    if (!existing.recurring) {
      refreshed.displayedOnWorkbench = false
    }

    state.checklists.splice(checklistIndex, 1, refreshed)
    req.reply({ statusCode: 200, body: clone(refreshed) })
  }).as('submitChecklist')

  cy.wrap(state, { log: false }).as('icDashboardState')

  return moduleConfig
}

export function visitIcDashboard(module = 'IC_FOOD') {
  cy.visitAuthenticated(MODULE_CONFIG[module].path)
  cy.wait('@getChecklists')
}

export function openCreateChecklistModal() {
  cy.contains('button', 'New checklist').click()
  cy.get('[role="dialog"][aria-label="Create checklist"]').should('be.visible')
}

export function openChecklistLibrary() {
  cy.contains('button', 'Open library').click()
  cy.get('[role="dialog"][aria-label="Checklist library"]').should('be.visible')
}

export function openTaskPoolModal() {
  cy.contains('button', 'Task pool').click()
  cy.get('[role="dialog"][aria-label="Manage task pool"]').should('be.visible')
}

export function getChecklistCard(title) {
  return cy.contains('.checklist-card h2', title).closest('.checklist-card')
}

export { MODULE_CONFIG }
