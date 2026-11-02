import { registerPlugin } from '@capacitor/core'
import type { ToolsApi } from './contracts'

export const tools = registerPlugin<ToolsApi>('GaruTools', {
  web: () => import('./web').then(({ WebTools }) => new WebTools()),
})
