<script setup>
defineProps({
  label: {
    type: String,
    required: true,
  },
  error: {
    type: String,
    default: '',
  },
  inputId: {
    type: String,
    default: '',
  },
})
</script>

<template>
  <div class="field-group" :class="{ 'has-error': error }">
    <label class="field-label" :for="inputId || undefined">{{ label }}</label>
    <div class="field-wrapper">
      <span v-if="$slots.icon" class="field-icon-wrap">
        <slot name="icon" />
      </span>
      <slot />
    </div>
    <slot name="after" />
    <span v-if="error" class="field-error">{{ error }}</span>
  </div>
</template>

<style scoped>
.field-group {
  display: grid;
  gap: var(--space-2);
}

.field-label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: #3f3d61;
}

.field-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.field-icon-wrap {
  position: absolute;
  left: var(--space-3);
  display: inline-flex;
  align-items: center;
  pointer-events: none;
  color: #8b89ad;
}

.field-icon-wrap :deep(svg) {
  width: 16px;
  height: 16px;
}

.field-error {
  font-size: var(--font-size-xs);
  color: var(--color-danger);
}
</style>
