import type { PluginListenerHandle } from '@capacitor/core'
import type { ToolsApi, ToolState } from './contracts'

export class ToolSession {
  private revision = -1
  private generation = 0
  private listener?: PluginListenerHandle

  constructor(
    private readonly api: ToolsApi,
    private readonly onState: (state: ToolState) => void,
  ) {}

  async connect(): Promise<void> {
    const generation = ++this.generation
    const listener = await this.api.addListener('stateChanged', (state) => {
      if (this.generation === generation) this.accept(state)
    })
    if (this.generation !== generation) {
      await listener.remove()
      return
    }
    this.listener = listener
    try {
      const state = await this.api.getState()
      if (this.generation === generation) this.accept(state)
    } catch (error) {
      await this.disconnect()
      throw error
    }
  }

  accept(state: ToolState): void {
    if (state.revision < this.revision) return
    this.revision = state.revision
    this.onState(state)
  }

  async disconnect(): Promise<void> {
    this.generation++
    const listener = this.listener
    this.listener = undefined
    await listener?.remove()
  }
}
