<template>
  <div class="page-root">
    <main class="page-main">
      <div class="page-content">

        <PageHeader
          eyebrow="Documents"
          title="Document storage and certificates"
          description="Centralized storage of guidelines, training material, and certificates"
        >
          <template #actions>
            <div class="insight-card">
              <span class="insight-label">Document Library</span>
              <span class="insight-value">{{ filteredDocuments.length }}</span>
              <span class="insight-text">{{ filteredDocuments.length === 1 ? 'document' : 'documents' }} available</span>
            </div>
          </template>
        </PageHeader>

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
          <DocumentUploadModal
            v-if="showUploadModal"
            @uploaded="onDocumentUploaded"
            @close="showUploadModal = false"
          />
        </Teleport>

        <!-- Preview modal -->
        <Teleport to="body">
          <DocumentPreviewModal
            v-if="previewDoc"
            :doc="previewDoc"
            :preview-url="previewUrl"
            :loading="previewLoading"
            :error="previewError"
            @close="closePreview"
            @download="handleDownload"
          />
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
              <DocumentCard
                v-for="doc in documentsForCategory(cat.value)"
                :key="doc.id"
                :id="`doc-${doc.id}`"
                :doc="doc"
                :is-admin-or-manager="isAdminOrManager"
                @preview="handlePreview"
                @download="handleDownload"
                @delete="handleDelete"
              />
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
import { fetchDocuments, downloadDocument, deleteDocument } from '@/api/documents'
import { useToast } from '@/composables/useToast'
import PageHeader from '@/components/layout/PageHeader.vue'
import DocumentUploadModal from '@/components/documents/DocumentUploadModal.vue'
import DocumentCard from '@/components/documents/DocumentCard.vue'
import DocumentPreviewModal from '@/components/documents/DocumentPreviewModal.vue'
import { CATEGORIES, MODULES } from '@/components/documents/documentHelpers'

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
const showUploadModal = ref(false)

// ── Certificate expiry alerts ──────────────────────────────────────────────

const expiryAlerts = computed(() => {
  const today = new Date()
  return documents.value
    .filter(doc => doc.category === 'CERTIFICATE' && doc.expiryDate)
    .flatMap(doc => {
      const daysLeft = Math.ceil((new Date(doc.expiryDate) - today) / (1000 * 60 * 60 * 24))
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

  if (category && CATEGORIES.some((item) => item.value === category)) activeCategory.value = category
  else if (!category) activeCategory.value = ''

  if (module && MODULES.some((item) => item.value === module)) activeModule.value = module
  else if (!module) activeModule.value = ''

  if (search) searchQuery.value = search
}

onMounted(async () => {
  applyRouteFilters(route.query)
  await loadDocuments()
})

watch(() => route.query, applyRouteFilters)

// ── Filtering ──────────────────────────────────────────────────────────────

const filteredDocuments = computed(() => {
  return documents.value.filter(doc => {
    if (activeCategory.value && doc.category !== activeCategory.value) return false
    if (activeModule.value && doc.module !== activeModule.value) return false
    if (searchQuery.value) {
      const q = searchQuery.value.toLowerCase()
      if (!doc.name.toLowerCase().includes(q) && !doc.originalFileName?.toLowerCase().includes(q)) return false
    }
    return true
  })
})

const visibleCategories = computed(() =>
  activeCategory.value ? CATEGORIES.filter(c => c.value === activeCategory.value) : CATEGORIES
)

function documentsForCategory(category) {
  return filteredDocuments.value.filter(d => d.category === category)
}

function toggleCategory(value) {
  const s = collapsedCategories.value
  if (s.has(value)) s.delete(value)
  else s.add(value)
  collapsedCategories.value = new Set(s)
}

// ── Upload modal ───────────────────────────────────────────────────────────

function onDocumentUploaded(newDoc) {
  documents.value.unshift(newDoc)
  showUploadModal.value = false
  toast.success('Document uploaded successfully.')
}

// ── Expiry banner ──────────────────────────────────────────────────────────

function scrollToDoc(id) {
  const el = document.getElementById(`doc-${id}`)
  if (!el) return
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
    toast.error('Failed to delete document. Please try again.')
  }
}
</script>

<style scoped>
.page-root {
  min-height: 100vh;
  background: transparent;
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
  min-width: min(200px, 100%);
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
  min-width: 0;
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
/* ── Responsive ── */
@media (max-width: 900px) {
  .page-main {
    padding: var(--space-6) var(--space-4);
  }

  .action-bar {
    flex-direction: column;
    align-items: stretch;
    padding: var(--space-4);
  }

  .search-wrap,
  .filter-group {
    width: 100%;
    min-width: 0;
  }

  .filter-select,
  .search-input {
    width: 100%;
  }

  .btn-upload {
    margin-left: 0;
    width: 100%;
    min-height: 40px;
    justify-content: center;
  }
}

@media (max-width: 640px) {
  .page-main {
    padding: var(--space-5) var(--space-3);
  }

  .category-header {
    padding: var(--space-3) var(--space-4);
  }
}
</style>
