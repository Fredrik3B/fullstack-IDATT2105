<template>
  <div
    v-if="open"
    class="overlay"
    role="dialog"
    aria-modal="true"
    aria-label="Manage fridge items"
  >
    <div class="modal">
      <header class="modal-header">
        <div>
          <div class="eyebrow">{{ moduleLabel }}</div>
          <h2>Fridge items</h2>
          <p class="subtitle">Create shared fridge, freezer, and hot-holding items with their own ranges.</p>
        </div>
        <button type="button" class="icon-button" aria-label="Close" @click="close">&times;</button>
      </header>

      <div class="modal-body">
        <section class="toolbar-panel">
          <div>
            <span class="panel-label">Shared temperature items</span>
            <p class="panel-copy">Temperature tasks should pick one of these items instead of defining their own range.</p>
          </div>
          <button type="button" class="primary" @click="isCreateOpen = true">New fridge item</button>
        </section>

        <p v-if="error" class="error" role="alert">{{ error }}</p>
        <div v-if="loading" class="state">Loading fridge items...</div>
        <div v-else-if="zones.length === 0" class="state">
          <p class="state-title">No fridge items yet</p>
          <p class="state-copy">Create the first reusable temperature item for this module.</p>
        </div>

        <div v-else class="zone-list">
          <article v-for="zone in zones" :key="zone.id" class="zone-row">
            <div class="zone-copy">
              <div class="zone-title">{{ zone.name }}</div>
              <div class="zone-meta">
                {{ formatTemperatureZoneType(zone.zoneType) }} · {{ formatRange(zone) }}
              </div>
            </div>
            <div class="zone-actions">
              <button type="button" class="secondary" @click="openEditZone(zone)">Edit</button>
              <button
                type="button"
                class="danger"
                :disabled="deletingZoneId === zone.id"
                @click="removeZone(zone)"
              >
                {{ deletingZoneId === zone.id ? 'Deleting...' : 'Delete' }}
              </button>
            </div>
          </article>
        </div>
      </div>
    </div>

    <CreateTemperatureZoneModal
      v-model:open="isCreateOpen"
      :module="module"
      :module-label="moduleLabel"
      @created="handleCreatedZone"
    />

    <CreateTemperatureZoneModal
      v-model:open="isEditOpen"
      mode="edit"
      :initial-zone="selectedZone"
      :module="module"
      :module-label="moduleLabel"
      @updated="handleUpdatedZone"
    />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useToast } from '@/composables/useToast'
import {
  createTemperatureZone,
  deleteTemperatureZone,
  fetchTemperatureZones,
  updateTemperatureZone,
} from '../../api/temperatureZones'
import CreateTemperatureZoneModal from './CreateTemperatureZoneModal.vue'
import { formatTemperatureZoneType } from './temperatureZoneOptions'

const props = defineProps({
  open: { type: Boolean, default: false },
  module: { type: String, required: true },
  moduleLabel: { type: String, default: '' },
})

const emit = defineEmits(['update:open', 'close', 'changed'])

const toast = useToast()
const zones = ref([])
const loading = ref(false)
const error = ref('')
const isCreateOpen = ref(false)
const isEditOpen = ref(false)
const selectedZone = ref(null)
const deletingZoneId = ref(null)

function sortZones(items) {
  return [...items].sort(
    (a, b) =>
      formatTemperatureZoneType(a.zoneType).localeCompare(formatTemperatureZoneType(b.zoneType)) ||
      String(a.name).localeCompare(String(b.name)),
  )
}

function formatRange(zone) {
  return `${zone.targetMin}°C to ${zone.targetMax}°C`
}

async function loadZones() {
  loading.value = true
  error.value = ''
  try {
    const data = await fetchTemperatureZones({ module: props.module })
    zones.value = sortZones(Array.isArray(data) ? data : [])
  } catch (err) {
    console.error('Failed to load temperature zones', err)
    error.value = 'Could not load fridge items.'
  } finally {
    loading.value = false
  }
}

watch(
  () => props.open,
  async (isOpen) => {
    if (isOpen) await loadZones()
  },
)

function close() {
  emit('update:open', false)
  emit('close')
}

function openEditZone(zone) {
  selectedZone.value = { ...zone }
  isEditOpen.value = true
}

async function handleCreatedZone(payload) {
  try {
    const created = await createTemperatureZone(payload)
    zones.value = sortZones([...zones.value, created])
    emit('changed')
    toast.success(`Created "${created.name}".`)
  } catch (err) {
    console.error('Failed to create temperature zone', err)
    error.value = err?.response?.data?.message ?? 'Could not create fridge item.'
    toast.error(error.value)
  }
}

async function handleUpdatedZone(payload) {
  if (!payload?.id) {
    error.value = 'Could not update fridge item.'
    return
  }

  try {
    const updated = await updateTemperatureZone({
      zoneId: payload.id,
      module: payload.module,
      name: payload.name,
      zoneType: payload.zoneType,
      targetMin: payload.targetMin,
      targetMax: payload.targetMax,
    })
    zones.value = sortZones(zones.value.map((zone) => (zone.id === updated.id ? updated : zone)))
    selectedZone.value = updated
    emit('changed')
    toast.success(`Updated "${updated.name}".`)
  } catch (err) {
    console.error('Failed to update temperature zone', err)
    error.value = err?.response?.data?.message ?? 'Could not update fridge item.'
    toast.error(error.value)
  }
}

async function removeZone(zone) {
  const confirmed = window.confirm(
    `Delete "${zone.name}"?\n\nThis only works if no task is still using this fridge item.`,
  )
  if (!confirmed) return

  deletingZoneId.value = zone.id
  error.value = ''
  try {
    await deleteTemperatureZone(zone.id)
    zones.value = zones.value.filter((entry) => entry.id !== zone.id)
    emit('changed')
    toast.success(`Deleted "${zone.name}".`)
  } catch (err) {
    console.error('Failed to delete temperature zone', err)
    error.value = err?.response?.data?.message ?? 'Could not delete fridge item.'
    toast.error(error.value)
  } finally {
    deletingZoneId.value = null
  }
}
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  z-index: 85;
  background: rgba(12, 12, 24, 0.55);
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 64px 16px 32px;
  overflow: auto;
}

.modal {
  width: min(760px, 100%);
  border-radius: var(--radius-xl);
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  box-shadow: 0 30px 90px rgba(0, 0, 0, 0.25);
  overflow: hidden;
}

.modal-header,
.modal-body {
  padding: 20px 24px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  border-bottom: 1px solid var(--color-border-subtle);
}

.eyebrow {
  margin-bottom: 4px;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

h2 {
  margin: 0;
  font-size: 26px;
}

.subtitle {
  margin: 6px 0 0;
  color: var(--color-text-muted);
}

.icon-button {
  width: 36px;
  height: 36px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-bg-primary);
  font-size: 24px;
  line-height: 1;
  cursor: pointer;
}

.modal-body {
  display: grid;
  gap: var(--space-4);
}

.toolbar-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
}

.panel-label {
  display: block;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-text-muted);
}

.panel-copy {
  margin: 6px 0 0;
  color: var(--color-text-secondary);
}

.primary,
.secondary,
.danger {
  min-height: 40px;
  padding: 0 14px;
  border: 1px solid transparent;
  border-radius: var(--radius-md);
  font: inherit;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  cursor: pointer;
}

.primary {
  background: var(--color-dark-secondary);
  color: white;
}

.secondary {
  background: var(--color-bg-primary);
  border-color: var(--color-border);
  color: var(--color-text-primary);
}

.danger {
  background: rgba(161, 29, 45, 0.12);
  color: #a11d2d;
}

.state,
.error {
  color: var(--color-text-muted);
}

.state {
  padding: var(--space-8) var(--space-4);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  background: var(--color-bg-secondary);
  text-align: center;
}

.state-title {
  margin: 0;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.state-copy {
  margin: var(--space-2) 0 0;
}

.error {
  color: #a11d2d;
}

.zone-list {
  display: grid;
  gap: 12px;
}

.zone-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 14px;
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  background: var(--color-bg-secondary);
}

.zone-copy {
  display: grid;
  gap: 4px;
}

.zone-title {
  font-weight: 700;
  color: var(--color-text-primary);
}

.zone-meta {
  font-size: 13px;
  color: var(--color-text-muted);
}

.zone-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

@media (max-width: 720px) {
  .toolbar-panel,
  .zone-row,
  .zone-actions {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
