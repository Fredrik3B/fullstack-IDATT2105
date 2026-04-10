/**
 * Recalculate derived progress/status fields for a checklist card.
 *
 * Mutates the input card in place.
 *
 * @param {{sections?: Array<{items?: Array<{state?: string}>}>, progress?: number|null, statusTone?: string, statusLabel?: string}|null|undefined} card
 * @returns {void}
 */
export function recalcCardProgress(card) {
  if (!card) return

  const sections = Array.isArray(card.sections) ? card.sections : []
  const total = sections.reduce(
    (acc, section) => acc + (Array.isArray(section.items) ? section.items.length : 0),
    0
  )
  const completed = sections.reduce((acc, section) => {
    if (!Array.isArray(section.items)) return acc
    return acc + section.items.filter((task) => task.state === 'completed').length
  }, 0)

  if (total <= 0) {
    card.progress = null
    card.statusTone = 'muted'
    return
  }

  card.statusTone = completed === 0 ? 'muted' : completed === total ? 'success' : 'warning'
  card.progress = Math.round((completed / total) * 100)

  const lower = String(card.statusLabel ?? '').toLowerCase()
  const doneWord = lower.includes('fullfort') ? 'fullfort' : 'completed'
  card.statusLabel =
    completed === total
      ? doneWord === 'fullfort'
        ? 'Fullfort'
        : 'Completed'
      : `${completed}/${total} ${doneWord}`
}

