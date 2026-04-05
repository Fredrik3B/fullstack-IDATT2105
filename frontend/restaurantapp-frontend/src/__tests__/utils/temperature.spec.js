import { describe, it, expect } from 'vitest'
import {
  isTemperatureTask,
  formatTemperatureTarget,
  isTemperatureDeviation,
} from '@/features/ic-checklists/temperature'

describe('isTemperatureTask', () => {
  it('returns falsy for null', () => {
    expect(isTemperatureTask(null)).toBeFalsy()
  })

  it('returns falsy for undefined', () => {
    expect(isTemperatureTask(undefined)).toBeFalsy()
  })

  it('returns false for a string', () => {
    expect(isTemperatureTask('temperature')).toBe(false)
  })

  it('returns false when task.type is not "temperature"', () => {
    expect(isTemperatureTask({ type: 'checkbox' })).toBe(false)
  })

  it('returns true when task.type is "temperature"', () => {
    expect(isTemperatureTask({ type: 'temperature' })).toBe(true)
  })
})

describe('formatTemperatureTarget', () => {
  it('returns empty string for a non-temperature task', () => {
    expect(formatTemperatureTarget({ type: 'checkbox' })).toBe('')
  })

  it('returns empty string for null', () => {
    expect(formatTemperatureTarget(null)).toBe('')
  })

  it('returns empty string when no min or max are set', () => {
    expect(formatTemperatureTarget({ type: 'temperature', unit: 'C' })).toBe('')
  })

  it('formats a range when both min and max are present', () => {
    expect(
      formatTemperatureTarget({ type: 'temperature', unit: 'C', targetMin: 2, targetMax: 8 }),
    ).toBe('2-8 C')
  })

  it('formats a min-only target with ≥', () => {
    expect(formatTemperatureTarget({ type: 'temperature', unit: 'C', targetMin: 2 })).toBe('≥ 2 C')
  })

  it('formats a max-only target with ≤', () => {
    expect(formatTemperatureTarget({ type: 'temperature', unit: 'C', targetMax: 8 })).toBe('≤ 8 C')
  })

  it('defaults unit to "C" when unit is not provided', () => {
    expect(formatTemperatureTarget({ type: 'temperature', targetMin: 2 })).toBe('≥ 2 C')
  })

  it('uses the provided unit string', () => {
    expect(
      formatTemperatureTarget({ type: 'temperature', unit: 'F', targetMin: 35, targetMax: 46 }),
    ).toBe('35-46 F')
  })

  it('ignores non-finite targetMin and treats it as absent', () => {
    expect(
      formatTemperatureTarget({ type: 'temperature', unit: 'C', targetMin: NaN, targetMax: 8 }),
    ).toBe('≤ 8 C')
  })
})

describe('isTemperatureDeviation', () => {
  it('returns false for a non-temperature task', () => {
    expect(isTemperatureDeviation({ type: 'checkbox' }, 5)).toBe(false)
  })

  it('returns false when valueC is NaN', () => {
    expect(isTemperatureDeviation({ type: 'temperature', targetMin: 2, targetMax: 8 }, NaN)).toBe(
      false,
    )
  })

  it('returns false when value is within range', () => {
    expect(isTemperatureDeviation({ type: 'temperature', targetMin: 2, targetMax: 8 }, 5)).toBe(
      false,
    )
  })

  it('returns false when value equals the minimum boundary', () => {
    expect(isTemperatureDeviation({ type: 'temperature', targetMin: 2, targetMax: 8 }, 2)).toBe(
      false,
    )
  })

  it('returns false when value equals the maximum boundary', () => {
    expect(isTemperatureDeviation({ type: 'temperature', targetMin: 2, targetMax: 8 }, 8)).toBe(
      false,
    )
  })

  it('returns true when value is below the minimum', () => {
    expect(isTemperatureDeviation({ type: 'temperature', targetMin: 2, targetMax: 8 }, 1)).toBe(
      true,
    )
  })

  it('returns true when value is above the maximum', () => {
    expect(isTemperatureDeviation({ type: 'temperature', targetMin: 2, targetMax: 8 }, 9)).toBe(
      true,
    )
  })

  it('returns false when no constraints are defined', () => {
    expect(isTemperatureDeviation({ type: 'temperature' }, 100)).toBe(false)
  })

  it('returns true for max-only constraint when value exceeds max', () => {
    expect(isTemperatureDeviation({ type: 'temperature', targetMax: 4 }, 5)).toBe(true)
  })

  it('returns false for max-only constraint when value is below max', () => {
    expect(isTemperatureDeviation({ type: 'temperature', targetMax: 4 }, 3)).toBe(false)
  })

  it('returns true for min-only constraint when value is below min', () => {
    expect(isTemperatureDeviation({ type: 'temperature', targetMin: 60 }, 55)).toBe(true)
  })
})
