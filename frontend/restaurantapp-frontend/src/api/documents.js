import api from './axiosInstance'

/**
 * Query filters for listing documents.
 *
 * @typedef {Object} DocumentFilters
 * @property {string} [category] - Document category.
 * @property {string} [module] - Module key used by the backend.
 */

/**
 * Fetch documents with optional filters.
 *
 * @param {DocumentFilters} [filters={}] - Optional category/module filters.
 * @returns {Promise<any[]>} Resolved document list from the backend.
 * @throws {Error} Propagates request failures from the API client.
 */
export async function fetchDocuments({ category, module } = {}) {
  const params = {}
  if (category) params.category = category
  if (module) params.module = module
  const { data } = await api.get('/api/documents', { params })
  return data
}

/**
 * Upload a document using multipart form data.
 *
 * @param {FormData} formData - Multipart payload, including file and metadata.
 * @returns {Promise<any>} Created document payload from the backend.
 * @throws {Error} Propagates request failures from the API client.
 */
export async function uploadDocument(formData) {
  const { data } = await api.post('/api/documents', formData, {
    headers: { 'Content-Type': undefined },
    timeout: 120000,
  })
  return data
}

/**
 * Download a document file as binary content.
 *
 * @param {number|string} id - Document identifier.
 * @returns {Promise<import('axios').AxiosResponse<Blob>>} Axios response with blob payload.
 * @throws {Error} Propagates request failures from the API client.
 */
export async function downloadDocument(id) {
  const response = await api.get(`/api/documents/${id}/download`, {
    responseType: 'blob',
  })
  return response
}

/**
 * Delete a document by id.
 *
 * @param {number|string} id - Document identifier.
 * @returns {Promise<void>} Resolves when deletion succeeds.
 * @throws {Error} Propagates request failures from the API client.
 */
export async function deleteDocument(id) {
  await api.delete(`/api/documents/${id}`)
}
