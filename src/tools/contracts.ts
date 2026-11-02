import type { PluginListenerHandle } from '@capacitor/core'
import type { ActionId, ThemeMode } from '../config/tools'

export interface ToolConfig {
  overlayEnabled: boolean
  enabledActions: ActionId[]
  pauseDurationMs: number
  themeMode: ThemeMode
}

export type ConfigUpdate = Partial<
  Pick<ToolConfig, 'pauseDurationMs' | 'themeMode' | 'enabledActions'>
>
export type SettingsPage = 'overlay' | 'accessibility'

export interface ToolState {
  revision: number
  config: ToolConfig
  permissions: {
    overlay: boolean
    accessibility: 'disabled' | 'enabled' | 'connected'
  }
  overlay: {
    status: 'stopped' | 'starting' | 'running' | 'permissionRequired' | 'failed'
    error: string | null
  }
  appearance: { dark: boolean }
}

export interface ToolsApi {
  getState(): Promise<ToolState>
  setOverlayEnabled(options: { enabled: boolean }): Promise<ToolState>
  updateConfig(options: ConfigUpdate): Promise<ToolState>
  openSettings(options: { kind: SettingsPage }): Promise<void>
  requestNotifications(): Promise<void>
  addListener(
    event: 'stateChanged',
    listener: (state: ToolState) => void,
  ): Promise<PluginListenerHandle>
}
