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

export function getSummaryAriaLabel(totals) {
  const safe = totals ?? { completed: 0, pending: 0, todo: 0 }
  return `Completed ${safe.completed}, flagged ${safe.pending}, not started ${safe.todo}`
}

