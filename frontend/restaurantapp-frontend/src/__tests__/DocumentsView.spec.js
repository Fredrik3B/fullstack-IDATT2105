import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import DocumentsView from '@/views/DocumentsView.vue'

// ── Mocks ──────────────────────────────────────────────────────────────────

vi.mock('@/api/documents', () => ({
  fetchDocuments: vi.fn(),
  uploadDocument: vi.fn(),
  downloadDocument: vi.fn(),
  deleteDocument: vi.fn(),
}))

vi.mock('lucide-vue-next', () => ({
  Search: { template: '<span />' },
  AlertTriangle: { template: '<span />' },
  ChevronDown: { template: '<span />' },
}))

import { fetchDocuments, uploadDocument, downloadDocument, deleteDocument } from '@/api/documents'
import { useAuthStore } from '@/stores/auth'

// ── Helpers ────────────────────────────────────────────────────────────────

function makeDoc(overrides = {}) {
  return {
    id: 1,
    name: 'Test Doc',
    originalFileName: 'test.pdf',
    category: 'GUIDELINES',
    module: 'SHARED',
    fileType: 'application/pdf',
    fileSize: 102400,
    uploadedAt: '2025-01-15T10:00:00Z',
    uploadedByName: 'Test User',
    expiryDate: null,
    externalUrl: null,
    description: '',
    ...overrides,
  }
}

async function mountView(authOverrides = {}) {
  const pinia = createPinia()
  setActivePinia(pinia)

  const auth = useAuthStore()
  auth.user = { id: 1, name: 'Test User', email: 'test@example.com' }
  auth.accessToken = 'fake-token'
  auth.restaurantId = 1
  Object.assign(auth, authOverrides)

  const wrapper = mount(DocumentsView, {
    global: { plugins: [pinia] },
    attachTo: document.body,
  })
  await flushPromises()
  return wrapper
}

// ── Setup ──────────────────────────────────────────────────────────────────

beforeEach(() => {
  vi.clearAllMocks()
  fetchDocuments.mockResolvedValue([])
})

// ── Tests ──────────────────────────────────────────────────────────────────

describe('DocumentsView', () => {

  // ── Loading & error states ─────────────────────────────────────────────

  describe('loading and error states', () => {
    it('sets loading state while fetching', async () => {
      let resolve
      fetchDocuments.mockReturnValue(new Promise(r => { resolve = r }))
      const pinia = createPinia()
      setActivePinia(pinia)
      const wrapper = mount(DocumentsView, {
        global: { plugins: [pinia] },
        attachTo: document.body,
      })
      // loading ref should be true before the promise resolves
      expect(wrapper.vm.loading).toBe(true)
      resolve([])
      await flushPromises()
      expect(wrapper.vm.loading).toBe(false)
    })

    it('shows error state when fetch fails', async () => {
      fetchDocuments.mockRejectedValue(new Error('Network error'))
      const wrapper = await mountView()
      expect(wrapper.text()).toContain('Failed to load documents')
    })

    it('renders document grid when fetch succeeds', async () => {
      fetchDocuments.mockResolvedValue([makeDoc()])
      const wrapper = await mountView()
      expect(wrapper.find('.doc-grid').exists()).toBe(true)
      expect(wrapper.text()).toContain('Test Doc')
    })
  })

  // ── Filtering ──────────────────────────────────────────────────────────

  describe('filtering', () => {
    it('filters documents by search query on name', async () => {
      fetchDocuments.mockResolvedValue([
        makeDoc({ id: 1, name: 'Hygiene Policy', originalFileName: 'hygiene.pdf' }),
        makeDoc({ id: 2, name: 'Fire Safety Plan', originalFileName: 'fire.pdf' }),
      ])
      const wrapper = await mountView()
      await wrapper.find('.search-input').setValue('hygiene')
      await wrapper.vm.$nextTick()
      expect(wrapper.text()).toContain('Hygiene Policy')
      expect(wrapper.text()).not.toContain('Fire Safety Plan')
    })

    it('filters documents by category select', async () => {
      fetchDocuments.mockResolvedValue([
        makeDoc({ id: 1, name: 'Doc A', category: 'GUIDELINES' }),
        makeDoc({ id: 2, name: 'Doc B', category: 'TRAINING' }),
      ])
      const wrapper = await mountView()
      await wrapper.find('#filter-cat').setValue('TRAINING')
      await wrapper.vm.$nextTick()
      // Only TRAINING category should be visible
      expect(wrapper.text()).toContain('Doc B')
      expect(wrapper.text()).not.toContain('Doc A')
    })

    it('filters documents by module select', async () => {
      fetchDocuments.mockResolvedValue([
        makeDoc({ id: 1, name: 'Shared Doc', module: 'SHARED' }),
        makeDoc({ id: 2, name: 'Food Doc', module: 'IC_FOOD' }),
      ])
      const wrapper = await mountView()
      await wrapper.find('#filter-module').setValue('IC_FOOD')
      await wrapper.vm.$nextTick()
      expect(wrapper.text()).toContain('Food Doc')
      expect(wrapper.text()).not.toContain('Shared Doc')
    })

    it('shows all documents when no filter is active', async () => {
      fetchDocuments.mockResolvedValue([
        makeDoc({ id: 1, name: 'Doc A', category: 'GUIDELINES' }),
        makeDoc({ id: 2, name: 'Doc B', category: 'TRAINING' }),
      ])
      const wrapper = await mountView()
      expect(wrapper.text()).toContain('Doc A')
      expect(wrapper.text()).toContain('Doc B')
    })
  })

  // ── Certificate expiry alerts ──────────────────────────────────────────

  describe('expiry alerts', () => {
    it('shows expiry banner for certificate expiring within 30 days', async () => {
      const soon = new Date()
      soon.setDate(soon.getDate() + 10)
      fetchDocuments.mockResolvedValue([
        makeDoc({ id: 1, name: 'Food Safety Cert', category: 'CERTIFICATE', expiryDate: soon.toISOString().split('T')[0] }),
      ])
      const wrapper = await mountView()
      expect(wrapper.find('.expiry-banner').exists()).toBe(true)
      expect(wrapper.text()).toContain('Food Safety Cert')
    })

    it('does not show expiry banner when certificate expires more than 30 days away', async () => {
      const future = new Date()
      future.setDate(future.getDate() + 60)
      fetchDocuments.mockResolvedValue([
        makeDoc({ id: 1, name: 'Valid Cert', category: 'CERTIFICATE', expiryDate: future.toISOString().split('T')[0] }),
      ])
      const wrapper = await mountView()
      expect(wrapper.find('.expiry-banner').exists()).toBe(false)
    })

    it('dismisses the expiry banner when close button is clicked', async () => {
      const soon = new Date()
      soon.setDate(soon.getDate() + 5)
      fetchDocuments.mockResolvedValue([
        makeDoc({ id: 1, name: 'Expiring Cert', category: 'CERTIFICATE', expiryDate: soon.toISOString().split('T')[0] }),
      ])
      const wrapper = await mountView()
      expect(wrapper.find('.expiry-banner').exists()).toBe(true)
      await wrapper.find('.expiry-banner-close').trigger('click')
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.expiry-banner').exists()).toBe(false)
    })

    it('marks expired certificates with expired label', async () => {
      const past = new Date()
      past.setDate(past.getDate() - 5)
      fetchDocuments.mockResolvedValue([
        makeDoc({ id: 1, name: 'Expired Cert', category: 'CERTIFICATE', expiryDate: past.toISOString().split('T')[0] }),
      ])
      const wrapper = await mountView()
      expect(wrapper.find('.expiry-chip--expired').exists()).toBe(true)
      expect(wrapper.text()).toContain('Expired')
    })
  })

  // ── Upload modal (admin/manager) ───────────────────────────────────────

  describe('upload modal', () => {
    it('shows upload button for admin', async () => {
      const pinia = createPinia()
      setActivePinia(pinia)
      const auth = useAuthStore()
      auth.user = { id: 1, name: 'Admin', email: 'admin@example.com' }
      auth.accessToken = 'fake-token'
      auth.roles = ['ROLE_ADMIN']
      // Manually set isAdminOrManager computed via store
      vi.spyOn(auth, 'isAdminOrManager', 'get').mockReturnValue(true)

      const wrapper = mount(DocumentsView, { global: { plugins: [pinia] }, attachTo: document.body })
      await flushPromises()
      expect(wrapper.find('.btn-upload').exists()).toBe(true)
    })

    it('hides upload button for non-admin', async () => {
      const pinia = createPinia()
      setActivePinia(pinia)
      const auth = useAuthStore()
      auth.user = { id: 2, name: 'Staff', email: 'staff@example.com' }
      auth.accessToken = 'fake-token'
      vi.spyOn(auth, 'isAdminOrManager', 'get').mockReturnValue(false)

      const wrapper = mount(DocumentsView, { global: { plugins: [pinia] }, attachTo: document.body })
      await flushPromises()
      expect(wrapper.find('.btn-upload').exists()).toBe(false)
    })

    it('opens upload modal when upload button is clicked', async () => {
      const pinia = createPinia()
      setActivePinia(pinia)
      const auth = useAuthStore()
      auth.user = { id: 1, name: 'Admin', email: 'admin@example.com' }
      auth.accessToken = 'fake-token'
      vi.spyOn(auth, 'isAdminOrManager', 'get').mockReturnValue(true)

      const wrapper = mount(DocumentsView, { global: { plugins: [pinia] }, attachTo: document.body })
      await flushPromises()
      expect(wrapper.vm.showUploadModal).toBe(false)
      await wrapper.find('.btn-upload').trigger('click')
      expect(wrapper.vm.showUploadModal).toBe(true)
    })

    it('closes upload modal when closeModal is called', async () => {
      const pinia = createPinia()
      setActivePinia(pinia)
      const auth = useAuthStore()
      vi.spyOn(auth, 'isAdminOrManager', 'get').mockReturnValue(true)

      const wrapper = mount(DocumentsView, { global: { plugins: [pinia] }, attachTo: document.body })
      await flushPromises()
      await wrapper.find('.btn-upload').trigger('click')
      expect(wrapper.vm.showUploadModal).toBe(true)
      wrapper.vm.closeModal()
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.showUploadModal).toBe(false)
    })

    it('shows expiry date field only when category is CERTIFICATE', async () => {
      const pinia = createPinia()
      setActivePinia(pinia)
      const auth = useAuthStore()
      vi.spyOn(auth, 'isAdminOrManager', 'get').mockReturnValue(true)

      const wrapper = mount(DocumentsView, { global: { plugins: [pinia] }, attachTo: document.body })
      await flushPromises()
      await wrapper.find('.btn-upload').trigger('click')
      await wrapper.vm.$nextTick()

      expect(document.querySelector('#upload-expiry')).toBeNull()
      // Set category to CERTIFICATE via the vm to trigger reactivity
      wrapper.vm.uploadForm.category = 'CERTIFICATE'
      await wrapper.vm.$nextTick()
      expect(document.querySelector('#upload-expiry')).not.toBeNull()
    })
  })

  // ── Delete ─────────────────────────────────────────────────────────────

  describe('delete', () => {
    it('removes document from list after successful delete', async () => {
      fetchDocuments.mockResolvedValue([makeDoc({ id: 5, name: 'To Delete' })])
      deleteDocument.mockResolvedValue()
      vi.spyOn(window, 'confirm').mockReturnValue(true)

      const pinia = createPinia()
      setActivePinia(pinia)
      const auth = useAuthStore()
      vi.spyOn(auth, 'isAdminOrManager', 'get').mockReturnValue(true)

      const wrapper = mount(DocumentsView, { global: { plugins: [pinia] }, attachTo: document.body })
      await flushPromises()

      expect(wrapper.text()).toContain('To Delete')
      await wrapper.find('.doc-btn--danger').trigger('click')
      await flushPromises()

      expect(deleteDocument).toHaveBeenCalledWith(5)
      expect(wrapper.text()).not.toContain('To Delete')
    })

    it('does not delete when user cancels confirmation', async () => {
      fetchDocuments.mockResolvedValue([makeDoc({ id: 5, name: 'Keep Me' })])
      vi.spyOn(window, 'confirm').mockReturnValue(false)

      const pinia = createPinia()
      setActivePinia(pinia)
      const auth = useAuthStore()
      vi.spyOn(auth, 'isAdminOrManager', 'get').mockReturnValue(true)

      const wrapper = mount(DocumentsView, { global: { plugins: [pinia] }, attachTo: document.body })
      await flushPromises()
      await wrapper.find('.doc-btn--danger').trigger('click')
      await flushPromises()

      expect(deleteDocument).not.toHaveBeenCalled()
      expect(wrapper.text()).toContain('Keep Me')
    })
  })

  // ── Category collapse ──────────────────────────────────────────────────

  describe('category collapse', () => {
    it('collapses a category section when header is clicked', async () => {
      fetchDocuments.mockResolvedValue([makeDoc({ id: 1, name: 'Policy Doc', category: 'GUIDELINES' })])
      const wrapper = await mountView()

      expect(wrapper.find('.doc-grid').exists()).toBe(true)
      await wrapper.find('.category-header').trigger('click')
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.doc-grid').exists()).toBe(false)
    })

    it('expands a collapsed category section when header is clicked again', async () => {
      fetchDocuments.mockResolvedValue([makeDoc({ id: 1, name: 'Policy Doc', category: 'GUIDELINES' })])
      const wrapper = await mountView()

      await wrapper.find('.category-header').trigger('click')
      await wrapper.vm.$nextTick()
      await wrapper.find('.category-header').trigger('click')
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.doc-grid').exists()).toBe(true)
    })
  })

})
