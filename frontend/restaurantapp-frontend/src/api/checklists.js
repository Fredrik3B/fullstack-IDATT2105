import api from './axiosInstance'

/**
 * Checklist backend contract (suggested)
 *
 * Goal:
 * - Same frontend logic for IC-Food and IC-Alcohol.
 * - Recurring tasks that "reset" automatically when the period changes (daily/weekly/monthly).
 *
 * Data model (per checklist card):
 * - id: string (checklistId)
 * - module: 'IC_FOOD' | 'IC_ALCOHOL' (or similar)
 * - period: 'daily' | 'weekly' | 'monthly'
 * - title/subtitle/featured/moduleChip/sections...
 *
 * Data model (per task):
 * - id: string (taskId)
 * - label/meta
 * - state: 'todo' | 'pending' | 'completed'
 * - highlighted?: boolean
 * - completedForPeriodKey?: string  (e.g. 2026-03-28, 2026-W13, 2026-03)
 * - completedAt?: string (ISO timestamp)
 * - pendingForPeriodKey?: string   (optional; if you want pending flags to reset/sync per period)
 *
 * Period keys:
 * - daily:   YYYY-MM-DD (local date)
 * - weekly:  ISO week key YYYY-Www (Monday-based)
 * - monthly: YYYY-MM
 *
 * Endpoints (suggested):
 * - GET /api/checklists?module=IC_FOOD
 *     -> returns ChecklistCard[]
 * - POST /api/checklists
 *     body: { module, period, title, subtitle?, sections: [{ title, items: [{ label, meta? }] }] }
 *     -> returns the created ChecklistCard with ids for checklist/sections/tasks
 * - PUT /api/checklists/{checklistId}/tasks/{taskId}/completion
 *     body: { state: 'completed'|'todo', periodKey: string, completedAt?: string }
 * - PUT /api/checklists/{checklistId}/tasks/{taskId}/flag
 *     body: { state: 'pending'|'todo', periodKey: string, flaggedAt?: string }
 */

export async function fetchChecklists({ module }) {
  const { data } = await api.get('/api/checklists', { params: { module } })
  return data
}

export async function setTaskCompletion({ checklistId, taskId, state, periodKey, completedAt }) {
  const { data } = await api.put(`/api/checklists/${checklistId}/tasks/${taskId}/completion`, {
    state,
    periodKey,
    completedAt
  })
  return data
}

export async function setTaskFlag({ checklistId, taskId, state, periodKey, flaggedAt }) {
  const { data } = await api.put(`/api/checklists/${checklistId}/tasks/${taskId}/flag`, {
    state,
    periodKey,
    flaggedAt
  })
  return data
}

export async function createChecklist({ module, period, title, subtitle, sections }) {
  const { data } = await api.post('/api/checklists', {
    module,
    period,
    title,
    subtitle,
    sections
  })
  return data
}

export async function updateChecklist({ checklistId, period, title, subtitle, sections }) {
  const { data } = await api.put(`/api/checklists/${checklistId}`, {
    period,
    title,
    subtitle,
    sections
  })
  return data
}
