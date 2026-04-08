<script setup>
import { computed } from 'vue'
import { fileIconClass, fileIconLabel } from '@/components/documents/documentHelpers'

const props = defineProps({
  doc: {
    type: Object,
    required: true,
  },
  previewUrl: {
    type: String,
    default: null,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  error: {
    type: String,
    default: null,
  },
})

const emit = defineEmits(['close', 'download'])

const isImage = computed(() => Boolean(props.previewUrl && props.doc.fileType && props.doc.fileType.includes('image')))
const isPdf = computed(() => Boolean(props.previewUrl && props.doc.fileType && props.doc.fileType.includes('pdf')))
</script>

<template>
  <div class="modal-backdrop" @click.self="emit('close')">
    <div class="preview-modal" role="dialog" aria-modal="true">
      <div class="modal-header">
        <h2 class="modal-title">{{ doc.name }}</h2>
        <div class="preview-header-actions">
          <button class="doc-btn" type="button" @click="emit('download', doc)">Download</button>
          <button class="modal-close" type="button" @click="emit('close')" aria-label="Close">✕</button>
        </div>
      </div>
      <div class="preview-body">
        <div v-if="loading" class="loading-state">Loading preview...</div>
        <div v-else-if="error" class="error-state">{{ error }}</div>
        <img
          v-else-if="isImage"
          :src="previewUrl"
          class="preview-img"
          :alt="doc.name"
        />
        <object
          v-else-if="isPdf"
          :data="previewUrl"
          type="application/pdf"
          class="preview-pdf"
        >
          <p class="preview-fallback">PDF could not be displayed. <button class="doc-btn" type="button" @click="emit('download', doc)">Download instead</button></p>
        </object>
        <div v-else class="preview-unsupported">
          <div :class="['doc-icon-large', fileIconClass(doc.fileType)]">{{ fileIconLabel(doc.fileType) }}</div>
          <p class="preview-unsupported-text">Preview not available for this file type.</p>
          <button class="doc-btn" type="button" @click="emit('download', doc)">Download to view</button>
        </div>
      </div>
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

.preview-modal {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  width: 100%;
  max-width: 860px;
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

.loading-state,
.error-state {
  padding: var(--space-6);
}

@media (max-width: 720px) {
  .modal-backdrop {
    align-items: flex-start;
    padding: var(--space-3);
  }

  .preview-modal {
    max-height: calc(100vh - (var(--space-3) * 2));
  }

  .modal-header {
    padding: var(--space-4);
    gap: var(--space-2);
  }

  .modal-title {
    font-size: var(--font-size-md);
  }

  .preview-header-actions {
    gap: var(--space-2);
  }

  .preview-pdf {
    min-height: 58vh;
  }
}
</style>
