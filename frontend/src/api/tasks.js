import api from './axiosInstance'

/**
 * Shared payload for task creation and updates.
 *
 * @typedef {Object} TaskPayload
 * @property {string} module - Module key owning the task.
 * @property {string} title - Display title.
 * @property {string|null} [meta] - Optional metadata/description.
 * @property {string} [sectionType] - Optional section grouping value.
 * @property {number|string|null} [temperatureZoneId] - Related zone id when task is temperature-based.
 * @property {number|null} [targetMin] - Optional lower target threshold.
 * @property {number|null} [targetMax] - Optional upper target threshold.
 */

/**
 * Fetch tasks for a module.
 *
 * @param {{ module: string }} params - Required module filter.
 * @returns {Promise<any[]>} Task list from the backend.
 * @throws {Error} Propagates request failures from the API client.
 */
export async function fetchTasks({ module }) {
  const { data } = await api.get('/api/tasks', { params: { module } })
  return data
}

/**
 * Create a new task.
 *
 * @param {TaskPayload} payload - Task creation payload.
 * @returns {Promise<any>} Created task payload.
 * @throws {Error} Propagates request failures from the API client.
 */
export async function createTask({ module, title, meta, sectionType, temperatureZoneId, targetMin, targetMax }) {
  const { data } = await api.post('/api/tasks', {
    module,
    title,
    meta,
    sectionType,
    temperatureZoneId,
    targetMin,
    targetMax,
  })
  return data
}

/**
 * Update an existing task.
 *
 * @param {TaskPayload & { taskId: number|string }} payload - Task update payload with id.
 * @returns {Promise<any>} Updated task payload.
 * @throws {Error} Propagates request failures from the API client.
 */
export async function updateTask({ taskId, module, title, meta, sectionType, temperatureZoneId, targetMin, targetMax }) {
  const { data } = await api.put(`/api/tasks/${taskId}`, {
    module,
    title,
    meta,
    sectionType,
    temperatureZoneId,
    targetMin,
    targetMax,
  })
  return data
}

/**
 * Delete a task by id.
 *
 * @param {number|string} taskId - Task identifier.
 * @returns {Promise<void>} Resolves when deletion succeeds.
 * @throws {Error} Propagates request failures from the API client.
 */
export async function deleteTask(taskId) {
  await api.delete(`/api/tasks/${taskId}`)
}
