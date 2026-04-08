import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'
import IcModuleDashboardView from '@/views/ic-views/IcModuleDashboardView.vue'

const pageState = {
  activePeriod: ref('Daily'),
  cards: ref([{ id: 'library-1', title: 'Library card' }]),
  workbenchCards: ref([{ id: 'workbench-1', title: 'Workbench card' }]),
  canManageChecklists: ref(true),
  canManageTaskPool: ref(true),
  loadedChecklistIds: ref(['workbench-1']),
  highlightedChecklistId: ref('workbench-1'),
  now: ref(new Date('2026-04-07T10:00:00Z')),
  dateLabel: ref('Tuesday, 07 April 2026'),
  isLoading: ref(false),
  loadError: ref(''),
  isCreateOpen: ref(false),
  isEditOpen: ref(true),
  isLibraryOpen: ref(true),
  isTaskPoolOpen: ref(true),
  isCreatingChecklist: ref(false),
  isUpdatingChecklist: ref(false),
  openingChecklistId: ref(null),
  deletingChecklistId: ref('delete-1'),
  removingChecklistId: ref(null),
  editingCard: ref({ id: 'delete-1', title: 'Line check' }),
  deleteDialog: ref({
    open: true,
    checklist: { id: 'delete-1', title: 'Line check' },
  }),
  togglePending: vi.fn(),
  toggleTask: vi.fn(),
  submitCard: vi.fn(),
  logTemperatureMeasurement: vi.fn(),
  reloadChecklists: vi.fn(),
  openCreateModal: vi.fn(),
  openLibraryModal: vi.fn(),
  openTaskPoolModal: vi.fn(),
  editChecklist: vi.fn(),
  closeEditModal: vi.fn(),
  requestDeleteChecklist: vi.fn(),
  closeDeleteDialog: vi.fn(),
  handleCreatedChecklist: vi.fn(),
  handleUpdatedChecklist: vi.fn(),
  handleDeleteChecklist: vi.fn(),
  openChecklistOnWorkbench: vi.fn(),
  removeChecklistFromWorkbench: vi.fn(),
}

vi.mock('@/composables/ic-checklists/moduleConfig', () => ({
  getIcModuleConfig: vi.fn(() => ({
    module: 'IC_FOOD',
    moduleLabel: 'IC-Food',
    moduleDescription: 'Food dashboard',
    summaryHint: 'Summary copy',
  })),
}))

vi.mock('@/composables/ic-checklists/useIcModulePage', () => ({
  useIcModulePage: vi.fn(() => pageState),
}))

vi.mock('@/components/ic-checklists/ChecklistDashboard.vue', () => ({
  default: {
    name: 'ChecklistDashboard',
    props: ['cards', 'highlightedChecklistId'],
    emits: ['manage-tasks'],
    template: '<div class="dashboard-stub" @click="$emit(\'manage-tasks\')">{{ cards.length }}|{{ highlightedChecklistId }}</div>',
  },
}))

vi.mock('@/components/ic-checklists/CreateChecklistModal.vue', () => ({
  default: {
    name: 'CreateChecklistModal',
    props: ['mode', 'taskPoolRefreshToken'],
    template: '<div class="create-modal-stub">{{ mode || "create" }}:{{ taskPoolRefreshToken }}</div>',
  },
}))

vi.mock('@/components/ic-checklists/ChecklistLibraryModal.vue', () => ({
  default: {
    name: 'ChecklistLibraryModal',
    props: ['cards', 'loadedChecklistIds'],
    template: '<div class="library-modal-stub">{{ cards.length }}|{{ loadedChecklistIds.length }}</div>',
  },
}))

vi.mock('@/components/ic-checklists/ManageTaskTemplatesModal.vue', () => ({
  default: {
    name: 'ManageTaskTemplatesModal',
    emits: ['changed'],
    template: '<button class="task-pool-stub" @click="$emit(\'changed\')">changed</button>',
  },
}))

vi.mock('@/components/ic-checklists/SharedConfirmDialog.vue', () => ({
  default: {
    name: 'SharedConfirmDialog',
    props: ['message', 'isProcessing'],
    emits: ['confirm'],
    template: '<button class="delete-dialog-stub" @click="$emit(\'confirm\')">{{ message }}|{{ isProcessing }}</button>',
  },
}))

describe('IcModuleDashboardView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    pageState.deleteDialog.value = {
      open: true,
      checklist: { id: 'delete-1', title: 'Line check' },
    }
    pageState.deletingChecklistId.value = 'delete-1'
  })

  it('passes dashboard and modal state through the module page composable', () => {
    const wrapper = mount(IcModuleDashboardView, {
      props: { module: 'IC_FOOD' },
    })

    expect(wrapper.find('.dashboard-stub').text()).toBe('1|workbench-1')
    expect(wrapper.find('.library-modal-stub').text()).toBe('1|1')
    expect(wrapper.findAll('.create-modal-stub').map((node) => node.text())).toEqual([
      'create:0',
      'edit:0',
    ])
    expect(wrapper.find('.delete-dialog-stub').text()).toContain(
      '"Line check" will be removed from the library and any current workbench usage.',
    )
  })

  it('increments the task pool refresh token after task templates change', async () => {
    const wrapper = mount(IcModuleDashboardView, {
      props: { module: 'IC_FOOD' },
    })

    await wrapper.find('.task-pool-stub').trigger('click')

    expect(wrapper.findAll('.create-modal-stub').map((node) => node.text())).toEqual([
      'create:1',
      'edit:1',
    ])
  })

  it('forwards delete confirmation to the composable handler with the selected checklist', async () => {
    const wrapper = mount(IcModuleDashboardView, {
      props: { module: 'IC_FOOD' },
    })

    await wrapper.find('.delete-dialog-stub').trigger('click')

    expect(pageState.handleDeleteChecklist).toHaveBeenCalledWith({
      id: 'delete-1',
      title: 'Line check',
    })
  })
})
