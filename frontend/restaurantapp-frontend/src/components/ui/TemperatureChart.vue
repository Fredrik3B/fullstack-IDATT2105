<template>
  <div class="chart-card">
    <canvas ref="canvas"></canvas>
  </div>
</template>

<script setup>import { ref, watch, nextTick, onUnmounted } from 'vue'
import { Chart, registerables } from 'chart.js'

Chart.register(...registerables)

const props = defineProps({
  log: { type: Array, required: true }
})

const canvas = ref(null)
let chart = null

watch(() => props.log, async () => {
  if (!props.log?.length) return
  await nextTick()
  if (!canvas.value) return
  render(props.log)
}, { immediate: true })

function render(log) {
  if (chart) chart.destroy()

  const labels = log.map(p => new Date(p.measuredAt).toLocaleString('en-US', {
    day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit'
  }))

  const values = log.map(p => p.valueC)
  const colors = log.map(p => p.withinRange ? '#16a34a' : '#dc2626')

  chart = new Chart(canvas.value, {
    type: 'line',
    data: {
      labels,
      datasets: [{
        label: 'Temperature (°C)',
        data: values,
        borderColor: '#334155',
        backgroundColor: 'transparent',
        pointBackgroundColor: colors,
        pointBorderColor: colors,
        pointRadius: 6,
        pointHoverRadius: 8,
        tension: 0.3
      }]
    },
    options: {
      responsive: true,
      plugins: {
        tooltip: {
          callbacks: {
            afterLabel: (ctx) => {
              const point = log[ctx.dataIndex]
              const status = point.withinRange ? 'Within range' : 'OUT OF RANGE'
              return `${status}\nRange: ${point.targetMin}°C – ${point.targetMax}°C\nBy: ${point.recordedBy}`
            }
          }
        },
        legend: { display: false }
      },
      scales: {
        y: {
          title: { display: true, text: '°C' },
          grid: { color: '#f1f5f9' }
        },
        x: { grid: { display: false } }
      }
    }
  })
}

onUnmounted(() => {
  if (chart) chart.destroy()
})
</script>

<style scoped>
.chart-card {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
}
</style>
