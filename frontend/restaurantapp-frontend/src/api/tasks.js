import api from './axiosInstance'

export async function fetchTasks({ module }) {
  const { data } = await api.get('/api/tasks', { params: { module } })
  return data
}

export async function createTask({ module, title, meta, sectionType, targetMin, targetMax }) {
  const { data } = await api.post('/api/tasks', {
    module,
    title,
    meta,
    sectionType,
    targetMin,
    targetMax
  })
  return data
}

export async function deleteTask(taskId) {
  await api.delete(`/api/tasks/${taskId}`)
}
