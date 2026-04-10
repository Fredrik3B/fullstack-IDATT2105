/**
 * @typedef {Object} ChecklistSummaryTotals
 * @property {number} completed
 * @property {number} pending
 * @property {number} todo
 * @property {number} total
 * @property {number} completedPct
 * @property {number} pendingPct
 * @property {number} todoPct
 */

/**
 * Count task states across checklist cards and compute percentages.
 *
 * @param {Array<{sections?: Array<{items?: Array<{state?: string}>}>}>} cards
 * @returns {ChecklistSummaryTotals}
 */
export function countTaskStates(cards) {
  const safeCards = Array.isArray(cards) ? cards : []
  let completed = 0
  let pending = 0
  let todo = 0

  safeCards.forEach((card) => {
    const sections = Array.isArray(card?.sections) ? card.sections : []
    sections.forEach((section) => {
      const items = Array.isArray(section?.items) ? section.items : []
      items.forEach((task) => {
        const state = String(task?.state ?? 'todo')
        if (state === 'completed') completed += 1
        else if (state === 'pending') pending += 1
        else todo += 1
      })
    })
  })

  const total = completed + pending + todo
  const pct = (value) => (total ? Math.round((value / total) * 100) : 0)

  return {
    completed,
    pending,
    todo,
    total,
    completedPct: pct(completed),
    pendingPct: pct(pending),
    todoPct: pct(todo)
  }
}

/**
 * Build an accessible text summary of checklist totals.
 *
 * @param {{completed?: number, pending?: number, todo?: number}|null|undefined} totals
 * @returns {string}
 */
export function getSummaryAriaLabel(totals) {
  const safe = totals ?? { completed: 0, pending: 0, todo: 0 }
  return `Completed ${safe.completed}, flagged ${safe.pending}, not started ${safe.todo}`
}

