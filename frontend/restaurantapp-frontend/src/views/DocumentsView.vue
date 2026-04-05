<template>
  <div class="page-root">
    <main class="page-main">
      <div class="page-content">

        <!-- Page Header -->
        <section class="page-header">
          <div class="page-header__main">
            <div class="eyebrow">Documents</div>
            <h1>Document storage and certificates</h1>
            <p class="page-description">Centralized storage of guidelines, training material, and certificates</p>
          </div>

          <div class="actions">
            <div class="insight-card">
              <span class="insight-label">Document Library</span>
              <span class="insight-value">{{ filteredDocuments.length }}</span>
              <span class="insight-text">{{ filteredDocuments.length === 1 ? 'document' : 'documents' }} available</span>
            </div>
          </div>
        </section>

        <!-- Action bar -->
        <div class="action-bar">
          <div class="search-wrap">
            <Search class="search-icon" :size="16" />
            <input
              v-model="searchQuery"
              class="search-input"
              type="search"
              placeholder="Search documents..."
            />
          </div>
          <div class="filter-group">
            <label class="filter-label" for="filter-cat">Category</label>
            <select v-model="activeCategory" class="filter-select" id="filter-cat">
              <option value="">All categories</option>
              <option v-for="cat in CATEGORIES" :key="cat.value" :value="cat.value">
                {{ cat.label }}
              </option>
            </select>
          </div>
          <div class="filter-group">
            <label class="filter-label" for="filter-module">Module</label>
            <select v-model="activeModule" class="filter-select" id="filter-module">
              <option value="">All modules</option>
              <option v-for="mod in MODULES" :key="mod.value" :value="mod.value">
                {{ mod.label }}
              </option>
            </select>
          </div>
          <button v-if="isAdminOrManager" class="btn-upload" type="button" @click="showUploadModal = true">
            + Upload document
          </button>
        </div>

        <!-- Upload modal -->
        <Teleport to="body">
          <div v-if="showUploadModal" class="modal-backdrop" @click.self="closeModal">
            <div class="modal" role="dialog" aria-modal="true" aria-labelledby="modal-title">
              <div class="modal-header">
                <h2 id="modal-title" class="modal-title">Upload document</h2>
                <button class="modal-close" type="button" @click="closeModal" aria-label="Close">✕</button>
              </div>

              <form class="modal-body" @submit.prevent="handleUpload">

                <!-- File / Link toggle -->
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

                <!-- Drop zone (file mode) -->
                <div
                  v-if="uploadMode === 'file'"
                  :class="['drop-zone', { 'drop-zone--active': isDragging, 'drop-zone--filled': uploadForm.file }]"
                  @dragover.prevent="isDragging = true"
                  @dragleave.prevent="isDragging = false"
                  @drop.prevent="onDrop"
                  @click="$refs.fileInput.click()"
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

                <!-- URL input (link mode) -->
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

                <!-- Name -->
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

                <!-- Category + Module row -->
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

                <!-- Expiry date (certificates only) -->
                <div v-if="uploadForm.category === 'CERTIFICATE'" class="form-group">
                  <label class="form-label" for="upload-expiry">Expiry date</label>
                  <input
                    id="upload-expiry"
                    v-model="uploadForm.expiryDate"
                    class="form-input"
                    type="date"
                  />
                </div>

                <!-- Description -->
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

                <!-- Upload error -->
                <p v-if="uploadError" class="upload-error">{{ uploadError }}</p>

                <div class="modal-actions">
                  <button type="button" class="btn-cancel" @click="closeModal">Cancel</button>
                  <button type="submit" class="btn-submit" :disabled="uploading || !uploadForm.file">
                    {{ uploading ? 'Uploading…' : 'Upload' }}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </Teleport>

        <!-- Preview modal -->
        <Teleport to="body">
          <div v-if="previewDoc" class="modal-backdrop" @click.self="closePreview">
            <div class="preview-modal" role="dialog" aria-modal="true">
              <div class="modal-header">
                <h2 class="modal-title">{{ previewDoc.name }}</h2>
                <div class="preview-header-actions">
                  <button class="doc-btn" type="button" @click="handleDownload(previewDoc)">Download</button>
                  <button class="modal-close" type="button" @click="closePreview" aria-label="Close">✕</button>
                </div>
              </div>
              <div class="preview-body">
                <div v-if="previewLoading" class="loading-state">Loading preview...</div>
                <div v-else-if="previewError" class="error-state">{{ previewError }}</div>
                <img
                  v-else-if="previewUrl && previewDoc.fileType && previewDoc.fileType.includes('image')"
                  :src="previewUrl"
                  class="preview-img"
                  :alt="previewDoc.name"
                />
                <object
                  v-else-if="previewUrl && previewDoc.fileType && previewDoc.fileType.includes('pdf')"
                  :data="previewUrl"
                  type="application/pdf"
                  class="preview-pdf"
                >
                  <p class="preview-fallback">PDF could not be displayed. <button class="doc-btn" type="button" @click="handleDownload(previewDoc)">Download instead</button></p>
                </object>
                <div v-else class="preview-unsupported">
                  <div :class="['doc-icon-large', fileIconClass(previewDoc.fileType)]">{{ fileIconLabel(previewDoc.fileType) }}</div>
                  <p class="preview-unsupported-text">Preview not available for this file type.</p>
                  <button class="doc-btn" type="button" @click="handleDownload(previewDoc)">Download to view</button>
                </div>
              </div>
            </div>
          </div>
        </Teleport>

        <!-- Certificate expiry alert -->
        <div v-if="expiryAlerts.length > 0 && !expiryBannerDismissed" class="expiry-banner">
          <AlertTriangle :size="15" class="expiry-icon" />
          <span class="expiry-banner-title">Certificate alerts</span>
          <div class="expiry-chips">
            <button
              v-for="alert in expiryAlerts"
              :key="alert.id"
              :class="['expiry-chip', alert.expired ? 'expiry-chip--expired' : 'expiry-chip--warning']"
              type="button"
              @click="scrollToDoc(alert.id)"
            >
              <span class="expiry-chip-name">{{ alert.name }}</span>
              <span class="expiry-chip-status">{{ alert.label }}</span>
            </button>
          </div>
          <button class="expiry-banner-close" type="button" @click="expiryBannerDismissed = true" aria-label="Dismiss">✕</button>
        </div>

        <!-- Loading state -->
        <div v-if="loading" class="loading-state">Loading documents...</div>

        <!-- Error state -->
        <div v-else-if="error" class="error-state">{{ error }}</div>

        <!-- Document sections -->
        <template v-else>
          <section
            v-for="cat in visibleCategories"
            :key="cat.value"
          >
            <div class="category-header" @click="toggleCategory(cat.value)">
              <h2 class="section-heading">{{ cat.label }}</h2>
              <span class="category-count">
                {{ documentsForCategory(cat.value).length }}
                {{ cat.value === 'CERTIFICATE' ? 'certificates' : 'documents' }}
              </span>
              <ChevronDown
                :size="18"
                :class="['category-chevron', { 'category-chevron--collapsed': collapsedCategories.has(cat.value) }]"
              />
            </div>
            <div v-if="documentsForCategory(cat.value).length === 0 && !collapsedCategories.has(cat.value)" class="empty-state">
              <p class="empty-title">No {{ cat.label.toLowerCase() }} uploaded</p>
              <p class="empty-sub">{{ cat.emptyHint }}</p>
            </div>
            <div v-else-if="!collapsedCategories.has(cat.value)" class="doc-grid">
              <div
                v-for="doc in documentsForCategory(cat.value)"
                :key="doc.id"
                :id="`doc-${doc.id}`"
                class="doc-card"
                @click="handlePreview(doc)"
              >
                <div :class="['doc-card-thumb', doc.externalUrl ? 'doc-icon--link' : fileIconClass(doc.fileType)]">
                  <span class="doc-card-type">{{ doc.externalUrl ? 'LINK' : fileIconLabel(doc.fileType) }}</span>
                  <div class="doc-card-overlay">
                    <span class="doc-card-overlay-text">{{ doc.externalUrl ? 'Open link' : 'Click to preview' }}</span>
                  </div>
                </div>
                <div class="doc-card-body" @click.stop>
                  <span class="doc-name">{{ doc.name }}</span>
                  <span class="doc-meta">{{ formatDate(doc.uploadedAt) }} · {{ formatSize(doc.fileSize) }}</span>
                  <span class="doc-meta">{{ doc.uploadedByName }}</span>
                  <div class="doc-card-tags">
                    <span :class="['doc-badge', moduleBadgeClass(doc.module)]">{{ moduleLabel(doc.module) }}</span>
                    <span v-if="doc.category === 'CERTIFICATE' && doc.expiryDate" :class="['cert-expiry', expiryClass(doc.expiryDate)]">{{ expiryLabel(doc.expiryDate) }}</span>
                  </div>
                  <div class="doc-card-actions">
                    <a v-if="doc.externalUrl" :href="doc.externalUrl" target="_blank" rel="noopener noreferrer" class="doc-btn">Open</a>
                    <button v-else class="doc-btn" type="button" @click="handleDownload(doc)">Download</button>
                    <button v-if="isAdminOrManager" class="doc-btn doc-btn--danger" type="button" @click="handleDelete(doc)">Delete</button>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </template>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Search, AlertTriangle, ChevronDown } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { fetchDocuments, uploadDocument, downloadDocument, deleteDocument } from '@/api/documents'
import { useToast } from '@/composables/useToast'

const auth = useAuthStore()
const route = useRoute()
const isAdminOrManager = computed(() => auth.isAdminOrManager)
const toast = useToast()

// ── State ──────────────────────────────────────────────────────────────────

const documents = ref([])
const loading = ref(false)
const error = ref(null)
const searchQuery = ref('')
const activeCategory = ref('')
const activeModule = ref('')
const expiryBannerDismissed = ref(false)
const collapsedCategories = ref(new Set())

// ── Certificate expiry alerts ──────────────────────────────────────────────

const expiryAlerts = computed(() => {
  const today = new Date()
  return documents.value
    .filter(doc => doc.category === 'CERTIFICATE' && doc.expiryDate)
    .flatMap(doc => {
      const expiry = new Date(doc.expiryDate)
      const daysLeft = Math.ceil((expiry - today) / (1000 * 60 * 60 * 24))
      if (daysLeft > 30) return []
      return [{
        id: doc.id,
        name: doc.name,
        expired: daysLeft < 0,
        label: daysLeft < 0
          ? `Expired ${Math.abs(daysLeft)} day${Math.abs(daysLeft) !== 1 ? 's' : ''} ago`
          : daysLeft === 0
            ? 'Expires today'
            : `Expires in ${daysLeft} day${daysLeft !== 1 ? 's' : ''}`,
      }]
    })
    .sort((a, b) => a.expired === b.expired ? 0 : a.expired ? -1 : 1)
})

// ── Upload modal state ─────────────────────────────────────────────────────

const showUploadModal = ref(false)
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

// ── Constants ──────────────────────────────────────────────────────────────

const CATEGORIES = [
  { value: 'GUIDELINES',   label: 'Guidelines',        emptyHint: 'Upload company policies and procedures for hygiene and alcohol handling.' },
  { value: 'TRAINING',     label: 'Training material',  emptyHint: 'Add course material, instructions, and training documents for employees.' },
  { value: 'CERTIFICATE',  label: 'Certificates',       emptyHint: 'Add employee certificates, e.g. serving license and food safety course.' },
  { value: 'AUDIT_REPORT', label: 'Audit & inspection', emptyHint: 'Store results from external food authority inspections.' },
  { value: 'HACCP',        label: 'HACCP / Risk',       emptyHint: 'Upload food safety hazard analysis and risk assessment documents.' },
  { value: 'EMERGENCY',    label: 'Emergency procedures', emptyHint: 'Add fire evacuation, first aid, and other emergency plans.' },
]

const MODULES = [
  { value: 'SHARED',     label: 'Shared' },
  { value: 'IC_FOOD',    label: 'IC-Food' },
  { value: 'IC_ALCOHOL', label: 'IC-Alcohol' },
]

// ── Data fetching ──────────────────────────────────────────────────────────

async function loadDocuments() {
  loading.value = true
  error.value = null
  try {
    documents.value = await fetchDocuments()
  } catch {
    error.value = 'Failed to load documents. Please try again.'
  } finally {
    loading.value = false
  }
}

function applyRouteFilters(query) {
  const category = String(query?.category ?? '').toUpperCase()
  const module = String(query?.module ?? '').toUpperCase()
  const search = String(query?.search ?? '')

  if (category && CATEGORIES.some((item) => item.value === category)) {
    activeCategory.value = category
  } else if (!category) {
    activeCategory.value = ''
  }

  if (module && MODULES.some((item) => item.value === module)) {
    activeModule.value = module
  } else if (!module) {
    activeModule.value = ''
  }

  if (search) {
    searchQuery.value = search
  }
}

onMounted(async () => {
  applyRouteFilters(route.query)
  await loadDocuments()
})

watch(
  () => route.query,
  (query) => {
    applyRouteFilters(query)
  }
)

// ── Filtering ──────────────────────────────────────────────────────────────

const filteredDocuments = computed(() => {
  return documents.value.filter(doc => {
    if (activeCategory.value && doc.category !== activeCategory.value) return false
    if (activeModule.value && doc.module !== activeModule.value) return false
    if (searchQuery.value) {
      const q = searchQuery.value.toLowerCase()
      if (!doc.name.toLowerCase().includes(q) && !doc.originalFileName.toLowerCase().includes(q)) return false
    }
    return true
  })
})

// Only show categories that have documents when a filter is active, or always show all when no filter
const visibleCategories = computed(() => {
  if (activeCategory.value) {
    return CATEGORIES.filter(c => c.value === activeCategory.value)
  }
  return CATEGORIES
})

function documentsForCategory(category) {
  return filteredDocuments.value.filter(d => d.category === category)
}

function toggleCategory(value) {
  const s = collapsedCategories.value
  if (s.has(value)) s.delete(value)
  else s.add(value)
  collapsedCategories.value = new Set(s) // trigger reactivity
}

// ── Upload modal actions ───────────────────────────────────────────────────

function closeModal() {
  showUploadModal.value = false
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
    documents.value.unshift(newDoc)
    closeModal()
  } catch {
    uploadError.value = 'Upload failed. Please try again.'
  } finally {
    uploading.value = false
    toast.success('Document uploaded successfully.')
  }
}

// ── Expiry banner ──────────────────────────────────────────────────────────

function scrollToDoc(id) {
  const el = document.getElementById(`doc-${id}`)
  if (!el) return
  // Clear category filter so the card is visible
  activeCategory.value = ''
  activeModule.value = ''
  setTimeout(() => {
    document.getElementById(`doc-${id}`)?.scrollIntoView({ behavior: 'smooth', block: 'center' })
    el.classList.add('doc-card--highlight')
    setTimeout(() => el.classList.remove('doc-card--highlight'), 1800)
  }, 50)
}

// ── Preview modal ──────────────────────────────────────────────────────────

const previewDoc = ref(null)
const previewUrl = ref(null)
const previewLoading = ref(false)
const previewError = ref(null)

async function handlePreview(doc) {
  if (doc.externalUrl) {
    window.open(doc.externalUrl, '_blank', 'noopener,noreferrer')
    return
  }
  previewDoc.value = doc
  previewUrl.value = null
  previewLoading.value = true
  previewError.value = null
  try {
    const response = await downloadDocument(doc.id)
    previewUrl.value = URL.createObjectURL(response.data)
  } catch {
    previewError.value = 'Failed to load preview.'
  } finally {
    previewLoading.value = false
  }
}

function closePreview() {
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  previewDoc.value = null
  previewUrl.value = null
  previewError.value = null
}

// ── Actions ────────────────────────────────────────────────────────────────

async function handleDownload(doc) {
  try {
    const response = await downloadDocument(doc.id)
    const url = URL.createObjectURL(response.data)
    const a = document.createElement('a')
    a.href = url
    a.download = doc.originalFileName
    a.click()
    URL.revokeObjectURL(url)
  } catch {
    alert('Failed to download file.')
    toast.error('Failed to download file. Please try again.')
  }
}

async function handleDelete(doc) {
  if (!confirm(`Delete "${doc.name}"? This cannot be undone.`)) return
  try {
    await deleteDocument(doc.id)
    documents.value = documents.value.filter(d => d.id !== doc.id)
    toast.success('Document deleted successfully.')
  } catch {
    alert('Failed to delete document.')
    toast.error('Failed to delete document. Please try again.')
  }
}

// ── Display helpers ────────────────────────────────────────────────────────

function fileIconLabel(fileType) {
  if (!fileType) return 'FILE'
  if (fileType.includes('pdf')) return 'PDF'
  if (fileType.includes('word') || fileType.includes('document')) return 'DOC'
  if (fileType.includes('image')) return 'IMG'
  if (fileType.includes('spreadsheet') || fileType.includes('excel')) return 'XLS'
  return 'FILE'
}

function fileIconClass(fileType) {
  if (!fileType) return 'doc-icon--file'
  if (fileType.includes('pdf')) return 'doc-icon--pdf'
  if (fileType.includes('word') || fileType.includes('document')) return 'doc-icon--doc'
  if (fileType.includes('image')) return 'doc-icon--img'
  if (fileType.includes('spreadsheet') || fileType.includes('excel')) return 'doc-icon--xls'
  return 'doc-icon--file'
}

function moduleBadgeClass(module) {
  if (module === 'IC_FOOD') return 'doc-badge--food'
  if (module === 'IC_ALCOHOL') return 'doc-badge--alcohol'
  return 'doc-badge--shared'
}

function moduleLabel(module) {
  if (module === 'IC_FOOD') return 'IC-Food'
  if (module === 'IC_ALCOHOL') return 'IC-Alcohol'
  return 'Shared'
}

function formatDate(isoString) {
  if (!isoString) return ''
  return new Date(isoString).toLocaleDateString('no-NO', { day: 'numeric', month: 'short', year: 'numeric' })
}

function formatSize(bytes) {
  if (!bytes) return ''
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function expiryClass(expiryDate) {
  const today = new Date()
  const expiry = new Date(expiryDate)
  const daysLeft = Math.ceil((expiry - today) / (1000 * 60 * 60 * 24))
  if (daysLeft < 0) return 'cert-expiry--expired'
  if (daysLeft <= 30) return 'cert-expiry--warning'
  return 'cert-expiry--ok'
}

function expiryLabel(expiryDate) {
  const today = new Date()
  const expiry = new Date(expiryDate)
  const daysLeft = Math.ceil((expiry - today) / (1000 * 60 * 60 * 24))
  if (daysLeft < 0) return 'Expired'
  if (daysLeft === 0) return 'Expires today'
  if (daysLeft <= 30) return `Expires in ${daysLeft}d`
  return 'Valid'
}
</script>

<style scoped>
.page-root {
  min-height: 100vh;
  background: var(--color-bg-secondary);
  position: relative;
}

.page-root::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    radial-gradient(circle at 20% 20%, rgba(212, 232, 53, 0.03) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(75, 74, 114, 0.02) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.page-root > * {
  position: relative;
  z-index: 1;
}

/* ── Page Header (IC-style) ── */
.page-header {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(340px, 0.9fr);
  gap: var(--space-6);
  padding: var(--space-8);
  border-radius: var(--radius-lg);
  background: linear-gradient(180deg, var(--color-dark-primary) 0%, #232248 100%);
  box-shadow: var(--shadow-md);
}

.page-header__main {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: var(--space-2);
}

.eyebrow {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 var(--space-3);
  border-radius: var(--radius-full);
  border: 1px solid rgba(212, 232, 53, 0.32);
  background: rgba(212, 232, 53, 0.08);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--color-accent);
}

.page-header h1 {
  margin: 0;
  font-size: clamp(32px, 4vw, 42px);
  line-height: var(--line-height-tight);
  color: #ffffff;
}

.page-description {
  margin: 0;
  max-width: 60ch;
  font-size: var(--font-size-md);
  line-height: var(--line-height-normal);
  color: rgba(255, 255, 255, 0.82);
}

.actions {
  display: grid;
  gap: var(--space-4);
  align-content: start;
}

.insight-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  padding: var(--space-5);
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(200, 200, 216, 0.18);
}

.insight-label {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-dark-border);
}

.insight-value {
  font-size: 22px;
  font-weight: var(--font-weight-bold);
  color: #ffffff;
}

.insight-text {
  font-size: var(--font-size-sm);
  line-height: var(--line-height-normal);
  color: rgba(255, 255, 255, 0.76);
}

/* ── Main ── */
.page-main {
  padding: var(--space-8) var(--space-6);
}

.page-content {
  max-width: var(--max-width);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

/* ── Loading / Error ── */
.loading-state,
.error-state {
  text-align: center;
  padding: var(--space-10);
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.error-state {
  color: var(--color-danger);
}

/* ── Action bar ── */
.action-bar {
  background: linear-gradient(135deg, var(--color-bg-primary) 0%, rgba(255, 255, 255, 0.7) 100%);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  padding: var(--space-5) var(--space-6);
  display: flex;
  align-items: flex-end;
  gap: var(--space-4);
  flex-wrap: wrap;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.2s ease;
}

.action-bar:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.search-wrap {
  position: relative;
  flex: 1;
  min-width: 200px;
}

.search-icon {
  position: absolute;
  left: var(--space-3);
  top: 50%;
  transform: translateY(-50%);
  color: var(--color-text-muted);
  pointer-events: none;
}

.search-input {
  width: 100%;
  height: 38px;
  padding: 0 var(--space-3) 0 var(--space-8);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
  background: var(--color-bg-primary);
  font-family: inherit;
  outline: none;
  box-sizing: border-box;
  transition: all 0.2s ease;
}

.search-input:focus {
  border-color: var(--color-accent);
  box-shadow: 0 0 0 3px rgba(0, 200, 100, 0.1);
  transform: translateY(-1px);
}

.search-input:hover {
  border-color: var(--color-dark-secondary);
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.filter-label {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-muted);
}

.filter-select {
  height: 38px;
  padding: 0 var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
  background: var(--color-bg-primary);
  font-family: inherit;
  outline: none;
  transition: all 0.2s ease;
  cursor: pointer;
}

.filter-select:focus {
  border-color: var(--color-accent);
  box-shadow: 0 0 0 3px rgba(0, 200, 100, 0.1);
}

.filter-select:hover {
  border-color: var(--color-dark-secondary);
}

.btn-upload {
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
  white-space: nowrap;
  transition: all 0.2s ease;
  margin-left: auto;
  box-shadow: 0 2px 8px rgba(0, 200, 100, 0.2);
  position: relative;
  overflow: hidden;
}

.btn-upload::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.btn-upload:hover::before {
  width: 300px;
  height: 300px;
}

.btn-upload:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 200, 100, 0.3);
}

.btn-upload:active {
  transform: translateY(0);
}

/* ── Category header ── */
.category-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
  cursor: pointer;
  user-select: none;
  background: linear-gradient(135deg, var(--color-bg-primary) 0%, rgba(255, 255, 255, 0.5) 100%);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  padding: var(--space-4) var(--space-5);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.category-header:hover {
  background: linear-gradient(135deg, var(--color-bg-subtle) 0%, rgba(255, 255, 255, 0.7) 100%);
  border-color: var(--color-border-strong);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

.category-header:active {
  transform: translateY(0);
}

.category-chevron {
  margin-left: auto;
  color: var(--color-text-muted);
  flex-shrink: 0;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), color 0.2s ease;
}

.category-header:hover .category-chevron {
  color: var(--color-text-secondary);
}

.category-chevron--collapsed {
  transform: rotate(-90deg);
}

.section-heading {
  margin: 0;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.category-count {
  font-size: var(--font-size-sm);
  color: var(--color-text-hint);
}

/* ── Empty state ── */
.empty-state {
  padding: var(--space-12) var(--space-6);
  text-align: center;
  background: linear-gradient(135deg, var(--color-bg-primary) 0%, rgba(255, 255, 255, 0.5) 100%);
  border: 2px dashed var(--color-border);
  border-radius: var(--radius-xl);
  position: relative;
  overflow: hidden;
}

.empty-state::before {
  content: '📄';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 8rem;
  opacity: 0.05;
  pointer-events: none;
}

.empty-title {
  margin: 0 0 var(--space-2) 0;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  position: relative;
  z-index: 1;
}

.empty-sub {
  margin: 0;
  font-size: var(--font-size-md);
  color: var(--color-text-secondary);
  max-width: 420px;
  margin-inline: auto;
  line-height: var(--line-height-normal);
  position: relative;
  z-index: 1;
}

/* ── Document grid ── */
.doc-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: var(--space-5);
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.doc-grid > * {
  animation: fadeInUp 0.4s ease-out backwards;
}

.doc-grid > *:nth-child(1) { animation-delay: 0.05s; }
.doc-grid > *:nth-child(2) { animation-delay: 0.1s; }
.doc-grid > *:nth-child(3) { animation-delay: 0.15s; }
.doc-grid > *:nth-child(4) { animation-delay: 0.2s; }
.doc-grid > *:nth-child(5) { animation-delay: 0.25s; }
.doc-grid > *:nth-child(6) { animation-delay: 0.3s; }
.doc-grid > *:nth-child(7) { animation-delay: 0.35s; }
.doc-grid > *:nth-child(8) { animation-delay: 0.4s; }
.doc-grid > *:nth-child(n+9) { animation-delay: 0.45s; }

/* ── Document card ── */
.doc-card {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  position: relative;
}

.doc-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, transparent 0%, rgba(255, 255, 255, 0.1) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
  border-radius: var(--radius-xl);
}

.doc-card:hover {
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12), 0 4px 8px rgba(0, 0, 0, 0.08);
  transform: translateY(-4px) scale(1.02);
  border-color: var(--color-border-strong);
}

.doc-card:hover::before {
  opacity: 1;
}

.doc-card:active {
  transform: translateY(-2px) scale(1.01);
}

.doc-card-thumb {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.05) 0%, transparent 100%);
  transition: transform 0.3s ease;
}

.doc-card:hover .doc-card-thumb {
  transform: scale(1.05);
}

.doc-card-type {
  font-size: 1.8rem;
  font-weight: var(--font-weight-bold);
  opacity: 0.65;
  letter-spacing: 0.02em;
  pointer-events: none;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.doc-card:hover .doc-card-type {
  opacity: 0.8;
  transform: scale(1.1);
}

.doc-card-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(0, 0, 0, 0.5) 0%, rgba(0, 0, 0, 0.3) 100%);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.25s ease, backdrop-filter 0.25s ease;
}

.doc-card:hover .doc-card-overlay {
  opacity: 1;
  backdrop-filter: blur(4px);
}

.doc-card-overlay-text {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  color: #fff;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  animation: gentlePulse 2s ease-in-out infinite;
}

@keyframes gentlePulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.85; }
}

/* File type thumb colors — enhanced with gradients */
.doc-icon--pdf  {
  background: linear-gradient(135deg, #ffe5dc 0%, #fde8e0 100%);
  color: #c0392b;
  box-shadow: inset 0 1px 3px rgba(192, 57, 43, 0.1);
}
.doc-icon--doc  {
  background: linear-gradient(135deg, #d0e7ff 0%, #dbeafe 100%);
  color: #1d4ed8;
  box-shadow: inset 0 1px 3px rgba(29, 78, 216, 0.1);
}
.doc-icon--img  {
  background: linear-gradient(135deg, #ede0ff 0%, #f3e8ff 100%);
  color: #7c3aed;
  box-shadow: inset 0 1px 3px rgba(124, 58, 237, 0.1);
}
.doc-icon--xls  {
  background: linear-gradient(135deg, #d1f7e0 0%, #dcfce7 100%);
  color: #15803d;
  box-shadow: inset 0 1px 3px rgba(21, 128, 61, 0.1);
}
.doc-icon--file {
  background: linear-gradient(135deg, var(--color-bg-subtle) 0%, var(--color-bg-secondary) 100%);
  color: var(--color-text-muted);
}
.doc-icon--link {
  background: linear-gradient(135deg, #d4e8ff 0%, #e0f0ff 100%);
  color: #1a6bbf;
  box-shadow: inset 0 1px 3px rgba(26, 107, 191, 0.1);
}

.doc-card-body {
  padding: var(--space-3) var(--space-3) var(--space-3);
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  flex: 1;
}

.doc-name {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.3;
}

.doc-meta {
  font-size: var(--font-size-xs);
  color: var(--color-text-hint);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.doc-card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-1);
  margin-top: var(--space-1);
}

.doc-badge {
  font-size: 10px;
  font-weight: var(--font-weight-bold);
  padding: 3px var(--space-2);
  border-radius: var(--radius-full);
  white-space: nowrap;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.doc-badge:hover {
  transform: scale(1.05);
}

.doc-badge--shared  {
  background: linear-gradient(135deg, var(--color-bg-subtle) 0%, rgba(240, 240, 240, 0.8) 100%);
  color: var(--color-dark-tertiary);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}
.doc-badge--food    {
  background: linear-gradient(135deg, #d1f7e0 0%, #dcfce7 100%);
  color: #15803d;
  box-shadow: 0 1px 3px rgba(21, 128, 61, 0.1);
}
.doc-badge--alcohol {
  background: linear-gradient(135deg, #fef3c7 0%, #fef9e7 100%);
  color: #92400e;
  box-shadow: 0 1px 3px rgba(146, 64, 14, 0.1);
}

.cert-expiry {
  font-size: 10px;
  font-weight: var(--font-weight-bold);
  padding: 3px var(--space-2);
  border-radius: var(--radius-full);
  white-space: nowrap;
  transition: transform 0.2s ease;
}

.cert-expiry:hover {
  transform: scale(1.05);
}

.cert-expiry--ok      {
  background: linear-gradient(135deg, var(--color-success-bg) 0%, rgba(220, 252, 231, 0.8) 100%);
  color: var(--color-success-text);
  box-shadow: 0 1px 3px rgba(21, 128, 61, 0.1);
}
.cert-expiry--warning {
  background: linear-gradient(135deg, var(--color-warning-bg) 0%, rgba(254, 249, 195, 0.9) 100%);
  color: var(--color-warning-text);
  box-shadow: 0 1px 3px rgba(202, 138, 4, 0.15);
  animation: warningPulse 2s ease-in-out infinite;
}
.cert-expiry--expired {
  background: linear-gradient(135deg, var(--color-danger-bg) 0%, rgba(254, 242, 242, 0.9) 100%);
  color: var(--color-danger-text);
  box-shadow: 0 1px 3px rgba(220, 38, 38, 0.15);
  animation: expiredPulse 1.5s ease-in-out infinite;
}

@keyframes warningPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.75; }
}

@keyframes expiredPulse {
  0%, 100% { opacity: 1; box-shadow: 0 1px 3px rgba(220, 38, 38, 0.15); }
  50% { opacity: 0.85; box-shadow: 0 2px 6px rgba(220, 38, 38, 0.25); }
}

.doc-card-actions {
  display: flex;
  gap: var(--space-2);
  margin-top: var(--space-2);
  flex-wrap: wrap;
}

.doc-btn {
  padding: 6px var(--space-3);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  background: var(--color-bg-primary);
  color: var(--color-text-secondary);
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.doc-btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.05);
  transform: translate(-50%, -50%);
  transition: width 0.4s, height 0.4s;
}

.doc-btn:hover::before {
  width: 200px;
  height: 200px;
}

.doc-btn:hover {
  border-color: var(--color-dark-primary);
  color: var(--color-dark-primary);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.doc-btn:active {
  transform: translateY(0);
}

.doc-btn--danger {
  color: var(--color-danger);
  border-color: var(--color-danger-border);
}
.doc-btn--danger:hover {
  background: var(--color-danger-bg);
  border-color: var(--color-danger);
  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.2);
}

/* ── Preview modal ── */
.preview-header-actions {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.preview-body {
  flex: 1;
  overflow: auto;
  display: flex;
  align-items: stretch;
  min-height: 0;
}

.preview-img {
  max-width: 100%;
  max-height: 75vh;
  object-fit: contain;
  margin: auto;
  display: block;
  padding: var(--space-4);
}

.preview-pdf {
  width: 100%;
  min-height: 70vh;
  border: none;
}

.preview-unsupported {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-4);
  padding: var(--space-10);
  flex: 1;
}

.doc-icon-large {
  width: 72px;
  height: 72px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
}

.preview-unsupported-text {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.preview-fallback {
  padding: var(--space-4);
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

/* ── Expiry alert banner ── */
.expiry-banner {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-wrap: wrap;
  background: linear-gradient(135deg, var(--color-warning-bg) 0%, var(--color-warning-bg) 100%);
  border: 1px solid var(--color-warning-border);
  border-left: 4px solid var(--color-warning-text);
  border-radius: var(--radius-xl);
  padding: var(--space-4) var(--space-5);
  box-shadow: 0 4px 12px rgba(234, 179, 8, 0.15);
  animation: slideInDown 0.4s ease-out;
}

@keyframes slideInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.expiry-icon {
  color: var(--color-warning-text);
  flex-shrink: 0;
  animation: warningBounce 2s ease-in-out infinite;
}

@keyframes warningBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
}

.expiry-banner-title {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  white-space: nowrap;
}

.expiry-chips {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  flex: 1;
}

.expiry-chip {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-full);
  border: none;
  cursor: pointer;
  font-family: inherit;
  font-size: var(--font-size-xs);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.expiry-chip:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.12);
}

.expiry-chip--expired {
  background: var(--color-danger-bg);
  color: var(--color-danger-text);
}

.expiry-chip--warning {
  background: var(--color-warning-bg);
  color: var(--color-warning-text);
}

.expiry-chip-name {
  font-weight: var(--font-weight-medium);
}

.expiry-chip-status {
  opacity: 0.7;
  font-size: 10px;
}

.expiry-banner-close {
  background: none;
  border: none;
  cursor: pointer;
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
  line-height: 1;
  padding: 0 var(--space-1);
  flex-shrink: 0;
  margin-left: auto;
  transition: color 0.15s;
}

.expiry-banner-close:hover {
  color: var(--color-text-primary);
}

.doc-card--highlight {
  outline: 2px solid var(--color-accent);
  outline-offset: 2px;
  animation: card-flash 1.8s ease forwards;
}

@keyframes card-flash {
  0%   { outline-color: var(--color-accent); }
  70%  { outline-color: var(--color-accent); }
  100% { outline-color: transparent; }
}

/* ── Upload modal ── */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
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
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
  display: flex;
  flex-direction: column;
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

.preview-modal {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  width: 100%;
  max-width: 860px;
  max-height: 90vh;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: modalSlideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
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
}

/* ── Upload type toggle ── */
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

/* ── Drop zone ── */
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

/* ── Form fields ── */
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

/* ── Modal actions ── */
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

/* ── Responsive ── */
@media (max-width: 900px) {
  .page-header {
    grid-template-columns: 1fr;
    padding: var(--space-6);
  }

  .actions {
    width: 100%;
  }

  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .btn-upload {
    margin-left: 0;
  }
}
</style>
