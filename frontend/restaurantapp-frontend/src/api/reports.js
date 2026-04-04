import api from './axiosInstance'

export async function fetchInspectionReport({ from, to } = {}) {
  const params = {}
  if (from) params.from = from
  if (to) params.to = to
  const { data } = await api.get('/api/reports/full-report', { params })
  return data
}

export async function fetchSummaryReport({ from, to } = {}) {
  const params = {}
  if (from) params.from = from
  if (to) params.to = to
  const { data } = await api.get('/api/reports/summary', { params })
  return data
}
