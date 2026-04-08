import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import StatCard from '@/components/dashboard/StatCard.vue'

describe('StatCard', () => {

  // ── Rendering ──────────────────────────────────────────────────────────

  describe('rendering', () => {
    it('renders the label', () => {
      const wrapper = mount(StatCard, { props: { label: 'Tasks remaining', value: 3 } })
      expect(wrapper.find('.stat-label').text()).toBe('Tasks remaining')
    })

    it('renders the value', () => {
      const wrapper = mount(StatCard, { props: { label: 'Tasks', value: 7 } })
      expect(wrapper.find('.stat-value').text()).toBe('7')
    })

    it('renders the hint when provided', () => {
      const wrapper = mount(StatCard, { props: { label: 'Tasks', value: 0, hint: 'Today only' } })
      expect(wrapper.find('.stat-hint').text()).toBe('Today only')
    })

    it('renders an empty hint when not provided', () => {
      const wrapper = mount(StatCard, { props: { label: 'Tasks', value: 0 } })
      expect(wrapper.find('.stat-hint').text()).toBe('')
    })
  })

  // ── Value variants ─────────────────────────────────────────────────────

  describe('valueVariant', () => {
    it('applies no variant class when valueVariant is empty', () => {
      const wrapper = mount(StatCard, { props: { label: 'Tasks', value: 0 } })
      expect(wrapper.find('.stat-value').classes()).not.toContain('stat-value--danger')
      expect(wrapper.find('.stat-value').classes()).not.toContain('stat-value--warning')
    })

    it('applies stat-value--danger class for danger variant', () => {
      const wrapper = mount(StatCard, { props: { label: 'Tasks', value: 5, valueVariant: 'danger' } })
      expect(wrapper.find('.stat-value').classes()).toContain('stat-value--danger')
    })

    it('applies stat-value--warning class for warning variant', () => {
      const wrapper = mount(StatCard, { props: { label: 'Tasks', value: 2, valueVariant: 'warning' } })
      expect(wrapper.find('.stat-value').classes()).toContain('stat-value--warning')
    })
  })

  // ── Interactive mode ───────────────────────────────────────────────────

  describe('interactive mode', () => {
    it('does not add interactive class when interactive is false', () => {
      const wrapper = mount(StatCard, { props: { label: 'Tasks', value: 0, interactive: false } })
      expect(wrapper.find('.stat-card').classes()).not.toContain('stat-card--interactive')
    })

    it('adds stat-card--interactive class when interactive is true', () => {
      const wrapper = mount(StatCard, { props: { label: 'Tasks', value: 0, interactive: true } })
      expect(wrapper.find('.stat-card').classes()).toContain('stat-card--interactive')
    })

    it('has role="button" when interactive', () => {
      const wrapper = mount(StatCard, { props: { label: 'Tasks', value: 0, interactive: true } })
      expect(wrapper.find('.stat-card').attributes('role')).toBe('button')
    })

    it('has tabindex="0" when interactive', () => {
      const wrapper = mount(StatCard, { props: { label: 'Tasks', value: 0, interactive: true } })
      expect(wrapper.find('.stat-card').attributes('tabindex')).toBe('0')
    })

    it('has no role attribute when not interactive', () => {
      const wrapper = mount(StatCard, { props: { label: 'Tasks', value: 0, interactive: false } })
      expect(wrapper.find('.stat-card').attributes('role')).toBeUndefined()
    })

    it('has no tabindex when not interactive', () => {
      const wrapper = mount(StatCard, { props: { label: 'Tasks', value: 0, interactive: false } })
      expect(wrapper.find('.stat-card').attributes('tabindex')).toBeUndefined()
    })
  })

  // ── Emits ──────────────────────────────────────────────────────────────

  describe('emits', () => {
    it('emits click when interactive card is clicked', async () => {
      const wrapper = mount(StatCard, { props: { label: 'Tasks', value: 0, interactive: true } })
      await wrapper.find('.stat-card').trigger('click')
      expect(wrapper.emitted('click')).toBeTruthy()
    })

    it('does not emit click when non-interactive card is clicked', async () => {
      const wrapper = mount(StatCard, { props: { label: 'Tasks', value: 0, interactive: false } })
      await wrapper.find('.stat-card').trigger('click')
      expect(wrapper.emitted('click')).toBeFalsy()
    })

    it('emits click on Enter keydown when interactive', async () => {
      const wrapper = mount(StatCard, { props: { label: 'Tasks', value: 0, interactive: true } })
      await wrapper.find('.stat-card').trigger('keydown.enter')
      expect(wrapper.emitted('click')).toBeTruthy()
    })

    it('emits click on Space keydown when interactive', async () => {
      const wrapper = mount(StatCard, { props: { label: 'Tasks', value: 0, interactive: true } })
      await wrapper.find('.stat-card').trigger('keydown.space')
      expect(wrapper.emitted('click')).toBeTruthy()
    })

    it('does not emit click on Enter when not interactive', async () => {
      const wrapper = mount(StatCard, { props: { label: 'Tasks', value: 0, interactive: false } })
      await wrapper.find('.stat-card').trigger('keydown.enter')
      expect(wrapper.emitted('click')).toBeFalsy()
    })
  })

})
