export function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('en-GB', { day: 'numeric', month: 'long', year: 'numeric' })
}

export function formatDateTime(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('en-GB', {
    day: 'numeric', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

export function formatRate(rate) {
  if (rate == null || isNaN(rate)) return '0'
  return Number(rate).toFixed(1)
}

export function rateClass(rate) {
  if (rate >= 90) return 'stat-ok'
  if (rate > 0) return 'stat-warn'
  return ''
}

export function formatTemperatureRange(point) {
  if (!point) return ''
  if (point.targetMin != null && point.targetMax != null) return `${point.targetMin}°C - ${point.targetMax}°C`
  if (point.targetMin != null) return `>= ${point.targetMin}°C`
  if (point.targetMax != null) return `<= ${point.targetMax}°C`
  return ''
}
