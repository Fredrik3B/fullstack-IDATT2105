<template>
  <div class="page-root">
    <!-- Page banner -->
    <section class="page-banner">
      <div class="page-banner-inner">
        <span class="page-tag">Documents</span>
        <h1 class="page-heading">Document storage and <span class="page-accent">certificates</span></h1>
        <p class="page-sub">Centralized storage of guidelines, training material, and certificates</p>
      </div>
    </section>

    <main class="page-main">
      <div class="page-content">

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
          <button v-if="isAdminOrManager" class="btn-upload" type="button">
            + Upload document
          </button>
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
            <div class="category-header">
              <h2 class="section-heading">{{ cat.label }}</h2>
              <span class="category-count">
                {{ documentsForCategory(cat.value).length }}
                {{ cat.value === 'CERTIFICATE' ? 'certificates' : 'documents' }}
              </span>
            </div>
            <div class="doc-list">
              <div v-if="documentsForCategory(cat.value).length === 0" class="empty-state">
                <p class="empty-title">No {{ cat.label.toLowerCase() }} uploaded</p>
                <p class="empty-sub">{{ cat.emptyHint }}</p>
              </div>
              <div
                v-for="doc in documentsForCategory(cat.value)"
                :key="doc.id"
                class="doc-row"
              >
                <div :class="['doc-icon', fileIconClass(doc.fileType)]">
                  {{ fileIconLabel(doc.fileType) }}
                </div>
                <div class="doc-info">
                  <span class="doc-name">{{ doc.name }}</span>
                  <span class="doc-meta">
                    Uploaded {{ formatDate(doc.uploadedAt) }} · {{ formatSize(doc.fileSize) }} · {{ doc.uploadedByName }}
                  </span>
                </div>
                <span :class="['doc-badge', moduleBadgeClass(doc.module)]">
                  {{ moduleLabel(doc.module) }}
                </span>
                <span
                  v-if="doc.category === 'CERTIFICATE' && doc.expiryDate"
                  :class="['cert-expiry', expiryClass(doc.expiryDate)]"
                >
                  {{ expiryLabel(doc.expiryDate) }}
                </span>
                <div class="doc-actions">
                  <button class="doc-btn" type="button" @click="handleDownload(doc)">
                    Download
                  </button>
                  <button
                    v-if="isAdminOrManager"
                    class="doc-btn doc-btn--danger"
                    type="button"
                    @click="handleDelete(doc)"
                  >
                    Delete
                  </button>
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
import { ref, computed, onMounted } from 'vue'
import { Search } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { fetchDocuments, downloadDocument, deleteDocument } from '@/api/documents'

const auth = useAuthStore()
const isAdminOrManager = computed(() => auth.isAdminOrManager)

// ── State ──────────────────────────────────────────────────────────────────

const documents = ref([])
const loading = ref(false)
const error = ref(null)
const searchQuery = ref('')
const activeCategory = ref('')
const activeModule = ref('')

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

onMounted(loadDocuments)

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
  }
}

async function handleDelete(doc) {
  if (!confirm(`Delete "${doc.name}"? This cannot be undone.`)) return
  try {
    await deleteDocument(doc.id)
    documents.value = documents.value.filter(d => d.id !== doc.id)
  } catch {
    alert('Failed to delete document.')
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
}

/* ── Page banner ── */
.page-banner {
  background: var(--color-dark-primary);
  border-bottom: 1px solid var(--color-dark-secondary);
  padding: var(--space-10) var(--space-6);
}

.page-banner-inner {
  max-width: 960px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.page-tag {
  display: inline-block;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--color-accent);
  border: 1px solid var(--color-accent);
  border-radius: var(--radius-full);
  padding: 3px var(--space-3);
  align-self: flex-start;
}

.page-heading {
  margin: 0;
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: #ffffff;
  line-height: var(--line-height-tight);
}

.page-accent {
  color: var(--color-accent);
}

.page-sub {
  margin: 0;
  font-size: var(--font-size-md);
  color: var(--color-dark-border);
}

/* ── Main ── */
.page-main {
  padding: var(--space-10) var(--space-6);
}

.page-content {
  max-width: 960px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
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
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--space-5) var(--space-6);
  display: flex;
  align-items: flex-end;
  gap: var(--space-4);
  flex-wrap: wrap;
  box-shadow: var(--shadow-sm);
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
  height: 36px;
  padding: 0 var(--space-3) 0 var(--space-8);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
  background: var(--color-bg-primary);
  font-family: inherit;
  outline: none;
  box-sizing: border-box;
  transition: border-color var(--transition-fast);
}

.search-input:focus {
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
  height: 36px;
  padding: 0 var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
  background: var(--color-bg-primary);
  font-family: inherit;
  outline: none;
  transition: border-color var(--transition-fast);
}

.filter-select:focus {
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
  transition: opacity var(--transition-fast);
  margin-left: auto;
}

.btn-upload:hover {
  opacity: 0.85;
}

/* ── Category header ── */
.category-header {
  display: flex;
  align-items: baseline;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
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

/* ── Document list ── */
.doc-list {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

/* ── Empty state ── */
.empty-state {
  padding: var(--space-10) var(--space-6);
  text-align: center;
}

.empty-title {
  margin: 0 0 var(--space-2) 0;
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
}

.empty-sub {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
  max-width: 420px;
  margin-inline: auto;
}

/* ── Document row ── */
.doc-row {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-6);
  border-bottom: 1px solid var(--color-border-subtle);
}

.doc-row:last-child {
  border-bottom: none;
}

.doc-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  flex-shrink: 0;
}

.doc-icon--pdf  { background: var(--color-danger-bg);   color: var(--color-danger-text); }
.doc-icon--doc  { background: #dbeafe;                  color: #1d4ed8; }
.doc-icon--img  { background: #f3e8ff;                  color: #7c3aed; }
.doc-icon--xls  { background: #dcfce7;                  color: #15803d; }
.doc-icon--file { background: var(--color-bg-subtle);   color: var(--color-text-muted); }

.doc-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  min-width: 0;
}

.doc-name {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.doc-meta {
  font-size: var(--font-size-xs);
  color: var(--color-text-hint);
}

.doc-badge {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  padding: 2px var(--space-2);
  border-radius: var(--radius-full);
  white-space: nowrap;
}

.doc-badge--shared  { background: var(--color-bg-subtle);   color: var(--color-dark-tertiary); }
.doc-badge--food    { background: #dcfce7;                   color: #15803d; }
.doc-badge--alcohol { background: #fef3c7;                   color: #92400e; }

.cert-expiry {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  padding: 2px var(--space-2);
  border-radius: var(--radius-full);
  white-space: nowrap;
}

.cert-expiry--ok      { background: var(--color-success-bg);  color: var(--color-success-text); }
.cert-expiry--warning { background: var(--color-warning-bg);  color: var(--color-warning-text); }
.cert-expiry--expired { background: var(--color-danger-bg);   color: var(--color-danger-text); }

.doc-actions {
  display: flex;
  gap: var(--space-2);
  flex-shrink: 0;
}

.doc-btn {
  padding: 4px var(--space-3);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-bg-primary);
  color: var(--color-text-secondary);
  cursor: pointer;
  font-family: inherit;
  transition: border-color var(--transition-fast), color var(--transition-fast);
}

.doc-btn:hover {
  border-color: var(--color-dark-primary);
  color: var(--color-dark-primary);
}

.doc-btn--danger { color: var(--color-danger); border-color: var(--color-danger-border); }
.doc-btn--danger:hover { background: var(--color-danger-bg); border-color: var(--color-danger); }

/* ── Responsive ── */
@media (max-width: 900px) {
  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }
  .btn-upload {
    margin-left: 0;
  }
}
</style>
