import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import MainDashboard from '@/views/MainDashboard.vue'

// ── Mocks ──────────────────────────────────────────────────────────────────

const mockRouterPush = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockRouterPush }),
}))

vi.mock('@/stores/auth', () => ({
  useAuthStore: vi.fn(),
}))

vi.mock('@/api/checklists', () => ({
  fetchChecklists: vi.fn(),
}))

vi.mock('@/api/documents', () => ({
  fetchDocuments: vi.fn(),
}))

vi.mock('@/api/temperatureMeasurements', () => ({
  fetchTemperatureMeasurements: vi.fn(),
}))

vi.mock('@/composables/ic-checklists/temperature', () => ({
  isTemperatureTask: vi.fn().mockReturnValue(false),
  isTemperatureDeviation: vi.fn().mockReturnValue(false),
}))

vi.mock('@/composables/ic-checklists/useTemperatureLog', () => ({
  useTemperatureLog: () => ({ latestByTaskId: { value: new Map() } }),
}))

vi.mock('@/components/dashboard/ChecklistModuleCard.vue', () => ({
  default: {
    template: '<div class="checklist-module-card" @click="$emit(\'open\')"><slot /></div>',
    props: ['label', 'variant', 'completedTasks', 'totalTasks', 'completionRate', 'checklists', 'isLoading', 'error'],
    emits: ['open'],
  },
}))

vi.mock('@/components/dashboard/StatCard.vue', () => ({
  default: {
    template: '<div class="stat-card" @click="$emit(\'click\')"><span class="stat-label">{{ label }}</span><span class="stat-value">{{ value }}</span><span class="stat-hint">{{ hint }}</span></div>',
    props: ['label', 'value', 'hint', 'interactive', 'valueVariant'],
    emits: ['click'],
  },
}))

import { useAuthStore } from '@/stores/auth'
import { fetchChecklists } from '@/api/checklists'
import { fetchDocuments } from '@/api/documents'
import { fetchTemperatureMeasurements } from '@/api/temperatureMeasurements'

// ── Helpers ────────────────────────────────────────────────────────────────

function makeAuthStore(overrides = {}) {
  return {
    user: { id: 1, name: 'Jane Doe', email: 'jane@example.com' },
    restaurant: { id: 'r1', name: 'Pizza Palace', joinCode: 'PIZ-1234' },
    isAdminOrManager: false,
    fetchJoinRequests: vi.fn().mockResolvedValue([]),
    fetchMembers: vi.fn().mockResolvedValue([]),
    ...overrides,
  }
}

function makeChecklist(overrides = {}) {
  return {
    id: 'cl1',
    title: 'Daily Food Check',
    period: 'DAILY',
    sections: [
      {
        id: 's1',
        items: [
          { id: 't1', state: 'todo', type: 'CHECK' },
          { id: 't2', state: 'completed', type: 'CHECK' },
        ],
      },
    ],
    ...overrides,
  }
}

function makeChecklistResponse(data = [], overrides = {}) {
  return {
    status: 200,
    data,
    lastModified: null,
    ...overrides,
  }
}

function mountView(storeOverrides = {}) {
  const pinia = createPinia()
  setActivePinia(pinia)
  useAuthStore.mockReturnValue(makeAuthStore(storeOverrides))

  return mount(MainDashboard, {
    global: { plugins: [pinia] },
    attachTo: document.body,
  })
}

// ── Setup ──────────────────────────────────────────────────────────────────

beforeEach(() => {
  vi.clearAllMocks()
  mockRouterPush.mockClear()
  fetchChecklists.mockResolvedValue(makeChecklistResponse())
  fetchDocuments.mockResolvedValue([])
  fetchTemperatureMeasurements.mockResolvedValue([])
})

// ── Tests ──────────────────────────────────────────────────────────────────

describe('MainDashboard', () => {

  // ── Rendering ──────────────────────────────────────────────────────────

  describe('rendering', () => {
    it('displays the user name in the welcome banner', async () => {
      const wrapper = mountView()
      await flushPromises()

      expect(wrapper.find('.welcome-accent').text()).toBe('Jane Doe')
    })

    it('falls back to email when user has no name', async () => {
      const wrapper = mountView({
        user: { id: 1, name: null, email: 'jane@example.com' },
      })
      await flushPromises()

      expect(wrapper.find('.welcome-accent').text()).toBe('jane@example.com')
    })

    it('displays the restaurant name in the welcome banner', async () => {
      const wrapper = mountView()
      await flushPromises()

      expect(wrapper.find('.welcome-sub').text()).toContain('Pizza Palace')
    })

    it('renders quick action buttons', async () => {
      const wrapper = mountView()
      await flushPromises()

      const quickActions = wrapper.findAll('.quick-action-card')
      expect(quickActions).toHaveLength(4)
      expect(wrapper.text()).toContain('Start IC-Food')
      expect(wrapper.text()).toContain('Start IC-Alcohol')
      expect(wrapper.text()).toContain('Report deviation')
      expect(wrapper.text()).toContain('Open documents')
    })

    it('renders both checklist module cards', async () => {
      const wrapper = mountView()
      await flushPromises()

      expect(wrapper.findAll('.checklist-module-card')).toHaveLength(2)
    })

    it('hides admin team panel for non-admin users', async () => {
      const wrapper = mountView({ isAdminOrManager: false })
      await flushPromises()

      expect(wrapper.find('.team-panel').exists()).toBe(false)
    })

    it('shows admin team panel for admin users', async () => {
      const wrapper = mountView({ isAdminOrManager: true })
      await flushPromises()

      expect(wrapper.find('.team-panel').exists()).toBe(true)
    })

    it('shows pending requests stat card only for admin', async () => {
      const wrapper = mountView({ isAdminOrManager: true })
      await flushPromises()

      const statLabels = wrapper.findAll('.stat-label').map(el => el.text())
      expect(statLabels).toContain('Pending requests')
    })

    it('hides pending requests stat card for staff', async () => {
      const wrapper = mountView({ isAdminOrManager: false })
      await flushPromises()

      const statLabels = wrapper.findAll('.stat-label').map(el => el.text())
      expect(statLabels).not.toContain('Pending requests')
    })
  })

  // ── Data loading ────────────────────────────────────────────────────────

  describe('data loading', () => {
    it('calls fetchChecklists for both IC_FOOD and IC_ALCOHOL on mount', async () => {
      mountView()
      await flushPromises()

      expect(fetchChecklists).toHaveBeenCalledWith({ module: 'IC_FOOD' })
      expect(fetchChecklists).toHaveBeenCalledWith({ module: 'IC_ALCOHOL' })
    })

    it('calls fetchDocuments on mount', async () => {
      mountView()
      await flushPromises()

      expect(fetchDocuments).toHaveBeenCalled()
    })

    it('does not call fetchJoinRequests for non-admin user', async () => {
      const fetchJoinRequests = vi.fn().mockResolvedValue([])
      mountView({ isAdminOrManager: false, fetchJoinRequests })
      await flushPromises()

      expect(fetchJoinRequests).not.toHaveBeenCalled()
    })

    it('calls fetchJoinRequests for admin user', async () => {
      const fetchJoinRequests = vi.fn().mockResolvedValue([])
      mountView({ isAdminOrManager: true, fetchJoinRequests })
      await flushPromises()

      expect(fetchJoinRequests).toHaveBeenCalledWith('PENDING')
    })
  })

  // ── Computed: task counts ───────────────────────────────────────────────

  describe('task counts from checklists', () => {
    it('shows 0 remaining tasks when no checklists', async () => {
      fetchChecklists.mockResolvedValue(makeChecklistResponse())
      const wrapper = mountView()
      await flushPromises()

      const remainingCard = wrapper.findAll('.stat-card').find(c => c.find('.stat-label').text() === 'Tasks remaining')
      expect(remainingCard.find('.stat-value').text()).toBe('0')
    })

    it('counts remaining and completed tasks from daily checklists', async () => {
      // IC_FOOD returns checklist with 1 todo + 1 completed; IC_ALCOHOL returns empty
      fetchChecklists
        .mockResolvedValueOnce(makeChecklistResponse([makeChecklist()]))
        .mockResolvedValueOnce(makeChecklistResponse())
      const wrapper = mountView()
      await flushPromises()

      const remainingCard = wrapper.findAll('.stat-card').find(c => c.find('.stat-label').text() === 'Tasks remaining')
      const completedCard = wrapper.findAll('.stat-card').find(c => c.find('.stat-label').text() === 'Tasks completed')

      expect(remainingCard.find('.stat-value').text()).toBe('1')
      expect(completedCard.find('.stat-value').text()).toBe('1')
    })

    it('ignores non-daily checklists in task count', async () => {
      fetchChecklists
        .mockResolvedValueOnce(makeChecklistResponse([
          makeChecklist({ period: 'WEEKLY' }),
          makeChecklist({ id: 'cl2', period: 'DAILY', sections: [{ id: 's2', items: [{ id: 't3', state: 'todo', type: 'CHECK' }] }] }),
        ]))
        .mockResolvedValueOnce(makeChecklistResponse())
      const wrapper = mountView()
      await flushPromises()

      const remainingCard = wrapper.findAll('.stat-card').find(c => c.find('.stat-label').text() === 'Tasks remaining')
      expect(remainingCard.find('.stat-value').text()).toBe('1')
    })
  })

  // ── Computed: expiring certificates ────────────────────────────────────

  describe('expiring certificates', () => {
    it('counts certificates expiring within 30 days', async () => {
      const soon = new Date()
      soon.setDate(soon.getDate() + 10)
      fetchDocuments.mockResolvedValue([
        { id: 1, category: 'CERTIFICATE', expiryDate: soon.toISOString().split('T')[0], name: 'Cert A', uploadedAt: null },
        { id: 2, category: 'GUIDELINES', expiryDate: null, name: 'Guide', uploadedAt: null },
      ])
      const wrapper = mountView()
      await flushPromises()

      const certCard = wrapper.findAll('.stat-card').find(c => c.find('.stat-label').text() === 'Expiring certificates')
      expect(certCard.find('.stat-value').text()).toBe('1')
    })

    it('does not count certificates expiring in more than 30 days', async () => {
      const far = new Date()
      far.setDate(far.getDate() + 60)
      fetchDocuments.mockResolvedValue([
        { id: 1, category: 'CERTIFICATE', expiryDate: far.toISOString().split('T')[0], name: 'Cert A', uploadedAt: null },
      ])
      const wrapper = mountView()
      await flushPromises()

      const certCard = wrapper.findAll('.stat-card').find(c => c.find('.stat-label').text() === 'Expiring certificates')
      expect(certCard.find('.stat-value').text()).toBe('0')
    })
  })

  // ── Alerts ─────────────────────────────────────────────────────────────

  describe('alerts section', () => {
    it('shows "No critical alerts" when everything is fine', async () => {
      const wrapper = mountView()
      await flushPromises()

      expect(wrapper.text()).toContain('No critical alerts right now.')
    })

    it('shows certificate expiry alert when certificates expiring', async () => {
      const soon = new Date()
      soon.setDate(soon.getDate() + 10)
      fetchDocuments.mockResolvedValue([
        { id: 1, category: 'CERTIFICATE', expiryDate: soon.toISOString().split('T')[0], name: 'Cert A', uploadedAt: null },
      ])
      const wrapper = mountView()
      await flushPromises()

      expect(wrapper.text()).toContain('certificate(s) expiring within 30 days')
    })

    it('shows remaining tasks alert when tasks remain', async () => {
      fetchChecklists
        .mockResolvedValueOnce(makeChecklistResponse([makeChecklist()]))
        .mockResolvedValueOnce(makeChecklistResponse())
      const wrapper = mountView()
      await flushPromises()

      expect(wrapper.text()).toContain('task(s) still open today')
    })
  })

  // ── Operational health label ────────────────────────────────────────────

  describe('operational health label', () => {
    it('shows all tasks complete when no tasks remain', async () => {
      fetchChecklists
        .mockResolvedValueOnce(makeChecklistResponse([makeChecklist({ sections: [{ id: 's1', items: [{ id: 't1', state: 'completed', type: 'CHECK' }] }] })]))
        .mockResolvedValueOnce(makeChecklistResponse())
      const wrapper = mountView()
      await flushPromises()

      expect(wrapper.find('.welcome-health').text()).toContain('all tracked daily tasks are currently complete')
    })

    it('shows continue checklists message when tasks remain', async () => {
      fetchChecklists
        .mockResolvedValueOnce(makeChecklistResponse([makeChecklist()]))
        .mockResolvedValueOnce(makeChecklistResponse())
      const wrapper = mountView()
      await flushPromises()

      expect(wrapper.find('.welcome-health').text()).toContain('continue daily checklists')
    })
  })

  // ── Quick action routing ────────────────────────────────────────────────

  describe('quick actions routing', () => {
    it('navigates to IC-Food dashboard when Start IC-Food is clicked', async () => {
      const wrapper = mountView()
      await flushPromises()

      const btn = wrapper.findAll('.quick-action-card').find(b => b.text().includes('Start IC-Food'))
      await btn.trigger('click')

      expect(mockRouterPush).toHaveBeenCalledWith({ name: 'ic-food-dashboard' })
    })

    it('navigates to IC-Alcohol dashboard when Start IC-Alcohol is clicked', async () => {
      const wrapper = mountView()
      await flushPromises()

      const btn = wrapper.findAll('.quick-action-card').find(b => b.text().includes('Start IC-Alcohol'))
      await btn.trigger('click')

      expect(mockRouterPush).toHaveBeenCalledWith({ name: 'ic-alcohol-dashboard' })
    })

    it('navigates to reports (deviation) when Report deviation is clicked', async () => {
      const wrapper = mountView()
      await flushPromises()

      const btn = wrapper.findAll('.quick-action-card').find(b => b.text().includes('Report deviation'))
      await btn.trigger('click')

      expect(mockRouterPush).toHaveBeenCalledWith({ name: 'reports', query: { action: 'deviation' } })
    })

    it('navigates to documents when Open documents is clicked', async () => {
      const wrapper = mountView()
      await flushPromises()

      const btn = wrapper.findAll('.quick-action-card').find(b => b.text().includes('Open documents'))
      await btn.trigger('click')

      expect(mockRouterPush).toHaveBeenCalledWith({ name: 'documents', query: undefined })
    })
  })

  // ── Team panel (admin only) ─────────────────────────────────────────────

  describe('team panel', () => {
    it('shows the join code in the team panel', async () => {
      const wrapper = mountView({ isAdminOrManager: true })
      await flushPromises()

      expect(wrapper.find('.team-stat-value--code').text()).toBe('PIZ-1234')
    })

    it('shows member count from fetchMembers result', async () => {
      const fetchMembers = vi.fn().mockResolvedValue([
        { id: 1, name: 'Alice', roles: ['STAFF'] },
        { id: 2, name: 'Bob', roles: ['ADMIN'] },
      ])
      const wrapper = mountView({ isAdminOrManager: true, fetchMembers })
      await flushPromises()

      const memberCard = wrapper.findAll('.team-stat-card').find(c => c.find('.team-stat-label').text() === 'Members')
      expect(memberCard.find('.team-stat-value').text()).toBe('2')
    })

    it('shows pending request count from fetchJoinRequests', async () => {
      const fetchJoinRequests = vi.fn().mockResolvedValue([{ id: 'r1' }, { id: 'r2' }])
      const wrapper = mountView({ isAdminOrManager: true, fetchJoinRequests })
      await flushPromises()

      const reqCard = wrapper.findAll('.team-stat-card').find(c => c.find('.team-stat-label').text() === 'Pending requests')
      expect(reqCard.find('.team-stat-value').text()).toBe('2')
    })

    it('navigates to admin panel when "Open admin panel" is clicked', async () => {
      const wrapper = mountView({ isAdminOrManager: true })
      await flushPromises()

      const btn = wrapper.findAll('button').find(b => b.text().includes('Open admin panel'))
      await btn.trigger('click')

      expect(mockRouterPush).toHaveBeenCalledWith({ name: 'admin-requests', query: undefined })
    })
  })

})
