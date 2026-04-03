import api from './axiosInstance'

export async function fetchDocuments({ category, module } = {}) {
  const params = {}
  if (category) params.category = category
  if (module) params.module = module
  const { data } = await api.get('/api/documents', { params })
  return data
}

export async function uploadDocument(formData) {
  const { data } = await api.post('/api/documents', formData, {
    headers: { 'Content-Type': undefined },
  })
  return data
}

export async function downloadDocument(id) {
  const response = await api.get(`/api/documents/${id}/download`, {
    responseType: 'blob',
  })
  return response
}

export async function deleteDocument(id) {
  await api.delete(`/api/documents/${id}`)
}
