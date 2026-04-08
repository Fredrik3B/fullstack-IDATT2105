import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/api/axiosInstance', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

import api from '@/api/axiosInstance'
import {
  createChecklist,
  deleteChecklist,
  fetchChecklists,
  setChecklistWorkbenchState,
  setTaskCompletion,
  setTaskFlag,
  submitChecklist,
  updateChecklist,
} from '@/api/checklists'

beforeEach(() => {
  vi.clearAllMocks()
})

describe('checklists API', () => {
  describe('fetchChecklists', () => {
    it('calls GET /api/checklists with module params and optional If-Modified-Since header', async () => {
      api.get.mockResolvedValue({
        status: 200,
        data: [{ id: 1 }],
        headers: { 'last-modified': 'Wed, 08 Apr 2026 10:00:00 GMT' },
      })

      const result = await fetchChecklists({
        module: 'IC_FOOD',
        ifModifiedSince: 'Tue, 07 Apr 2026 10:00:00 GMT',
      })

      expect(api.get).toHaveBeenCalledWith('/api/checklists', {
        params: { module: 'IC_FOOD' },
        headers: { 'If-Modified-Since': 'Tue, 07 Apr 2026 10:00:00 GMT' },
        validateStatus: expect.any(Function),
      })
      expect(result).toEqual({
        status: 200,
        data: [{ id: 1 }],
        lastModified: 'Wed, 08 Apr 2026 10:00:00 GMT',
      })
    })

    it('returns null data for 304 responses and omits the header when not provided', async () => {
      api.get.mockResolvedValue({
        status: 304,
        data: [{ id: 1 }],
        headers: {},
      })

      const result = await fetchChecklists()

      expect(api.get).toHaveBeenCalledWith('/api/checklists', {
        params: { module: undefined },
        headers: undefined,
        validateStatus: expect.any(Function),
      })
      expect(result).toEqual({
        status: 304,
        data: null,
        lastModified: null,
      })
    })

    it('accepts 304 as a valid status and rejects 500', async () => {
      api.get.mockResolvedValue({
        status: 200,
        data: [],
        headers: {},
      })

      await fetchChecklists()

      const [, options] = api.get.mock.calls[0]
      expect(options.validateStatus(304)).toBe(true)
      expect(options.validateStatus(500)).toBe(false)
    })
  })

  describe('task state actions', () => {
    it('persists completion updates and returns response data', async () => {
      api.put.mockResolvedValue({ data: { ok: true } })

      const result = await setTaskCompletion({
        checklistId: 'card-1',
        taskId: 'task-1',
        state: 'completed',
        periodKey: '2026-04-08',
        completedAt: '2026-04-08T09:30:00Z',
      })

      expect(api.put).toHaveBeenCalledWith('/api/checklists/card-1/tasks/task-1/completion', {
        state: 'completed',
        periodKey: '2026-04-08',
        completedAt: '2026-04-08T09:30:00Z',
      })
      expect(result).toEqual({ ok: true })
    })

    it('persists pending flag updates and returns response data', async () => {
      api.put.mockResolvedValue({ data: { state: 'pending' } })

      const result = await setTaskFlag({
        checklistId: 'card-1',
        taskId: 'task-1',
        state: 'pending',
        periodKey: '2026-04-08',
      })

      expect(api.put).toHaveBeenCalledWith('/api/checklists/card-1/tasks/task-1/flag', {
        state: 'pending',
        periodKey: '2026-04-08',
      })
      expect(result).toEqual({ state: 'pending' })
    })
  })

  describe('checklist mutations', () => {
    it('creates a checklist with the provided payload', async () => {
      api.post.mockResolvedValue({ data: { id: 'new-checklist' } })

      const payload = {
        module: 'IC_FOOD',
        period: 'daily',
        title: 'Opening checks',
        subtitle: 'Kitchen',
        recurring: true,
        displayedOnWorkbench: true,
        taskTemplateIds: ['template-1'],
      }

      const result = await createChecklist(payload)

      expect(api.post).toHaveBeenCalledWith('/api/checklists', payload)
      expect(result).toEqual({ id: 'new-checklist' })
    })

    it('updates a checklist with the provided payload', async () => {
      api.put.mockResolvedValue({ data: { id: 'card-1', title: 'Updated' } })

      const payload = {
        checklistId: 'card-1',
        period: 'weekly',
        title: 'Updated',
        subtitle: 'Bar',
        recurring: false,
        displayedOnWorkbench: false,
        taskTemplateIds: ['template-2'],
      }

      const result = await updateChecklist(payload)

      expect(api.put).toHaveBeenCalledWith('/api/checklists/card-1', {
        period: 'weekly',
        title: 'Updated',
        subtitle: 'Bar',
        recurring: false,
        displayedOnWorkbench: false,
        taskTemplateIds: ['template-2'],
      })
      expect(result).toEqual({ id: 'card-1', title: 'Updated' })
    })

    it('submits a checklist and toggles workbench state through dedicated endpoints', async () => {
      api.put
        .mockResolvedValueOnce({ data: { id: 'card-1', submitted: true } })
        .mockResolvedValueOnce({ data: { id: 'card-1', displayedOnWorkbench: false } })

      const submitted = await submitChecklist({ checklistId: 'card-1' })
      const workbenchState = await setChecklistWorkbenchState({
        checklistId: 'card-1',
        displayedOnWorkbench: false,
      })

      expect(api.put).toHaveBeenNthCalledWith(1, '/api/checklists/card-1/submit')
      expect(api.put).toHaveBeenNthCalledWith(2, '/api/checklists/card-1/workbench', {
        displayedOnWorkbench: false,
      })
      expect(submitted).toEqual({ id: 'card-1', submitted: true })
      expect(workbenchState).toEqual({ id: 'card-1', displayedOnWorkbench: false })
    })

    it('deletes a checklist without returning a value', async () => {
      api.delete.mockResolvedValue({})

      const result = await deleteChecklist({ checklistId: 'card-1' })

      expect(api.delete).toHaveBeenCalledWith('/api/checklists/card-1')
      expect(result).toBeUndefined()
    })
  })
})
