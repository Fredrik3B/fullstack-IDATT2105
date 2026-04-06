export function isTemperatureTask(task) {
  return task && typeof task === 'object' && task.type === 'temperature'
}

export function formatTemperatureTarget(task) {
  if (!isTemperatureTask(task)) return ''

  const unit = String(task.unit ?? 'C')
  const min = Number.isFinite(task.targetMin) ? task.targetMin : null
  const max = Number.isFinite(task.targetMax) ? task.targetMax : null

  if (min !== null && max !== null) return `${min}-${max} ${unit}`
  if (min !== null) return `≥ ${min} ${unit}`
  if (max !== null) return `≤ ${max} ${unit}`
  return ''
}

export function isTemperatureDeviation(task, valueC) {
  if (!isTemperatureTask(task)) return false
  if (!Number.isFinite(valueC)) return false

  const min = Number.isFinite(task.targetMin) ? task.targetMin : null
  const max = Number.isFinite(task.targetMax) ? task.targetMax : null

  if (min !== null && valueC < min) return true
  if (max !== null && valueC > max) return true
  return false
}

