import { computed, ref } from 'vue'

// Backend integration toggle:
// - Keep this `false` until the backend endpoints described in `src/api/temperatureMeasurements.js` exist.
// - When backend is ready, set to `true` and replace the localStorage writes with API reads/writes.
const USE_BACKEND = false

// import { createTemperatureMeasurement, fetchTemperatureMeasurements } from '../../../api/temperatureMeasurements'

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
    id: raw.id ?? `${raw.taskId ?? 'task'}-${raw.measuredAt ?? Date.now()}`,
    module: raw.module ?? null,
    checklistId: raw.checklistId ?? null,
    taskId: raw.taskId ?? null,
    valueC,
    measuredAt: raw.measuredAt ?? new Date().toISOString(),
    periodKey: raw.periodKey ?? null
  }
}

export function useTemperatureLog({ module }) {
  const measurements = ref([])

  // Frontend-only bootstrapping from localStorage (until backend exists).
  if (!USE_BACKEND) {
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
      measuredAt: measuredAt ?? new Date().toISOString(),
      periodKey
    })
    if (!entry) return null

    // Frontend behavior:
    // - Optimistically show the entry immediately.
    // Backend behavior (when enabled):
    // - Persist the measurement and return the created record with a server-generated id.
    measurements.value = [entry, ...measurements.value].slice(0, 250)

    if (USE_BACKEND) {
      // TODO (backend):
      // const created = await createTemperatureMeasurement(entry)
      // measurements.value = [created, ...measurements.value.filter((m) => m.id !== entry.id)]
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
