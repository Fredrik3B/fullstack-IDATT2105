import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import ChecklistLibraryModal from '@/components/ic-checklists/ChecklistLibraryModal.vue'

function makeCards() {
  return [
    {
      id: '2',
      title: 'Zebra checks',
      subtitle: '',
      period: 'monthly',
      progress: 25,
      displayedOnWorkbench: false,
      recurring: false,
      sections: [{ items: [{}, {}] }],
    },
    {
      id: '1',
      title: 'Alpha checks',
      subtitle: 'Before opening',
      period: 'weekly',
      progress: 100,
      displayedOnWorkbench: true,
      recurring: true,
      sections: [{ items: [{}] }],
    },
  ]
}

describe('ChecklistLibraryModal', () => {
  it('does not render anything when closed', () => {
    const wrapper = mount(ChecklistLibraryModal, {
      props: {
        open: false,
      },
    })

    expect(wrapper.find('[role="dialog"]').exists()).toBe(false)
  })

  it('sorts cards, shows summary counts, and emits open-checklist for available rows', async () => {
    const wrapper = mount(ChecklistLibraryModal, {
      props: {
        open: true,
        moduleLabel: 'Food',
        cards: makeCards(),
        loadedChecklistIds: ['2'],
      },
    })

    expect(wrapper.text()).toContain('Saved checklists')
    expect(wrapper.text()).toContain('2')
    expect(wrapper.text()).toContain('Already on workbench')
    expect(wrapper.text()).toContain('1')

    const titles = wrapper.findAll('.library-title').map((node) => node.text())
    expect(titles).toEqual(['Alpha checks', 'Zebra checks'])
    expect(wrapper.text()).toContain('Weekly')
    expect(wrapper.text()).toContain('Monthly')
    expect(wrapper.text()).toContain('1 tasks')
    expect(wrapper.text()).toContain('2 tasks')

    const buttons = wrapper.findAll('.open-button')
    expect(buttons[0].text()).toBe('Open on workbench')
    expect(buttons[1].text()).toBe('Already loaded')
    expect(buttons[1].attributes('disabled')).toBeDefined()

    await buttons[0].trigger('click')

    expect(wrapper.emitted('open-checklist')).toEqual([[makeCards()[1]]])
  })

  it('shows opening state, empty fallback, and emits close events', async () => {
    const wrapper = mount(ChecklistLibraryModal, {
      props: {
        open: true,
        moduleLabel: 'Food',
        cards: [],
      },
    })

    expect(wrapper.text()).toContain('No checklists yet')

    await wrapper.setProps({
      cards: makeCards(),
      openingChecklistId: '1',
    })

    const firstButton = wrapper.findAll('.open-button')[0]
    expect(firstButton.text()).toBe('Opening...')
    expect(firstButton.attributes('disabled')).toBeDefined()

    await wrapper.find('.icon-button').trigger('click')

    expect(wrapper.emitted('update:open')).toEqual([[false]])
    expect(wrapper.emitted('close')).toEqual([[]])
  })
})
