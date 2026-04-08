import { describe, it, expect, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useToast } from '@/composables/useToast'
import { useToastStore } from '@/stores/toast'

beforeEach(() => {
  setActivePinia(createPinia())
})

describe('useToast', () => {
  it('returns the toast store instance', () => {
    const toast = useToast()
    const store = useToastStore()
    expect(toast).toBe(store)
  })

  it('exposes success, error, warning, info, add, remove, and toasts', () => {
    const toast = useToast()
    expect(typeof toast.success).toBe('function')
    expect(typeof toast.error).toBe('function')
    expect(typeof toast.warning).toBe('function')
    expect(typeof toast.info).toBe('function')
    expect(typeof toast.add).toBe('function')
    expect(typeof toast.remove).toBe('function')
    expect(Array.isArray(toast.toasts)).toBe(true)
  })

  it('adding a toast via useToast is reflected in useToastStore', () => {
    const toast = useToast()
    toast.success('Hello')
    const store = useToastStore()
    expect(store.toasts).toHaveLength(1)
    expect(store.toasts[0].message).toBe('Hello')
    expect(store.toasts[0].type).toBe('success')
  })
})
