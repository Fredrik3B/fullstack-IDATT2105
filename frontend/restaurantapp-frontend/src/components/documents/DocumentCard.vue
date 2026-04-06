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

<style scoped>
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
</style>
