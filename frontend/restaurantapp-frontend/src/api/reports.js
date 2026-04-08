import api from './axiosInstance'

/**
 * Date range filters for report endpoints.
 *
 * @typedef {Object} ReportDateRange
 * @property {string} [from] - Inclusive start date/time in backend-accepted format.
 * @property {string} [to] - Inclusive end date/time in backend-accepted format.
 */

/**
 * Fetch the full inspection report payload.
 *
 * @param {ReportDateRange} [range={}] - Optional date range filter.
 * @returns {Promise<any>} Full report payload from the backend.
 * @throws {Error} Propagates request failures from the API client.
 */
export async function fetchInspectionReport({ from, to } = {}) {
  const params = {}
  if (from) params.from = from
  if (to) params.to = to
  const { data } = await api.get('/api/reports/full-report', { params })
  return data
}

/**
 * Fetch summarized report metrics.
 *
 * @param {ReportDateRange} [range={}] - Optional date range filter.
 * @returns {Promise<any>} Summary payload from the backend.
 * @throws {Error} Propagates request failures from the API client.
 */
export async function fetchSummaryReport({ from, to } = {}) {
  const params = {}
  if (from) params.from = from
  if (to) params.to = to
  const { data } = await api.get('/api/reports/summary', { params })
  return data
}

/**
 * Create a deviation report entry.
 *
 * @param {Record<string, any>} report - Deviation report payload.
 * @returns {Promise<any>} Created deviation report payload.
 * @throws {Error} Propagates request failures from the API client.
 */
export async function createDeviationReport(report) {
  const { data } = await api.post('/api/reports/deviations', report)
  return data
}
