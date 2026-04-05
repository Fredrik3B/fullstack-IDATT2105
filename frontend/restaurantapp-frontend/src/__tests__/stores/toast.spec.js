import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useToastStore } from '@/stores/toast'

describe('useToastStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  describe('initial state', () => {
    it('starts with an empty toasts list', () => {
      const store = useToastStore()
      expect(store.toasts).toEqual([])
    })
  })

  describe('add', () => {
    it('adds a toast with the given type and message', () => {
      const store = useToastStore()
      store.add({ type: 'success', message: 'Saved!' })
      expect(store.toasts).toHaveLength(1)
      expect(store.toasts[0].type).toBe('success')
      expect(store.toasts[0].message).toBe('Saved!')
    })

    it('assigns a unique numeric id to each toast', () => {
      const store = useToastStore()
      store.add({ type: 'success', message: 'A' })
      store.add({ type: 'error', message: 'B' })
      const [a, b] = store.toasts
      expect(typeof a.id).toBe('number')
      expect(typeof b.id).toBe('number')
      expect(a.id).not.toBe(b.id)
    })

    it('stores the duration on the toast object', () => {
      const store = useToastStore()
      store.add({ type: 'info', message: 'Info', duration: 1000 })
      expect(store.toasts[0].duration).toBe(1000)
    })

    it('defaults duration to 0 for error type', () => {
      const store = useToastStore()
      store.add({ type: 'error', message: 'Oops' })
      expect(store.toasts[0].duration).toBe(0)
    })

    it('defaults duration to 4000 for non-error types', () => {
      const store = useToastStore()
      store.add({ type: 'success', message: 'OK' })
      expect(store.toasts[0].duration).toBe(4000)
    })

    it('auto-removes a toast after its duration expires', () => {
      const store = useToastStore()
      store.add({ type: 'success', message: 'Gone soon', duration: 2000 })
      expect(store.toasts).toHaveLength(1)
      vi.advanceTimersByTime(2000)
      expect(store.toasts).toHaveLength(0)
    })

    it('does not auto-remove a toast with duration 0', () => {
      const store = useToastStore()
      store.add({ type: 'error', message: 'Stays', duration: 0 })
      vi.advanceTimersByTime(100000)
      expect(store.toasts).toHaveLength(1)
    })
  })

  describe('remove', () => {
    it('removes a toast by id', () => {
      const store = useToastStore()
      store.add({ type: 'success', message: 'To remove' })
      const id = store.toasts[0].id
      store.remove(id)
      expect(store.toasts).toHaveLength(0)
    })

    it('only removes the toast with the matching id', () => {
      const store = useToastStore()
      store.add({ type: 'success', message: 'Keep me' })
      store.add({ type: 'success', message: 'Remove me' })
      const idToRemove = store.toasts[1].id
      store.remove(idToRemove)
      expect(store.toasts).toHaveLength(1)
      expect(store.toasts[0].message).toBe('Keep me')
    })

    it('does nothing when id does not exist', () => {
      const store = useToastStore()
      store.add({ type: 'info', message: 'Existing' })
      store.remove(-999)
      expect(store.toasts).toHaveLength(1)
    })
  })

  describe('success shorthand', () => {
    it('adds a success toast', () => {
      const store = useToastStore()
      store.success('Operation succeeded')
      expect(store.toasts[0].type).toBe('success')
      expect(store.toasts[0].message).toBe('Operation succeeded')
    })

    it('uses the default non-error duration (4000ms)', () => {
      const store = useToastStore()
      store.success('OK')
      expect(store.toasts[0].duration).toBe(4000)
    })
  })

  describe('error shorthand', () => {
    it('adds an error toast', () => {
      const store = useToastStore()
      store.error('Something went wrong')
      expect(store.toasts[0].type).toBe('error')
      expect(store.toasts[0].message).toBe('Something went wrong')
    })

    it('uses duration 0 so the error persists until dismissed', () => {
      const store = useToastStore()
      store.error('Persistent error')
      expect(store.toasts[0].duration).toBe(0)
      vi.advanceTimersByTime(100000)
      expect(store.toasts).toHaveLength(1)
    })
  })

  describe('warning shorthand', () => {
    it('adds a warning toast', () => {
      const store = useToastStore()
      store.warning('Watch out')
      expect(store.toasts[0].type).toBe('warning')
      expect(store.toasts[0].message).toBe('Watch out')
    })

    it('uses a 6000ms duration for warnings', () => {
      const store = useToastStore()
      store.warning('Still here')
      expect(store.toasts[0].duration).toBe(6000)
      vi.advanceTimersByTime(5999)
      expect(store.toasts).toHaveLength(1)
      vi.advanceTimersByTime(1)
      expect(store.toasts).toHaveLength(0)
    })
  })

  describe('info shorthand', () => {
    it('adds an info toast', () => {
      const store = useToastStore()
      store.info('Just so you know')
      expect(store.toasts[0].type).toBe('info')
      expect(store.toasts[0].message).toBe('Just so you know')
    })

    it('uses the default non-error duration (4000ms)', () => {
      const store = useToastStore()
      store.info('Note')
      expect(store.toasts[0].duration).toBe(4000)
    })
  })
})
