import api from './axiosInstance'

export async function fetchTemperatureZones({ module }) {
  const { data } = await api.get('/api/temperature-zones', { params: { module } })
  return data
}

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

export async function deleteTemperatureZone(zoneId) {
  await api.delete(`/api/temperature-zones/${zoneId}`)
}
