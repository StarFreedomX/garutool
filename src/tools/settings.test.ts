import { describe, expect, it } from 'vitest'
import type { ActionId } from '../config/tools'
import { toolCatalog } from '../config/tools'
import { validateConfigUpdate } from './settings'

describe('configuration updates', () => {
  it('preserves partial updates so changing appearance does not reset other settings', () => {
    expect(validateConfigUpdate({ themeMode: 'dark' })).toEqual({ themeMode: 'dark' })
    expect(validateConfigUpdate({ pauseDurationMs: 500 })).toEqual({ pauseDurationMs: 500 })
  })

  it('accepts every duration offered in the settings UI', () => {
    for (const pauseDurationMs of toolCatalog.pauseDuration.options) {
      expect(validateConfigUpdate({ pauseDurationMs })).toEqual({ pauseDurationMs })
    }
  })

  it('accepts all-off and keeps enabled buttons unique in catalog order', () => {
    expect(validateConfigUpdate({ enabledActions: [] })).toEqual({ enabledActions: [] })
    expect(
      validateConfigUpdate({ enabledActions: ['globalBack', 'pausePulse', 'globalBack'] }),
    ).toEqual({ enabledActions: ['pausePulse', 'globalBack'] })
  })

  it('rejects unknown actions at the native bridge boundary', () => {
    expect(() => validateConfigUpdate({ enabledActions: ['unknown' as ActionId] })).toThrow()
  })

  it.each([NaN, Infinity, 0, 100.5, 2001])(
    'rejects invalid pause duration %s',
    (pauseDurationMs) => {
      expect(() => validateConfigUpdate({ pauseDurationMs })).toThrow()
    },
  )
})
