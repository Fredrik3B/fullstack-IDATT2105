import api from './axiosInstance'

/**
 * Shared payload for temperature zone creation and updates.
 *
 * @typedef {Object} TemperatureZonePayload
 * @property {string} module - Module key owning the zone.
 * @property {string} name - Zone display name.
 * @property {string} zoneType - Zone classification used by the backend.
 * @property {number|null} [targetMin] - Optional lower bound in Celsius.
 * @property {number|null} [targetMax] - Optional upper bound in Celsius.
 */

/**
 * Fetch temperature zones for a module.
 *
 * @param {{ module: string }} params - Required module filter.
 * @returns {Promise<any[]>} Temperature zone list.
 * @throws {Error} Propagates request failures from the API client.
 */
export async function fetchTemperatureZones({ module }) {
  const { data } = await api.get('/api/temperature-zones', { params: { module } })
  return data
}

/**
 * Create a temperature zone.
 *
 * @param {TemperatureZonePayload} payload - Zone creation payload.
 * @returns {Promise<any>} Created temperature zone payload.
 * @throws {Error} Propagates request failures from the API client.
 */
export async function createTemperatureZone({ module, name, zoneType, targetMin, targetMax }) {
  const { data } = await api.post('/api/temperature-zones', {
    module,
    name,
    zoneType,
    targetMin,
    targetMax,
  })
  return data
}

/**
 * Update a temperature zone.
 *
 * @param {TemperatureZonePayload & { zoneId: number|string }} payload - Zone update payload with id.
 * @returns {Promise<any>} Updated temperature zone payload.
 * @throws {Error} Propagates request failures from the API client.
 */
export async function updateTemperatureZone({ zoneId, module, name, zoneType, targetMin, targetMax }) {
  const { data } = await api.put(`/api/temperature-zones/${zoneId}`, {
    module,
    name,
    zoneType,
    targetMin,
    targetMax,
  })
  return data
}

/**
 * Delete a temperature zone by id.
 *
 * @param {number|string} zoneId - Temperature zone identifier.
 * @returns {Promise<void>} Resolves when deletion succeeds.
 * @throws {Error} Propagates request failures from the API client.
 */
export async function deleteTemperatureZone(zoneId) {
  await api.delete(`/api/temperature-zones/${zoneId}`)
}
