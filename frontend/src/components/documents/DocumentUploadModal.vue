<script setup>
/**
 * DocumentUploadModal
 *
 * Modal dialog for uploading a new document or adding an external URL link.
 * Supports drag-and-drop and click-to-browse file selection. The upload mode
 * can be toggled between file upload and URL link. Certificate documents
 * optionally accept an expiry date.
 *
 * After a successful upload the form is reset and `uploaded` is emitted with
 * the new document object returned by the API. Closing the modal (by the
 * backdrop click, cancel button, or after upload) resets the form state.
 *
 * @emits uploaded - Payload: the newly created document object from the server.
 * @emits close    - User dismissed the modal without uploading.
 */
import { ref } from 'vue'
import { uploadDocument } from '@/api/documents'
import { CATEGORIES, MODULES, formatSize } from '@/components/documents/documentHelpers'

const emit = defineEmits(['uploaded', 'close'])

const uploading = ref(false)
const uploadError = ref(null)
const isDragging = ref(false)
const fileInput = ref(null)
const uploadMode = ref('file')

const uploadForm = ref({
  file: null,
  externalUrl: '',
  name: '',
  description: '',
  category: '',
  module: '',
  expiryDate: '',
})

/** Resets all form fields and clears any previous upload error. */
function resetForm() {
  uploadError.value = null
  uploadMode.value = 'file'
  uploadForm.value = { file: null, externalUrl: '', name: '', description: '', category: '', module: '', expiryDate: '' }
}

/**
 * Reads the first file from a file input change event and stores it in the form.
 * @param {Event} event - The native `change` event from the file input element.
 */
function onFileChange(event) {
  const file = event.target.files[0]
  if (file) uploadForm.value.file = file
}

/**
 * Handles a drag-and-drop file drop and stores the first dropped file in the form.
 * @param {DragEvent} event - The native `drop` event from the drop zone element.
 */
function onDrop(event) {
  isDragging.value = false
  const file = event.dataTransfer.files[0]
  if (file) uploadForm.value.file = file
}

/**
 * Validates the form, builds a FormData payload, calls the upload API, then
 * emits `uploaded` with the new document on success or sets `uploadError` on failure.
 */
async function handleUpload() {
  uploading.value = true
  uploadError.value = null
  try {
    const formData = new FormData()
    if (uploadMode.value === 'file') {
      if (!uploadForm.value.file) { uploadError.value = 'Please select a file.'; uploading.value = false; return }
      formData.append('file', uploadForm.value.file)
    } else {
      if (!uploadForm.value.externalUrl) { uploadError.value = 'Please enter a URL.'; uploading.value = false; return }
      formData.append('externalUrl', uploadForm.value.externalUrl)
    }
    formData.append('name', uploadForm.value.name)
    formData.append('category', uploadForm.value.category)
    formData.append('module', uploadForm.value.module)
    if (uploadForm.value.description) formData.append('description', uploadForm.value.description)
    if (uploadForm.value.expiryDate) formData.append('expiryDate', uploadForm.value.expiryDate)

    const newDoc = await uploadDocument(formData)
    resetForm()
    emit('uploaded', newDoc)
  } catch {
    uploadError.value = 'Upload failed. Please try again.'
  } finally {
    uploading.value = false
  }
}

/** Resets the form and emits `close` to signal the parent to hide the modal. */
function handleClose() {
  resetForm()
  emit('close')
}
</script>

<template>
  <div class="modal-backdrop" @click.self="handleClose">
    <div class="modal" role="dialog" aria-modal="true" aria-labelledby="modal-title">
      <div class="modal-header">
        <h2 id="modal-title" class="modal-title">Upload document</h2>
        <button class="modal-close" type="button" @click="handleClose" aria-label="Close">✕</button>
      </div>

      <form class="modal-body" @submit.prevent="handleUpload">
        <div class="upload-type-toggle">
          <button
            type="button"
            :class="['toggle-btn', uploadMode === 'file' && 'toggle-btn--active']"
            @click="uploadMode = 'file'"
          >Upload file</button>
          <button
            type="button"
            :class="['toggle-btn', uploadMode === 'link' && 'toggle-btn--active']"
            @click="uploadMode = 'link'"
          >Link to URL</button>
        </div>

        <div
          v-if="uploadMode === 'file'"
          :class="['drop-zone', { 'drop-zone--active': isDragging, 'drop-zone--filled': uploadForm.file }]"
          @dragover.prevent="isDragging = true"
          @dragleave.prevent="isDragging = false"
          @drop.prevent="onDrop"
          @click="fileInput.click()"
        >
          <input
            ref="fileInput"
            type="file"
            class="file-input-hidden"
            @change="onFileChange"
          />
          <template v-if="uploadForm.file">
            <span class="drop-zone-filename">{{ uploadForm.file.name }}</span>
            <span class="drop-zone-size">{{ formatSize(uploadForm.file.size) }}</span>
          </template>
          <template v-else>
            <span class="drop-zone-hint">Drag & drop a file here, or click to browse</span>
          </template>
        </div>

        <div v-if="uploadMode === 'link'" class="form-group">
          <label class="form-label" for="upload-url">URL <span class="required">*</span></label>
          <input
            id="upload-url"
            v-model="uploadForm.externalUrl"
            class="form-input"
            type="url"
            placeholder="https://lovdata.no/dokument/..."
            :required="uploadMode === 'link'"
          />
        </div>

        <div class="form-group">
          <label class="form-label" for="upload-name">Document name <span class="required">*</span></label>
          <input
            id="upload-name"
            v-model="uploadForm.name"
            class="form-input"
            type="text"
            placeholder="e.g. Hygiene policy 2026"
            required
          />
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label" for="upload-category">Category <span class="required">*</span></label>
            <select id="upload-category" v-model="uploadForm.category" class="form-select" required>
              <option value="" disabled>Select category</option>
              <option v-for="cat in CATEGORIES" :key="cat.value" :value="cat.value">{{ cat.label }}</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label" for="upload-module">Module <span class="required">*</span></label>
            <select id="upload-module" v-model="uploadForm.module" class="form-select" required>
              <option value="" disabled>Select module</option>
              <option v-for="mod in MODULES" :key="mod.value" :value="mod.value">{{ mod.label }}</option>
            </select>
          </div>
        </div>

        <div v-if="uploadForm.category === 'CERTIFICATE'" class="form-group">
          <label class="form-label" for="upload-expiry">Expiry date</label>
          <input
            id="upload-expiry"
            v-model="uploadForm.expiryDate"
            class="form-input"
            type="date"
          />
        </div>

        <div class="form-group">
          <label class="form-label" for="upload-desc">Description <span class="optional">(optional)</span></label>
          <textarea
            id="upload-desc"
            v-model="uploadForm.description"
            class="form-textarea"
            rows="2"
            placeholder="Short description of this document"
          />
        </div>

        <p v-if="uploadError" class="upload-error">{{ uploadError }}</p>

        <div class="modal-actions">
          <button type="button" class="btn-cancel" @click="handleClose">Cancel</button>
          <button
            type="submit"
            class="btn-submit"
            :disabled="uploading || (uploadMode === 'file' ? !uploadForm.file : !uploadForm.externalUrl)"
          >
            {{ uploading ? 'Uploading…' : 'Upload' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow-y: auto;
  z-index: 100;
  padding: var(--space-6);
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    backdrop-filter: blur(0px);
  }
  to {
    opacity: 1;
    backdrop-filter: blur(4px);
  }
}

.modal {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  width: 100%;
  max-width: 520px;
  max-height: calc(100vh - (var(--space-6) * 2));
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: modalSlideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes modalSlideUp {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-5) var(--space-6);
  border-bottom: 1px solid var(--color-border);
}

.modal-title {
  margin: 0;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.modal-close {
  background: none;
  border: none;
  cursor: pointer;
  font-size: var(--font-size-md);
  color: var(--color-text-muted);
  line-height: 1;
  padding: var(--space-1);
}

.modal-close:hover {
  color: var(--color-text-primary);
}

.modal-body {
  padding: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  overflow-y: auto;
}

.upload-type-toggle {
  display: flex;
  background: var(--color-bg-subtle);
  border-radius: var(--radius-md);
  padding: 3px;
  gap: 3px;
}

.toggle-btn {
  flex: 1;
  padding: var(--space-2) var(--space-3);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-text-muted);
  cursor: pointer;
  font-family: inherit;
  transition: background 0.15s, color 0.15s;
}

.toggle-btn--active {
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  box-shadow: var(--shadow-sm);
}

.drop-zone {
  border: 2px dashed var(--color-border-strong);
  border-radius: var(--radius-md);
  padding: var(--space-8) var(--space-6);
  text-align: center;
  cursor: pointer;
  transition: border-color var(--transition-fast), background var(--transition-fast);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-1);
}

.drop-zone:hover,
.drop-zone--active {
  border-color: var(--color-accent);
  background: var(--color-accent-light);
}

.drop-zone--filled {
  border-style: solid;
  border-color: var(--color-accent);
}

.file-input-hidden {
  display: none;
}

.drop-zone-hint {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.drop-zone-filename {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}

.drop-zone-size {
  font-size: var(--font-size-xs);
  color: var(--color-text-hint);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.form-label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}

.required { color: var(--color-danger); }
.optional  { font-weight: normal; color: var(--color-text-muted); }

.form-input,
.form-select,
.form-textarea {
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
  background: var(--color-bg-primary);
  font-family: inherit;
  outline: none;
  transition: border-color var(--transition-fast);
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  border-color: var(--color-dark-secondary);
}

.form-textarea {
  resize: vertical;
}

.upload-error {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--color-danger);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding-top: var(--space-2);
  position: sticky;
  bottom: 0;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.75), var(--color-bg-primary) 35%);
  padding-bottom: max(var(--space-2), env(safe-area-inset-bottom));
}

.btn-cancel {
  padding: var(--space-2) var(--space-5);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  background: var(--color-bg-primary);
  color: var(--color-text-secondary);
  cursor: pointer;
  font-family: inherit;
}

.btn-cancel:hover {
  border-color: var(--color-dark-primary);
  color: var(--color-dark-primary);
}

.btn-submit {
  padding: var(--space-2) var(--space-5);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  border: none;
  border-radius: var(--radius-md);
  background: var(--color-accent);
  color: var(--color-dark-primary);
  cursor: pointer;
  font-family: inherit;
  transition: opacity var(--transition-fast);
}

.btn-submit:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-submit:not(:disabled):hover {
  opacity: 0.85;
}

@media (max-width: 720px) {
  .modal-backdrop {
    align-items: flex-start;
    padding: var(--space-3);
  }

  .modal {
    max-height: calc(100vh - (var(--space-3) * 2));
  }

  .modal-header,
  .modal-body {
    padding-left: var(--space-4);
    padding-right: var(--space-4);
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: var(--space-3);
  }

  .drop-zone {
    padding: var(--space-6) var(--space-4);
  }

  .modal-actions {
    justify-content: stretch;
  }

  .btn-cancel,
  .btn-submit {
    flex: 1;
    min-height: 40px;
  }
}
</style>
