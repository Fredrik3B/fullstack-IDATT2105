import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import DocumentPreviewModal from '@/components/documents/DocumentPreviewModal.vue'

// ── Helpers ────────────────────────────────────────────────────────────────

function makeDoc(overrides = {}) {
  return {
    id: 1,
    name: 'Hygiene Policy',
    fileType: 'application/pdf',
    externalUrl: null,
    ...overrides,
  }
}

function mountModal(props = {}) {
  return mount(DocumentPreviewModal, {
    props: {
      doc: makeDoc(),
      previewUrl: null,
      loading: false,
      error: null,
      ...props,
    },
  })
}

// ── Tests ──────────────────────────────────────────────────────────────────

describe('DocumentPreviewModal', () => {

  // ── Rendering ───────────────────────────────────────────────────────────

  describe('rendering', () => {
    it('displays the document name in the header', () => {
      const wrapper = mountModal({ doc: makeDoc({ name: 'Fire Safety Plan' }) })
      expect(wrapper.find('.modal-title').text()).toBe('Fire Safety Plan')
    })

    it('renders a Download button in the header', () => {
      const wrapper = mountModal()
      expect(wrapper.find('.doc-btn').exists()).toBe(true)
      expect(wrapper.find('.doc-btn').text()).toBe('Download')
    })

    it('renders a close button', () => {
      const wrapper = mountModal()
      expect(wrapper.find('.modal-close').exists()).toBe(true)
    })
  })

  // ── Loading state ────────────────────────────────────────────────────────

  describe('loading state', () => {
    it('shows loading state when loading is true', () => {
      const wrapper = mountModal({ loading: true })
      expect(wrapper.find('.loading-state').exists()).toBe(true)
      expect(wrapper.text()).toContain('Loading preview')
    })

    it('hides loading state when loading is false', () => {
      const wrapper = mountModal({ loading: false })
      expect(wrapper.find('.loading-state').exists()).toBe(false)
    })
  })

  // ── Error state ──────────────────────────────────────────────────────────

  describe('error state', () => {
    it('shows error message when error prop is set', () => {
      const wrapper = mountModal({ error: 'Preview unavailable' })
      expect(wrapper.find('.error-state').exists()).toBe(true)
      expect(wrapper.find('.error-state').text()).toBe('Preview unavailable')
    })

    it('does not show error state when error is null', () => {
      const wrapper = mountModal({ error: null })
      expect(wrapper.find('.error-state').exists()).toBe(false)
    })
  })

  // ── Preview content ──────────────────────────────────────────────────────

  describe('preview content', () => {
    it('renders an <img> for image files with a previewUrl', () => {
      const wrapper = mountModal({
        doc: makeDoc({ fileType: 'image/png', name: 'Photo' }),
        previewUrl: 'blob:http://localhost/abc',
      })
      const img = wrapper.find('.preview-img')
      expect(img.exists()).toBe(true)
      expect(img.attributes('src')).toBe('blob:http://localhost/abc')
      expect(img.attributes('alt')).toBe('Photo')
    })

    it('renders an <object> for PDF files with a previewUrl', () => {
      const wrapper = mountModal({
        doc: makeDoc({ fileType: 'application/pdf' }),
        previewUrl: 'blob:http://localhost/pdf',
      })
      expect(wrapper.find('.preview-pdf').exists()).toBe(true)
      expect(wrapper.find('.preview-pdf').attributes('data')).toBe('blob:http://localhost/pdf')
    })

    it('shows unsupported preview for unknown file types', () => {
      const wrapper = mountModal({
        doc: makeDoc({ fileType: 'application/zip' }),
        previewUrl: null,
      })
      expect(wrapper.find('.preview-unsupported').exists()).toBe(true)
      expect(wrapper.text()).toContain('Preview not available for this file type')
    })

    it('shows unsupported view when previewUrl is null even for PDF', () => {
      const wrapper = mountModal({
        doc: makeDoc({ fileType: 'application/pdf' }),
        previewUrl: null,
      })
      // No previewUrl → isPdf is false → falls through to unsupported
      expect(wrapper.find('.preview-pdf').exists()).toBe(false)
      expect(wrapper.find('.preview-unsupported').exists()).toBe(true)
    })

    it('shows correct file type label in unsupported view', () => {
      const wrapper = mountModal({
        doc: makeDoc({ fileType: 'application/zip' }),
        previewUrl: null,
      })
      expect(wrapper.find('.doc-icon-large').text()).toBe('FILE')
    })
  })

  // ── Emits ────────────────────────────────────────────────────────────────

  describe('emits', () => {
    it('emits "close" when ✕ button is clicked', async () => {
      const wrapper = mountModal()
      await wrapper.find('.modal-close').trigger('click')
      expect(wrapper.emitted('close')).toBeTruthy()
    })

    it('emits "close" when backdrop is clicked', async () => {
      const wrapper = mountModal()
      await wrapper.find('.modal-backdrop').trigger('click')
      expect(wrapper.emitted('close')).toBeTruthy()
    })

    it('emits "download" with doc when header Download button is clicked', async () => {
      const doc = makeDoc({ name: 'My File' })
      const wrapper = mountModal({ doc })
      await wrapper.find('.doc-btn').trigger('click')
      expect(wrapper.emitted('download')).toBeTruthy()
      expect(wrapper.emitted('download')[0][0]).toEqual(doc)
    })

    it('emits "download" when "Download to view" button in unsupported view is clicked', async () => {
      const doc = makeDoc({ fileType: 'application/zip' })
      const wrapper = mountModal({ doc, previewUrl: null })
      const downloadBtn = wrapper.findAll('.doc-btn').find(b => b.text() === 'Download to view')
      await downloadBtn.trigger('click')
      expect(wrapper.emitted('download')).toBeTruthy()
      expect(wrapper.emitted('download')[0][0]).toEqual(doc)
    })

    it('does not emit "close" when modal content (not backdrop) is clicked', async () => {
      const wrapper = mountModal()
      await wrapper.find('.preview-modal').trigger('click')
      expect(wrapper.emitted('close')).toBeFalsy()
    })
  })

})
