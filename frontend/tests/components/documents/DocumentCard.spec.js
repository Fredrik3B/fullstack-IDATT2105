import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import DocumentCard from '@/components/documents/DocumentCard.vue'

// ── Helpers ────────────────────────────────────────────────────────────────

function makeDoc(overrides = {}) {
  return {
    id: 1,
    name: 'Hygiene Policy',
    originalFileName: 'hygiene.pdf',
    category: 'GUIDELINES',
    module: 'SHARED',
    fileType: 'application/pdf',
    fileSize: 204800,
    uploadedAt: '2025-06-01T10:00:00Z',
    uploadedByName: 'Alice Smith',
    expiryDate: null,
    externalUrl: null,
    description: '',
    ...overrides,
  }
}

function mountCard(docOverrides = {}, props = {}) {
  return mount(DocumentCard, {
    props: { doc: makeDoc(docOverrides), ...props },
    attachTo: document.body,
  })
}

// ── Tests ──────────────────────────────────────────────────────────────────

describe('DocumentCard', () => {

  // ── Basic rendering ─────────────────────────────────────────────────────

  describe('rendering', () => {
    it('displays the document name', () => {
      const wrapper = mountCard({ name: 'Fire Safety Plan' })
      expect(wrapper.find('.doc-name').text()).toBe('Fire Safety Plan')
    })

    it('displays the uploader name', () => {
      const wrapper = mountCard({ uploadedByName: 'Bob Jones' })
      expect(wrapper.text()).toContain('Bob Jones')
    })

    it('displays the formatted file size', () => {
      const wrapper = mountCard({ fileSize: 1024 })
      expect(wrapper.text()).toContain('1.0 KB')
    })

    it('shows "PDF" type label for PDF file', () => {
      const wrapper = mountCard({ fileType: 'application/pdf' })
      expect(wrapper.find('.doc-card-type').text()).toBe('PDF')
    })

    it('shows "IMG" type label for image file', () => {
      const wrapper = mountCard({ fileType: 'image/png' })
      expect(wrapper.find('.doc-card-type').text()).toBe('IMG')
    })

    it('shows "LINK" type label for external URL documents', () => {
      const wrapper = mountCard({ externalUrl: 'https://example.com', fileType: null })
      expect(wrapper.find('.doc-card-type').text()).toBe('LINK')
    })

    it('applies pdf icon class for pdf file type', () => {
      const wrapper = mountCard({ fileType: 'application/pdf' })
      expect(wrapper.find('.doc-card-thumb').classes()).toContain('doc-icon--pdf')
    })

    it('applies link icon class for external URL', () => {
      const wrapper = mountCard({ externalUrl: 'https://example.com' })
      expect(wrapper.find('.doc-card-thumb').classes()).toContain('doc-icon--link')
    })
  })

  // ── Module badge ─────────────────────────────────────────────────────────

  describe('module badge', () => {
    it('shows Shared badge for SHARED module', () => {
      const wrapper = mountCard({ module: 'SHARED' })
      const badge = wrapper.find('.doc-badge')
      expect(badge.text()).toBe('Shared')
      expect(badge.classes()).toContain('doc-badge--shared')
    })

    it('shows IC-Food badge for IC_FOOD module', () => {
      const wrapper = mountCard({ module: 'IC_FOOD' })
      const badge = wrapper.find('.doc-badge')
      expect(badge.text()).toBe('IC-Food')
      expect(badge.classes()).toContain('doc-badge--food')
    })

    it('shows IC-Alcohol badge for IC_ALCOHOL module', () => {
      const wrapper = mountCard({ module: 'IC_ALCOHOL' })
      const badge = wrapper.find('.doc-badge')
      expect(badge.text()).toBe('IC-Alcohol')
      expect(badge.classes()).toContain('doc-badge--alcohol')
    })
  })

  // ── Expiry chip (certificates only) ─────────────────────────────────────

  describe('expiry chip', () => {
    it('does not show expiry chip for non-certificate documents', () => {
      const wrapper = mountCard({ category: 'GUIDELINES', expiryDate: '2030-01-01' })
      expect(wrapper.find('.cert-expiry').exists()).toBe(false)
    })

    it('does not show expiry chip for certificate with no expiry date', () => {
      const wrapper = mountCard({ category: 'CERTIFICATE', expiryDate: null })
      expect(wrapper.find('.cert-expiry').exists()).toBe(false)
    })

    it('shows "Valid" with ok class for certificate expiring far in the future', () => {
      const far = new Date()
      far.setDate(far.getDate() + 90)
      const wrapper = mountCard({ category: 'CERTIFICATE', expiryDate: far.toISOString().split('T')[0] })
      const chip = wrapper.find('.cert-expiry')
      expect(chip.exists()).toBe(true)
      expect(chip.text()).toBe('Valid')
      expect(chip.classes()).toContain('cert-expiry--ok')
    })

    it('shows "Expires in Xd" with warning class for certificate expiring within 30 days', () => {
      const soon = new Date()
      soon.setDate(soon.getDate() + 15)
      const wrapper = mountCard({ category: 'CERTIFICATE', expiryDate: soon.toISOString().split('T')[0] })
      const chip = wrapper.find('.cert-expiry')
      expect(chip.text()).toMatch(/Expires in \d+d/)
      expect(chip.classes()).toContain('cert-expiry--warning')
    })

    it('shows "Expired" with expired class for past expiry date', () => {
      const past = new Date()
      past.setDate(past.getDate() - 5)
      const wrapper = mountCard({ category: 'CERTIFICATE', expiryDate: past.toISOString().split('T')[0] })
      const chip = wrapper.find('.cert-expiry')
      expect(chip.text()).toBe('Expired')
      expect(chip.classes()).toContain('cert-expiry--expired')
    })
  })

  // ── Actions: download / open link ────────────────────────────────────────

  describe('actions', () => {
    it('shows Download button for regular files', () => {
      const wrapper = mountCard()
      expect(wrapper.find('button.doc-btn').text()).toBe('Download')
    })

    it('shows Open link anchor for external URL documents', () => {
      const wrapper = mountCard({ externalUrl: 'https://example.com' })
      const link = wrapper.find('a.doc-btn')
      expect(link.exists()).toBe(true)
      expect(link.text()).toBe('Open')
      expect(link.attributes('href')).toBe('https://example.com')
      expect(link.attributes('target')).toBe('_blank')
    })

    it('normalizes protocol-less external links to https', () => {
      const wrapper = mountCard({ externalUrl: 'example.com/docs' })
      const link = wrapper.find('a.doc-btn')
      expect(link.exists()).toBe(true)
      expect(link.attributes('href')).toBe('https://example.com/docs')
    })

    it('hides Delete button when isAdminOrManager is false', () => {
      const wrapper = mountCard({}, { isAdminOrManager: false })
      expect(wrapper.find('.doc-btn--danger').exists()).toBe(false)
    })

    it('shows Delete button when isAdminOrManager is true', () => {
      const wrapper = mountCard({}, { isAdminOrManager: true })
      expect(wrapper.find('.doc-btn--danger').exists()).toBe(true)
    })
  })

  // ── Emits ────────────────────────────────────────────────────────────────

  describe('emits', () => {
    it('emits "preview" with the doc when card thumbnail is clicked', async () => {
      const doc = makeDoc()
      const wrapper = mount(DocumentCard, { props: { doc } })
      await wrapper.find('.doc-card').trigger('click')
      expect(wrapper.emitted('preview')).toBeTruthy()
      expect(wrapper.emitted('preview')[0][0]).toEqual(doc)
    })

    it('emits "download" with the doc when Download button is clicked', async () => {
      const doc = makeDoc()
      const wrapper = mount(DocumentCard, { props: { doc } })
      await wrapper.find('button.doc-btn').trigger('click')
      expect(wrapper.emitted('download')).toBeTruthy()
      expect(wrapper.emitted('download')[0][0]).toEqual(doc)
    })

    it('emits "delete" with the doc when Delete button is clicked', async () => {
      const doc = makeDoc()
      const wrapper = mount(DocumentCard, { props: { doc, isAdminOrManager: true } })
      await wrapper.find('.doc-btn--danger').trigger('click')
      expect(wrapper.emitted('delete')).toBeTruthy()
      expect(wrapper.emitted('delete')[0][0]).toEqual(doc)
    })

    it('does not emit "preview" when action buttons inside card body are clicked', async () => {
      const doc = makeDoc()
      const wrapper = mount(DocumentCard, { props: { doc } })
      // .doc-card-body uses @click.stop — clicks inside should not bubble to card
      await wrapper.find('.doc-card-body').trigger('click')
      expect(wrapper.emitted('preview')).toBeFalsy()
    })
  })

})
