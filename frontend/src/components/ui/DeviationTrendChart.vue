<template>
  <div class="chart-card">
    <canvas ref="canvas"></canvas>
  </div>
</template>

<script setup>
/**
 * DeviationTrendChart
 *
 * Bar chart that visualises the number of deviations per day over a given period.
 * Wraps Chart.js and manages its own lifecycle: the chart instance is created (or
 * re-created) whenever `points` changes and destroyed on unmount to prevent leaks.
 *
 * @prop {Array} points - Array of `{ date: string, count: number }` data points
 *                        where `date` is an ISO date string and `count` is the
 *                        number of deviations recorded on that day.
 */
import { ref, watch, nextTick, onUnmounted } from 'vue'
import { Chart, registerables } from 'chart.js'

Chart.register(...registerables)

const props = defineProps({
  points: { type: Array, required: true },
})

const canvas = ref(null)
let chart = null

watch(
  () => props.points,
  async () => {
    if (!props.points?.length) return
    await nextTick()
    if (!canvas.value) return
    render(props.points)
  },
  { immediate: true },
)

/**
 * Destroys any existing chart instance and renders a new bar chart for the given data points.
 * @param {{ date: string, count: number }[]} points - Deviation data points to render.
 */
function render(points) {
  if (chart) chart.destroy()

  const labels = points.map((point) =>
    new Date(point.date).toLocaleDateString('en-GB', {
      day: 'numeric',
      month: 'short',
    }),
  )
  const values = points.map((point) => Number(point.count || 0))
  const maxValue = Math.max(...values, 0)

  chart = new Chart(canvas.value, {
    type: 'bar',
    data: {
      labels,
      datasets: [
        {
          label: 'Deviations',
          data: values,
          backgroundColor: 'rgba(180, 83, 9, 0.28)',
          borderColor: 'rgba(180, 83, 9, 0.75)',
          borderWidth: 1,
          borderRadius: 4,
          barThickness: 18,
          maxBarThickness: 24,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          displayColors: false,
          backgroundColor: 'rgba(15, 23, 42, 0.94)',
          titleColor: '#f8fafc',
          bodyColor: '#e2e8f0',
          padding: 10,
          callbacks: {
            label: (ctx) => `${ctx.raw} deviation${ctx.raw === 1 ? '' : 's'}`,
          },
        },
      },
      scales: {
        y: {
          beginAtZero: true,
          suggestedMax: Math.max(3, maxValue + 1),
          ticks: {
            precision: 0,
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
            maxTicksLimit: 8,
            font: {
              size: 10,
              weight: '500',
            },
          },
          grid: { display: false },
          border: {
            display: false,
          },
        },
      },
    },
  })
}

onUnmounted(() => {
  if (chart) chart.destroy()
})
</script>

<style scoped>
.chart-card {
  min-height: 240px;
  background: #ffffff;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 18px;
}

canvas {
  width: 100%;
  height: 100%;
}
</style>
