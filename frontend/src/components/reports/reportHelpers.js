/**
 * Format a date value as localized date-only text.
 *
 * @param {string|Date|null|undefined} dateStr
 * @returns {string}
 */
export function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('en-GB', { day: 'numeric', month: 'long', year: 'numeric' })
}

/**
 * Format a date value as localized date and time text.
 *
 * @param {string|Date|null|undefined} dateStr
 * @returns {string}
 */
export function formatDateTime(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('en-GB', {
    day: 'numeric', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

/**
 * Format a numeric rate with one decimal precision.
 *
 * @param {number|string|null|undefined} rate
 * @returns {string}
 */
export function formatRate(rate) {
  if (rate == null || isNaN(rate)) return '0'
  return Number(rate).toFixed(1)
}

/**
 * Resolve CSS class for compliance rate styling.
 *
 * @param {number} rate
 * @returns {''|'stat-ok'|'stat-warn'}
 */
export function rateClass(rate) {
  if (rate >= 90) return 'stat-ok'
  if (rate > 0) return 'stat-warn'
  return ''
}

/**
 * Format optional target temperature range for chart/report points.
 *
 * @param {{targetMin?: number|null, targetMax?: number|null}|null|undefined} point
 * @returns {string}
 */
export function formatTemperatureRange(point) {
  if (!point) return ''
  if (point.targetMin != null && point.targetMax != null) return `${point.targetMin}°C - ${point.targetMax}°C`
  if (point.targetMin != null) return `>= ${point.targetMin}°C`
  if (point.targetMax != null) return `<= ${point.targetMax}°C`
  return ''
}
