<template>
  <div
    class="deviation-modal"
    role="dialog"
    aria-modal="true"
    aria-labelledby="deviation-modal-title"
    aria-describedby="deviation-modal-description"
  >
    <div class="modal-header">
      <div class="modal-intro">
        <h2 id="deviation-modal-title" class="modal-title">Report deviation</h2>
        <p id="deviation-modal-description" class="modal-description">
          Capture what happened, who handled it, and what will help prevent it from happening again.
        </p>
      </div>

      <button class="modal-close" type="button" @click="$emit('cancel')" aria-label="Close">
        &times;
      </button>
    </div>

    <form class="modal-body" @submit.prevent="submit">
      <section class="summary-banner" aria-label="Required details">
        <div class="summary-banner__icon">!</div>
        <div class="summary-banner__copy">
          <strong>Complete all sections before submitting.</strong>
          <span>Start with the deviation details, then document follow-up and prevention.</span>
        </div>
      </section>

      <section class="form-section">
        <div class="section-heading">
          <h3>Incident details</h3>
          <p>Make the event easy to identify for later review.</p>
        </div>

        <div class="form-grid">
          <label class="field field--full">
            <span class="field-label">Deviation for <span class="required">*</span></span>
            <input
              id="dev-name"
              v-model="form.deviationName"
              class="field-input"
              type="text"
              placeholder="e.g. Fridge temperature too high"
            />
          </label>

          <label class="field">
            <span class="field-label">Severity <span class="required">*</span></span>
            <select id="dev-severity" v-model="form.severity" class="field-input">
              <option value="" disabled>Select severity</option>
              <option value="MINOR">Minor</option>
              <option value="MODERATE">Moderate</option>
              <option value="MAJOR">Major</option>
              <option value="CRITICAL">Critical</option>
            </select>
          </label>

          <label class="field">
            <span class="field-label">When did it happen <span class="required">*</span></span>
            <input id="dev-time" v-model="form.timestamp" class="field-input" type="datetime-local" />
          </label>

          <label class="field field--full">
            <span class="field-label">What went wrong <span class="required">*</span></span>
            <textarea
              id="dev-desc"
              v-model="form.description"
              class="field-textarea field-textarea--lg"
              rows="4"
              placeholder="Describe the deviation, what was affected, and any immediate risk."
            ></textarea>
          </label>
        </div>
      </section>

      <section class="form-section">
        <div class="section-heading">
          <h3>People involved</h3>
          <p>Record who noticed, escalated, and handled the issue.</p>
        </div>

        <div class="form-grid">
          <label class="field">
            <span class="field-label">Noticed by <span class="required">*</span></span>
            <input id="dev-noticed" v-model="form.noticedBy" class="field-input" type="text" placeholder="Employee or role" />
          </label>

          <label class="field">
            <span class="field-label">Reported to <span class="required">*</span></span>
            <input id="dev-reported" v-model="form.reportedTo" class="field-input" type="text" placeholder="Manager or responsible lead" />
          </label>

          <label class="field field--full">
            <span class="field-label">Handled by <span class="required">*</span></span>
            <input id="dev-processed" v-model="form.processedBy" class="field-input" type="text" placeholder="Person who followed up the deviation" />
          </label>
        </div>
      </section>

      <section class="form-section">
        <div class="section-heading">
          <h3>Response and prevention</h3>
          <p>Document what was done now and what should stop a repeat.</p>
        </div>

        <div class="form-grid">
          <label class="field field--full">
            <span class="field-label">Immediate actions taken <span class="required">*</span></span>
            <textarea
              id="dev-immediate"
              v-model="form.immediateAction"
              class="field-textarea"
              rows="3"
              placeholder="What was done right away to reduce risk?"
            ></textarea>
          </label>

          <label class="field field--full">
            <span class="field-label">Believed cause <span class="required">*</span></span>
            <textarea
              id="dev-cause"
              v-model="form.believedCause"
              class="field-textarea"
              rows="3"
              placeholder="What likely caused the deviation?"
            ></textarea>
          </label>

          <label class="field field--full">
            <span class="field-label">Corrective measures to prevent recurrence <span class="required">*</span></span>
            <textarea
              id="dev-measures"
              v-model="form.correctiveMeasures"
              class="field-textarea"
              rows="3"
              placeholder="What should be changed going forward?"
            ></textarea>
          </label>

          <label class="field field--full">
            <span class="field-label">Measures already done <span class="required">*</span></span>
            <textarea
              id="dev-done"
              v-model="form.correctiveMeasuresDone"
              class="field-textarea"
              rows="3"
              placeholder="Describe what has already been completed."
            ></textarea>
          </label>
        </div>
      </section>

      <p v-if="error" class="form-error" role="alert">{{ error }}</p>

      <div class="form-actions">
        <button class="btn-cancel" type="button" @click="$emit('cancel')">Cancel</button>
        <button class="btn-submit" type="submit" :disabled="submitting || !isValid">
          {{ submitting ? 'Submitting...' : 'Submit report' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
/**
 * DeviationReportForm
 *
 * Multi-section form for filing a deviation report. Collects incident details
 * (name, severity, timestamp, description), the people involved (noticed by,
 * reported to, handled by), and the response (immediate action, believed cause,
 * corrective measures, measures already completed).
 *
 * All fields are required; the submit button stays disabled until `isValid` is true.
 * On successful submission the form data is sent to the API and `submitted` is
 * emitted with the server response. Errors are shown inline below the form.
 *
 * @emits cancel    - User dismissed the form without submitting.
 * @emits submitted - Payload: the deviation report object returned by the API.
 */
import { computed, reactive, ref } from 'vue'
import { createDeviationReport } from '@/api/reports'

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

/** Submits the deviation report to the API and emits `submitted` on success. */
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
.deviation-modal {
  background:
    linear-gradient(180deg, rgba(240, 247, 204, 0.7) 0%, rgba(240, 247, 204, 0) 140px),
    var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  box-shadow: 0 24px 60px rgba(26, 26, 46, 0.22);
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-6);
  border-bottom: 1px solid var(--color-border);
}

.modal-intro {
  display: grid;
  gap: var(--space-2);
}

.modal-title {
  margin: 0;
  font-size: 28px;
  line-height: 1.05;
  color: var(--color-text-primary);
}

.modal-description {
  margin: 0;
  max-width: 56ch;
  font-size: var(--font-size-md);
  line-height: var(--line-height-normal);
  color: var(--color-text-muted);
}

.modal-close {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.82);
  color: var(--color-text-secondary);
  font-size: 26px;
  line-height: 1;
  cursor: pointer;
  transition:
    transform var(--transition-fast),
    border-color var(--transition-fast),
    color var(--transition-fast);
}

.modal-close:hover {
  transform: translateY(-1px);
  border-color: var(--color-border-strong);
  color: var(--color-text-primary);
}

.modal-body {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
  padding: var(--space-6);
  max-height: calc(90vh - 120px);
  overflow-y: auto;
}

.summary-banner {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: var(--space-3);
  align-items: start;
  padding: var(--space-4);
  border: 1px solid var(--color-warning-border);
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, var(--color-warning-bg), rgba(255, 255, 255, 0.98));
}

.summary-banner__icon {
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(232, 192, 48, 0.18);
  color: var(--color-warning-text);
  font-weight: var(--font-weight-bold);
}

.summary-banner__copy {
  display: grid;
  gap: 4px;
}

.summary-banner__copy strong {
  font-size: var(--font-size-md);
  color: var(--color-text-primary);
}

.summary-banner__copy span {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.form-section {
  display: grid;
  gap: var(--space-4);
  padding: var(--space-5);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: linear-gradient(180deg, rgba(244, 244, 246, 0.72) 0%, rgba(255, 255, 255, 0.96) 100%);
}

.section-heading {
  display: grid;
  gap: var(--space-1);
}

.section-heading h3 {
  margin: 0;
  font-size: var(--font-size-lg);
  color: var(--color-text-primary);
}

.section-heading p {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}

.field {
  display: grid;
  gap: var(--space-2);
  min-width: 0;
}

.field--full {
  grid-column: 1 / -1;
}

.field-label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-secondary);
}

.required {
  color: var(--color-danger);
}

.field-input,
.field-textarea {
  width: 100%;
  min-height: 46px;
  padding: 11px 13px;
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.96);
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  font-family: inherit;
  box-sizing: border-box;
  outline: none;
  transition:
    border-color var(--transition-fast),
    box-shadow var(--transition-fast),
    background var(--transition-fast);
}

.field-input::placeholder,
.field-textarea::placeholder {
  color: var(--color-text-hint);
}

.field-input:focus,
.field-textarea:focus {
  border-color: var(--color-dark-secondary);
  box-shadow: 0 0 0 4px rgba(45, 43, 85, 0.08);
  background: #fff;
}

.field-textarea {
  min-height: 110px;
  resize: vertical;
}

.field-textarea--lg {
  min-height: 138px;
}

.form-error {
  margin: 0;
  padding: var(--space-3) var(--space-4);
  border: 1px solid var(--color-danger-border);
  border-radius: var(--radius-md);
  background: var(--color-danger-bg);
  color: var(--color-danger-text);
  font-size: var(--font-size-sm);
}

.form-actions {
  position: sticky;
  bottom: 0;
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding-top: var(--space-4);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.1), var(--color-bg-primary) 30%);
}

.btn-cancel,
.btn-submit {
  min-height: 42px;
  padding: 0 var(--space-5);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  font-family: inherit;
  cursor: pointer;
  transition:
    transform var(--transition-fast),
    opacity var(--transition-fast),
    border-color var(--transition-fast);
}

.btn-cancel {
  border: 1px solid var(--color-border-strong);
  background: rgba(255, 255, 255, 0.94);
  color: var(--color-text-secondary);
  font-weight: var(--font-weight-medium);
}

.btn-cancel:hover {
  transform: translateY(-1px);
  border-color: var(--color-dark-primary);
  color: var(--color-text-primary);
}

.btn-submit {
  border: none;
  background: linear-gradient(135deg, var(--color-accent) 0%, #e7f06f 100%);
  color: var(--color-dark-primary);
  font-weight: var(--font-weight-bold);
  box-shadow: 0 10px 20px rgba(212, 232, 53, 0.28);
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-1px);
  opacity: 0.92;
}

.btn-submit:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: none;
}

@media (max-width: 720px) {
  .modal-header,
  .modal-body {
    padding: var(--space-4);
  }

  .modal-title {
    font-size: 24px;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .summary-banner {
    grid-template-columns: 1fr;
  }

  .summary-banner__icon {
    width: 28px;
    height: 28px;
  }

  .form-section {
    padding: var(--space-4);
  }

  .form-actions {
    flex-direction: column-reverse;
  }

  .btn-cancel,
  .btn-submit {
    width: 100%;
  }
}
</style>
