import { describe, expect, it, vi } from 'vitest'
import { ToolSession } from './ToolSession'
import { defaultConfig } from './settings'
import type { ToolsApi, ToolState } from './contracts'

function deferred<T>() {
  let resolve!: (value: T) => void
  let reject!: (reason: Error) => void
  const promise = new Promise<T>((yes, no) => {
    resolve = yes
    reject = no
  })
  return { promise, resolve, reject }
}

function state(revision: number, enabled: boolean): ToolState {
  return {
    revision,
    config: { ...defaultConfig, overlayEnabled: enabled },
    permissions: { overlay: true, accessibility: 'connected' },
    overlay: { status: enabled ? 'running' : 'stopped', error: null },
    appearance: { dark: false },
  }
}

function backend() {
  const initial = deferred<ToolState>()
  const remove = vi.fn(async () => {})
  let emit!: (value: ToolState) => void
  const api: ToolsApi = {
    getState: () => initial.promise,
    setOverlayEnabled: vi.fn(),
    updateConfig: vi.fn(),
    openSettings: vi.fn(),
    requestNotifications: vi.fn(),
    addListener: async (_event, listener) => {
      emit = listener
      return { remove }
    },
  }
  return { api, initial, remove, emit: (value: ToolState) => emit(value) }
}

describe('ToolSession', () => {
  it('keeps a notification stop event when the initial state request resolves later', async () => {
    const native = backend()
    const render = vi.fn()
    const session = new ToolSession(native.api, render)
    const connecting = session.connect()
    await Promise.resolve()
    native.emit(state(2, false))
    native.initial.resolve(state(1, true))
    await connecting
    expect(render.mock.calls.map(([snapshot]) => snapshot.config.overlayEnabled)).toEqual([false])
    await session.disconnect()
  })

  it('removes a subscription that finishes registering after the page closes', async () => {
    const native = backend()
    const registration = deferred<{ remove: () => Promise<void> }>()
    native.api.addListener = () => registration.promise
    const session = new ToolSession(native.api, vi.fn())
    const connecting = session.connect()
    await session.disconnect()
    registration.resolve({ remove: native.remove })
    await connecting
    expect(native.remove).toHaveBeenCalledTimes(1)
  })

  it('releases the native listener if reading the initial state fails', async () => {
    const native = backend()
    const session = new ToolSession(native.api, vi.fn())
    const connecting = session.connect()
    const result = expect(connecting).rejects.toThrow('Unavailable')
    native.initial.reject(new Error('Unavailable'))
    await result
    expect(native.remove).toHaveBeenCalledTimes(1)
  })
})
