import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import DocumentsView from '@/views/DocumentsView.vue'

// ── Mocks ──────────────────────────────────────────────────────────────────

const mockRoute = { query: {} }
const mockRouterPush = vi.fn()

vi.mock('vue-router', () => ({
  useRoute: () => mockRoute,
  useRouter: () => ({ push: mockRouterPush }),
}))

vi.mock('@/api/documents', () => ({
  fetchDocuments: vi.fn(),
  downloadDocument: vi.fn(),
  deleteDocument: vi.fn(),
}))

vi.mock('@/composables/useToast', () => ({
  useToast: () => ({ success: vi.fn(), error: vi.fn(), info: vi.fn() }),
}))

// Stub child components — DocumentCard renders doc.name so filtering tests work
vi.mock('@/components/documents/DocumentCard.vue', () => ({
  default: {
    template: '<div class="doc-card"><span class="doc-name">{{ doc?.name }}</span><button class="delete-btn" @click="$emit(\'delete\', doc)">Delete</button></div>',
    props: ['doc', 'isAdminOrManager'],
    emits: ['preview', 'download', 'delete'],
  },
}))

vi.mock('@/components/documents/DocumentUploadModal.vue', () => ({
  default: { template: '<div class="upload-modal" />', emits: ['uploaded', 'close'] },
}))

vi.mock('@/components/documents/DocumentPreviewModal.vue', () => ({
  default: { template: '<div class="preview-modal" />', props: ['doc', 'previewUrl', 'loading', 'error'], emits: ['close', 'download'] },
}))

vi.mock('lucide-vue-next', () => ({
  Search: { template: '<span />' },
  AlertTriangle: { template: '<span />' },
  ChevronDown: { template: '<span />' },
}))

import { fetchDocuments, deleteDocument } from '@/api/documents'
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

function mountView(authSetup = () => {}) {
  const pinia = createPinia()
  setActivePinia(pinia)

  const auth = useAuthStore()
  auth.user = { id: 1, name: 'Test User', email: 'test@example.com' }
  auth.accessToken = 'fake-token'
  authSetup(auth)

  return mount(DocumentsView, {
    global: { plugins: [pinia] },
    attachTo: document.body,
  })
}

// ── Setup ──────────────────────────────────────────────────────────────────

beforeEach(() => {
  vi.clearAllMocks()
  mockRoute.query = {}
  mockRouterPush.mockClear()
  fetchDocuments.mockResolvedValue([])
})

// ── Tests ──────────────────────────────────────────────────────────────────

describe('DocumentsView', () => {

  // ── Loading & error states ─────────────────────────────────────────────

  describe('loading and error states', () => {
    it('shows loading state while fetching', async () => {
      let resolve
      fetchDocuments.mockReturnValue(new Promise(r => { resolve = r }))
      const wrapper = mountView()
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.loading-state').exists()).toBe(true)
      resolve([])
      await flushPromises()
      expect(wrapper.find('.loading-state').exists()).toBe(false)
    })

    it('shows error state when fetch fails', async () => {
      fetchDocuments.mockRejectedValue(new Error('Network error'))
      const wrapper = mountView()
      await flushPromises()
      expect(wrapper.find('.error-state').exists()).toBe(true)
      expect(wrapper.text()).toContain('Failed to load documents')
    })

    it('renders document grid when fetch succeeds with documents', async () => {
      fetchDocuments.mockResolvedValue([makeDoc()])
      const wrapper = mountView()
      await flushPromises()
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
      const wrapper = mountView()
      await flushPromises()

      await wrapper.find('.search-input').setValue('hygiene')
      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('Hygiene Policy')
      expect(wrapper.text()).not.toContain('Fire Safety Plan')
    })

    it('filters documents by original filename', async () => {
      fetchDocuments.mockResolvedValue([
        makeDoc({ id: 1, name: 'Document A', originalFileName: 'alcohol_policy.pdf' }),
        makeDoc({ id: 2, name: 'Document B', originalFileName: 'food_guide.pdf' }),
      ])
      const wrapper = mountView()
      await flushPromises()

      await wrapper.find('.search-input').setValue('alcohol')
      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('Document A')
      expect(wrapper.text()).not.toContain('Document B')
    })

    it('filters documents by category select', async () => {
      fetchDocuments.mockResolvedValue([
        makeDoc({ id: 1, name: 'Doc A', category: 'GUIDELINES' }),
        makeDoc({ id: 2, name: 'Doc B', category: 'TRAINING' }),
      ])
      const wrapper = mountView()
      await flushPromises()

      await wrapper.find('#filter-cat').setValue('TRAINING')
      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('Doc B')
      expect(wrapper.text()).not.toContain('Doc A')
    })

    it('filters documents by module select', async () => {
      fetchDocuments.mockResolvedValue([
        makeDoc({ id: 1, name: 'Shared Doc', module: 'SHARED' }),
        makeDoc({ id: 2, name: 'Food Doc', module: 'IC_FOOD', category: 'GUIDELINES' }),
      ])
      const wrapper = mountView()
      await flushPromises()

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
      const wrapper = mountView()
      await flushPromises()

      expect(wrapper.text()).toContain('Doc A')
      expect(wrapper.text()).toContain('Doc B')
    })

    it('updates document count in insight card after filtering', async () => {
      fetchDocuments.mockResolvedValue([
        makeDoc({ id: 1, name: 'Doc A', category: 'GUIDELINES' }),
        makeDoc({ id: 2, name: 'Doc B', category: 'TRAINING' }),
      ])
      const wrapper = mountView()
      await flushPromises()

      expect(wrapper.find('.insight-value').text()).toBe('2')

      await wrapper.find('#filter-cat').setValue('GUIDELINES')
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.insight-value').text()).toBe('1')
    })
  })

  // ── Route query filters ─────────────────────────────────────────────────

  describe('route query filters', () => {
    it('pre-fills category filter from route query', async () => {
      mockRoute.query = { category: 'training' }
      fetchDocuments.mockResolvedValue([
        makeDoc({ id: 1, name: 'Training Doc', category: 'TRAINING' }),
        makeDoc({ id: 2, name: 'Guide Doc', category: 'GUIDELINES' }),
      ])
      const wrapper = mountView()
      await flushPromises()

      expect(wrapper.text()).toContain('Training Doc')
      expect(wrapper.text()).not.toContain('Guide Doc')
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
      const wrapper = mountView()
      await flushPromises()

      expect(wrapper.find('.expiry-banner').exists()).toBe(true)
      expect(wrapper.text()).toContain('Food Safety Cert')
    })

    it('does not show expiry banner when certificate expires more than 30 days away', async () => {
      const future = new Date()
      future.setDate(future.getDate() + 60)
      fetchDocuments.mockResolvedValue([
        makeDoc({ id: 1, name: 'Valid Cert', category: 'CERTIFICATE', expiryDate: future.toISOString().split('T')[0] }),
      ])
      const wrapper = mountView()
      await flushPromises()

      expect(wrapper.find('.expiry-banner').exists()).toBe(false)
    })

    it('dismisses the expiry banner when close button is clicked', async () => {
      const soon = new Date()
      soon.setDate(soon.getDate() + 5)
      fetchDocuments.mockResolvedValue([
        makeDoc({ id: 1, name: 'Expiring Cert', category: 'CERTIFICATE', expiryDate: soon.toISOString().split('T')[0] }),
      ])
      const wrapper = mountView()
      await flushPromises()

      await wrapper.find('.expiry-banner-close').trigger('click')
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.expiry-banner').exists()).toBe(false)
    })

    it('marks expired certificates with expired chip class', async () => {
      const past = new Date()
      past.setDate(past.getDate() - 5)
      fetchDocuments.mockResolvedValue([
        makeDoc({ id: 1, name: 'Expired Cert', category: 'CERTIFICATE', expiryDate: past.toISOString().split('T')[0] }),
      ])
      const wrapper = mountView()
      await flushPromises()

      expect(wrapper.find('.expiry-chip--expired').exists()).toBe(true)
      expect(wrapper.text()).toContain('Expired')
    })

    it('marks soon-expiring certificates with warning chip class', async () => {
      const soon = new Date()
      soon.setDate(soon.getDate() + 15)
      fetchDocuments.mockResolvedValue([
        makeDoc({ id: 1, name: 'Warning Cert', category: 'CERTIFICATE', expiryDate: soon.toISOString().split('T')[0] }),
      ])
      const wrapper = mountView()
      await flushPromises()

      expect(wrapper.find('.expiry-chip--warning').exists()).toBe(true)
    })
  })

  // ── Upload button visibility ────────────────────────────────────────────

  describe('upload button visibility', () => {
    it('shows upload button for admin or manager', async () => {
      const wrapper = mountView(auth => {
        vi.spyOn(auth, 'isAdminOrManager', 'get').mockReturnValue(true)
      })
      await flushPromises()

      expect(wrapper.find('.btn-upload').exists()).toBe(true)
    })

    it('hides upload button for staff', async () => {
      const wrapper = mountView(auth => {
        vi.spyOn(auth, 'isAdminOrManager', 'get').mockReturnValue(false)
      })
      await flushPromises()

      expect(wrapper.find('.btn-upload').exists()).toBe(false)
    })

    it('opens upload modal when upload button is clicked', async () => {
      const wrapper = mountView(auth => {
        vi.spyOn(auth, 'isAdminOrManager', 'get').mockReturnValue(true)
      })
      await flushPromises()

      expect(wrapper.vm.showUploadModal).toBe(false)
      await wrapper.find('.btn-upload').trigger('click')
      // Modal is teleported to body — check reactive state instead
      expect(wrapper.vm.showUploadModal).toBe(true)
    })
  })

  // ── Category collapse ──────────────────────────────────────────────────

  describe('category collapse', () => {
    it('collapses a category section when header is clicked', async () => {
      fetchDocuments.mockResolvedValue([makeDoc({ id: 1, name: 'Policy Doc', category: 'GUIDELINES' })])
      const wrapper = mountView()
      await flushPromises()

      expect(wrapper.find('.doc-grid').exists()).toBe(true)
      await wrapper.find('.category-header').trigger('click')
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.doc-grid').exists()).toBe(false)
    })

    it('expands a collapsed category when header is clicked again', async () => {
      fetchDocuments.mockResolvedValue([makeDoc({ id: 1, name: 'Policy Doc', category: 'GUIDELINES' })])
      const wrapper = mountView()
      await flushPromises()

      await wrapper.find('.category-header').trigger('click')
      await wrapper.vm.$nextTick()
      await wrapper.find('.category-header').trigger('click')
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.doc-grid').exists()).toBe(true)
    })
  })

  // ── Delete via DocumentCard event ──────────────────────────────────────

  describe('delete', () => {
    it('calls deleteDocument and removes doc when confirmed', async () => {
      fetchDocuments.mockResolvedValue([makeDoc({ id: 5, name: 'To Delete' })])
      deleteDocument.mockResolvedValue()
      vi.spyOn(window, 'confirm').mockReturnValue(true)

      const wrapper = mountView()
      await flushPromises()

      expect(wrapper.text()).toContain('To Delete')
      await wrapper.find('.delete-btn').trigger('click')
      await flushPromises()

      expect(deleteDocument).toHaveBeenCalledWith(5)
      expect(wrapper.text()).not.toContain('To Delete')
    })

    it('does not delete when user cancels confirmation', async () => {
      fetchDocuments.mockResolvedValue([makeDoc({ id: 5, name: 'Keep Me' })])
      vi.spyOn(window, 'confirm').mockReturnValue(false)

      const wrapper = mountView()
      await flushPromises()

      await wrapper.find('.delete-btn').trigger('click')
      await flushPromises()

      expect(deleteDocument).not.toHaveBeenCalled()
      expect(wrapper.text()).toContain('Keep Me')
    })
  })

})
