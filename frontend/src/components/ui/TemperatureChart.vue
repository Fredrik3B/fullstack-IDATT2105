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

  const values = log.map((p) => Number(p.valueC))
  const minTargets = log.map((p) => Number(p.targetMin))
  const maxTargets = log.map((p) => Number(p.targetMax))
  const colors = log.map(p => p.withinRange ? '#16a34a' : '#dc2626')
  const yBounds = calculateYAxisBounds({ values, minTargets, maxTargets })

  chart = new Chart(canvas.value, {
    type: 'line',
    data: {
      labels,
      datasets: [
        {
          label: 'Maximum allowed',
          data: maxTargets,
          borderColor: 'rgba(14, 116, 144, 0.7)',
          backgroundColor: 'transparent',
          borderDash: [4, 4],
          borderWidth: 1,
          pointRadius: 0,
          pointHoverRadius: 0,
          tension: 0,
          order: 1,
        },
        {
          label: 'Minimum allowed',
          data: minTargets,
          borderColor: 'rgba(14, 116, 144, 0.7)',
          backgroundColor: 'rgba(14, 116, 144, 0.08)',
          borderDash: [4, 4],
          borderWidth: 1,
          pointRadius: 0,
          pointHoverRadius: 0,
          tension: 0,
          fill: '-1',
          order: 1,
        },
        {
          label: 'Temperature (°C)',
          data: values,
          borderColor: '#334155',
          backgroundColor: 'transparent',
          pointBackgroundColor: colors,
          pointBorderColor: colors,
          pointBorderWidth: 1,
          pointRadius: log.map((point) => (point.withinRange ? 2.5 : 3.5)),
          pointHoverRadius: log.map((point) => (point.withinRange ? 4 : 5)),
          tension: 0.2,
          borderWidth: 1.5,
          fill: false,
          order: 2,
        },
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      interaction: {
        intersect: false,
        mode: 'index',
      },
      plugins: {
        tooltip: {
          displayColors: false,
          backgroundColor: 'rgba(15, 23, 42, 0.94)',
          titleColor: '#f8fafc',
          bodyColor: '#e2e8f0',
          padding: 10,
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
          min: yBounds.min,
          max: yBounds.max,
          title: { display: true, text: '°C' },
          ticks: {
            color: '#64748b',
            font: {
              size: 10,
              weight: '500',
            },
          },
          grid: {
            color: 'rgba(148, 163, 184, 0.14)',
            drawBorder: false,
          },
          border: {
            display: false,
          },
        },
        x: {
          ticks: {
            color: '#64748b',
            maxRotation: 0,
            autoSkip: true,
            maxTicksLimit: 6,
            font: {
              size: 10,
              weight: '500',
            },
          },
          grid: { display: false },
          border: {
            display: false,
          },
        }
      }
    }
  })
}

function calculateYAxisBounds({ values, minTargets, maxTargets }) {
  const seriesValues = [...values, ...minTargets, ...maxTargets].filter((value) => Number.isFinite(value))
  if (!seriesValues.length) {
    return { min: -10, max: 10 }
  }

  const observedMin = Math.min(...seriesValues)
  const observedMax = Math.max(...seriesValues)
  const targetMin = Math.min(...minTargets.filter((value) => Number.isFinite(value)))
  const targetMax = Math.max(...maxTargets.filter((value) => Number.isFinite(value)))
  const safeMin = Number.isFinite(targetMin) ? targetMin : observedMin
  const safeMax = Number.isFinite(targetMax) ? targetMax : observedMax
  const span = Math.max(1, safeMax - safeMin)

  let min
  let max

  if (safeMax <= 0) {
    min = safeMin - Math.max(8, span * 2)
    max = Math.max(0, safeMax + Math.max(3, span * 0.5))
  } else if (safeMin >= 0) {
    min = Math.min(-5, safeMin - Math.max(4, span * 1.25))
    max = safeMax + Math.max(8, span * 2.5)
  } else {
    min = safeMin - Math.max(5, span * 1.25)
    max = safeMax + Math.max(5, span * 1.25)
  }

  min = Math.min(min, observedMin - 2)
  max = Math.max(max, observedMax + 2)

  return {
    min: roundAxisMin(min),
    max: roundAxisMax(max),
  }
}

function roundAxisMin(value) {
  if (value >= 0) return Math.floor(value)
  if (value > -10) return -5
  return Math.floor(value / 5) * 5
}

function roundAxisMax(value) {
  if (value <= 0) return 0
  if (value < 10) return 10
  return Math.ceil(value / 5) * 5
}

onUnmounted(() => {
  if (chart) chart.destroy()
})
</script>

<style scoped>
.chart-card {
  min-height: 300px;
  background: #ffffff;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 18px;
  box-shadow: none;
}

canvas {
  width: 100%;
  height: 100%;
}
</style>
