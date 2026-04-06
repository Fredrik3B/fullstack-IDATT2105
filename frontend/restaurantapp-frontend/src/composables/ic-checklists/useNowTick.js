import { onMounted, onUnmounted, ref } from 'vue'

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

