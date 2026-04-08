import { ref } from 'vue'
import { defineStore } from 'pinia'

/**
 * @typedef {'success'|'error'|'warning'|'info'} ToastType
 */

/**
 * @typedef {Object} ToastInput
 * @property {ToastType} type - Toast variant.
 * @property {string} message - Message shown to the user.
 * @property {number} [duration] - Auto-dismiss duration in ms. `0` means sticky.
 */

/**
 * @typedef {Object} ToastItem
 * @property {number} id - Generated local identifier.
 * @property {ToastType} type - Toast variant.
 * @property {string} message - Rendered message.
 * @property {number} duration - Active timeout in ms. `0` means sticky.
 */

let nextId = 1

/**
 * Toast store for transient user notifications.
 */
export const useToastStore = defineStore('toast', () => {
  /** @type {import('vue').Ref<ToastItem[]>} */
  const toasts = ref([])

  /**
   * Add a toast notification.
   *
   * Default duration is sticky for errors and 4s for other types.
   *
   * @param {ToastInput} param0 - Toast payload.
   * @returns {void}
   */
  function add({ type, message, duration = type === 'error' ? 0 : 4000 }) {
    const id = nextId++
    toasts.value.push({ id, type, message, duration })

    if (duration > 0) {
      setTimeout(() => remove(id), duration)
    }
  }

  /**
   * Remove a toast by id.
   *
   * @param {number} id - Toast identifier.
   * @returns {void}
   */
  function remove(id) {
    toasts.value = toasts.value.filter(t => t.id !== id)
  }

  /** @param {string} message @returns {void} */
  function success(message) { add({ type: 'success', message }) }
  /** @param {string} message @returns {void} */
  function error(message)   { add({ type: 'error',   message }) }
  /** @param {string} message @returns {void} */
  function warning(message) { add({ type: 'warning', message, duration: 6000 }) }
  /** @param {string} message @returns {void} */
  function info(message)    { add({ type: 'info',    message }) }

  return { toasts, add, remove, success, error, warning, info }
})
