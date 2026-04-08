/**
 * Determine whether a task uses temperature semantics.
 *
 * @param {any} task
 * @returns {boolean}
 */
export function isTemperatureTask(task) {
  if (!task || typeof task !== 'object') return false

  return (
    task.type === 'temperature' ||
    task.sectionType === 'TEMPERATURE_CONTROL' ||
    task.temperatureZoneId != null ||
    task.targetMin != null ||
    task.targetMax != null
  )
}

export function hasTemperatureMeasurementForPeriod(task, periodKey) {
  if (!isTemperatureTask(task)) return true

  const latest = task?.latestMeasurement
  if (!latest) return false
  if (!latest.periodKey) return true

  return String(latest.periodKey) === String(periodKey ?? '')
}


/**
 * Format configured temperature range/threshold for UI display.
 *
 * @param {any} task
 * @returns {string}
 */
export function formatTemperatureTarget(task) {
  if (!isTemperatureTask(task)) return ''

  const unit = String(task.unit ?? 'C')
  const min = Number.isFinite(task.targetMin) ? task.targetMin : null
  const max = Number.isFinite(task.targetMax) ? task.targetMax : null

  if (min !== null && max !== null) return `${min}-${max} ${unit}`
  if (min !== null) return `>= ${min} ${unit}`
  if (max !== null) return `<= ${max} ${unit}`
  return ''
}

/**
 * Check whether a measured value is outside configured task bounds.
 *
 * @param {any} task
 * @param {number} valueC
 * @returns {boolean}
 */
export function isTemperatureDeviation(task, valueC) {
  if (!isTemperatureTask(task)) return false
  if (!Number.isFinite(valueC)) return false

  const min = Number.isFinite(task.targetMin) ? task.targetMin : null
  const max = Number.isFinite(task.targetMax) ? task.targetMax : null

  if (min !== null && valueC < min) return true
  if (max !== null && valueC > max) return true
  return false
}
