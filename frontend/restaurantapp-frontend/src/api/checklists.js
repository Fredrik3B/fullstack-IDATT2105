import api from './axiosInstance'

/**
 * Query filters for checklist listing.
 *
 * @typedef {Object} ChecklistFilters
 * @property {string} [module] - Module key.
 * @property {string} [ifModifiedSince] - Last known Last-Modified header value.
 */

/**
 * Response shape from fetchChecklists including conditional request metadata.
 *
 * @typedef {Object} FetchChecklistsResult
 * @property {number} status - HTTP status (including 304 when not modified).
 * @property {any[]|null} data - Checklist data or null for 304 responses.
 * @property {string|null} lastModified - Last-Modified header from response when provided.
 */

/**
 * Shared task state mutation payload for checklist tasks.
 *
 * @typedef {Object} ChecklistTaskMutationPayload
 * @property {number|string} checklistId - Checklist identifier.
 * @property {number|string} taskId - Task identifier.
 * @property {string} state - Next task state value.
 * @property {string} [periodKey] - Optional period grouping key.
 */

/**
 * Shared payload for checklist create/update operations.
 *
 * @typedef {Object} ChecklistPayload
 * @property {string} module - Module key.
 * @property {string} period - Recurrence period.
 * @property {string} title - Checklist title.
 * @property {string} [subtitle] - Optional subtitle.
 * @property {boolean} recurring - Whether checklist recurs.
 * @property {boolean} displayedOnWorkbench - Workbench visibility flag.
 * @property {(number|string)[]} [taskTemplateIds] - Linked task template ids.
 */

/**
 * Fetch checklists with support for If-Modified-Since and 304 responses.
 *
 * @param {ChecklistFilters} [filters={}] - Optional query and cache headers.
 * @returns {Promise<FetchChecklistsResult>} Checklist result plus status metadata.
 * @throws {Error} Propagates request failures from the API client.
 */
export async function fetchChecklists({ module, ifModifiedSince } = {}) {
  const response = await api.get('/api/checklists', {
    params: { module },
    headers: ifModifiedSince ? { 'If-Modified-Since': ifModifiedSince } : undefined,
    validateStatus: (status) => (status >= 200 && status < 300) || status === 304,
  })

  return {
    status: response.status,
    data: response.status === 304 ? null : response.data,
    lastModified: response.headers?.['last-modified'] ?? null,
  }
}

/**
 * Set checklist task completion state.
 *
 * @param {ChecklistTaskMutationPayload & { completedAt?: string }} payload - Completion update payload.
 * @returns {Promise<any>} Updated task/checklist payload.
 * @throws {Error} Propagates request failures from the API client.
 */
export async function setTaskCompletion({ checklistId, taskId, state, periodKey, completedAt }) {
  const { data } = await api.put(`/api/checklists/${checklistId}/tasks/${taskId}/completion`, {
    state,
    periodKey,
    completedAt
  })
  return data
}

/**
 * Set checklist task flagged state.
 *
 * @param {ChecklistTaskMutationPayload} payload - Flag update payload.
 * @returns {Promise<any>} Updated task/checklist payload.
 * @throws {Error} Propagates request failures from the API client.
 */
export async function setTaskFlag({ checklistId, taskId, state, periodKey }) {
  const { data } = await api.put(`/api/checklists/${checklistId}/tasks/${taskId}/flag`, {
    state,
    periodKey
  })
  return data
}

/**
 * Create a checklist.
 *
 * @param {ChecklistPayload} payload - Checklist creation payload.
 * @returns {Promise<any>} Created checklist payload.
 * @throws {Error} Propagates request failures from the API client.
 */
export async function createChecklist({ module, period, title, subtitle, recurring, displayedOnWorkbench, taskTemplateIds }) {
  const { data } = await api.post('/api/checklists', {
    module,
    period,
    title,
    subtitle,
    recurring,
    displayedOnWorkbench,
    taskTemplateIds
  })
  return data
}

/**
 * Update a checklist.
 *
 * @param {ChecklistPayload & { checklistId: number|string }} payload - Checklist update payload with id.
 * @returns {Promise<any>} Updated checklist payload.
 * @throws {Error} Propagates request failures from the API client.
 */
export async function updateChecklist({ checklistId, period, title, subtitle, recurring, displayedOnWorkbench, taskTemplateIds }) {
  const { data } = await api.put(`/api/checklists/${checklistId}`, {
    period,
    title,
    subtitle,
    recurring,
    displayedOnWorkbench,
    taskTemplateIds
  })
  return data
}

/**
 * Submit a checklist.
 *
 * @param {{ checklistId: number|string }} payload - Checklist id wrapper.
 * @returns {Promise<any>} Submitted checklist payload.
 * @throws {Error} Propagates request failures from the API client.
 */
export async function submitChecklist({ checklistId }) {
  const { data } = await api.put(`/api/checklists/${checklistId}/submit`)
  return data
}

/**
 * Toggle checklist workbench visibility state.
 *
 * @param {{ checklistId: number|string, displayedOnWorkbench: boolean }} payload - Target checklist and visibility flag.
 * @returns {Promise<any>} Updated checklist payload.
 * @throws {Error} Propagates request failures from the API client.
 */
export async function setChecklistWorkbenchState({ checklistId, displayedOnWorkbench }) {
  const { data } = await api.put(`/api/checklists/${checklistId}/workbench`, {
    displayedOnWorkbench
  })
  return data
}

/**
 * Delete a checklist by id.
 *
 * @param {{ checklistId: number|string }} payload - Checklist id wrapper.
 * @returns {Promise<void>} Resolves when deletion succeeds.
 * @throws {Error} Propagates request failures from the API client.
 */
export async function deleteChecklist({ checklistId }) {
  await api.delete(`/api/checklists/${checklistId}`)
}
