<script setup>
defineProps({
  doc: {
    type: Object,
    required: true,
  },
  isAdminOrManager: {
    type: Boolean,
    default: false,
  },
})

defineEmits(['preview', 'download', 'delete'])

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
  const daysLeft = Math.ceil((new Date(expiryDate) - new Date()) / (1000 * 60 * 60 * 24))
  if (daysLeft < 0) return 'cert-expiry--expired'
  if (daysLeft <= 30) return 'cert-expiry--warning'
  return 'cert-expiry--ok'
}

function expiryLabel(expiryDate) {
  const daysLeft = Math.ceil((new Date(expiryDate) - new Date()) / (1000 * 60 * 60 * 24))
  if (daysLeft < 0) return 'Expired'
  if (daysLeft === 0) return 'Expires today'
  if (daysLeft <= 30) return `Expires in ${daysLeft}d`
  return 'Valid'
}
</script>

<template>
  <div class="doc-card" @click="$emit('preview', doc)">
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
        <span
          v-if="doc.category === 'CERTIFICATE' && doc.expiryDate"
          :class="['cert-expiry', expiryClass(doc.expiryDate)]"
        >{{ expiryLabel(doc.expiryDate) }}</span>
      </div>
      <div class="doc-card-actions">
        <a v-if="doc.externalUrl" :href="doc.externalUrl" target="_blank" rel="noopener noreferrer" class="doc-btn">Open</a>
        <button v-else class="doc-btn" type="button" @click="$emit('download', doc)">Download</button>
        <button v-if="isAdminOrManager" class="doc-btn doc-btn--danger" type="button" @click="$emit('delete', doc)">Delete</button>
      </div>
    </div>
  </div>
</template>
