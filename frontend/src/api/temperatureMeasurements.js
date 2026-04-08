import api from './axiosInstance'

/**
 * Payload for creating a temperature measurement.
 *
 * @typedef {Object} TemperatureMeasurementCreatePayload
 * @property {string} module - Module key owning the measurement.
 * @property {number|string} checklistId - Checklist identifier.
 * @property {number|string} taskId - Task identifier.
 * @property {number} valueC - Measured value in Celsius.
 * @property {string} measuredAt - ISO date/time when measurement was taken.
 * @property {string} [periodKey] - Optional period grouping key.
 */

/**
 * Date/module filters for querying measurements.
 *
 * @typedef {Object} TemperatureMeasurementFilters
 * @property {string} [module] - Module key.
 * @property {string} [from] - Inclusive start date/time in backend-accepted format.
 * @property {string} [to] - Inclusive end date/time in backend-accepted format.
 */

/**
 * Create a temperature measurement entry.
 *
 * @param {TemperatureMeasurementCreatePayload} payload - Create payload.
 * @returns {Promise<any>} Created measurement payload.
 * @throws {Error} Propagates request failures from the API client.
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

/**
 * Fetch temperature measurements with optional filters.
 *
 * @param {TemperatureMeasurementFilters} [filters={}] - Query filters.
 * @returns {Promise<any[]>} Matching measurements from the backend.
 * @throws {Error} Propagates request failures from the API client.
 */
export async function fetchTemperatureMeasurements({ module, from, to } = {}) {
  const { data } = await api.get('/api/temperature-measurements', { params: { module, from, to } })
  return data
}
