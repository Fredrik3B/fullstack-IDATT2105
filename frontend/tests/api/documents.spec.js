import { describe, it, expect, beforeEach, vi } from 'vitest'

vi.mock('@/api/axiosInstance', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    delete: vi.fn(),
  },
}))

import api from '@/api/axiosInstance'
import {
  fetchDocuments,
  uploadDocument,
  downloadDocument,
  deleteDocument,
} from '@/api/documents'

beforeEach(() => {
  vi.clearAllMocks()
})

// ── Tests ──────────────────────────────────────────────────────────────────

describe('documents API', () => {

  // ── fetchDocuments ─────────────────────────────────────────────────────

  describe('fetchDocuments', () => {
    it('calls GET /api/documents with no params by default', async () => {
      api.get.mockResolvedValue({ data: [] })
      await fetchDocuments()
      expect(api.get).toHaveBeenCalledWith('/api/documents', { params: {} })
    })

    it('includes category param when provided', async () => {
      api.get.mockResolvedValue({ data: [] })
      await fetchDocuments({ category: 'CERTIFICATE' })
      expect(api.get).toHaveBeenCalledWith('/api/documents', { params: { category: 'CERTIFICATE' } })
    })

    it('includes module param when provided', async () => {
      api.get.mockResolvedValue({ data: [] })
      await fetchDocuments({ module: 'IC_FOOD' })
      expect(api.get).toHaveBeenCalledWith('/api/documents', { params: { module: 'IC_FOOD' } })
    })

    it('includes both category and module when both provided', async () => {
      api.get.mockResolvedValue({ data: [] })
      await fetchDocuments({ category: 'TRAINING', module: 'IC_ALCOHOL' })
      expect(api.get).toHaveBeenCalledWith('/api/documents', {
        params: { category: 'TRAINING', module: 'IC_ALCOHOL' },
      })
    })

    it('omits falsy category/module from params', async () => {
      api.get.mockResolvedValue({ data: [] })
      await fetchDocuments({ category: '', module: null })
      expect(api.get).toHaveBeenCalledWith('/api/documents', { params: {} })
    })

    it('returns the data from the response', async () => {
      const docs = [{ id: 1, name: 'Policy' }]
      api.get.mockResolvedValue({ data: docs })
      const result = await fetchDocuments()
      expect(result).toEqual(docs)
    })

    it('propagates errors from the API', async () => {
      api.get.mockRejectedValue(new Error('Network error'))
      await expect(fetchDocuments()).rejects.toThrow('Network error')
    })
  })

  // ── uploadDocument ─────────────────────────────────────────────────────

  describe('uploadDocument', () => {
    it('calls POST /api/documents with the provided FormData', async () => {
      const formData = new FormData()
      formData.append('name', 'My Doc')
      api.post.mockResolvedValue({ data: { id: 99, name: 'My Doc' } })

      await uploadDocument(formData)

      expect(api.post).toHaveBeenCalledWith(
        '/api/documents',
        formData,
        {
          headers: { 'Content-Type': undefined },
          timeout: 120000,
        },
      )
    })

    it('returns the created document from the response', async () => {
      const newDoc = { id: 99, name: 'My Doc' }
      api.post.mockResolvedValue({ data: newDoc })
      const result = await uploadDocument(new FormData())
      expect(result).toEqual(newDoc)
    })

    it('propagates upload errors', async () => {
      api.post.mockRejectedValue(new Error('Upload failed'))
      await expect(uploadDocument(new FormData())).rejects.toThrow('Upload failed')
    })
  })

  // ── downloadDocument ───────────────────────────────────────────────────

  describe('downloadDocument', () => {
    it('calls GET /api/documents/:id/download with responseType blob', async () => {
      const fakeResponse = { data: new Blob(['pdf content']) }
      api.get.mockResolvedValue(fakeResponse)

      await downloadDocument(42)

      expect(api.get).toHaveBeenCalledWith('/api/documents/42/download', { responseType: 'blob' })
    })

    it('returns the full response (not just data)', async () => {
      const fakeResponse = { data: new Blob(), headers: { 'content-type': 'application/pdf' } }
      api.get.mockResolvedValue(fakeResponse)
      const result = await downloadDocument(42)
      expect(result).toBe(fakeResponse)
    })

    it('propagates download errors', async () => {
      api.get.mockRejectedValue(new Error('Not found'))
      await expect(downloadDocument(99)).rejects.toThrow('Not found')
    })
  })

  // ── deleteDocument ─────────────────────────────────────────────────────

  describe('deleteDocument', () => {
    it('calls DELETE /api/documents/:id', async () => {
      api.delete.mockResolvedValue({})
      await deleteDocument(5)
      expect(api.delete).toHaveBeenCalledWith('/api/documents/5')
    })

    it('does not return a value', async () => {
      api.delete.mockResolvedValue({})
      const result = await deleteDocument(5)
      expect(result).toBeUndefined()
    })

    it('propagates delete errors', async () => {
      api.delete.mockRejectedValue(new Error('Forbidden'))
      await expect(deleteDocument(5)).rejects.toThrow('Forbidden')
    })
  })

})
