import api from './axiosInstance'

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

export async function setTaskCompletion({ checklistId, taskId, state, periodKey, completedAt }) {
  const { data } = await api.put(`/api/checklists/${checklistId}/tasks/${taskId}/completion`, {
    state,
    periodKey,
    completedAt
  })
  return data
}

export async function setTaskFlag({ checklistId, taskId, state, periodKey }) {
  const { data } = await api.put(`/api/checklists/${checklistId}/tasks/${taskId}/flag`, {
    state,
    periodKey
  })
  return data
}

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

export async function submitChecklist({ checklistId }) {
  const { data } = await api.put(`/api/checklists/${checklistId}/submit`)
  return data
}

export async function setChecklistWorkbenchState({ checklistId, displayedOnWorkbench }) {
  const { data } = await api.put(`/api/checklists/${checklistId}/workbench`, {
    displayedOnWorkbench
  })
  return data
}

export async function deleteChecklist({ checklistId }) {
  await api.delete(`/api/checklists/${checklistId}`)
}
