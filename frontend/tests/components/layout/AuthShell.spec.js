import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import AuthShell from '@/components/layout/AuthShell.vue'

describe('AuthShell', () => {

  // ── Default props ──────────────────────────────────────────────────────

  describe('default rendering', () => {
    it('renders the default tag text', () => {
      const wrapper = mount(AuthShell)
      expect(wrapper.find('.aside-tag').text()).toBe('Welcome')
    })

    it('renders the default aside title', () => {
      const wrapper = mount(AuthShell)
      expect(wrapper.find('.aside-title').text()).toContain('Run your checks with confidence')
    })

    it('renders the default aside body text', () => {
      const wrapper = mount(AuthShell)
      expect(wrapper.find('.aside-body').text()).toContain('calmer workflow')
    })

    it('renders the brand name "ICSystem"', () => {
      const wrapper = mount(AuthShell)
      expect(wrapper.find('.brand-title').text()).toBe('ICSystem')
    })

    it('renders the default eyebrow text', () => {
      const wrapper = mount(AuthShell)
      expect(wrapper.find('.eyebrow').text()).toBe('Account access')
    })

    it('renders the default title', () => {
      const wrapper = mount(AuthShell)
      expect(wrapper.find('.title').text()).toBe('Welcome back')
    })
  })

  // ── Custom props ───────────────────────────────────────────────────────

  describe('custom props', () => {
    it('renders a custom tag', () => {
      const wrapper = mount(AuthShell, { props: { tag: 'New user' } })
      expect(wrapper.find('.aside-tag').text()).toBe('New user')
    })

    it('renders a custom eyebrow', () => {
      const wrapper = mount(AuthShell, { props: { eyebrow: 'Create account' } })
      expect(wrapper.find('.eyebrow').text()).toBe('Create account')
    })

    it('renders a custom title', () => {
      const wrapper = mount(AuthShell, { props: { title: 'Register' } })
      expect(wrapper.find('.title').text()).toBe('Register')
    })

    it('renders a custom subtitle', () => {
      const wrapper = mount(AuthShell, { props: { subtitle: 'Already have an account?' } })
      expect(wrapper.find('.subtitle').text()).toBe('Already have an account?')
    })

    it('renders a custom asideTitle', () => {
      const wrapper = mount(AuthShell, { props: { asideTitle: 'Join your team' } })
      expect(wrapper.find('.aside-title').text()).toBe('Join your team')
    })
  })

  // ── Features list ──────────────────────────────────────────────────────

  describe('features list', () => {
    it('renders feature items when features array is provided', () => {
      const wrapper = mount(AuthShell, {
        props: { features: ['Daily checklists', 'Temperature logging', 'Document storage'] },
      })
      const items = wrapper.findAll('.aside-list li')
      expect(items).toHaveLength(3)
      expect(items[0].text()).toBe('Daily checklists')
      expect(items[2].text()).toBe('Document storage')
    })

    it('does not render the features list when features is empty', () => {
      const wrapper = mount(AuthShell, { props: { features: [] } })
      expect(wrapper.find('.aside-list').exists()).toBe(false)
    })
  })

  // ── Slots ──────────────────────────────────────────────────────────────

  describe('slots', () => {
    it('renders default slot content inside card-body', () => {
      const wrapper = mount(AuthShell, {
        slots: { default: '<form class="login-form" />' },
      })
      expect(wrapper.find('.card-body .login-form').exists()).toBe(true)
    })

    it('renders footer slot inside card-footer when provided', () => {
      const wrapper = mount(AuthShell, {
        slots: { footer: '<a class="footer-link">Sign up</a>' },
      })
      expect(wrapper.find('.card-footer .footer-link').exists()).toBe(true)
    })

    it('does not render card-footer when footer slot is absent', () => {
      const wrapper = mount(AuthShell)
      expect(wrapper.find('.card-footer').exists()).toBe(false)
    })
  })

})
