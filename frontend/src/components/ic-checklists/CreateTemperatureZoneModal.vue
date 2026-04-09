<template>
  <div
    v-if="open"
    class="overlay"
    role="dialog"
    aria-modal="true"
    aria-label="Create temperature item"
  >
    <div class="modal">
      <header class="modal-header">
        <div>
          <div class="eyebrow">{{ moduleLabel }}</div>
          <h2>{{ isEditMode ? 'Edit fridge item' : 'Create fridge item' }}</h2>
          <p class="subtitle">
            Set the physical unit and its allowed temperature range here. Tasks will reuse it.
          </p>
        </div>
        <button type="button" class="icon-button" aria-label="Close" @click="close">&times;</button>
      </header>

      <form class="modal-body" @submit.prevent="submit">
        <div class="grid">
          <label class="field full">
            <span class="label">Item name</span>
            <input
              v-model.trim="name"
              class="input"
              type="text"
              required
              placeholder="e.g. Main fridge"
            />
          </label>

          <label class="field">
            <span class="label">Type</span>
            <select v-model="zoneType" class="input" required>
              <option disabled value="">Choose type</option>
              <option v-for="option in TEMPERATURE_ZONE_OPTIONS" :key="option" :value="option">
                {{ formatTemperatureZoneType(option) }}
              </option>
            </select>
          </label>

          <div class="range-card">
            <div class="range-label">Temperature range</div>
            <div class="range-grid">
              <label class="field">
                <span class="label">Target min</span>
                <input v-model.number="targetMin" class="input" type="number" step="0.1" required />
              </label>
              <label class="field">
                <span class="label">Target max</span>
                <input v-model.number="targetMax" class="input" type="number" step="0.1" required />
              </label>
            </div>
          </div>
        </div>

        <p v-if="error" class="error" role="alert">{{ error }}</p>

        <footer class="modal-footer">
          <button type="button" class="secondary" @click="close">Cancel</button>
          <button type="submit" class="primary">
            {{ isEditMode ? 'Save changes' : 'Save item' }}
          </button>
        </footer>
      </form>
    </div>
  </div>
</template>

<script setup>
/**
 * CreateTemperatureZoneModal
 *
 * Modal for creating or editing a shared temperature zone (fridge, freezer,
 * hot-holding item). Collects a name, zone type, and target min/max temperature
 * range. Zone types are populated from the shared `TEMPERATURE_ZONE_OPTIONS` constant.
 *
 * In edit mode (`mode: 'edit'`) the form is pre-populated from `initialZone`.
 * On success the modal emits `created` or `updated` with the zone form values.
 *
 * @prop {boolean} [open]        - Controls modal visibility (v-model compatible).
 * @prop {string}  [mode]        - 'create' (default) or 'edit'.
 * @prop {Object}  [initialZone] - Zone data to pre-populate in edit mode.
 * @prop {string}  module        - Module key the zone belongs to.
 * @prop {string}  [moduleLabel] - Module display name for the eyebrow.
 *
 * @emits update:open - Emitted with `false` when the modal closes.
 * @emits created     - Payload: `{ name, zoneType, targetMin, targetMax }`.
 * @emits updated     - Payload: same shape as `created`.
 * @emits close       - Modal dismissed without saving.
 */
import { computed, ref, watch } from 'vue'
import { TEMPERATURE_ZONE_OPTIONS, formatTemperatureZoneType } from '@/composables/ic-checklists/temperatureZoneOptions'

const props = defineProps({
  open: { type: Boolean, default: false },
  mode: {
    type: String,
    default: 'create',
    validator: (value) => ['create', 'edit'].includes(value),
  },
  initialZone: { type: Object, default: null },
  module: { type: String, required: true },
  moduleLabel: { type: String, default: '' },
})

const emit = defineEmits(['update:open', 'created', 'updated', 'close'])

const name = ref('')
const zoneType = ref('')
const targetMin = ref(null)
const targetMax = ref(null)
const error = ref('')

const isEditMode = computed(() => props.mode === 'edit')

function reset() {
  name.value = ''
  zoneType.value = ''
  targetMin.value = null
  targetMax.value = null
  error.value = ''
}

function initFromZone(zone) {
  if (!zone) {
    reset()
    return
  }
  name.value = String(zone.name ?? '').trim()
  zoneType.value = String(zone.zoneType ?? '')
  targetMin.value = zone.targetMin ?? null
  targetMax.value = zone.targetMax ?? null
  error.value = ''
}

watch(
  () => props.open,
  (isOpen) => {
    if (!isOpen) return
    if (isEditMode.value) {
      initFromZone(props.initialZone)
      return
    }
    reset()
  },
)

watch(
  () => props.initialZone,
  (zone) => {
    if (props.open && isEditMode.value) initFromZone(zone)
  },
)

function close() {
  emit('update:open', false)
  emit('close')
}

function submit() {
  error.value = ''
  if (!name.value.trim()) {
    error.value = 'Item name is required.'
    return
  }
  if (!zoneType.value) {
    error.value = 'Type is required.'
    return
  }
  if (!Number.isFinite(Number(targetMin.value)) || !Number.isFinite(Number(targetMax.value))) {
    error.value = 'Both target values are required.'
    return
  }
  if (Number(targetMin.value) > Number(targetMax.value)) {
    error.value = 'Target min cannot be greater than target max.'
    return
  }

  const payload = {
    id: props.initialZone?.id ?? null,
    module: props.module,
    name: name.value.trim(),
    zoneType: zoneType.value,
    targetMin: Number(targetMin.value),
    targetMax: Number(targetMax.value),
  }

  if (isEditMode.value) emit('updated', payload)
  else emit('created', payload)
  close()
}
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  z-index: 90;
  background: rgba(12, 12, 24, 0.55);
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 64px 16px 32px;
  overflow: auto;
}

.modal {
  width: min(640px, 100%);
  max-height: calc(100vh - 48px);
  display: flex;
  flex-direction: column;
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
  line-height: 1.1;
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
  overflow: auto;
  display: grid;
  gap: var(--space-4);
}

.grid,
.range-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.field {
  display: grid;
  gap: 8px;
}

.full,
.range-card {
  grid-column: 1 / -1;
}

.range-card {
  display: grid;
  gap: 12px;
  padding: 14px;
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  background: var(--color-bg-secondary);
}

.range-label,
.label {
  font-size: 13px;
  font-weight: 700;
  color: var(--color-text-secondary);
}

.input {
  width: 100%;
  min-height: 44px;
  padding: 10px 12px;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border-strong);
  background: white;
  box-sizing: border-box;
  font: inherit;
  color: var(--color-text-primary);
}

select.input {
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
  padding-right: 42px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 12 12' fill='none'%3E%3Cpath d='M2.25 4.5L6 8.25L9.75 4.5' stroke='%2320203a' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 14px center;
  background-size: 12px 12px;
}

.error {
  margin: 0;
  color: #a11d2d;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.primary,
.secondary {
  min-height: 44px;
  padding: 0 16px;
  border-radius: var(--radius-md);
  border: 1px solid transparent;
  cursor: pointer;
}

.primary {
  background: var(--color-dark-secondary);
  color: white;
}

.secondary {
  background: var(--color-bg-secondary);
  border-color: var(--color-border);
  color: var(--color-text-primary);
}

@media (max-width: 720px) {
  .grid,
  .range-grid {
    grid-template-columns: 1fr;
  }
}
</style>
