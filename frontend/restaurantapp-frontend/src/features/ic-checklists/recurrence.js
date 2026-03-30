const PERIOD_ENUM_BY_LABEL = {
  Daily: 'daily',
  Weekly: 'weekly',
  Monthly: 'monthly'
}

export function normalizePeriodEnum(periodLike) {
  const raw = String(periodLike ?? '').trim()
  if (!raw) return null

  if (PERIOD_ENUM_BY_LABEL[raw]) return PERIOD_ENUM_BY_LABEL[raw]

  const lower = raw.toLowerCase()
  if (lower.startsWith('day')) return 'daily'
  if (lower.startsWith('week')) return 'weekly'
  if (lower.startsWith('month')) return 'monthly'
  if (lower === 'daily' || lower === 'weekly' || lower === 'monthly') return lower

  return null
}

export function periodEnumToLabel(periodEnum) {
  const normalized = normalizePeriodEnum(periodEnum)
  if (normalized === 'weekly') return 'Weekly'
  if (normalized === 'monthly') return 'Monthly'
  return 'Daily'
}

function pad2(value) {
  return String(value).padStart(2, '0')
}

function toLocalDateParts(date) {
  const yyyy = date.getFullYear()
  const mm = pad2(date.getMonth() + 1)
  const dd = pad2(date.getDate())
  return { yyyy, mm, dd }
}

// ISO week (Monday-based), derived in UTC to avoid DST edge cases.
function getISOWeekYearAndNumber(date) {
  const utc = new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()))
  const day = utc.getUTCDay() || 7 // Sun=7
  utc.setUTCDate(utc.getUTCDate() + 4 - day) // nearest Thursday
  const isoYear = utc.getUTCFullYear()
  const yearStart = new Date(Date.UTC(isoYear, 0, 1))
  const week = Math.ceil(((utc - yearStart) / 86400000 + 1) / 7)
  return { isoYear, week }
}

export function getPeriodKey(periodEnumLike, date = new Date()) {
  const periodEnum = normalizePeriodEnum(periodEnumLike) ?? 'daily'

  if (periodEnum === 'weekly') {
    const { isoYear, week } = getISOWeekYearAndNumber(date)
    return `${isoYear}-W${pad2(week)}`
  }

  if (periodEnum === 'monthly') {
    const { yyyy, mm } = toLocalDateParts(date)
    return `${yyyy}-${mm}`
  }

  const { yyyy, mm, dd } = toLocalDateParts(date)
  return `${yyyy}-${mm}-${dd}`
}

export function getCardPeriodEnum(card) {
  if (!card || typeof card !== 'object') return null

  const direct = normalizePeriodEnum(card.period ?? card.recurrence ?? card.repeat)
  if (direct) return direct

  const subtitle = String(card.subtitle ?? '').toLowerCase()
  if (subtitle.includes('daily')) return 'daily'
  if (subtitle.includes('weekly')) return 'weekly'
  if (subtitle.includes('monthly')) return 'monthly'

  return null
}

export function seedCompletionMetaForCurrentPeriod(cards, now = new Date()) {
  if (!Array.isArray(cards)) return

  cards.forEach((card) => {
    const cardPeriodEnum = getCardPeriodEnum(card) ?? 'daily'
    const periodKey = getPeriodKey(cardPeriodEnum, now)
    const sections = Array.isArray(card.sections) ? card.sections : []
    sections.forEach((section) => {
      const items = Array.isArray(section.items) ? section.items : []
      items.forEach((task) => {
        if (!task || typeof task !== 'object') return
        if (task.state !== 'completed') return
        if (!task.completedForPeriodKey) task.completedForPeriodKey = periodKey
        if (!task.completedAt) task.completedAt = now.toISOString()
      })
    })
  })
}

function getEffectiveTaskState(task, currentPeriodKey) {
  if (!task || typeof task !== 'object') return task

  if (task.state === 'completed') {
    if (task.completedForPeriodKey && task.completedForPeriodKey !== currentPeriodKey) {
      return { ...task, state: 'todo', highlighted: false }
    }
  }

  if (task.state === 'pending') {
    if (task.pendingForPeriodKey && task.pendingForPeriodKey !== currentPeriodKey) {
      return { ...task, state: 'todo', highlighted: false }
    }
  }

  return task
}

export function deriveDisplayCards(cards, { activePeriodLabel, now = new Date() } = {}) {
  const sourceCards = Array.isArray(cards) ? cards : []
  const activeEnum = normalizePeriodEnum(activePeriodLabel)

  return sourceCards
    .map((card, sourceIndex) => {
      const cardPeriodEnum = getCardPeriodEnum(card) ?? 'daily'
      const currentPeriodKey = getPeriodKey(cardPeriodEnum, now)

      const sections = Array.isArray(card.sections) ? card.sections : []
      const displaySections = sections.map((section) => {
        const items = Array.isArray(section.items) ? section.items : []
        return {
          ...section,
          items: items.map((task) => getEffectiveTaskState(task, currentPeriodKey))
        }
      })

      return {
        ...card,
        __sourceIndex: sourceIndex,
        sections: displaySections
      }
    })
    .filter((card) => {
      if (!activeEnum) return true
      const cardEnum = getCardPeriodEnum(card)
      if (!cardEnum) return true
      return cardEnum === activeEnum
    })
}
