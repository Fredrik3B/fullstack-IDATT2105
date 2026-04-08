import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import FormField from '@/components/ui/FormField.vue'

describe('FormField', () => {

  // ── Label ──────────────────────────────────────────────────────────────

  describe('label', () => {
    it('renders the label text', () => {
      const wrapper = mount(FormField, { props: { label: 'Email address' } })
      expect(wrapper.find('.field-label').text()).toContain('Email address')
    })

    it('associates label with input via for/inputId', () => {
      const wrapper = mount(FormField, { props: { label: 'Email', inputId: 'email' } })
      expect(wrapper.find('label').attributes('for')).toBe('email')
    })

    it('does not set for attribute when inputId is empty', () => {
      const wrapper = mount(FormField, { props: { label: 'Email' } })
      expect(wrapper.find('label').attributes('for')).toBeUndefined()
    })

    it('renders labelHint when provided', () => {
      const wrapper = mount(FormField, { props: { label: 'Password', labelHint: 'min 8 chars' } })
      expect(wrapper.find('.field-label-hint').text()).toBe('min 8 chars')
    })

    it('does not render labelHint element when labelHint is empty', () => {
      const wrapper = mount(FormField, { props: { label: 'Password' } })
      expect(wrapper.find('.field-label-hint').exists()).toBe(false)
    })
  })

  // ── Slots ──────────────────────────────────────────────────────────────

  describe('slots', () => {
    it('renders default slot content inside field-wrapper', () => {
      const wrapper = mount(FormField, {
        props: { label: 'Name' },
        slots: { default: '<input id="name" />' },
      })
      expect(wrapper.find('.field-wrapper input').exists()).toBe(true)
    })

    it('renders icon slot inside field-icon-wrap when provided', () => {
      const wrapper = mount(FormField, {
        props: { label: 'Email' },
        slots: { icon: '<svg class="my-icon" />' },
      })
      expect(wrapper.find('.field-icon-wrap .my-icon').exists()).toBe(true)
    })

    it('does not render field-icon-wrap when icon slot is absent', () => {
      const wrapper = mount(FormField, { props: { label: 'Email' } })
      expect(wrapper.find('.field-icon-wrap').exists()).toBe(false)
    })

    it('renders after slot content', () => {
      const wrapper = mount(FormField, {
        props: { label: 'Password' },
        slots: { after: '<span class="strength-bar" />' },
      })
      expect(wrapper.find('.strength-bar').exists()).toBe(true)
    })
  })

  // ── Error state ────────────────────────────────────────────────────────

  describe('error state', () => {
    it('shows error message when error prop is set', () => {
      const wrapper = mount(FormField, { props: { label: 'Email', error: 'Invalid email' } })
      expect(wrapper.find('.field-error').text()).toBe('Invalid email')
    })

    it('does not render field-error element when error is empty', () => {
      const wrapper = mount(FormField, { props: { label: 'Email', error: '' } })
      expect(wrapper.find('.field-error').exists()).toBe(false)
    })

    it('adds has-error class to field-group when error is set', () => {
      const wrapper = mount(FormField, { props: { label: 'Email', error: 'Required' } })
      expect(wrapper.find('.field-group').classes()).toContain('has-error')
    })

    it('does not add has-error class when error is empty', () => {
      const wrapper = mount(FormField, { props: { label: 'Email' } })
      expect(wrapper.find('.field-group').classes()).not.toContain('has-error')
    })
  })

})
