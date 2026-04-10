import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'

vi.mock('@/api/reports', () => ({
  createDeviationReport: vi.fn(),
}))

import { createDeviationReport } from '@/api/reports'
import DeviationReportForm from '@/components/reports/DeviationReportForm.vue'

// ── Helpers ────────────────────────────────────────────────────────────────

function mountForm() {
  return mount(DeviationReportForm, { attachTo: document.body })
}

/** Fill every required field so the form becomes valid */
async function fillAllFields(wrapper) {
  await wrapper.find('#dev-name').setValue('Fridge too warm')
  await wrapper.find('#dev-severity').setValue('MAJOR')
  await wrapper.find('#dev-time').setValue('2024-06-15T10:30')
  await wrapper.find('#dev-noticed').setValue('Alice')
  await wrapper.find('#dev-reported').setValue('Manager Bob')
  await wrapper.find('#dev-processed').setValue('Alice')
  await wrapper.find('#dev-desc').setValue('Temperature was 12°C instead of <5°C')
  await wrapper.find('#dev-immediate').setValue('Moved items to backup fridge')
  await wrapper.find('#dev-cause').setValue('Seal on door worn out')
  await wrapper.find('#dev-measures').setValue('Replace door seal')
  await wrapper.find('#dev-done').setValue('Backup fridge used, maintenance called')
}

// ── Setup ──────────────────────────────────────────────────────────────────

beforeEach(() => {
  vi.clearAllMocks()
  createDeviationReport.mockResolvedValue({ id: 1 })
})

// ── Tests ──────────────────────────────────────────────────────────────────

describe('DeviationReportForm', () => {

  // ── Initial state ───────────────────────────────────────────────────────

  describe('initial state', () => {
    it('renders all required input fields', () => {
      const wrapper = mountForm()
      expect(wrapper.find('#dev-name').exists()).toBe(true)
      expect(wrapper.find('#dev-severity').exists()).toBe(true)
      expect(wrapper.find('#dev-time').exists()).toBe(true)
      expect(wrapper.find('#dev-noticed').exists()).toBe(true)
      expect(wrapper.find('#dev-reported').exists()).toBe(true)
      expect(wrapper.find('#dev-processed').exists()).toBe(true)
      expect(wrapper.find('#dev-desc').exists()).toBe(true)
      expect(wrapper.find('#dev-immediate').exists()).toBe(true)
      expect(wrapper.find('#dev-cause').exists()).toBe(true)
      expect(wrapper.find('#dev-measures').exists()).toBe(true)
      expect(wrapper.find('#dev-done').exists()).toBe(true)
    })

    it('submit button is disabled when the form is empty', () => {
      const wrapper = mountForm()
      expect(wrapper.find('.btn-submit').element.disabled).toBe(true)
    })

    it('does not show an error message initially', () => {
      const wrapper = mountForm()
      expect(wrapper.find('.form-error').exists()).toBe(false)
    })
  })

  // ── isValid ─────────────────────────────────────────────────────────────

  describe('form validation', () => {
    it('submit button remains disabled when only some fields are filled', async () => {
      const wrapper = mountForm()
      // Fill only a few fields — still invalid
      await wrapper.find('#dev-name').setValue('Fridge too warm')
      await wrapper.find('#dev-severity').setValue('MAJOR')
      expect(wrapper.find('.btn-submit').element.disabled).toBe(true)
    })

    it('submit button becomes enabled when all fields are filled', async () => {
      const wrapper = mountForm()
      await fillAllFields(wrapper)
      expect(wrapper.find('.btn-submit').element.disabled).toBe(false)
    })

    it('submit button goes back to disabled if a required field is cleared', async () => {
      const wrapper = mountForm()
      await fillAllFields(wrapper)
      expect(wrapper.find('.btn-submit').element.disabled).toBe(false)

      await wrapper.find('#dev-name').setValue('')
      expect(wrapper.find('.btn-submit').element.disabled).toBe(true)
    })
  })

  // ── Cancel / close ──────────────────────────────────────────────────────

  describe('cancel / close', () => {
    it('emits "cancel" when the cancel button is clicked', async () => {
      const wrapper = mountForm()
      await wrapper.find('.btn-cancel').trigger('click')
      expect(wrapper.emitted('cancel')).toBeTruthy()
    })

    it('emits "cancel" when the × close button in the header is clicked', async () => {
      const wrapper = mountForm()
      await wrapper.find('.modal-close').trigger('click')
      expect(wrapper.emitted('cancel')).toBeTruthy()
    })
  })

  // ── Submit ──────────────────────────────────────────────────────────────

  describe('successful submission', () => {
    it('calls createDeviationReport with all form field values', async () => {
      const wrapper = mountForm()
      await fillAllFields(wrapper)
      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(createDeviationReport).toHaveBeenCalledOnce()
      const payload = createDeviationReport.mock.calls[0][0]
      expect(payload.deviationName).toBe('Fridge too warm')
      expect(payload.severity).toBe('MAJOR')
      expect(payload.occurredAt).toBe('2024-06-15T10:30')
      expect(payload.noticedBy).toBe('Alice')
      expect(payload.reportedTo).toBe('Manager Bob')
      expect(payload.processedBy).toBe('Alice')
      expect(payload.description).toBe('Temperature was 12°C instead of <5°C')
      expect(payload.immediateAction).toBe('Moved items to backup fridge')
      expect(payload.believedCause).toBe('Seal on door worn out')
      expect(payload.correctiveMeasures).toBe('Replace door seal')
      expect(payload.correctiveMeasuresDone).toBe('Backup fridge used, maintenance called')
    })

    it('emits "submitted" with the API response on success', async () => {
      const apiResult = { id: 42, deviationName: 'Fridge too warm' }
      createDeviationReport.mockResolvedValue(apiResult)

      const wrapper = mountForm()
      await fillAllFields(wrapper)
      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.emitted('submitted')).toBeTruthy()
      expect(wrapper.emitted('submitted')[0]).toEqual([apiResult])
    })

    it('shows "Submitting..." on the button while the request is in flight', async () => {
      let resolve
      createDeviationReport.mockReturnValue(new Promise(r => { resolve = r }))

      const wrapper = mountForm()
      await fillAllFields(wrapper)
      await wrapper.find('form').trigger('submit')
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.btn-submit').text()).toBe('Submitting...')
      expect(wrapper.find('.btn-submit').element.disabled).toBe(true)

      resolve({ id: 1 })
      await flushPromises()

      expect(wrapper.find('.btn-submit').text()).toBe('Submit report')
    })
  })

  // ── Error handling ──────────────────────────────────────────────────────

  describe('submission errors', () => {
    it('shows error message when the API call fails', async () => {
      createDeviationReport.mockRejectedValue({ message: 'Server error' })

      const wrapper = mountForm()
      await fillAllFields(wrapper)
      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.find('.form-error').exists()).toBe(true)
      expect(wrapper.find('.form-error').text()).toBe('Server error')
    })

    it('prefers error.response.data.detail over error.message', async () => {
      createDeviationReport.mockRejectedValue({
        response: { data: { detail: 'Validation failed: severity required' } },
        message: 'Generic error',
      })

      const wrapper = mountForm()
      await fillAllFields(wrapper)
      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.find('.form-error').text()).toBe('Validation failed: severity required')
    })

    it('falls back to "Failed to submit report" when error has neither detail nor message', async () => {
      createDeviationReport.mockRejectedValue({})

      const wrapper = mountForm()
      await fillAllFields(wrapper)
      await wrapper.find('form').trigger('submit')
      await flushPromises()

      expect(wrapper.find('.form-error').text()).toBe('Failed to submit report')
    })

    it('clears the previous error when a new submission starts', async () => {
      createDeviationReport
        .mockRejectedValueOnce({ message: 'First attempt failed' })
        .mockResolvedValueOnce({ id: 1 })

      const wrapper = mountForm()
      await fillAllFields(wrapper)

      await wrapper.find('form').trigger('submit')
      await flushPromises()
      expect(wrapper.find('.form-error').exists()).toBe(true)

      await wrapper.find('form').trigger('submit')
      await flushPromises()
      expect(wrapper.find('.form-error').exists()).toBe(false)
    })

    it('re-enables the submit button after an error', async () => {
      createDeviationReport.mockRejectedValue({ message: 'Error' })

      const wrapper = mountForm()
      await fillAllFields(wrapper)
      await wrapper.find('form').trigger('submit')
      await flushPromises()

      // Form is still valid (fields still filled), button should be enabled again
      expect(wrapper.find('.btn-submit').element.disabled).toBe(false)
    })
  })

})
