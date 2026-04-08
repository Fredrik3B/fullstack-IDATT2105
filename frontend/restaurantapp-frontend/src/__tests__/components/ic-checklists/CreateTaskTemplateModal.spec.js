import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import CreateTaskTemplateModal from '@/components/ic-checklists/CreateTaskTemplateModal.vue'

vi.mock('@/api/temperatureZones', () => ({
  fetchTemperatureZones: vi.fn(),
}))

import { fetchTemperatureZones } from '@/api/temperatureZones'

function makeZones() {
  return [
    { id: 7, name: 'Main fridge', zoneType: 'FRIDGE', targetMin: 0, targetMax: 4 },
    { id: 9, name: 'Freezer', zoneType: 'FREEZER', targetMin: -22, targetMax: -18 },
  ]
}

function mountModal(props = {}) {
  return mount(CreateTaskTemplateModal, {
    props: {
      open: false,
      module: 'IC_FOOD',
      moduleLabel: 'Food',
      ...props,
    },
  })
}

describe('CreateTaskTemplateModal', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    fetchTemperatureZones.mockResolvedValue(makeZones())
  })

  it('loads fridge items on open and validates required fields before submit', async () => {
    const wrapper = mountModal()
    await wrapper.setProps({ open: true })
    await flushPromises()

    expect(fetchTemperatureZones).toHaveBeenCalledWith({ module: 'IC_FOOD' })
    expect(wrapper.text()).toContain('Create task')

    await wrapper.find('form').trigger('submit.prevent')
    expect(wrapper.text()).toContain('Task title is required.')

    await wrapper.find('input[type="text"]').setValue('Check fridge seals')
    await wrapper.find('form').trigger('submit.prevent')
    expect(wrapper.text()).toContain('Section type is required.')
  })

  it('requires a fridge item for temperature tasks and exposes the manage-zones action', async () => {
    const wrapper = mountModal()
    await wrapper.setProps({ open: true })
    await flushPromises()

    await wrapper.find('input[type="text"]').setValue('Check fridge')
    await wrapper.find('select').setValue('TEMPERATURE_CONTROL')
    await flushPromises()

    expect(wrapper.text()).toContain('No fridge item selected yet')

    await wrapper.find('form').trigger('submit.prevent')
    expect(wrapper.text()).toContain('Choose a fridge item for temperature tasks.')

    await wrapper.find('.zone-link').trigger('click')
    expect(wrapper.emitted('manage-zones')).toEqual([[]])
  })

  it('emits a created payload with zone-derived temperature bounds and closes the modal', async () => {
    const wrapper = mountModal()
    await wrapper.setProps({ open: true })
    await flushPromises()

    const inputs = wrapper.findAll('input[type="text"]')
    await inputs[0].setValue('Check main fridge')
    await inputs[1].setValue('Morning reading')
    await wrapper.find('select').setValue('TEMPERATURE_CONTROL')
    await flushPromises()

    const selects = wrapper.findAll('select')
    await selects[1].setValue('7')
    await wrapper.find('form').trigger('submit.prevent')

    expect(wrapper.emitted('created')).toEqual([
      [
        {
          id: null,
          module: 'IC_FOOD',
          title: 'Check main fridge',
          meta: 'Morning reading',
          sectionType: 'TEMPERATURE_CONTROL',
          temperatureZoneId: 7,
          targetMin: 0,
          targetMax: 4,
        },
      ],
    ])
    expect(wrapper.emitted('update:open')).toEqual([[false]])
    expect(wrapper.emitted('close')).toEqual([[]])
  })

  it('initializes edit mode from the provided task and emits updated payloads', async () => {
    const wrapper = mountModal({
      open: false,
      mode: 'edit',
      initialTask: {
        id: 12,
        title: 'Check freezer',
        meta: 'Evening',
        sectionType: 'TEMPERATURE_CONTROL',
        temperatureZoneId: 9,
      },
    })
    await wrapper.setProps({ open: true })
    await flushPromises()

    const inputs = wrapper.findAll('input[type="text"]')
    expect(inputs[0].element.value).toBe('Check freezer')
    expect(inputs[1].element.value).toBe('Evening')
    expect(wrapper.text()).toContain('Freezer')

    await wrapper.find('form').trigger('submit.prevent')

    expect(wrapper.emitted('updated')).toEqual([
      [
        {
          id: 12,
          module: 'IC_FOOD',
          title: 'Check freezer',
          meta: 'Evening',
          sectionType: 'TEMPERATURE_CONTROL',
          temperatureZoneId: 9,
          targetMin: -22,
          targetMax: -18,
        },
      ],
    ])
  })
})
