import { ref } from 'vue'
import { defineStore } from 'pinia'

let nextId = 1

export const useToastStore = defineStore('toast', () => {
  const toasts = ref([])

  function add({ type, message, duration = type === 'error' ? 0 : 4000 }) {
    const id = nextId++
    toasts.value.push({ id, type, message, duration })

    if (duration > 0) {
      setTimeout(() => remove(id), duration)
    }
  }

  function remove(id) {
    toasts.value = toasts.value.filter(t => t.id !== id)
  }

  function success(message) { add({ type: 'success', message }) }
  function error(message)   { add({ type: 'error',   message }) }
  function warning(message) { add({ type: 'warning', message, duration: 6000 }) }
  function info(message)    { add({ type: 'info',    message }) }

  return { toasts, add, remove, success, error, warning, info }
})
