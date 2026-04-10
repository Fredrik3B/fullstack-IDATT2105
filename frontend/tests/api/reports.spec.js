import { describe, it, expect, beforeEach, vi } from 'vitest'

vi.mock('@/api/axiosInstance', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

import api from '@/api/axiosInstance'
import {
  fetchInspectionReport,
  fetchSummaryReport,
  createDeviationReport,
} from '@/api/reports'

beforeEach(() => {
  vi.clearAllMocks()
})

// ── Tests ──────────────────────────────────────────────────────────────────

describe('reports API', () => {

  // ── fetchInspectionReport ──────────────────────────────────────────────

  describe('fetchInspectionReport', () => {
    it('calls GET /api/reports/full-report with no params by default', async () => {
      api.get.mockResolvedValue({ data: {} })
      await fetchInspectionReport()
      expect(api.get).toHaveBeenCalledWith('/api/reports/full-report', { params: {} })
    })

    it('includes from param when provided', async () => {
      api.get.mockResolvedValue({ data: {} })
      await fetchInspectionReport({ from: '2024-01-01T00:00:00' })
      expect(api.get).toHaveBeenCalledWith('/api/reports/full-report', {
        params: { from: '2024-01-01T00:00:00' },
      })
    })

    it('includes to param when provided', async () => {
      api.get.mockResolvedValue({ data: {} })
      await fetchInspectionReport({ to: '2024-01-31T23:59:59' })
      expect(api.get).toHaveBeenCalledWith('/api/reports/full-report', {
        params: { to: '2024-01-31T23:59:59' },
      })
    })

    it('includes both from and to when both provided', async () => {
      api.get.mockResolvedValue({ data: {} })
      await fetchInspectionReport({ from: '2024-01-01T00:00:00', to: '2024-01-31T23:59:59' })
      expect(api.get).toHaveBeenCalledWith('/api/reports/full-report', {
        params: { from: '2024-01-01T00:00:00', to: '2024-01-31T23:59:59' },
      })
    })

    it('omits falsy from/to values', async () => {
      api.get.mockResolvedValue({ data: {} })
      await fetchInspectionReport({ from: '', to: null })
      expect(api.get).toHaveBeenCalledWith('/api/reports/full-report', { params: {} })
    })

    it('returns the data from the response', async () => {
      const report = { sections: [], period: 'MONTHLY' }
      api.get.mockResolvedValue({ data: report })
      const result = await fetchInspectionReport()
      expect(result).toEqual(report)
    })

    it('propagates errors from the API', async () => {
      api.get.mockRejectedValue(new Error('Network error'))
      await expect(fetchInspectionReport()).rejects.toThrow('Network error')
    })
  })

  // ── fetchSummaryReport ─────────────────────────────────────────────────

  describe('fetchSummaryReport', () => {
    it('calls GET /api/reports/summary with no params by default', async () => {
      api.get.mockResolvedValue({ data: {} })
      await fetchSummaryReport()
      expect(api.get).toHaveBeenCalledWith('/api/reports/summary', { params: {} })
    })

    it('includes from param when provided', async () => {
      api.get.mockResolvedValue({ data: {} })
      await fetchSummaryReport({ from: '2024-03-01T00:00:00' })
      expect(api.get).toHaveBeenCalledWith('/api/reports/summary', {
        params: { from: '2024-03-01T00:00:00' },
      })
    })

    it('includes both from and to when both provided', async () => {
      api.get.mockResolvedValue({ data: {} })
      await fetchSummaryReport({ from: '2024-03-01T00:00:00', to: '2024-03-31T23:59:59' })
      expect(api.get).toHaveBeenCalledWith('/api/reports/summary', {
        params: { from: '2024-03-01T00:00:00', to: '2024-03-31T23:59:59' },
      })
    })

    it('omits falsy from/to values', async () => {
      api.get.mockResolvedValue({ data: {} })
      await fetchSummaryReport({ from: null, to: undefined })
      expect(api.get).toHaveBeenCalledWith('/api/reports/summary', { params: {} })
    })

    it('returns the data from the response', async () => {
      const summary = { totalDeviations: 3, completionRate: 0.85 }
      api.get.mockResolvedValue({ data: summary })
      const result = await fetchSummaryReport()
      expect(result).toEqual(summary)
    })

    it('propagates errors from the API', async () => {
      api.get.mockRejectedValue(new Error('Unauthorized'))
      await expect(fetchSummaryReport()).rejects.toThrow('Unauthorized')
    })
  })

  // ── createDeviationReport ──────────────────────────────────────────────

  describe('createDeviationReport', () => {
    it('calls POST /api/reports/deviations with the provided report', async () => {
      const report = { title: 'Spoiled fish', severity: 'HIGH' }
      api.post.mockResolvedValue({ data: { id: 7, ...report } })

      await createDeviationReport(report)

      expect(api.post).toHaveBeenCalledWith('/api/reports/deviations', report)
    })

    it('returns the created deviation report from the response', async () => {
      const created = { id: 7, title: 'Spoiled fish', severity: 'HIGH' }
      api.post.mockResolvedValue({ data: created })
      const result = await createDeviationReport({ title: 'Spoiled fish', severity: 'HIGH' })
      expect(result).toEqual(created)
    })

    it('propagates errors from the API', async () => {
      api.post.mockRejectedValue(new Error('Validation failed'))
      await expect(createDeviationReport({})).rejects.toThrow('Validation failed')
    })
  })

})
