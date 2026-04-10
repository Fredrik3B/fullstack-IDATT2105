<script setup>
/**
 * FormField
 *
 * Wrapper that provides consistent label, optional icon, and inline error styling
 * for any form control. The default slot receives the input element and the `icon`
 * named slot can hold a Lucide icon placed absolutely to the left of the input.
 * An `after` named slot is available for hints or secondary controls below the input.
 *
 * @prop {string} label      - Visible label text for the field.
 * @prop {string} [labelHint]- Secondary hint appended to the label (e.g. "(optional)").
 * @prop {string} [error]    - Validation error message; adds the `has-error` modifier.
 * @prop {string} [inputId]  - Value forwarded to the label's `for` attribute for accessibility.
 */
defineProps({
  label: {
    type: String,
    required: true,
  },
  labelHint: {
    type: String,
    default: '',
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
    <label class="field-label" :for="inputId || undefined">
      {{ label }}
      <span v-if="labelHint" class="field-label-hint">{{ labelHint }}</span>
    </label>
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
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: #3f3d61;
}

.field-label-hint {
  color: #9391b3;
  font-size: 11px;
  font-weight: var(--font-weight-normal);
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
