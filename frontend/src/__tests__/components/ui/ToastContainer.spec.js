import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import { useToastStore } from '@/stores/toast'

// Stub lucide icons so they render as simple spans
vi.mock('lucide-vue-next', () => ({
  CheckCircle:   { template: '<span class="icon-success" />' },
  AlertCircle:   { template: '<span class="icon-error" />' },
  AlertTriangle: { template: '<span class="icon-warning" />' },
  Info:          { template: '<span class="icon-info" />' },
  X:             { template: '<span class="icon-x" />' },
}))

// ── Helpers ────────────────────────────────────────────────────────────────

function mountContainer() {
  const pinia = createPinia()
  setActivePinia(pinia)

  // Stub Teleport so the toast content renders inline (not in document.body)
  return mount(ToastContainer, {
    global: {
      plugins: [pinia],
      stubs: { Teleport: true },
    },
  })
}

// ── Setup ──────────────────────────────────────────────────────────────────

beforeEach(() => {
  vi.useFakeTimers()
})

// ── Tests ──────────────────────────────────────────────────────────────────

describe('ToastContainer', () => {

  // ── Empty state ────────────────────────────────────────────────────────

  describe('empty state', () => {
    it('renders no toasts when the store is empty', () => {
      const wrapper = mountContainer()
      expect(wrapper.findAll('.toast')).toHaveLength(0)
    })
  })

  // ── Rendering toasts ───────────────────────────────────────────────────

  describe('rendering toasts', () => {
    it('renders a toast message from the store', () => {
      const wrapper = mountContainer()
      const store = useToastStore()
      store.success('File uploaded')
      return wrapper.vm.$nextTick().then(() => {
        expect(wrapper.find('.toast-message').text()).toBe('File uploaded')
      })
    })

    it('applies toast--success class for success type', async () => {
      const wrapper = mountContainer()
      useToastStore().success('Done')
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.toast').classes()).toContain('toast--success')
    })

    it('applies toast--error class for error type', async () => {
      const wrapper = mountContainer()
      useToastStore().error('Something failed')
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.toast').classes()).toContain('toast--error')
    })

    it('applies toast--warning class for warning type', async () => {
      const wrapper = mountContainer()
      useToastStore().warning('Low disk space')
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.toast').classes()).toContain('toast--warning')
    })

    it('applies toast--info class for info type', async () => {
      const wrapper = mountContainer()
      useToastStore().info('Session started')
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.toast').classes()).toContain('toast--info')
    })

    it('renders multiple toasts', async () => {
      const wrapper = mountContainer()
      const store = useToastStore()
      store.success('First')
      store.error('Second')
      await wrapper.vm.$nextTick()
      expect(wrapper.findAll('.toast')).toHaveLength(2)
    })
  })

  // ── Close button ───────────────────────────────────────────────────────

  describe('close button', () => {
    it('removes a toast when close button is clicked', async () => {
      const wrapper = mountContainer()
      useToastStore().success('Removable toast')
      await wrapper.vm.$nextTick()

      expect(wrapper.findAll('.toast')).toHaveLength(1)
      await wrapper.find('.toast-close').trigger('click')
      await wrapper.vm.$nextTick()

      expect(wrapper.findAll('.toast')).toHaveLength(0)
    })

    it('removes only the clicked toast when multiple exist', async () => {
      const wrapper = mountContainer()
      const store = useToastStore()
      store.success('Keep me')
      store.error('Remove me')
      await wrapper.vm.$nextTick()

      const closeButtons = wrapper.findAll('.toast-close')
      await closeButtons[1].trigger('click')
      await wrapper.vm.$nextTick()

      expect(wrapper.findAll('.toast')).toHaveLength(1)
      expect(wrapper.find('.toast-message').text()).toBe('Keep me')
    })
  })

  // ── Auto-dismiss ───────────────────────────────────────────────────────

  describe('auto-dismiss', () => {
    it('auto-removes a success toast after 4 seconds', async () => {
      const wrapper = mountContainer()
      useToastStore().success('Auto-gone')
      await wrapper.vm.$nextTick()
      expect(wrapper.findAll('.toast')).toHaveLength(1)

      vi.advanceTimersByTime(4000)
      await wrapper.vm.$nextTick()

      expect(wrapper.findAll('.toast')).toHaveLength(0)
    })

    it('does not auto-remove an error toast (duration 0)', async () => {
      const wrapper = mountContainer()
      useToastStore().error('Persistent error')
      await wrapper.vm.$nextTick()

      vi.advanceTimersByTime(10000)
      await wrapper.vm.$nextTick()

      expect(wrapper.findAll('.toast')).toHaveLength(1)
    })
  })

  // ── Toast store: add / remove API ──────────────────────────────────────

  describe('toast store', () => {
    it('store.add with custom duration auto-dismisses after that duration', async () => {
      const wrapper = mountContainer()
      const store = useToastStore()
      store.add({ type: 'info', message: 'Custom duration', duration: 2000 })
      await wrapper.vm.$nextTick()
      expect(wrapper.findAll('.toast')).toHaveLength(1)

      vi.advanceTimersByTime(2000)
      await wrapper.vm.$nextTick()

      expect(wrapper.findAll('.toast')).toHaveLength(0)
    })

    it('store.remove by id removes the correct toast', async () => {
      const wrapper = mountContainer()
      const store = useToastStore()
      store.add({ type: 'success', message: 'Stay', duration: 0 })
      store.add({ type: 'info', message: 'Go', duration: 0 })
      await wrapper.vm.$nextTick()

      const idToRemove = store.toasts[1].id
      store.remove(idToRemove)
      await wrapper.vm.$nextTick()

      expect(wrapper.findAll('.toast')).toHaveLength(1)
      expect(wrapper.find('.toast-message').text()).toBe('Stay')
    })
  })

})
