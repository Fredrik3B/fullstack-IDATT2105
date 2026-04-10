import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import DocumentUploadModal from '@/components/documents/DocumentUploadModal.vue'

// ── Mocks ──────────────────────────────────────────────────────────────────

vi.mock('@/api/documents', () => ({
  uploadDocument: vi.fn(),
}))

import { uploadDocument } from '@/api/documents'

// ── Helpers ────────────────────────────────────────────────────────────────

function mountModal() {
  return mount(DocumentUploadModal, { attachTo: document.body })
}

function makeFile(name = 'report.pdf', type = 'application/pdf', size = 1024) {
  return new File(['content'], name, { type, size })
}

async function fillRequiredFields(wrapper, file = makeFile()) {
  // Simulate file selection
  const input = wrapper.find('.file-input-hidden')
  Object.defineProperty(input.element, 'files', { value: [file], configurable: true })
  await input.trigger('change')

  await wrapper.find('#upload-name').setValue('My Document')
  await wrapper.find('#upload-category').setValue('GUIDELINES')
  await wrapper.find('#upload-module').setValue('SHARED')
}

// ── Setup ──────────────────────────────────────────────────────────────────

beforeEach(() => {
  vi.clearAllMocks()
})

// ── Tests ──────────────────────────────────────────────────────────────────

describe('DocumentUploadModal', () => {

  // ── Rendering ───────────────────────────────────────────────────────────

  describe('rendering', () => {
    it('shows the modal title', () => {
      const wrapper = mountModal()
      expect(wrapper.find('.modal-title').text()).toBe('Upload document')
    })

    it('starts in file upload mode', () => {
      const wrapper = mountModal()
      expect(wrapper.find('.drop-zone').exists()).toBe(true)
      expect(wrapper.find('#upload-url').exists()).toBe(false)
    })

    it('renders category and module selects', () => {
      const wrapper = mountModal()
      expect(wrapper.find('#upload-category').exists()).toBe(true)
      expect(wrapper.find('#upload-module').exists()).toBe(true)
    })

    it('does not show expiry date field initially', () => {
      const wrapper = mountModal()
      expect(wrapper.find('#upload-expiry').exists()).toBe(false)
    })

    it('does not show upload error initially', () => {
      const wrapper = mountModal()
      expect(wrapper.find('.upload-error').exists()).toBe(false)
    })

    it('submit button is disabled when no file is selected', () => {
      const wrapper = mountModal()
      expect(wrapper.find('.btn-submit').element.disabled).toBe(true)
    })
  })

  // ── Mode toggling ────────────────────────────────────────────────────────

  describe('upload mode toggle', () => {
    it('switches to link mode when "Link to URL" is clicked', async () => {
      const wrapper = mountModal()
      await wrapper.findAll('.toggle-btn')[1].trigger('click')
      expect(wrapper.find('#upload-url').exists()).toBe(true)
      expect(wrapper.find('.drop-zone').exists()).toBe(false)
    })

    it('switches back to file mode when "Upload file" is clicked', async () => {
      const wrapper = mountModal()
      await wrapper.findAll('.toggle-btn')[1].trigger('click')
      await wrapper.findAll('.toggle-btn')[0].trigger('click')
      expect(wrapper.find('.drop-zone').exists()).toBe(true)
    })

    it('submit button is disabled in link mode when URL is empty', async () => {
      const wrapper = mountModal()
      await wrapper.findAll('.toggle-btn')[1].trigger('click')
      expect(wrapper.find('.btn-submit').element.disabled).toBe(true)
    })

    it('submit button is enabled in link mode when URL is filled', async () => {
      const wrapper = mountModal()
      await wrapper.findAll('.toggle-btn')[1].trigger('click')
      await wrapper.find('#upload-url').setValue('https://example.com/doc')
      expect(wrapper.find('.btn-submit').element.disabled).toBe(false)
    })
  })

  // ── File selection ────────────────────────────────────────────────────────

  describe('file selection', () => {
    it('shows filename and size after file is selected', async () => {
      const wrapper = mountModal()
      const file = makeFile('report.pdf', 'application/pdf', 2048)
      const input = wrapper.find('.file-input-hidden')
      Object.defineProperty(input.element, 'files', { value: [file], configurable: true })
      await input.trigger('change')

      expect(wrapper.find('.drop-zone-filename').text()).toBe('report.pdf')
    })

    it('enables submit button after file is selected', async () => {
      const wrapper = mountModal()
      const input = wrapper.find('.file-input-hidden')
      Object.defineProperty(input.element, 'files', { value: [makeFile()], configurable: true })
      await input.trigger('change')
      expect(wrapper.find('.btn-submit').element.disabled).toBe(false)
    })
  })

  // ── Expiry date field ─────────────────────────────────────────────────────

  describe('expiry date field', () => {
    it('shows expiry date field when CERTIFICATE category is selected', async () => {
      const wrapper = mountModal()
      await wrapper.find('#upload-category').setValue('CERTIFICATE')
      expect(wrapper.find('#upload-expiry').exists()).toBe(true)
    })

    it('hides expiry date field for other categories', async () => {
      const wrapper = mountModal()
      await wrapper.find('#upload-category').setValue('TRAINING')
      expect(wrapper.find('#upload-expiry').exists()).toBe(false)
    })
  })

  // ── Upload submission ─────────────────────────────────────────────────────

  describe('upload submission', () => {
    it('calls uploadDocument and emits "uploaded" on success', async () => {
      const newDoc = { id: 99, name: 'My Document' }
      uploadDocument.mockResolvedValue(newDoc)
      const wrapper = mountModal()
      await fillRequiredFields(wrapper)

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(uploadDocument).toHaveBeenCalledOnce()
      expect(wrapper.emitted('uploaded')).toBeTruthy()
      expect(wrapper.emitted('uploaded')[0][0]).toEqual(newDoc)
    })

    it('shows "Uploading…" text while upload is in progress', async () => {
      let resolve
      uploadDocument.mockReturnValue(new Promise(r => { resolve = r }))
      const wrapper = mountModal()
      await fillRequiredFields(wrapper)

      await wrapper.find('form').trigger('submit')
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.btn-submit').text()).toBe('Uploading…')
      resolve({ id: 1 })
    })

    it('shows error message when upload fails', async () => {
      uploadDocument.mockRejectedValue(new Error('Network error'))
      const wrapper = mountModal()
      await fillRequiredFields(wrapper)

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.find('.upload-error').exists()).toBe(true)
      expect(wrapper.find('.upload-error').text()).toContain('Upload failed')
    })

    it('resets form and clears error after successful upload', async () => {
      uploadDocument.mockResolvedValue({ id: 1, name: 'My Document' })
      const wrapper = mountModal()
      await fillRequiredFields(wrapper)

      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.find('#upload-name').element.value).toBe('')
      expect(wrapper.find('.upload-error').exists()).toBe(false)
    })

    it('shows error when submitting in file mode without a file', async () => {
      const wrapper = mountModal()
      // Fill name/category/module but skip file
      await wrapper.find('#upload-name').setValue('My Document')
      await wrapper.find('#upload-category').setValue('GUIDELINES')
      await wrapper.find('#upload-module').setValue('SHARED')

      // Manually trigger handleUpload since submit button is disabled
      await wrapper.vm.handleUpload()
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.upload-error').text()).toContain('Please select a file')
      expect(uploadDocument).not.toHaveBeenCalled()
    })

    it('shows error when submitting in link mode without a URL', async () => {
      const wrapper = mountModal()
      await wrapper.findAll('.toggle-btn')[1].trigger('click') // switch to link mode
      await wrapper.find('#upload-name').setValue('My Link')

      await wrapper.vm.handleUpload()
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.upload-error').text()).toContain('Please enter a URL')
      expect(uploadDocument).not.toHaveBeenCalled()
    })
  })

  // ── Close behaviour ───────────────────────────────────────────────────────

  describe('close behaviour', () => {
    it('emits "close" when Cancel button is clicked', async () => {
      const wrapper = mountModal()
      await wrapper.find('.btn-cancel').trigger('click')
      expect(wrapper.emitted('close')).toBeTruthy()
    })

    it('emits "close" when the ✕ button is clicked', async () => {
      const wrapper = mountModal()
      await wrapper.find('.modal-close').trigger('click')
      expect(wrapper.emitted('close')).toBeTruthy()
    })

    it('emits "close" when backdrop is clicked', async () => {
      const wrapper = mountModal()
      await wrapper.find('.modal-backdrop').trigger('click')
      expect(wrapper.emitted('close')).toBeTruthy()
    })

    it('resets form state when closed', async () => {
      const wrapper = mountModal()
      await wrapper.find('#upload-name').setValue('Some Name')
      await wrapper.find('.btn-cancel').trigger('click')
      // After re-open (new mount), form would be clean — verify internal state reset
      expect(wrapper.vm.uploadForm.name).toBe('')
    })
  })

})
