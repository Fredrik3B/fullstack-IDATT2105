<template>
  <div class="form-card">
    <h2 class="form-title">Report deviation</h2>

    <div class="form-grid">
      <div class="field">
        <label class="field-label" for="dev-name">Deviation for</label>
        <input class="field-input" id="dev-name" v-model="form.deviationName"
               placeholder="e.g. Fridge temperature too high" />
      </div>

      <div class="field">
        <label class="field-label" for="dev-severity">Severity</label>
        <select class="field-input" id="dev-severity" v-model="form.severity">
          <option value="" disabled>Select severity</option>
          <option value="MINOR">Minor</option>
          <option value="MODERATE">Moderate</option>
          <option value="MAJOR">Major</option>
          <option value="CRITICAL">Critical</option>
        </select>
      </div>

      <div class="field">
        <label class="field-label" for="dev-time">When did it happen</label>
        <input class="field-input" id="dev-time" type="datetime-local" v-model="form.timestamp" />
      </div>

      <div class="field">
        <label class="field-label" for="dev-noticed">Noticed by</label>
        <input class="field-input" id="dev-noticed" v-model="form.noticedBy" />
      </div>

      <div class="field">
        <label class="field-label" for="dev-reported">Reported to</label>
        <input class="field-input" id="dev-reported" v-model="form.reportedTo" />
      </div>

      <div class="field">
        <label class="field-label" for="dev-processed">Handled by</label>
        <input class="field-input" id="dev-processed" v-model="form.processedBy" />
      </div>

      <div class="field field--full">
        <label class="field-label" for="dev-desc">What went wrong</label>
        <textarea class="field-textarea" id="dev-desc" v-model="form.description"
                  rows="3" placeholder="Describe the deviation..."></textarea>
      </div>

      <div class="field field--full">
        <label class="field-label" for="dev-immediate">Immediate actions taken</label>
        <textarea class="field-textarea" id="dev-immediate" v-model="form.immediateAction"
                  rows="2"></textarea>
      </div>

      <div class="field field--full">
        <label class="field-label" for="dev-cause">Believed cause</label>
        <textarea class="field-textarea" id="dev-cause" v-model="form.believedCause"
                  rows="2"></textarea>
      </div>

      <div class="field field--full">
        <label class="field-label" for="dev-measures">Corrective measures to prevent recurrence</label>
        <textarea class="field-textarea" id="dev-measures" v-model="form.correctiveMeasures"
                  rows="2"></textarea>
      </div>

      <div class="field field--full">
        <label class="field-label" for="dev-done">Measures already done</label>
        <textarea class="field-textarea" id="dev-done" v-model="form.correctiveMeasuresDone"
                  rows="2"></textarea>
      </div>
    </div>

    <div class="form-actions">
      <button class="btn-cancel" type="button" @click="$emit('cancel')">Cancel</button>
      <button class="btn-submit" type="button" @click="submit" :disabled="submitting || !isValid">
        {{ submitting ? 'Submitting...' : 'Submit report' }}
      </button>
    </div>

    <p v-if="error" class="form-error">{{ error }}</p>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { createDeviationReport } from '../../api/reports'

const emit = defineEmits(['cancel', 'submitted'])

const submitting = ref(false)
const error = ref('')

const form = reactive({
  deviationName: '',
  severity: '',
  timestamp: '',
  noticedBy: '',
  reportedTo: '',
  processedBy: '',
  description: '',
  immediateAction: '',
  believedCause: '',
  correctiveMeasures: '',
  correctiveMeasuresDone: ''
})

const isValid = computed(() => {
  return form.deviationName
    && form.severity
    && form.timestamp
    && form.noticedBy
    && form.reportedTo
    && form.processedBy
    && form.description
    && form.immediateAction
    && form.believedCause
    && form.correctiveMeasures
    && form.correctiveMeasuresDone
})

async function submit() {
  submitting.value = true
  error.value = ''
  try {
    const result = await createDeviationReport(form)
    emit('submitted', result)
  } catch (e) {
    error.value = e.response?.data?.detail || e.message || 'Failed to submit report'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.form-card {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
}
.form-title {
  margin: 0 0 var(--space-6);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
}
.field {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}
.field--full {
  grid-column: 1 / -1;
}
.field-label {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-muted);
}
.field-input,
.field-textarea {
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
  background: var(--color-bg-primary);
  font-family: inherit;
  outline: none;
}
.field-input:focus,
.field-textarea:focus {
  border-color: var(--color-dark-secondary);
}
.field-textarea {
  resize: vertical;
  min-height: 60px;
}
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  margin-top: var(--space-6);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
}
.btn-cancel {
  height: 36px;
  padding: 0 var(--space-5);
  background: var(--color-bg-primary);
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  cursor: pointer;
  font-family: inherit;
}
.btn-cancel:hover { border-color: var(--color-dark-primary); }
.btn-submit {
  height: 36px;
  padding: 0 var(--space-5);
  background: var(--color-accent);
  color: var(--color-dark-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-family: inherit;
}
.btn-submit:hover { opacity: 0.85; }
.btn-submit:disabled { opacity: 0.5; cursor: not-allowed; }
.form-error {
  margin: var(--space-3) 0 0;
  font-size: var(--font-size-sm);
  color: var(--color-danger, #dc2626);
}

@media (max-width: 600px) {
  .form-grid { grid-template-columns: 1fr; }
}
</style>
