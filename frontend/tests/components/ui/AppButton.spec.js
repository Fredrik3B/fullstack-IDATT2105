import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import AppButton from '@/components/ui/AppButton.vue'

describe('AppButton', () => {

  // ── Rendering ──────────────────────────────────────────────────────────

  describe('rendering', () => {
    it('renders slot content', () => {
      const wrapper = mount(AppButton, { slots: { default: 'Save' } })
      expect(wrapper.text()).toBe('Save')
    })

    it('defaults to primary variant', () => {
      const wrapper = mount(AppButton)
      expect(wrapper.find('button').classes()).toContain('btn--primary')
    })

    it('applies secondary variant class', () => {
      const wrapper = mount(AppButton, { props: { variant: 'secondary' } })
      expect(wrapper.find('button').classes()).toContain('btn--secondary')
    })

    it('applies danger variant class', () => {
      const wrapper = mount(AppButton, { props: { variant: 'danger' } })
      expect(wrapper.find('button').classes()).toContain('btn--danger')
    })

    it('applies btn--full class when fullWidth is true', () => {
      const wrapper = mount(AppButton, { props: { fullWidth: true } })
      expect(wrapper.find('button').classes()).toContain('btn--full')
    })

    it('does not apply btn--full when fullWidth is false', () => {
      const wrapper = mount(AppButton, { props: { fullWidth: false } })
      expect(wrapper.find('button').classes()).not.toContain('btn--full')
    })

    it('passes the type prop to the native button', () => {
      const wrapper = mount(AppButton, { props: { type: 'submit' } })
      expect(wrapper.find('button').attributes('type')).toBe('submit')
    })

    it('defaults to type="button"', () => {
      const wrapper = mount(AppButton)
      expect(wrapper.find('button').attributes('type')).toBe('button')
    })
  })

  // ── Loading state ──────────────────────────────────────────────────────

  describe('loading state', () => {
    it('shows spinner and hides slot content when loading', () => {
      const wrapper = mount(AppButton, {
        props: { loading: true },
        slots: { default: 'Submit' },
      })
      expect(wrapper.find('.btn-spinner').exists()).toBe(true)
      expect(wrapper.text()).toBe('')
    })

    it('disables the button when loading', () => {
      const wrapper = mount(AppButton, { props: { loading: true } })
      expect(wrapper.find('button').element.disabled).toBe(true)
    })

    it('shows slot and no spinner when not loading', () => {
      const wrapper = mount(AppButton, {
        props: { loading: false },
        slots: { default: 'Submit' },
      })
      expect(wrapper.find('.btn-spinner').exists()).toBe(false)
      expect(wrapper.text()).toBe('Submit')
    })
  })

  // ── Disabled state ─────────────────────────────────────────────────────

  describe('disabled state', () => {
    it('disables the button when disabled prop is true', () => {
      const wrapper = mount(AppButton, { props: { disabled: true } })
      expect(wrapper.find('button').element.disabled).toBe(true)
    })

    it('is not disabled by default', () => {
      const wrapper = mount(AppButton)
      expect(wrapper.find('button').element.disabled).toBe(false)
    })

    it('is disabled when both loading and disabled are true', () => {
      const wrapper = mount(AppButton, { props: { loading: true, disabled: true } })
      expect(wrapper.find('button').element.disabled).toBe(true)
    })
  })

})
