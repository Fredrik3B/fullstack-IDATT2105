import { computed, ref } from 'vue'
import { useToast } from '@/composables/useToast'

// Backend integration toggle:
// - Keep this `false` until the backend endpoints described in `src/api/temperatureMeasurements.js` exist.
// - When backend is ready, set to `true` and replace the localStorage writes with API reads/writes.
const USE_BACKEND = true

import { createTemperatureMeasurement, fetchTemperatureMeasurements } from '../../api/temperatureMeasurements'

function storageKey(module) {
  return `ic.temperatureMeasurements.v1.${String(module ?? 'UNKNOWN')}`
}

function hasLocalStorage() {
  return typeof globalThis !== 'undefined' && typeof globalThis.localStorage !== 'undefined'
}

function safeParse(json) {
  try {
    return JSON.parse(json)
  } catch {
    return null
  }
}

function normalizeMeasurement(raw) {
  if (!raw || typeof raw !== 'object') return null
  const valueC = Number(raw.valueC)
  if (!Number.isFinite(valueC)) return null

  return {
    id: raw.id != null ? String(raw.id) : `${raw.taskId ?? 'task'}-${raw.measuredAt ?? Date.now()}`,
    module: raw.module ?? null,
    checklistId: raw.checklistId != null ? String(raw.checklistId) : null,
    taskId: raw.taskId != null ? String(raw.taskId) : null,
    valueC,
    measuredAt: raw.measuredAt ?? toBackendDateTime(new Date()),
    periodKey: raw.periodKey ?? null
  }
}

function toBackendDateTime(value) {
  const date = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(date.getTime())) return new Date().toISOString().slice(0, 19)
  // Backend expects LocalDateTime-like payloads without timezone suffix.
  return date.toISOString().slice(0, 19)
}

export function useTemperatureLog({ module }) {
  const toast = useToast()
  const measurements = ref([])

  if (USE_BACKEND) {
    const to = new Date()
    const from = new Date(Date.now() - 90 * 24 * 60 * 60 * 1000)
    void fetchTemperatureMeasurements({ module, from: toBackendDateTime(from), to: toBackendDateTime(to) })
      .then((rows) => {
        const parsed = Array.isArray(rows) ? rows.map(normalizeMeasurement).filter(Boolean) : []
        measurements.value = parsed
      })
      .catch((err) => {
        console.error('Failed to fetch temperature measurements', err)
        measurements.value = []
      })
  } else {
    const raw = hasLocalStorage() ? safeParse(localStorage.getItem(storageKey(module)) ?? '[]') : []
    const parsed = Array.isArray(raw) ? raw.map(normalizeMeasurement).filter(Boolean) : []
    measurements.value = parsed
  }

  const latestByTaskId = computed(() => {
    const map = new Map()
    measurements.value.forEach((entry) => {
      const taskId = entry?.taskId
      if (!taskId) return
      const previous = map.get(taskId)
      if (!previous) {
        map.set(taskId, entry)
        return
      }
      if (String(entry.measuredAt) > String(previous.measuredAt)) map.set(taskId, entry)
    })
    return map
  })

  async function logTemperature({ checklistId, taskId, valueC, measuredAt, periodKey }) {
    const entry = normalizeMeasurement({
      module,
      checklistId,
      taskId,
      valueC,
      measuredAt: measuredAt ?? toBackendDateTime(new Date()),
      periodKey
    })
    if (!entry) return null

    measurements.value = [entry, ...measurements.value].slice(0, 250)

    if (USE_BACKEND) {
      try {
        const created = await createTemperatureMeasurement(entry)
        const normalized = normalizeMeasurement(created)
        if (normalized) {
          measurements.value = [normalized, ...measurements.value.filter((m) => m.id !== entry.id)].slice(0, 250)
          toast.success('Temperature reading saved.')
          return normalized
        }
      } catch (err) {
        console.error('Failed to persist temperature measurement', err)
        measurements.value = measurements.value.filter((m) => m.id !== entry.id)
        toast.error(err?.response?.data?.detail ?? err?.response?.data?.message ?? 'Could not save temperature reading.')
        return null
      }
      return entry
    }

    if (hasLocalStorage()) localStorage.setItem(storageKey(module), JSON.stringify(measurements.value))
    return entry
  }

  return {
    measurements,
    latestByTaskId,
    logTemperature
  }
}
