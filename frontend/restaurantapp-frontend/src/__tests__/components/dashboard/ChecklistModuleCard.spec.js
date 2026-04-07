import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ChecklistModuleCard from '@/components/dashboard/ChecklistModuleCard.vue'

// ── Helpers ────────────────────────────────────────────────────────────────

function makeChecklist(overrides = {}) {
  return {
    id: 'cl1',
    title: 'Daily Food Check',
    period: 'DAILY',
    subtitle: null,
    sections: [
      { id: 's1', items: [{ id: 't1' }, { id: 't2' }] },
    ],
    ...overrides,
  }
}

function mountCard(props = {}) {
  return mount(ChecklistModuleCard, {
    props: {
      label: 'IC-Food',
      variant: 'food',
      completedTasks: 0,
      totalTasks: 0,
      completionRate: 0,
      checklists: [],
      isLoading: false,
      error: '',
      ...props,
    },
  })
}

// ── Tests ──────────────────────────────────────────────────────────────────

describe('ChecklistModuleCard', () => {

  // ── Rendering ──────────────────────────────────────────────────────────

  describe('rendering', () => {
    it('renders the label in the header', () => {
      const wrapper = mountCard({ label: 'IC-Food' })
      expect(wrapper.find('.checklist-title').text()).toBe('IC-Food')
    })

    it('applies the food variant class', () => {
      const wrapper = mountCard({ variant: 'food' })
      expect(wrapper.find('.checklist-module-card').classes()).toContain('checklist-module-card--food')
    })

    it('applies the alcohol variant class', () => {
      const wrapper = mountCard({ variant: 'alcohol' })
      expect(wrapper.find('.checklist-module-card').classes()).toContain('checklist-module-card--alcohol')
    })

    it('renders an Open button', () => {
      const wrapper = mountCard()
      expect(wrapper.find('.module-open-btn').text()).toBe('Open')
    })
  })

  // ── Progress bar ───────────────────────────────────────────────────────

  describe('progress bar', () => {
    it('shows completed/total task count text', () => {
      const wrapper = mountCard({ completedTasks: 3, totalTasks: 5 })
      expect(wrapper.find('.module-progress__text').text()).toBe('3 / 5 tasks completed')
    })

    it('sets progress bar fill width from completionRate', () => {
      const wrapper = mountCard({ completionRate: 60 })
      expect(wrapper.find('.module-progress__fill').attributes('style')).toContain('width: 60%')
    })

    it('applies alcohol fill class for alcohol variant', () => {
      const wrapper = mountCard({ variant: 'alcohol' })
      expect(wrapper.find('.module-progress__fill').classes()).toContain('module-progress__fill--alcohol')
    })

    it('does not apply alcohol fill class for food variant', () => {
      const wrapper = mountCard({ variant: 'food' })
      expect(wrapper.find('.module-progress__fill').classes()).not.toContain('module-progress__fill--alcohol')
    })
  })

  // ── States ─────────────────────────────────────────────────────────────

  describe('loading state', () => {
    it('shows loading hint when isLoading is true', () => {
      const wrapper = mountCard({ isLoading: true })
      expect(wrapper.find('.checklist-hint').text()).toBe('Loading checklists...')
    })

    it('hides checklist cards while loading', () => {
      const wrapper = mountCard({ isLoading: true, checklists: [makeChecklist()] })
      expect(wrapper.findAll('.checklist-preview')).toHaveLength(0)
    })
  })

  describe('error state', () => {
    it('shows error message when error prop is set', () => {
      const wrapper = mountCard({ error: 'Failed to load' })
      expect(wrapper.find('.checklist-hint').text()).toBe('Failed to load')
    })
  })

  describe('empty state', () => {
    it('shows empty hint when checklists array is empty', () => {
      const wrapper = mountCard({ label: 'IC-Food', checklists: [] })
      expect(wrapper.find('.checklist-hint').text()).toContain('No daily IC-Food checklists found')
    })
  })

  // ── Checklist cards ────────────────────────────────────────────────────

  describe('checklist cards', () => {
    it('renders one card per checklist', () => {
      const wrapper = mountCard({
        checklists: [makeChecklist(), makeChecklist({ id: 'cl2', title: 'Weekly Check' })],
      })
      expect(wrapper.findAll('.checklist-preview')).toHaveLength(2)
    })

    it('renders checklist title', () => {
      const wrapper = mountCard({ checklists: [makeChecklist({ title: 'Hygiene Round' })] })
      expect(wrapper.find('.checklist-preview__title').text()).toBe('Hygiene Round')
    })

    it('formats period to title case', () => {
      const wrapper = mountCard({ checklists: [makeChecklist({ period: 'WEEKLY' })] })
      expect(wrapper.find('.checklist-preview__period').text()).toBe('Weekly')
    })

    it('formats DAILY period correctly', () => {
      const wrapper = mountCard({ checklists: [makeChecklist({ period: 'DAILY' })] })
      expect(wrapper.find('.checklist-preview__period').text()).toBe('Daily')
    })

    it('shows "Unknown" for null period', () => {
      const wrapper = mountCard({ checklists: [makeChecklist({ period: null })] })
      expect(wrapper.find('.checklist-preview__period').text()).toBe('Unknown')
    })

    it('shows correct task count from sections', () => {
      const wrapper = mountCard({
        checklists: [makeChecklist({
          sections: [
            { id: 's1', items: [{ id: 't1' }, { id: 't2' }] },
            { id: 's2', items: [{ id: 't3' }] },
          ],
        })],
      })
      expect(wrapper.find('.checklist-preview__meta').text()).toBe('3 tasks')
    })

    it('shows 0 tasks when sections are empty', () => {
      const wrapper = mountCard({ checklists: [makeChecklist({ sections: [] })] })
      expect(wrapper.find('.checklist-preview__meta').text()).toBe('0 tasks')
    })

    it('renders subtitle when provided', () => {
      const wrapper = mountCard({
        checklists: [makeChecklist({ subtitle: 'Morning round' })],
      })
      expect(wrapper.find('.checklist-preview__subtitle').text()).toBe('Morning round')
    })

    it('does not render subtitle element when null', () => {
      const wrapper = mountCard({ checklists: [makeChecklist({ subtitle: null })] })
      expect(wrapper.find('.checklist-preview__subtitle').exists()).toBe(false)
    })
  })

  // ── Emits ──────────────────────────────────────────────────────────────

  describe('emits', () => {
    it('emits "open" when Open button is clicked', async () => {
      const wrapper = mountCard()
      await wrapper.find('.module-open-btn').trigger('click')
      expect(wrapper.emitted('open')).toBeTruthy()
    })

    it('emits "open" when a checklist preview card is clicked', async () => {
      const wrapper = mountCard({ checklists: [makeChecklist()] })
      await wrapper.find('.checklist-preview').trigger('click')
      expect(wrapper.emitted('open')).toBeTruthy()
    })

    it('emits "open" on Enter keydown on a checklist preview', async () => {
      const wrapper = mountCard({ checklists: [makeChecklist()] })
      await wrapper.find('.checklist-preview').trigger('keydown.enter')
      expect(wrapper.emitted('open')).toBeTruthy()
    })
  })

})
