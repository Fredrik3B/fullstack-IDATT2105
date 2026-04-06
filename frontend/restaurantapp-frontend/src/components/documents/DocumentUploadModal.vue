<script setup>
import { ref } from 'vue'
import { uploadDocument } from '@/api/documents'

const CATEGORIES = [
  { value: 'GUIDELINES',   label: 'Guidelines' },
  { value: 'TRAINING',     label: 'Training material' },
  { value: 'CERTIFICATE',  label: 'Certificates' },
  { value: 'AUDIT_REPORT', label: 'Audit & inspection' },
  { value: 'HACCP',        label: 'HACCP / Risk' },
  { value: 'EMERGENCY',    label: 'Emergency procedures' },
]

const MODULES = [
  { value: 'SHARED',     label: 'Shared' },
  { value: 'IC_FOOD',    label: 'IC-Food' },
  { value: 'IC_ALCOHOL', label: 'IC-Alcohol' },
]

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

function resetForm() {
  uploadError.value = null
  uploadMode.value = 'file'
  uploadForm.value = { file: null, externalUrl: '', name: '', description: '', category: '', module: '', expiryDate: '' }
}

function onFileChange(event) {
  const file = event.target.files[0]
  if (file) uploadForm.value.file = file
}

function onDrop(event) {
  isDragging.value = false
  const file = event.dataTransfer.files[0]
  if (file) uploadForm.value.file = file
}

function formatSize(bytes) {
  if (!bytes) return ''
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

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
          <button type="submit" class="btn-submit" :disabled="uploading || !uploadForm.file">
            {{ uploading ? 'Uploading…' : 'Upload' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
