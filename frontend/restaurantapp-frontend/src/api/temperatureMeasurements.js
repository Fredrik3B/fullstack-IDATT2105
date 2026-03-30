import api from './axiosInstance'

/**
 * Temperature measurements backend contract (suggested)
 *
 * Why this is separate from checklist task "state":
 * - `pending/flag` in this UI means "critical task that should be done", not a deviation log.
 * - Temperature should be logged as measurements (a time series), and deviations should be derived
 *   by comparing logged values to targets on the task.
 *
 * Suggested data model (measurement):
 * - id: string
 * - module: 'IC_FOOD' | 'IC_ALCOHOL'
 * - checklistId: string
 * - taskId: string
 * - valueC: number
 * - measuredAt: string (ISO timestamp)
 * - periodKey: string (same periodKey scheme as checklists: YYYY-MM-DD / YYYY-Www / YYYY-MM)
 *
 * Suggested endpoints:
 * - POST /api/temperature-measurements
 *     body: { module, checklistId, taskId, valueC, measuredAt, periodKey }
 *     -> returns created measurement (with id)
 * - GET /api/temperature-measurements?module=IC_FOOD&from=...&to=...
 *     -> returns Measurement[]
 *
 * Notes:
 * - Keep checklist tasks declarative: targets live on the task (min/max), measurements live here.
 * - Backend should validate module/checklist/task relationships and store authoritative timestamps.
 */

export async function createTemperatureMeasurement({ module, checklistId, taskId, valueC, measuredAt, periodKey }) {
  const { data } = await api.post('/api/temperature-measurements', {
    module,
    checklistId,
    taskId,
    valueC,
    measuredAt,
    periodKey
  })
  return data
}

export async function fetchTemperatureMeasurements({ module, from, to } = {}) {
  const { data } = await api.get('/api/temperature-measurements', { params: { module, from, to } })
  return data
}

