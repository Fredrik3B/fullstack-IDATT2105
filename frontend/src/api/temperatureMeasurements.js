import api from './axiosInstance'

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
