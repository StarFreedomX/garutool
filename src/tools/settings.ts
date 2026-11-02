import { toolCatalog } from '../config/tools'
import type { ThemeMode } from '../config/tools'
import type { ConfigUpdate, ToolConfig } from './contracts'

export const defaultConfig: ToolConfig = {
  overlayEnabled: false,
  pauseDurationMs: toolCatalog.pauseDuration.default,
  themeMode: 'system',
}

export function isThemeMode(value: unknown): value is ThemeMode {
  return toolCatalog.themeModes.some((theme) => theme.value === value)
}

export function validateConfigUpdate(input: ConfigUpdate): ConfigUpdate {
  const result: ConfigUpdate = {}
  if (input.themeMode !== undefined) {
    if (!isThemeMode(input.themeMode)) throw new Error('请选择有效的外观')
    result.themeMode = input.themeMode
  }
  if (input.pauseDurationMs !== undefined) {
    const value = input.pauseDurationMs
    const { min, max } = toolCatalog.pauseDuration
    if (!Number.isInteger(value) || value < min || value > max) throw new Error('暂停时长超出范围')
    result.pauseDurationMs = value
  }
  return result
}
