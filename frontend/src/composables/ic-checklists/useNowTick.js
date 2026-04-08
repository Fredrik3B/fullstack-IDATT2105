import { onMounted, onUnmounted, ref } from 'vue'

/**
 * Reactive clock updated at a fixed interval.
 *
 * @param {number} [intervalMs=60000] - Tick interval in milliseconds.
 * @returns {import('vue').Ref<Date>}
 */
export function useNowTick(intervalMs = 60_000) {
  const now = ref(new Date())
  let timer = null

  onMounted(() => {
    timer = setInterval(() => {
      now.value = new Date()
    }, intervalMs)
  })

  onUnmounted(() => {
    if (timer) clearInterval(timer)
  })

  return now
}

