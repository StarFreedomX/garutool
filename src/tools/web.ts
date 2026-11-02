import { WebPlugin } from '@capacitor/core'
import { defaultConfig, validateConfigUpdate } from './settings'
import type { ConfigUpdate, ToolConfig, ToolState } from './contracts'

export class WebTools extends WebPlugin {
  private config: ToolConfig = { ...defaultConfig }
  private revision = 0
  private readonly systemTheme = matchMedia('(prefers-color-scheme: dark)')

  constructor() {
    super()
    try {
      const saved = JSON.parse(localStorage.getItem('garutool-preview') ?? '{}') as ConfigUpdate
      this.config = { ...this.config, ...validateConfigUpdate(saved) }
    } catch {
      /* Invalid preview settings use the defaults. */
    }
    this.systemTheme.addEventListener('change', () => this.publish())
  }

  async getState(): Promise<ToolState> {
    return {
      revision: this.revision,
      config: { ...this.config },
      permissions: { overlay: false, accessibility: 'disabled' },
      overlay: { status: 'stopped', error: null },
      appearance: {
        dark:
          this.config.themeMode === 'dark' ||
          (this.config.themeMode === 'system' && this.systemTheme.matches),
      },
    }
  }

  async setOverlayEnabled(): Promise<ToolState> {
    throw new Error('请在 Android 中开启悬浮窗')
  }

  async updateConfig(update: ConfigUpdate): Promise<ToolState> {
    this.config = { ...this.config, ...validateConfigUpdate(update) }
    localStorage.setItem('garutool-preview', JSON.stringify(this.config))
    this.publish()
    return this.getState()
  }

  async openSettings(): Promise<void> {
    throw new Error('请在 Android 中设置权限')
  }
  async requestNotifications(): Promise<void> {}

  private publish() {
    this.revision++
    void this.getState().then((state) => this.notifyListeners('stateChanged', state))
  }
}
